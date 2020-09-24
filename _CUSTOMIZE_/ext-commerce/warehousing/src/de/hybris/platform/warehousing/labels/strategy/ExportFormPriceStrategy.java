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
package de.hybris.platform.warehousing.labels.strategy;

import de.hybris.platform.ordersplitting.model.ConsignmentEntryModel;

import java.math.BigDecimal;

/**
 * Strategy used for obtaining the product prices visible on the Export Form
 */
public interface ExportFormPriceStrategy
{
	/**
	 * Calculates single product price for use in Export Form. Should not return null
	 * @param consignmentEntry
	 *           {@link ConsignmentEntryModel} to calculate product price for
	 * @return product price calculated based on consignment entry
	 */
	BigDecimal calculateProductPrice(ConsignmentEntryModel consignmentEntry);

	/**
	 * Calculates total price of product for use in Export Form based on precalculated item price. Should not return
	 * null.
	 * @param productPrice
	 *           price of single product item
	 * @param consignmentEntry
	 *           {@link ConsignmentEntryModel} to calculate total for, based on precalculated productPrice
	 * @return total price calculated based on product price
	 */
	BigDecimal calculateTotalPrice(BigDecimal productPrice, ConsignmentEntryModel consignmentEntry);
}
