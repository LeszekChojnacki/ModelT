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
package de.hybris.platform.payment.strategy;

import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.payment.dto.CardInfo;
import de.hybris.platform.payment.model.PaymentTransactionModel;

import java.math.BigDecimal;


/**
 * Attaches PaymentInfo to the assigned PaymentTransactionModel instance.
 */
public interface PaymentInfoCreatorStrategy
{
	/**
	 * Attaches PaymentInfo to the assigned PaymentTransactionModel instance.
	 * 
	 * @param paymentTransactionModel
	 *           the payment transaction
	 * @param userModel
	 *           the user
	 * @param cardInfo
	 *           the card info
	 * @param amount
	 *           the amount
	 */
	void attachPaymentInfo(final PaymentTransactionModel paymentTransactionModel, final UserModel userModel,
			final CardInfo cardInfo, BigDecimal amount);
}
