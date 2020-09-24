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
package de.hybris.platform.warehousing.warehouse.service;

import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.ordersplitting.WarehouseService;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.storelocator.model.PointOfServiceModel;

import java.util.Collection;


/**
 * Service for retrieving warehouses by delivery country and availability.
 */
public interface WarehousingWarehouseService extends WarehouseService
{

	/**
	 * Find all warehouses where the associated base store can deliver to the specified country.
	 *
	 * @param baseStore
	 * 		- cannot be <tt>null</tt>
	 * @param country
	 * 		- cannot be <tt>null</tt>
	 * @return a collection of sourcing locations; never <tt>null</tt>
	 */
	Collection<WarehouseModel> getWarehousesByBaseStoreDeliveryCountry(BaseStoreModel baseStore, CountryModel country);

	/**
	 * Check if given {@link WarehouseModel} belong to given {@link PointOfServiceModel}
	 *
	 * @param warehouse
	 * 		-given {@link WarehouseModel} to be validated - cannot be <tt>null</tt>
	 * @param pointOfService
	 * 		given {@link PointOfServiceModel} - cannot be <tt>null</tt>
	 * @return true if {@link WarehouseModel} belongs to given {@link PointOfServiceModel}
	 */
	boolean isWarehouseInPoS(WarehouseModel warehouse, PointOfServiceModel pointOfService);
}
