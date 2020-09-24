/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.datahubbackoffice.daos.impl;

import de.hybris.platform.datahubbackoffice.daos.DataHubInstanceDAO;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;

import de.hybris.platform.datahubbackoffice.model.DataHubInstanceModelModel;

import java.util.List;

import org.springframework.beans.factory.annotation.Required;

public class DefaultDataHubInstanceDAOImpl implements DataHubInstanceDAO
{
	private static final String QUERY_STRING = "SELECT {p:" + DataHubInstanceModelModel.PK + "} "
		+ "FROM {" + DataHubInstanceModelModel._TYPECODE + " AS p} ";

	private FlexibleSearchService flexibleSearchService;

	@Override
	public List<DataHubInstanceModelModel> findDataHubInstances()
	{
		final FlexibleSearchQuery flexibleSearchQuery = new FlexibleSearchQuery(QUERY_STRING);

		return flexibleSearchService.<DataHubInstanceModelModel>search(flexibleSearchQuery).getResult();
	}

	@Required
	public void setFlexibleSearchService(final FlexibleSearchService flexibleSearchService)
	{
		this.flexibleSearchService = flexibleSearchService;
	}
}
