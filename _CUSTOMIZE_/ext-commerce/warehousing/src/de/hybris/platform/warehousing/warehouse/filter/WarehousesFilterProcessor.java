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

import java.util.Collection;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;



/**
 * This is a utility class that filters {@link WarehouseModel}(s) according to a collection of filters during ATP calculation and before Sourcing.
 */
public class WarehousesFilterProcessor
{
	private Collection<WarehousesFilter> filters;

	/**
	 * Applies a collection of {@link WarehousesFilter}(s) on the given set of {@link WarehouseModel}(s) to be used during ATP calculation and before Sourcing.
	 *
	 * @param warehouses
	 * 		- Initial set of {@link WarehouseModel}(s) being considered
	 * @return the final set of {@link WarehouseModel}(s) after applying the filter.
	 */
	public Set<WarehouseModel> filterLocations(final Set<WarehouseModel> warehouses)
	{
		Set<WarehouseModel> finalWarehouses = warehouses;
		if (CollectionUtils.isNotEmpty(finalWarehouses) && CollectionUtils.isNotEmpty(getFilters()))
		{
			for (final WarehousesFilter warehousesFilter : getFilters())
			{
				finalWarehouses = warehousesFilter.applyFilter(finalWarehouses);
			}
		}
		return finalWarehouses;
	}

	protected Collection<WarehousesFilter> getFilters()
	{
		return filters;
	}

	@Required
	public void setFilters(final Collection<WarehousesFilter> filters)
	{
		this.filters = filters;
	}
}
