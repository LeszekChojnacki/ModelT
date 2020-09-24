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
package com.hybris.backoffice.spring.security;

import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.spring.security.CoreUserDetails;
import de.hybris.platform.spring.security.CoreUserDetailsService;

import java.util.Collection;
import java.util.LinkedHashSet;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.hybris.backoffice.catalogversioneventhandling.AvailableCatalogVersionsTag;


/**
 * Backoffice specific implementation for providing user data access.
 */
public class BackofficeUserDetailsService extends CoreUserDetailsService
{
	private boolean activateCatalogVersions = false;
	private static final String CATALOG_VERSIONS_TAG = "catalog_versions_tag";

	private CatalogVersionService catalogVersionService;
	private UserService userService;
	private SessionService sessionService;
	private AvailableCatalogVersionsTag availableCatalogVersionsTag;


	/**
	 * Locates the user based on the username and
	 * <p>
	 * <li>calls getSessionProxy().getSession().setUserByUID( ... )
	 * <li>sets all available catalog versions as sessions catalogs
	 * <p>
	 * after that
	 *
	 * @param username
	 *           the username presented to the {@link AuthenticationProvider}
	 * @return a fully populated user record (could be <code>null</code>)
	 * @throws UsernameNotFoundException
	 *            if the user could not be found or the user has no GrantedAuthority
	 */
	@Override
	public CoreUserDetails loadUserByUsername(final String username)
	{
		final CoreUserDetails userDetails = super.loadUserByUsername(username);

		final UserModel user;
		try
		{
			user = getUserService().getUserForUID(username);
		}
		catch (final UnknownIdentifierException e)
		{
			throw new UsernameNotFoundException("User not found!", e);
		}

		if (isActivateCatalogVersions())
		{
			final Collection<CatalogVersionModel> allowedVersions = new LinkedHashSet<>();
			getSessionService().executeInLocalView(new SessionExecutionBody()
			{
				@Override
				public void executeWithoutResult()
				{
					if (getUserService().isAdmin(user))
					{
						allowedVersions.addAll(getCatalogVersionService().getAllCatalogVersions());
					}
					else
					{
						allowedVersions.addAll(getCatalogVersionService().getAllReadableCatalogVersions(user));
						allowedVersions.addAll(getCatalogVersionService().getAllWritableCatalogVersions(user));
					}
				}
			}, user);

			getCatalogVersionService().setSessionCatalogVersions(allowedVersions);
			getSessionService().setAttribute(CATALOG_VERSIONS_TAG, getAvailableCatalogVersionsTag().getTag());
		}

		return userDetails;
	}

	public boolean isActivateCatalogVersions()
	{
		return activateCatalogVersions;
	}

	public void setActivateCatalogVersions(final boolean activateCatalogVersions)
	{
		this.activateCatalogVersions = activateCatalogVersions;
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

	protected CatalogVersionService getCatalogVersionService()
	{
		return catalogVersionService;
	}

	@Required
	public void setCatalogVersionService(final CatalogVersionService catalogVersionService)
	{
		this.catalogVersionService = catalogVersionService;
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
