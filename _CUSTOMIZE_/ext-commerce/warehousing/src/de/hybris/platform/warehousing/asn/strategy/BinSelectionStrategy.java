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
package de.hybris.platform.warehousing.asn.strategy;

import de.hybris.platform.warehousing.model.AdvancedShippingNoticeEntryModel;

import java.util.Map;


/**
 * Strategy used to select a bin and product quantity to be associated with a stock level
 */
public interface BinSelectionStrategy
{
	/**
	 * Gets a map of bin and product quantity needed for stock levels creation. <br>
	 * Created is stock level per each entry with given quantity and bin.
	 *
	 * @return map with products quantity assigned to bins
	 */
	Map<String, Integer> getBinsForAsnEntry(final AdvancedShippingNoticeEntryModel asnEntry);
}
