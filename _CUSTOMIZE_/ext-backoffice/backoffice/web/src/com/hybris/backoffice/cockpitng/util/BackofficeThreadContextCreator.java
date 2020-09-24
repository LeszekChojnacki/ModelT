/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package com.hybris.backoffice.cockpitng.util;

import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.AbstractTenant;
import de.hybris.platform.core.Registry;
import de.hybris.platform.core.Tenant;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.threadregistry.OperationInfo;
import de.hybris.platform.core.threadregistry.RegistrableThread;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.util.AtomicCounter;
import de.hybris.platform.util.MediaUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.zkoss.util.Locales;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.sys.SessionsCtrl;

import com.hybris.cockpitng.modules.core.impl.CockpitModuleComponentDefinitionService;
import com.hybris.cockpitng.util.CockpitThreadContextCreator;


public class BackofficeThreadContextCreator implements CockpitThreadContextCreator
{
	private static final Logger LOG = LoggerFactory.getLogger(BackofficeThreadContextCreator.class);
	protected static final String SPRING_SECURITY_CONTEXT = "_spring_security_context";
	protected static final String SPRING_REQUEST_CONTEXT = "_spring_request_context";
	protected static final String ZK_SESSION = "zk_session";
	protected static final String HYBRIS_TENANT = "tenant";
	protected static final String CURRENT_LOCALE = "_locales";
	protected static final String SESSION_USER = "user";
	protected static final String HYBRIS_SESSION_CATALOG_VERSIONS = "sessionCatalogVersions";
	protected static final String PUBLIC_MEDIA_URL_RENDERER = "publicMediaUrlRenderer";
	protected static final String SECURE_MEDIA_URL_RENDERER = "secureMediaUrlRenderer";
	public static final String MEDIA_SSL_MODE_ENABLED = "mediaSSLModeEnabled";
	private CatalogVersionService catalogVersionService;
	private UserService userService;
	private I18NService i18nService;
	private CockpitModuleComponentDefinitionService componentDefinitionService;
	private AtomicCounter atomicCounter = new AtomicCounter();

	@Override
	public void execute(final Runnable runnable)
	{
		final int operationId = atomicCounter.generateNext();
		final Map<String, Object> parentThreadCtx = createThreadContext();
		parentThreadCtx.put("BackgroundOperationId", operationId);
		new RegistrableThread(new RunnableWithParentThreadContext(runnable, parentThreadCtx), "BackofficeLO-" + operationId).withInitialInfo(
				OperationInfo.builder().asNotSuspendableOperation().withCategory("Backoffice Long Operation").build()).start();
	}

	private class RunnableWithParentThreadContext implements Runnable
	{
		private final Runnable runnable;
		private final Map<String, Object> parentThreadCtx;

		protected RunnableWithParentThreadContext(final Runnable runnable, final Map<String, Object> parentThreadCtx)
		{
			this.runnable = runnable;
			this.parentThreadCtx = parentThreadCtx;
		}

		@Override
		public void run()
		{
			try
			{
				initThreadContext(parentThreadCtx);
				setCockpitNGClassLoader();
				runnable.run();
			}
			finally
			{
				cleanUp(parentThreadCtx);
			}
		}

		private void setCockpitNGClassLoader()
		{
			final ApplicationContext externalApplicationContext = componentDefinitionService.getExternalApplicationContext();
			if (externalApplicationContext != null)
			{
				Thread.currentThread().setContextClassLoader(externalApplicationContext.getClassLoader());
			}
		}
	}

	@Override
	public Map<String, Object> createThreadContext()
	{
		final Map<String, Object> ret = new HashMap<>();
		ret.put(HYBRIS_TENANT, Registry.getCurrentTenant());
		ret.put(SESSION_USER, userService.getCurrentUser());
		ret.put(HYBRIS_SESSION_CATALOG_VERSIONS, catalogVersionService.getSessionCatalogVersions());
		ret.put(ZK_SESSION, Sessions.getCurrent());
		ret.put(SPRING_SECURITY_CONTEXT, SecurityContextHolder.getContext());
		ret.put(CURRENT_LOCALE, Locales.getCurrent());
		ret.put(SPRING_REQUEST_CONTEXT, createRequestAttributesCopy());
		ret.put(PUBLIC_MEDIA_URL_RENDERER, MediaUtil.getCurrentPublicMediaURLRenderer());
		ret.put(SECURE_MEDIA_URL_RENDERER, MediaUtil.getCurrentSecureMediaURLRenderer());
		ret.put(MEDIA_SSL_MODE_ENABLED, MediaUtil.isCurrentRequestSSLModeEnabled());
		return ret;
	}

	protected RequestAttributes createRequestAttributesCopy()
	{
		final HttpServletRequest request = (HttpServletRequest) Executions.getCurrent().getNativeRequest();
		final HttpServletRequestWrapper requestWrapper = new BackofficeThreadHttpServletRequestWrapper(request);
		return new ServletRequestAttributes(requestWrapper);
	}

	protected static class BackofficeThreadHttpServletRequestWrapper extends HttpServletRequestWrapper
	{

		private static final HttpServletRequest UNSUPPORTED_REQUEST = (HttpServletRequest) Proxy
				.newProxyInstance(BackofficeThreadHttpServletRequestWrapper.class.getClassLoader(), new Class[]
		{ HttpServletRequest.class }, new UnsupportedOperationExceptionInvocationHandler());

		private final String requestURI;

		private final String contextPath;

		private final String servletPath;

		private final String pathInfo;

		private final String queryString;

		private final String method;

		private final Session session;

		private final ServletContext servletContext;

