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
package com.hybris.backoffice.security;

import de.hybris.platform.catalog.CatalogService;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.servicelayer.web.DynamicCatalogVersionActivationFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.filter.GenericFilterBean;

import com.hybris.backoffice.catalogversioneventhandling.AvailableCatalogVersionsTag;


public class BackofficeDynamicCatalogVersionActivationFilter extends GenericFilterBean
{
	private static final Logger LOG = Logger.getLogger(DynamicCatalogVersionActivationFilter.class.getName());

	private static final String CATALOG_VERSIONS_TAG = "catalog_versions_tag";

	private CatalogVersionService catalogVersionService;

	private CatalogService catalogService;

	private UserService userService;

	private SessionService sessionService;

	private AvailableCatalogVersionsTag availableCatalogVersionsTag;

	/**
	 * Before the filterChain gets processed, BackofficeDynamicCatalogVersionActivationFilter takes care of activating the
	 * catalog versions properly
	 *
	 * @param request
	 *           the request
	 * @param response
	 *           the response
	 * @param filterChain
	 *           the filterChain
	 */
	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain filterChain)
			throws IOException, ServletException
	{
		if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse))
		{
			throw new ServletException("BackofficeDynamicCatalogVersionActivationFilter just supports HTTP requests");
		}
		final HttpServletRequest httpRequest = (HttpServletRequest) request;
		final HttpServletResponse httpResponse = (HttpServletResponse) response;
		final HttpSession httpSession = httpRequest.getSession();

		if (isAvailableCatalogVersionsChanged())
		{
			setSessionCatalogVersions(httpRequest, httpSession, getCatalogVersionsForCurrentUser());
		}

		filterChain.doFilter(httpRequest, httpResponse);
	}

	protected boolean isAvailableCatalogVersionsChanged()
	{
		return !getAvailableCatalogVersionsTag().getTag().equals(getSessionService().getAttribute(CATALOG_VERSIONS_TAG));
	}

	protected Collection<CatalogVersionModel> getCatalogVersionsForCurrentUser()
	{
		final UserModel currentUser = getUserService().getCurrentUser();

		if (currentUser == null)
		{
			return Collections.emptyList();
		}
		if (getUserService().isAdmin(currentUser))
		{
			return getCatalogVersionService().getAllCatalogVersions();
		}
		final Set<CatalogVersionModel> catVersions = new HashSet<>();

		catVersions.addAll(catalogVersionService.getAllReadableCatalogVersions(currentUser));
		catVersions.addAll(catalogVersionService.getAllWritableCatalogVersions(currentUser));

		return SetUtils.unmodifiableSet(catVersions);
	}

	protected void setSessionCatalogVersions(final HttpServletRequest request, final HttpSession httpSession,
			final Collection<CatalogVersionModel> versions)
	{
		if (versions == null)
		{
			getCatalogVersionService().setSessionCatalogVersions(CollectionUtils.emptyCollection());
		}
		else
		{
			getCatalogVersionService().setSessionCatalogVersions(versions);
		}
		getSessionService().setAttribute(CATALOG_VERSIONS_TAG, getAvailableCatalogVersionsTag().getTag());
		LOG.debug("Active catalog versions tag " + getAvailableCatalogVersionsTag().getTag());
	}

	protected CatalogVersionService getCatalogVersionService()
	{
		return catalogVersionService;
	}

	@Required
	public void setCatalogVersionService(final CatalogVersionService catalogVersionService)
	{
		this.catalogVersionService = catalogVersionService;
	}

	protected CatalogService getCatalogService()
	{
		return catalogService;
	}

	@Required
	public void setCatalogService(final CatalogService catalogService)
	{
		this.catalogService = catalogService;
	}

	protected UserService getUserService()
	{
		return userService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	protected SessionService getSessionService()
	{
		return sessionService;
	}

	@Required
	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
	}

	protected AvailableCatalogVersionsTag getAvailableCatalogVersionsTag()
	{
		return availableCatalogVersionsTag;
	}

	@Required
	public void setAvailableCatalogVersionsTag(final AvailableCatalogVersionsTag availableCatalogVersionsTag)
	{
		this.availableCatalogVersionsTag = availableCatalogVersionsTag;
	}
}
