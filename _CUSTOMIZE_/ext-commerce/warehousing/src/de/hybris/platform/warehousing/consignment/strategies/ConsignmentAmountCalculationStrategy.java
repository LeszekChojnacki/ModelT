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
package de.hybris.platform.warehousing.consignment.strategies;

import de.hybris.platform.ordersplitting.model.ConsignmentEntryModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;

import java.math.BigDecimal;


/**
 * This strategy provides methods to calculate amount for a {@link ConsignmentModel} to perform payment and tax related operations.
 */
public interface ConsignmentAmountCalculationStrategy
{
	/**
	 * Calculates the amount to be captured for the given {@link ConsignmentModel}
	 *
	 * @param consignment
	 * 		given {@link ConsignmentModel}
	 * @return the amount to be captured for the given {@link ConsignmentModel}
	 */
	BigDecimal calculateCaptureAmount(ConsignmentModel consignment);

	/**
	 * Calculates the amount which has been already captured on the {@link de.hybris.platform.core.model.order.OrderModel}
	 * of the given {@link ConsignmentModel}
	 *
	 * @param consignment
	 * 		given {@link ConsignmentModel}
	 * @return {@link BigDecimal} the sum of all previously captured amount
	 */
	BigDecimal calculateAlreadyCapturedAmount(ConsignmentModel consignment);

	/**
	 * Calculates the order total of the given {@link ConsignmentModel}
	 *
	 * @param consignment
	 * 		given {@link ConsignmentModel}
	 * @return {@link BigDecimal} the order total
	 */
	BigDecimal calculateTotalOrderAmount(ConsignmentModel consignment);

	/**
	 * Calculates the discount amount for the {@link ConsignmentModel} taking a part of {@link de.hybris.platform.core.model.order.OrderModel#TOTALDISCOUNTS}
	 *
	 * @param consignment
	 * 		given {@link ConsignmentModel}
	 * @return {@link BigDecimal} the discount for the give consignment
	 */
	BigDecimal calculateDiscountAmount(ConsignmentModel consignment);


	/**
	 * Calculates amount for {@link ConsignmentEntryModel}
	 *
	 * @param consignmentEntry
	 * 		given {@link ConsignmentEntryModel}
	 * @param includeTaxes
	 * 		if true returns the amount with taxes otherwise - without taxes
	 * @return {@link BigDecimal} amount of the consignment entry
	 */
	BigDecimal calculateConsignmentEntryAmount(ConsignmentEntryModel consignmentEntry, boolean includeTaxes);
}