		public BackofficeThreadHttpServletRequestWrapper(final HttpServletRequest request)
		{
			super(UNSUPPORTED_REQUEST);

			this.session = Sessions.getCurrent(false);
			this.servletContext = request.getServletContext();
			this.requestURI = request.getRequestURI();
			this.contextPath = request.getContextPath();
			this.servletPath = request.getServletPath();
			this.pathInfo = request.getPathInfo();
			this.queryString = request.getQueryString();
			this.method = request.getMethod();
		}

		@Override
		public HttpSession getSession()
		{
			return (HttpSession) session.getNativeSession();
		}

		@Override
		public HttpSession getSession(final boolean create)
		{
			return getSession();
		}

		@Override
		public ServletContext getServletContext()
		{
			try
			{
				return super.getServletContext();
			}
			catch (final Exception e)
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug("Cannot return servlet context from parent. Use fallback context.", e);
				}
				// TSB-2935, CNG-2806 Avoid NullPointer in background thread after parent thread's request is recycled.
				return servletContext;
			}
		}

		@Override
		public String getContextPath()
		{
			return contextPath;
		}

		@Override
		public String getRequestURI()
		{
			return requestURI;
		}

		@Override
		public String getServletPath()
		{
			return servletPath;
		}

		@Override
		public String getPathInfo()
		{
			return pathInfo;
		}

		@Override
		public String getQueryString()
		{
			return queryString;
		}

		@Override
		public String getMethod()
		{
			return method;
		}

	}

	private static class UnsupportedOperationExceptionInvocationHandler implements InvocationHandler
	{

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args)
		{
			throw new UnsupportedOperationException("LongOperation does not cover invocation: " + method.toGenericString());
		}
	}

	@Override
	public void initThreadContext(final Map<String, Object> ctx)
	{
		if (ctx != null)
		{
			final Optional<Tenant> tenant = getFromContext(ctx, HYBRIS_TENANT, Tenant.class);
			if (tenant.isPresent())
			{
				Registry.setCurrentTenant(tenant.get());
			}
			else
			{
				Registry.activateMasterTenant();
			}

			getFromContext(ctx, CURRENT_LOCALE, Locale.class).ifPresent(this::setCurrentLocale);
			getFromContext(ctx, ZK_SESSION, Session.class).ifPresent(SessionsCtrl::setCurrent);
			getFromContext(ctx, SPRING_SECURITY_CONTEXT, SecurityContext.class).ifPresent(SecurityContextHolder::setContext);
			getFromContext(ctx, SPRING_REQUEST_CONTEXT, RequestAttributes.class)
					.ifPresent(RequestContextHolder::setRequestAttributes);
			getFromContext(ctx, SESSION_USER, UserModel.class).ifPresent(userService::setCurrentUser);
			getFromContext(ctx, HYBRIS_SESSION_CATALOG_VERSIONS, Collection.class).ifPresent(this::setSessionCatalogVersions);
			getFromContext(ctx, PUBLIC_MEDIA_URL_RENDERER, MediaUtil.PublicMediaURLRenderer.class).ifPresent(MediaUtil::setCurrentPublicMediaURLRenderer);
			getFromContext(ctx, SECURE_MEDIA_URL_RENDERER, MediaUtil.SecureMediaURLRenderer.class).ifPresent(MediaUtil::setCurrentSecureMediaURLRenderer);
			getFromContext(ctx, MEDIA_SSL_MODE_ENABLED, Boolean.class).ifPresent(MediaUtil::setCurrentRequestSSLModeEnabled);

		}
	}

	protected void setSessionCatalogVersions(final Collection<CatalogVersionModel> catalogVersions)
	{
		catalogVersionService.setSessionCatalogVersions(catalogVersions);
	}

	protected void setCurrentLocale(final Locale currentLocale)
	{
		final Locale sessionLocale = i18nService.getSupportedLocales().contains(currentLocale) ? currentLocale
				: JaloSession.getCurrentSession().getSessionContext().getLocale();
		Locales.setThreadLocal(sessionLocale);
		i18nService.setCurrentLocale(sessionLocale);
	}

	protected <T> Optional<T> getFromContext(final Map<String, Object> ctx, final String paramName, final Class<T> paramValueType)
	{
		final Object value = ctx.get(paramName);
		if (value != null && paramValueType.isInstance(value))
		{
			return Optional.of((T) value);
		}
		return Optional.empty();
	}

	protected void cleanUp(final Map<String, Object> ctx)
	{
		SecurityContextHolder.clearContext();
		RequestContextHolder.resetRequestAttributes();
		SessionsCtrl.setCurrent((Session) null);
		final Tenant currentTenant = Registry.getCurrentTenant();
		if (currentTenant instanceof AbstractTenant)
		{
			((AbstractTenant) currentTenant).setActiveSessionForCurrentThread(null);
			((AbstractTenant) currentTenant).getActiveSessionContextList().clear();
		}
		MediaUtil.unsetCurrentPublicMediaURLRenderer();
		MediaUtil.unsetCurrentSecureMediaURLRenderer();
		MediaUtil.unsetCurrentRequestSSLModeEnabled();
	}

	@Required
	public void setCatalogVersionService(final CatalogVersionService catalogVersionService)
	{
		this.catalogVersionService = catalogVersionService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	@Required
	public void setI18nService(final I18NService i18nService)
	{
		this.i18nService = i18nService;
	}

	@Required
	public void setComponentDefinitionService(final CockpitModuleComponentDefinitionService componentDefinitionService)
	{
		this.componentDefinitionService = componentDefinitionService;
	}
}
