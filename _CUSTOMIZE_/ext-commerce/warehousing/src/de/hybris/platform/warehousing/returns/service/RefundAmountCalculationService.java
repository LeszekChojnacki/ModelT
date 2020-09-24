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
package de.hybris.platform.warehousing.returns.service;

import de.hybris.platform.returns.model.RefundEntryModel;
import de.hybris.platform.returns.model.ReturnEntryModel;
import de.hybris.platform.returns.model.ReturnRequestModel;

import java.math.BigDecimal;

/**
 * Service for handling refundAmount for {@link ReturnRequestModel}. It calculates and persists the refund amount for ReturnRequest and the included {@link RefundEntryModel}
 */
public interface RefundAmountCalculationService
{
	/**
	 * Returns the custom refund amount for a {@link ReturnRequestModel}.
	 *
	 * @param returnRequest
	 * 		the {@link ReturnRequestModel} for which refund amount needs to be calculated
	 * @return {@link BigDecimal} representing the custom refund amount
	 */
	BigDecimal getCustomRefundAmount(final ReturnRequestModel returnRequest);

	/**
	 * Gets the {@link RefundEntryModel#_amount} based on the basePrice of products being returned in the entry.
	 *
	 * @param refundEntryModel
	 * 		the {@link RefundEntryModel} for which refund amount needs to be calculated
	 * @return the refundAmount for the requested {@link RefundEntryModel}
	 */
	BigDecimal getCustomRefundEntryAmount(final ReturnEntryModel refundEntryModel);

	/**
	 * Returns the original refund amount for a {@link ReturnRequestModel}.
	 *
	 * @param returnRequest
	 * 		the {@link ReturnRequestModel} for which refund amount needs to be calculated
	 * @return {@link BigDecimal} representing the original refund amount
	 */
	BigDecimal getOriginalRefundAmount(final ReturnRequestModel returnRequest);

	/**
	 * Calculates and persists the {@link RefundEntryModel#_amount} based on the amount of products being returned in the entry.
	 *
	 * @param refundEntryModel
	 * 		the {@link RefundEntryModel} for which refund entry amount needs to be calculated
	 * @return the sum of all refund amount {@link RefundEntryModel} for all entries, otherwise 0
	 */
	BigDecimal getOriginalRefundEntryAmount(final ReturnEntryModel refundEntryModel);
}
