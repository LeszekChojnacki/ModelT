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
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.search.impl.DefaultFlexibleSearchService;
import de.hybris.platform.validation.model.constraints.ConstraintGroupModel;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.daos.BackofficeValidationDao;


public class DefaultBackofficeValidationDao implements BackofficeValidationDao
{
	private DefaultFlexibleSearchService flexibleSearchService;

	@Required
	public void setFlexibleSearchService(final DefaultFlexibleSearchService flexibleSearchService)
	{
		this.flexibleSearchService = flexibleSearchService;
	}

	public DefaultFlexibleSearchService getFlexibleSearchService()
	{
		return flexibleSearchService;
	}

	@Override
	public Collection<ConstraintGroupModel> getConstraintGroups(final Collection<String> groupsIds)
	{
		final FlexibleSearchQuery fsq = new FlexibleSearchQuery("SELECT {PK} FROM {ConstraintGroup} WHERE {ID} IN (?ids)");
		fsq.addQueryParameter("ids", groupsIds);
		fsq.setResultClassList(Arrays.asList(new Class[] { ConstraintGroupModel.class }));
		final SearchResult<ConstraintGroupModel> resultSet = getFlexibleSearchService().search(fsq);
		return resultSet.getResult();
	}
}
