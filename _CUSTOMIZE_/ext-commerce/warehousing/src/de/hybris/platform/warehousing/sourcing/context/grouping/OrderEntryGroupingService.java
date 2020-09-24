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
package de.hybris.platform.warehousing.sourcing.context.grouping;

import de.hybris.platform.core.model.order.AbstractOrderModel;

import java.util.Collection;
import java.util.Set;


/**
 * Allows to split orders and groups of order entries into smaller groups of order entries using a series of matchers.
 *
 * @see OrderEntryGroup
 * @see OrderEntryMatcher
 */
public interface OrderEntryGroupingService
{
	/**
	 * Split an order into a groups of order entries based on a collection of matchers.
	 *
	 * @param order
	 *           - the abstract order entry model
	 * @param matchers
	 *           - the order entry matchers
	 * @return a set of order entry groups; never <tt>null</tt>
	 */
	Set<OrderEntryGroup> splitOrderByMatchers(AbstractOrderModel order, Collection<OrderEntryMatcher<?>> matchers);

	/**
	 * Split an existing collection of order entry groups into smaller groups using a single matcher.
	 *
	 * @param groups
	 *           - the collection of order entry groups
	 * @param matcher
	 *           - the order entry matcher
	 * @return a set of order entry groups; never <tt>null</tt>
	 */
	Set<OrderEntryGroup> splitGroupsByMatcher(Set<OrderEntryGroup> groups, OrderEntryMatcher<?> matcher);

	/**
	 * Split a single order entry group into multiple groups using a single matcher.
	 *
	 * @param group
	 *           - the order entry group
	 * @param matcher
	 *           - the order entry matcher
	 * @return a set of order entry groups; never <tt>null</tt>
	 */
	Set<OrderEntryGroup> splitGroupByMatcher(OrderEntryGroup group, OrderEntryMatcher<?> matcher);
}
