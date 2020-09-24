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

import org.springframework.beans.factory.annotation.Required;

import com.hybris.cockpitng.core.user.AuthorityGroupService;
import com.hybris.cockpitng.core.user.CockpitUserService;
import com.hybris.cockpitng.core.user.impl.AuthorityGroup;
import com.hybris.cockpitng.util.js.JsWidgetSessionDTO;
import com.hybris.cockpitng.util.js.JsWidgetSessionInfoDecorator;


public class AuthorityGroupJsWidgetSessionDecorator implements JsWidgetSessionInfoDecorator
{
	private AuthorityGroupService authorityGroupService;
	private CockpitUserService cockpitUserService;

	@Required
	public void setAuthorityGroupService(final AuthorityGroupService authorityGroupService)
	{
		this.authorityGroupService = authorityGroupService;
	}

	@Required
	public void setCockpitUserService(final CockpitUserService cockpitUserService)
	{
		this.cockpitUserService = cockpitUserService;
	}

	@Override
	public JsWidgetSessionDTO decorate(final JsWidgetSessionDTO dto)
	{
		final AuthorityGroup activeAuthorityGroupForUser = authorityGroupService
				.getActiveAuthorityGroupForUser(cockpitUserService.getCurrentUser());
		dto.setActiveAuthorityGroup(activeAuthorityGroupForUser);
		return dto;
	}

}
