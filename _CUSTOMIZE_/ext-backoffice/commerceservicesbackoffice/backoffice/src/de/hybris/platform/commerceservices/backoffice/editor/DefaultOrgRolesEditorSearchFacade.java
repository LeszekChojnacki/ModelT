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
package de.hybris.platform.commerceservices.backoffice.editor;

import de.hybris.platform.commerceservices.organization.utils.OrgUtils;
import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.cockpitng.editor.commonreferenceeditor.ReferenceEditorSearchFacade;
import com.hybris.cockpitng.search.data.SearchQueryData;
import com.hybris.cockpitng.search.data.pageable.Pageable;
import com.hybris.cockpitng.search.data.pageable.PageableList;


/**
 * Default roles provider for organization.
 *
 */
public class DefaultOrgRolesEditorSearchFacade implements ReferenceEditorSearchFacade
{
	private static final int DEFAULT_PAGE_SIZE = 5;

	private UserService userService;

	@Override
	public Pageable search(final SearchQueryData searchQueryData)
	{
		final List<UserGroupModel> orgRoles = new ArrayList<>();
		for (final String uid : OrgUtils.getRoleUids())
		{
			orgRoles.add(getUserService().getUserGroupForUID(uid));
		}
		return new PageableList<UserGroupModel>(orgRoles, DEFAULT_PAGE_SIZE);
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
}
