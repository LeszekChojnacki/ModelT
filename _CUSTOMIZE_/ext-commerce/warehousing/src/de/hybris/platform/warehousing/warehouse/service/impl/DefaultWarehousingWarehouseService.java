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
package de.hybris.platform.warehousing.warehouse.service.impl;

import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.ordersplitting.impl.DefaultWarehouseService;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.storelocator.model.PointOfServiceModel;
import de.hybris.platform.warehousing.atp.strategy.impl.WarehousingWarehouseSelectionStrategy;
import de.hybris.platform.warehousing.warehouse.service.WarehousingWarehouseService;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;


/**
 * OrderManagement implementation of {@link de.hybris.platform.ordersplitting.WarehouseService}.<br>
 * Also, it provides the default implementation for {@link WarehousingWarehouseService}
 */
public class DefaultWarehousingWarehouseService extends DefaultWarehouseService implements WarehousingWarehouseService
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultWarehousingWarehouseService.class);
	private WarehousingWarehouseSelectionStrategy warehousingWarehouseSelectionStrategy;

	@Override
	public Collection<WarehouseModel> getWarehousesByBaseStoreDeliveryCountry(final BaseStoreModel baseStore,
			final CountryModel country) throws IllegalArgumentException
	{
		validateParameterNotNull(baseStore, "BaseStore cannot be null.");
		validateParameterNotNull(country, "Country cannot be null.");
		Assert.isTrue(CollectionUtils.isNotEmpty(baseStore.getDeliveryCountries()),
				String.format("Basestore:[%s] does not support delivery to any Country", baseStore.getUid()));

		Set<WarehouseModel> filteredWarehouses = new HashSet<>();
		if (baseStore.getDeliveryCountries().contains(country))
		{
			filteredWarehouses.addAll(getWarehousingWarehouseSelectionStrategy().getWarehousesForBaseStore(baseStore));
		}
		else
		{
			LOGGER.info("Basestore:[{}] does not support delivery to Country:[{}]", baseStore.getUid(), country.getName());
		}

		return filteredWarehouses;
	}

	@Override
	public boolean isWarehouseInPoS(final WarehouseModel warehouse, final PointOfServiceModel pointOfService)
	{
		validateParameterNotNullStandardMessage("warehouse", warehouse);
		validateParameterNotNullStandardMessage("pointOfService", pointOfService);
		return pointOfService.getWarehouses().stream().anyMatch(w -> w.equals(warehouse));
	}

	protected WarehousingWarehouseSelectionStrategy getWarehousingWarehouseSelectionStrategy()
	{
		return warehousingWarehouseSelectionStrategy;
	}

	@Required
	public void setWarehousingWarehouseSelectionStrategy(
			final WarehousingWarehouseSelectionStrategy warehousingWarehouseSelectionStrategy)
	{
		this.warehousingWarehouseSelectionStrategy = warehousingWarehouseSelectionStrategy;
	}
}
