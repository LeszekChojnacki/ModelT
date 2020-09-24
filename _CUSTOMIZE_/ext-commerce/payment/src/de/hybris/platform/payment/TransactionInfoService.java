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
package de.hybris.platform.payment;

import de.hybris.platform.payment.model.PaymentTransactionEntryModel;


/**
 * Checks if a payment authorization transaction is 'successful' respectively 'valid'
 */
public interface TransactionInfoService
{
	/**
	 * Checks if the payment authorization transaction is 'valid'
	 * 
	 * @param entry
	 *           {@link PaymentTransactionEntryModel}
	 * @return true, if valid
	 */
	boolean isValid(PaymentTransactionEntryModel entry);

	/**
	 * Checks if the payment authorization transaction is 'successful'
	 * 
	 * @param entry
	 *           {@link PaymentTransactionEntryModel}
	 * @return true, if successful
	 */
	boolean isSuccessful(PaymentTransactionEntryModel entry);
}
