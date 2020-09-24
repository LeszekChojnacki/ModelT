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

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;


/**
 * The matcher will determine which attribute of a {@link AbstractOrderEntryModel} will be used for matching.
 *
 * @param <T>
 *           - the matcher object type
 */
public interface OrderEntryMatcher<T>
{
	/**
	 * Get the order entry model attribute used to match it to other order entries.
	 *
	 * @param orderEntry
	 *           - the abstract order entry model
	 * @return the matching attribute; or <tt>null</tt> if the attribute is <tt>null</tt>
	 */
	T getMatchingObject(final AbstractOrderEntryModel orderEntry);
}
