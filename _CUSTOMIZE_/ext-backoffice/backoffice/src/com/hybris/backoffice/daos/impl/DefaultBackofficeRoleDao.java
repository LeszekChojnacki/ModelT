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
package com.hybris.backoffice.daos.impl;

import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.daos.BackofficeRoleDao;
import com.hybris.backoffice.model.user.BackofficeRoleModel;


/**
 * Default implementation for {@link DefaultBackofficeRoleDao}.
 */
public class DefaultBackofficeRoleDao implements BackofficeRoleDao
{
	private FlexibleSearchService flexibleSearchService;

	@Override
	public Set<BackofficeRoleModel> findAllBackofficeRoles()
	{
		final Set<BackofficeRoleModel> backOfficeRoles = new LinkedHashSet<BackofficeRoleModel>();
		final String queryString = "SELECT {PK} FROM {" + BackofficeRoleModel._TYPECODE + "}";

		final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
		query.setResultClassList(Arrays.asList(BackofficeRoleModel.class));
		final SearchResult<BackofficeRoleModel> search = flexibleSearchService.search(query);

		backOfficeRoles.addAll(search.getResult());

		return backOfficeRoles;
	}

	protected FlexibleSearchService getFlexibleSearchService()
	{
		return flexibleSearchService;
	}

	@Required
	public void setFlexibleSearchService(final FlexibleSearchService flexibleSearchService)
	{
		this.flexibleSearchService = flexibleSearchService;
	}

}
