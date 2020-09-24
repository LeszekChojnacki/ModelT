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

import java.util.Date;


/**
 * Strategy used to calculate release date for stock level based on {@link AdvancedShippingNoticeEntryModel#ASN}
 */
public interface AsnReleaseDateStrategy
{
	/**
	 * Gets {@link de.hybris.platform.ordersplitting.model.StockLevelModel#RELEASEDATE} based on given {@link AdvancedShippingNoticeEntryModel#ASN}
	 *
	 * @param asnEntry
	 * 		the given {@link AdvancedShippingNoticeEntryModel}
	 * @return release date for Stock Level
	 */
	Date getReleaseDateForStockLevel(final AdvancedShippingNoticeEntryModel asnEntry);
}
