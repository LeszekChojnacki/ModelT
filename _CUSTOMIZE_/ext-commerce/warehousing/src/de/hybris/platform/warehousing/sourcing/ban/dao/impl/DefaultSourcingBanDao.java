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
 *
 */
package de.hybris.platform.warehousing.sourcing.ban.dao.impl;

import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.warehousing.model.SourcingBanModel;
import de.hybris.platform.warehousing.sourcing.ban.dao.SourcingBanDao;

import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;


/**
 * Default implementation of the sourcing ban DAO
 */
public class DefaultSourcingBanDao implements SourcingBanDao
{
	private FlexibleSearchService flexibleSearchService;

	@Override
	public Collection<SourcingBanModel> getSourcingBan(final Collection<WarehouseModel> warehouseModels,
			final Date currentDateMinusBannedDays)
	{
		final String query =
				"SELECT {" + SourcingBanModel.PK + "} FROM {" + SourcingBanModel._TYPECODE + "} WHERE {" + SourcingBanModel.WAREHOUSE
						+ "} IN (?warehousemodels) and {" + SourcingBanModel.CREATIONTIME + "} >= " + "?currenttime-banneddays";
		final FlexibleSearchQuery fsQuery = new FlexibleSearchQuery(query);
		fsQuery.addQueryParameter("warehousemodels", warehouseModels);
		fsQuery.addQueryParameter("currenttime-banneddays", currentDateMinusBannedDays);

		final Collection<SourcingBanModel> results = getSourcingBans(fsQuery);
		return results.isEmpty() ? Collections.emptyList() : results;
	}

	protected FlexibleSearchService getFlexibleSearchService()
	{
		return flexibleSearchService;
	}

	@Required
	public void setFlexibleSearchService(FlexibleSearchService flexibleSearchService)
	{
		this.flexibleSearchService = flexibleSearchService;
	}

	protected <T extends SourcingBanModel> Collection<T> getSourcingBans(final FlexibleSearchQuery query)
	{
		final SearchResult<T> result = getFlexibleSearchService().search(query);
		return result.getResult();
	}



}
