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

import de.hybris.platform.core.model.security.UserRightModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.daos.BackofficeUserRightsDao;


public class DefaultBackofficeUserRightsDao implements BackofficeUserRightsDao
{
	private transient FlexibleSearchService flexibleSearchService;

	private static final String FIND_USER_RIGHTS_BY_CODE = "select {PK} from {" + UserRightModel._TYPECODE + " as u} where {u."
			+ UserRightModel.CODE + "} = ?code";
	private static final String CODE = "code";

	@Override
	public Collection<UserRightModel> findUserRightsByCode(final String code)
	{
		final FlexibleSearchQuery query = new FlexibleSearchQuery(FIND_USER_RIGHTS_BY_CODE);
		query.addQueryParameter(CODE, code);
		final SearchResult<UserRightModel> result = flexibleSearchService.search(query);
		return result.getResult();
	}

	@Required
	public void setFlexibleSearchService(final FlexibleSearchService flexibleSearchService)
	{
		this.flexibleSearchService = flexibleSearchService;
	}

}
