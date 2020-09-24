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
package com.hybris.backoffice;

import de.hybris.platform.spring.HybrisContextLoaderListener;
import de.hybris.platform.util.Utilities;

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.ClassUtils;
import org.springframework.web.context.WebApplicationContext;
import org.zkoss.lang.Library;

import com.hybris.cockpitng.core.spring.CockpitApplicationContext;
import com.hybris.cockpitng.modules.core.impl.CockpitModulesApplicationContextInitializer;


/**
 * A {@link javax.servlet.ServletContextListener} that initializes application context for Backoffice.
 *
 * @see BackofficeApplicationContext
 */
public class BackofficeApplicationContextInitializer extends HybrisContextLoaderListener implements HttpSessionListener
{
	private static final Logger LOG = LoggerFactory.getLogger(BackofficeApplicationContextInitializer.class);

	private static final String COCKPIT_PROPERTIES_BEN_ID = "cockpitProperties";
	private static final String ZK_LIBRARY_SETTING_PREFIX = "backoffice.zk.lib.setting.";

	@Override
	public void sessionCreated(final HttpSessionEvent httpSessionEvent)
	{
		final ApplicationContext applicationContext = (ApplicationContext) httpSessionEvent.getSession().getServletContext()
				.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		if (applicationContext instanceof ConfigurableApplicationContext)
		{
			final CockpitModulesApplicationContextInitializer initializer = applicationContext
					.getBean("moduleComponentDefinitionService", CockpitModulesApplicationContextInitializer.class);
			if (!initializer.isInitialized())
			{
				((ConfigurableApplicationContext) applicationContext).refresh();
			}
		}
	}

	@Override
	public void sessionDestroyed(final HttpSessionEvent httpSessionEvent)
	{
		// NOOP - only initializes application context for backoffice
	}

	@Override
	protected WebApplicationContext createWebApplicationContext(final ServletContext servletContext)
	{
		final String contextClassName = servletContext.getInitParameter("contextClass");
		if (contextClassName != null)
		{
			try
			{
				final Class<? extends WebApplicationContext> contextClass = (Class<? extends WebApplicationContext>) ClassUtils
						.forName(contextClassName, ClassUtils.getDefaultClassLoader());
				if (!CockpitApplicationContext.class.isAssignableFrom(contextClass))
				{
					throw new ApplicationContextException("Custom context class [" + contextClass.getName() + "] is not of type ["
							+ CockpitApplicationContext.class.getName() + "]");
				}
				return BeanUtils.instantiate(contextClass);
			}
			catch (final ClassNotFoundException ex)
			{
				throw new ApplicationContextException("Failed to load custom context class [" + contextClassName + "]", ex);
			}
		}
		else
		{
			return createDefaultWebApplicationContext(servletContext);
		}
	}

	protected WebApplicationContext createDefaultWebApplicationContext(final ServletContext servletContext)
	{
		final String tenantId = this.getTenanId(servletContext.getContextPath());
		final String contextPath = servletContext.getContextPath();
		return new BackofficeApplicationContext(tenantId, contextPath);
	}

	@Override
	public void contextInitialized(final ServletContextEvent event)
	{
		applyZKSettingByConvention(Utilities.getConfig().getAllParameters());

		super.contextInitialized(event);
	}

	/**
	 * All settings prefixed with {@link #ZK_LIBRARY_SETTING_PREFIX} will be applied on ZK Library, by convention the prefix
	 * will be removed and the trailing part of the key will be used as property name. The value will be used as provided
	 * (no conversions). <br>
	 * Method {@link Library#setProperty(String, String)} is called to set the properties on ZK engine.
	 *
	 * @param properties
	 *           application setting map
	 */
	protected void applyZKSettingByConvention(final Map<String, String> properties)
	{
		for (final Map.Entry<String, String> setting : properties.entrySet())
		{
			if (!setting.getKey().startsWith(ZK_LIBRARY_SETTING_PREFIX))
			{
				continue;
			}

			final String zkProperty = setting.getKey().substring(ZK_LIBRARY_SETTING_PREFIX.length());
			final String value = setting.getValue();
			try
			{
				if (StringUtils.isNotBlank(value))
				{
					LOG.info("Setting custom ZK library setting: [{}={}].", zkProperty, value);
					applyZKProperty(zkProperty, value);
				}
			}
			catch (final RuntimeException e)
			{
				LOG.warn(
						String.format("Unexpected error during custom setting processing: setting='%s' value='%s'", zkProperty, value),
						e);
			}
		}
	}

	protected void applyZKProperty(final String zkProperty, final String value)
	{
		Library.setProperty(zkProperty, value);
	}

}
