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
package de.hybris.platform.warehousing.sourcing.filter.impl;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.warehousing.sourcing.filter.SourcingFilterResultOperator;
import de.hybris.platform.warehousing.sourcing.filter.SourcingLocationFilter;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;



/**
 * Base sourcing filter class providing basic chain filtering functionalities. To implement a filter logic, extend this
 * class and implement the abstract applyFilter() method.
 */
public abstract class AbstractBaseSourcingLocationFilter implements SourcingLocationFilter
{
	protected SourcingFilterResultOperator filterResultOperator;

	/**
	 * Method used to apply the filter on the order and return a set of sourcing locations.
	 *
	 * @param order
	 *           - Order to be sourced (input value); cannot be <tt>null</tt>
	 * @param locations
	 *           - Set of sourcing locations (output value); cannot be <tt>null</tt>
	 */
	public abstract Collection<WarehouseModel> applyFilter(AbstractOrderModel order, Set<WarehouseModel> locations);

	@Override
	public void filterLocations(final AbstractOrderModel order, final Set<WarehouseModel> locations)
	{
		if (order == null || locations == null)
		{
			throw new IllegalArgumentException("Parameters order and locations cannot be null");
		}
		if (filterResultOperator == null)
		{
			throw new IllegalArgumentException("Parameter filterResultOperator cannot be null");
		}

		final Collection<WarehouseModel> filteredResults = applyFilter(order, locations);
		combineFilteredLocations(filteredResults, locations);
	}

	/**
	 * Combine a collection of filtered results and the sourcing locations according to the
	 * {@link SourcingFilterResultOperator} value defined.<br/>
	 * The AND operator returns a set of locations containing only items present in both input parameters (disjunction
	 * set).<br/>
	 * The OR and NONE operator returns a set of locations containing all items present in both input parameters
	 * (conjunction set).
	 *
	 * The NOT operator returns a set of locations excluding locations in Filtered results
	 *
	 * @param filteredResults
	 *           - Filtered results to be combined to the locations parameter (input value); cannot be <tt>null</tt>
	 * @param locations
	 *           - Current set of sourcing locations (output value); cannot be <tt>null</tt>
	 */
	protected void combineFilteredLocations(final Collection<WarehouseModel> filteredResults, final Set<WarehouseModel> locations)
	{
		if (filteredResults != null)
		{
			if (filterResultOperator == SourcingFilterResultOperator.AND)
			{
				// find all locations present in both result sets
				final List<WarehouseModel> tmpLocations = locations.stream().filter(warehouse -> filteredResults.contains(warehouse))
						.collect(Collectors.toList());
				locations.clear();
				locations.addAll(tmpLocations);
				// NOT operator
			}else if (filterResultOperator == SourcingFilterResultOperator.NOT){
				locations.removeAll(filteredResults);
			}
			else
			{
				// OR or NONE operator
				locations.addAll(filteredResults);
			}
		}
	}

	protected SourcingFilterResultOperator getFilterResultOperator()
	{
		return filterResultOperator;
	}

	@Override
	@Required
	public void setFilterResultOperator(final SourcingFilterResultOperator operator)
	{
		this.filterResultOperator = operator;
	}
}
