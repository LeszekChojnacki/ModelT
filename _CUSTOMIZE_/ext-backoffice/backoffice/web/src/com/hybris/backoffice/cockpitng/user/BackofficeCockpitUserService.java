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
package com.hybris.backoffice.cockpitng.user;

import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.cockpitng.core.user.CockpitUserService;
import com.hybris.cockpitng.core.util.CockpitProperties;
import com.hybris.cockpitng.util.CockpitSessionService;


/**
 * Implementation of {@link CockpitUserService} for the hybris platform, using {@link UserService}.
 */
public class BackofficeCockpitUserService implements CockpitUserService
{
	public static final String BACKOFFICE_ADMIN_GROUP = "backofficeadmingroup";
	public static final String IS_ADMIN_SESSION_KEY = BackofficeCockpitUserService.class.getName() + "_isAdminUser";
	protected static final String CURRENT_USER_VERIFIES_ANONYMOUS_USER_PROPERTY = "gettingcurrentuser.annonymoususer.verification";

	private static final Logger LOG = Logger.getLogger(BackofficeCockpitUserService.class);

	private UserService userService;
	private CockpitSessionService cockpitSessionService;
	private CockpitProperties cockpitProperties;

	@Override
	public String getCurrentUser()
	{
		final UserModel currentUser = userService.getCurrentUser();
		if (currentUser == null || isVerifiedAnonymousUser(currentUser))
		{
			return null;
		}
		else
		{
			return currentUser.getUid();
		}
	}

	protected boolean isVerifiedAnonymousUser(final UserModel user)
	{
		final boolean verifyAnonymoususer = cockpitProperties.getBoolean(CURRENT_USER_VERIFIES_ANONYMOUS_USER_PROPERTY, false);
		return verifyAnonymoususer && userService.isAnonymousUser(user);
	}

	@Override
	public void setCurrentUser(final String userID)
	{
		try
		{
			final UserModel user = userID == null ? null : userService.getUserForUID(userID);
			userService.setCurrentUser(user);
		}
		catch (final UnknownIdentifierException e)
		{
			LOG.error("Could not set current user '" + userID + "', user not found.");
		}
	}

	@Override
	public boolean isAdmin(final String userID)
	{
		if (userID == null)
		{
			return false;
		}
		final Object cachedIsAdmin = getCockpitSessionService().getAttribute(IS_ADMIN_SESSION_KEY);
		if (cachedIsAdmin instanceof Boolean)
		{
			return ((Boolean) cachedIsAdmin).booleanValue();
		}
		boolean isAdmin = false;
		try
		{
			final UserModel currentUser = userService.getUserForUID(userID);
			if (currentUser != null)
			{
				final Set<UserGroupModel> allUserGroupsForUser = userService.getAllUserGroupsForUser(currentUser);
				if (CollectionUtils.isNotEmpty(allUserGroupsForUser))
				{
					isAdmin = allUserGroupsForUser.stream()
							.anyMatch(group -> StringUtils.equals(BACKOFFICE_ADMIN_GROUP, group.getUid()));
				}
			}
		}
		catch (final UnknownIdentifierException e)
		{
			LOG.warn(e.getMessage(), e);
		}
		getCockpitSessionService().setAttribute(IS_ADMIN_SESSION_KEY, Boolean.valueOf(isAdmin));
		return isAdmin;
	}

	@Override
	public boolean isLocalizedEditorInitiallyExpanded()
	{
		return getUserService().getCurrentUser().getUserprofile().getExpandInitial();
	}

	public UserService getUserService()
	{
		return userService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	public CockpitSessionService getCockpitSessionService()
	{
		return cockpitSessionService;
	}

	@Required
	public void setCockpitSessionService(final CockpitSessionService cockpitSessionService)
	{
		this.cockpitSessionService = cockpitSessionService;
	}

	public CockpitProperties getCockpitProperties()
	{
		return cockpitProperties;
	}

	@Required
	public void setCockpitProperties(final CockpitProperties cockpitProperties)
	{
		this.cockpitProperties = cockpitProperties;
	}

}
