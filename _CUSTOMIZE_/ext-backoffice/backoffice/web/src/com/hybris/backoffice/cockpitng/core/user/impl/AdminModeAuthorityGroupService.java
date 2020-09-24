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
package com.hybris.backoffice.cockpitng.core.user.impl;

import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;

import com.hybris.cockpitng.core.user.impl.AuthorityGroup;


/**
 * Extends the {@link DefaultPlatformAuthorityGroupService} for the purpose of admin mode. It uses different session
 * attribute to save the impersonation authority group.
 */
public class AdminModeAuthorityGroupService extends DefaultPlatformAuthorityGroupService
{

	static final String IMPERSONATED_AUTHORITY_GROUP = "impersonatedAuthorityGroup";

	@Override
	public AuthorityGroup getActiveAuthorityGroupForUser(final String userId)
	{
		final Session session = getCurrentSession();
		if (session != null && isAdmin(userId))
		{
			final Object attribute = session.getAttribute(IMPERSONATED_AUTHORITY_GROUP);
			if (attribute instanceof AuthorityGroup)
			{
				return (AuthorityGroup) attribute;
			}
		}
		return null;
	}

	/**
	 * Checks if given user is admin.
	 */
	protected boolean isAdmin(final String userId)
	{
		return getCockpitUserService().isAdmin(userId);
	}

	/**
	 * Checks if current user is admin.
	 *
	 * @deprecated since 6.7 use {@link AdminModeAuthorityGroupService#isAdmin(String) readImpersonatedPermitted(String)}
	 *             instead.
	 */
	@Deprecated
	protected boolean readImpersonatedPermitted()
	{
		return isAdmin(getCockpitUserService().getCurrentUser());
	}

	@Override
	public void setActiveAuthorityGroupForUser(final AuthorityGroup activeAuthorityGroup)
	{
		final Session session = getCurrentSession();
		if (session != null)
		{
			session.setAttribute(IMPERSONATED_AUTHORITY_GROUP, activeAuthorityGroup);
		}
		resetGroupChangeListeners();
	}

	protected Session getCurrentSession()
	{
		return Sessions.getCurrent();
	}
}
