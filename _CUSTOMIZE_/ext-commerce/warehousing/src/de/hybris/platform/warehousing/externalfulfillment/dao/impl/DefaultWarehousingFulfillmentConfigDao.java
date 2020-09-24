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
package de.hybris.platform.warehousing.externalfulfillment.dao.impl;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.warehousing.externalfulfillment.dao.WarehousingFulfillmentConfigDao;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * The default implementation of {@link WarehousingFulfillmentConfigDao}
 */
public class DefaultWarehousingFulfillmentConfigDao implements WarehousingFulfillmentConfigDao
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultWarehousingFulfillmentConfigDao.class);

	protected static final String GET_WAREHOUSES = "getWarehouses";

	private FlexibleSearchService flexibleSearchService;
	private List<String> warehouseFulfillmentProcessConfigs;

	@Override
	public Object getConfiguration(final WarehouseModel warehouse)
	{
		final List configsList = new ArrayList();
		Object result = null;

		if (!CollectionUtils.isEmpty(getWarehouseFulfillmentProcessConfigs()))
		{
			getWarehouseFulfillmentProcessConfigs().forEach(warehouseFulfillmentConfig ->
			{
				final String warehouseFulfillmentProcessConfigQuery =
						"SELECT {" + ItemModel.PK + "} FROM {" + warehouseFulfillmentConfig + "}";
				final FlexibleSearchQuery fsQuery = new FlexibleSearchQuery(warehouseFulfillmentProcessConfigQuery);
				final SearchResult searchResult = getFlexibleSearchService().search(fsQuery);
				if (searchResult != null && !CollectionUtils.isEmpty(searchResult.getResult()))
				{
					searchResult.getResult().forEach(config -> collectRelatedConfigs(warehouse, configsList, config));
				}
			});
		}

		if (configsList.size() == 1)
		{
			result = configsList.iterator().next();
		}
		else if (configsList.size() > 1)
		{
			throw new AmbiguousIdentifierException("More than one config contains the warehouse: [" + warehouse.getCode() + "]");
		}

		return result;
	}

	/**
	 * Collect the configurations which are linked to the given {@link WarehouseModel} through an attribute named 'warehouses'
	 *
	 * @param warehouse
	 * 		the {@link WarehouseModel} for which the configuration is requested
	 * @param configsList
	 * 		the existing configuration's list to add to
	 * @param config
	 * 		the configuration to test
	 */
	protected void collectRelatedConfigs(final WarehouseModel warehouse, final List configsList, final Object config)
	{
		try
		{
			Collection<WarehouseModel> warehouses = (Collection<WarehouseModel>) config.getClass().getMethod(GET_WAREHOUSES)
					.invoke(config);
			if (warehouses.contains(warehouse))
			{
				configsList.add(config);
			}
		}
		catch (final NoSuchMethodException | IllegalAccessException | InvocationTargetException e) //NOSONAR
		{
			LOGGER.warn(
					"No method 'getWarehouses()' to retrieve the warehouses linked to [{}].", config.getClass().getSimpleName());
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

	protected List<String> getWarehouseFulfillmentProcessConfigs()
	{
		return warehouseFulfillmentProcessConfigs;
	}

	@Required
	public void setWarehouseFulfillmentProcessConfigs(final List<String> warehouseFulfillmentProcessConfigs)
	{
		this.warehouseFulfillmentProcessConfigs = warehouseFulfillmentProcessConfigs;
	}

}
