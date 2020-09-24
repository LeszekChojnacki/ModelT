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
package de.hybris.platform.warehousing.returns.dao.impl;

import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.warehousing.model.RestockConfigModel;
import de.hybris.platform.warehousing.returns.RestockException;
import de.hybris.platform.warehousing.returns.dao.RestockConfigDao;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;


/**
 * Default implementation of the {@link RestockConfigDao}
 */
public class DefaultRestockConfigDao implements RestockConfigDao
{
	private FlexibleSearchService flexibleSearchService;
	@Override
	public RestockConfigModel getRestockConfig() throws RestockException
	{
		final String query = "SELECT {" + RestockConfigModel.PK + "} FROM {" + RestockConfigModel._TYPECODE + "}";
		final FlexibleSearchQuery fsQuery = new FlexibleSearchQuery(query);

		final Collection<RestockConfigModel> results= getRestockConfig(fsQuery);

		if(results.isEmpty()) {
			return null;
		} else if(results.size() == 1) {
			return results.iterator().next();
		} else {
			throw new RestockException("Only one restockConfig record allowed");
		}
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

	protected <T extends RestockConfigModel> Collection<T> getRestockConfig(final FlexibleSearchQuery query)
	{
		final SearchResult<T> result = getFlexibleSearchService().search(query);
		return result.getResult();
	}
}
