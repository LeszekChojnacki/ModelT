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
package de.hybris.platform.warehousing.atp.strategy.impl;

import de.hybris.platform.commerceservices.model.PickUpDeliveryModeModel;
import de.hybris.platform.commerceservices.stock.strategies.impl.DefaultWarehouseSelectionStrategy;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.warehousing.warehouse.filter.WarehousesFilterProcessor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Required;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;


/**
 * Warehousing extension of {@link DefaultWarehouseSelectionStrategy}
 */
public class WarehousingWarehouseSelectionStrategy extends DefaultWarehouseSelectionStrategy
{
	private WarehousesFilterProcessor warehousesFilterProcessor;

	@Override
	public List<WarehouseModel> getWarehousesForBaseStore(final BaseStoreModel baseStore)
	{
		validateParameterNotNull(baseStore, "baseStore must not be null");

		final Set<WarehouseModel> warehouses = getWarehousesFilterProcessor()
				.filterLocations(Sets.newHashSet(baseStore.getWarehouses()));
		return Stream.concat(warehouses.stream().filter(warehouse -> warehouse.getDeliveryModes().stream()
						.anyMatch(deliveryMode -> !(deliveryMode instanceof PickUpDeliveryModeModel))),
				warehouses.stream().filter(warehouse -> warehouse.getDeliveryModes().isEmpty())).distinct()
				.collect(Collectors.toList());
	}

	protected WarehousesFilterProcessor getWarehousesFilterProcessor()
	{
		return warehousesFilterProcessor;
	}

	@Required
	public void setWarehousesFilterProcessor(WarehousesFilterProcessor warehousesFilterProcessor)
	{
		this.warehousesFilterProcessor = warehousesFilterProcessor;
	}
}
