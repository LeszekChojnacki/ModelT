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
package de.hybris.platform.warehousing.warehouse.filter;

import de.hybris.platform.ordersplitting.model.WarehouseModel;

import java.util.Set;


/**
 * Filter interface used to filter set of {@link WarehouseModel}(s), required for ATP calculation and before Sourcing.
 */
public interface WarehousesFilter
{

	/**
	 * Removes the filtered {@link WarehouseModel}(s) from the given set of {@link WarehouseModel}(s), to be used during ATP calculation.
	 *
	 * @param warehouses
	 * 		- Initial set of {@link WarehouseModel}(s) being considered
	 * @return the final set of {@link WarehouseModel}(s) after applying the filter.
	 */
	Set<WarehouseModel> applyFilter(Set<WarehouseModel> warehouses);
}
