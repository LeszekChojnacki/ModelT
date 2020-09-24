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
package de.hybris.platform.warehousing.asn.service;

import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.warehousing.model.AdvancedShippingNoticeModel;

import java.util.List;


/**
 * Service for handling {@link AdvancedShippingNoticeModel}.
 */
public interface AsnService
{
	/**
	 * Creates {@link de.hybris.platform.ordersplitting.model.StockLevelModel}s based on given asn entries taken from
	 * {@link AdvancedShippingNoticeModel}
	 *
	 * @param asn
	 * 		advanced shipping notice
	 */
	void processAsn(AdvancedShippingNoticeModel asn);

	/**
	 * Confirms the receipt of given {@link AdvancedShippingNoticeModel}
	 *
	 * @param internalId
	 * 		the given {@link AdvancedShippingNoticeModel#INTERNALID}
	 * @return the updated {@link AdvancedShippingNoticeModel}
	 */
	AdvancedShippingNoticeModel confirmAsnReceipt(String internalId);

	/**
	 * Returns {@link AdvancedShippingNoticeModel} for given {@link AdvancedShippingNoticeModel#INTERNALID}
	 *
	 * @param internalId
	 * 		the given {@link AdvancedShippingNoticeModel#INTERNALID}
	 * @return the {@link AdvancedShippingNoticeModel}
	 */
	AdvancedShippingNoticeModel getAsnForInternalId(String internalId);

	/**
	 * Returns list of {@link StockLevelModel}(s) for the given {@link AdvancedShippingNoticeModel}
	 *
	 * @param asn
	 * 		the {@link AdvancedShippingNoticeModel}
	 * @return the list of corresponding {@link StockLevelModel}
	 */
	List<StockLevelModel> getStockLevelsForAsn(AdvancedShippingNoticeModel asn);

	/**
	 * Cancels the {@link AdvancedShippingNoticeModel} for the given {@link AdvancedShippingNoticeModel#INTERNALID}
	 *
	 * @param internalId
	 * 		the given {@link AdvancedShippingNoticeModel#INTERNALID}
	 * @return the updated {@link AdvancedShippingNoticeModel}
	 */
	AdvancedShippingNoticeModel cancelAsn(String internalId);
}
