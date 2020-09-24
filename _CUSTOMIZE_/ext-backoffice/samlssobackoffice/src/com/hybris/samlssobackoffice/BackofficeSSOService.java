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
package com.hybris.samlssobackoffice;

import de.hybris.platform.core.Registry;
import de.hybris.platform.core.model.security.PrincipalModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.samlsinglesignon.DefaultSSOService;
import de.hybris.platform.samlsinglesignon.model.SamlUserGroupModel;

import java.util.Collection;
import java.util.List;
import java.util.Objects;



/**
 * Extends the {@link DefaultSSOService} to allow to control the SSO user access to Backoffice.
 * <ul>
 * <li>When the SSO user mapping is done in properties, additional property is allowed:
 * 
 * <pre>
 * sso.mapping.&lt;usergroup&gt;.enableBackofficeLogin=true
 * </pre>
 * 
 * That will allow users with &lt;usergroup&gt access to backoffice.</li>
 * <li>When mapping is done in database, there's additional field provided:
 * {@link SamlUserGroupModel#ENABLEBACKOFFICELOGIN}, that works analogically.</li>
 * </ul>
 * Enabling access to backoffice means setting the {@link PrincipalModel#BACKOFFICELOGINDISABLED} of the SSO user to
 * false.
 */
public class BackofficeSSOService extends DefaultSSOService
{

	protected static final String ENABLE_BACKOFFICE_LOGIN_PARAM = "enableBackofficeLogin";

	@Override
	protected SSOUserMapping findMappingInProperties(final Collection<String> roles)
	{
		final SSOUserMapping mapping = super.findMappingInProperties(roles);

		if (mapping != null)
		{
			mapping.getParameters().put(ENABLE_BACKOFFICE_LOGIN_PARAM, getEnableBackofficeLogin(roles));
		}
		return mapping;
	}

	protected boolean getEnableBackofficeLogin(final Collection<String> roles)
	{
		return roles.stream().anyMatch(
				role -> Registry.getCurrentTenantNoFallback().getConfig()
						.getBoolean("sso.mapping." + role + ".enableBackofficeLogin", false));
	}

	@Override
	protected SSOUserMapping performMapping(final List<SamlUserGroupModel> userGroupModels)
	{
		final SSOUserMapping mapping = super.performMapping(userGroupModels);

		final boolean enableBackofficeLogin = userGroupModels.stream().anyMatch(SamlUserGroupModel::getEnableBackofficeLogin);
		mapping.getParameters().put(ENABLE_BACKOFFICE_LOGIN_PARAM, enableBackofficeLogin);

		return mapping;
	}


	@Override
	protected void adjustUserAttributes(final UserModel user, final SSOUserMapping mapping)
	{
		super.adjustUserAttributes(user, mapping);
		if (mapping.getParameters().containsKey(ENABLE_BACKOFFICE_LOGIN_PARAM))
		{
			if (Objects.equals(mapping.getParameters().get(ENABLE_BACKOFFICE_LOGIN_PARAM), true))
			{
				user.setBackOfficeLoginDisabled(false);
			}
			else
			{
				throw new IllegalStateException("Backoffice login disabled");
			}
		}
	}

}
