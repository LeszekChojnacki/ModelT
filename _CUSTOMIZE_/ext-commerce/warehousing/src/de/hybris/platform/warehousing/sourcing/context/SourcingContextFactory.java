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
package de.hybris.platform.warehousing.sourcing.context;

import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.warehousing.data.sourcing.SourcingContext;
import de.hybris.platform.warehousing.sourcing.context.grouping.OrderEntryGroup;

import java.util.Collection;


/**
 * Factory used for creating a {@link SourcingContext}.
 */
public interface SourcingContextFactory
{

	/**
	 * Create a new sourcing context.
	 *
	 * @param groups
	 *           - the order entries grouped by one or more OrderEntryMatcher strategies
	 * @param locations
	 *           - the sourcing locations to be populated; cannot be <tt>null</tt> or empty
	 * @return collection of sourcing context; never <tt>null</tt>
	 */
	Collection<SourcingContext> create(Collection<OrderEntryGroup> groups, Collection<WarehouseModel> locations);

}
