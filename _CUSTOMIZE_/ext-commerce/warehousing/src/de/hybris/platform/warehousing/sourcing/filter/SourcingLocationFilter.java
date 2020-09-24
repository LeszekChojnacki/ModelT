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
package de.hybris.platform.warehousing.sourcing.filter;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;

import java.util.Set;


/**
 * Sourcing filter interface used to obtain a minimal working subset of locations to be used for sourcing of an order.
 */
public interface SourcingLocationFilter
{
	/**
	 * Apply the filter on the order and returns a set of locations to be used for sourcing.
	 *
	 * @param order
	 *           - Order to be sourced (input value); cannot be <tt>null</tt>
	 * @param locations
	 *           - Set of sourcing locations (output value); cannot be <tt>null</tt>
	 */
	void filterLocations(AbstractOrderModel order, Set<WarehouseModel> locations);

	/**
	 * Utility method used to decide whether the filtered result set should be a union (OR) or an intersection (AND).
	 *
	 * @param operator
	 *           - AND or OR operator to apply on the result sets; cannot be <tt>null</tt>
	 */
	void setFilterResultOperator(SourcingFilterResultOperator operator);
}
