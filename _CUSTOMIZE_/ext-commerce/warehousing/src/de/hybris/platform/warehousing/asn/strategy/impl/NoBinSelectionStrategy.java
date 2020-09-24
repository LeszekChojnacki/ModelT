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
package de.hybris.platform.warehousing.asn.strategy.impl;

import de.hybris.platform.warehousing.asn.strategy.BinSelectionStrategy;
import de.hybris.platform.warehousing.model.AdvancedShippingNoticeEntryModel;

import java.util.HashMap;
import java.util.Map;


/**
 * Strategy to apply when no bin should be assigned to stock level. Returned map has just one entry - one StockLevel
 * without bin will be created.
 */
public class NoBinSelectionStrategy implements BinSelectionStrategy
{

	@Override
	public Map<String, Integer> getBinsForAsnEntry(final AdvancedShippingNoticeEntryModel asnEntry)
	{
		final Map<String, Integer> bins = new HashMap<>();
		bins.put(null, asnEntry.getQuantity());
		return bins;
	}

}
