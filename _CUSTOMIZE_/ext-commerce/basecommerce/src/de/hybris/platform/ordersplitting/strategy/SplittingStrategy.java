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
package de.hybris.platform.ordersplitting.strategy;

import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.strategy.impl.OrderEntryGroup;

import java.util.List;


/**
 *
 */
public interface SplittingStrategy
{


	/**
	 * Perform the strategy.
	 * 
	 * @param orderEntryGroup
	 *           the order entry list
	 * 
	 * @return the list< list< order entry model>>
	 */
	List<OrderEntryGroup> perform(final List<OrderEntryGroup> orderEntryGroup);


	/**
	 * After splitting.
	 * 
	 * @param group
	 *           the group
	 * @param createdOne
	 *           the created one
	 */
	void afterSplitting(final OrderEntryGroup group, final ConsignmentModel createdOne);
}
