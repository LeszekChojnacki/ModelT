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
package de.hybris.platform.payment.impl;

import de.hybris.platform.payment.TransactionInfoService;
import de.hybris.platform.payment.dto.TransactionStatus;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;

import java.util.Calendar;


/**
 * Checks if a payment authorization transaction is 'successful' respectively 'valid'
 */
public class DefaultTransactionInfoService implements TransactionInfoService
{

	/**
	 * Checks if the payment authorization transaction is 'successful'
	 * 
	 * @param entry
	 *           {@link PaymentTransactionEntryModel}
	 * @return true, if @link {@link PaymentTransactionEntryModel#TRANSACTIONSTATUS} equals
	 *         {@link TransactionStatus#ACCEPTED}
	 */
	@Override
	public boolean isSuccessful(final PaymentTransactionEntryModel entry)
	{
		return check(entry, TransactionStatus.ACCEPTED);
	}

	/**
	 * Checks if the payment authorization transaction is 'valid'
	 * 
	 * @param entry
	 *           {@link PaymentTransactionEntryModel}
	 * @return true, if {@link PaymentTransactionEntryModel#TIME} is not older than 24hrs and is of tpye
	 *         {@link PaymentTransactionType#AUTHORIZATION} OR if it is not of type
	 *         {@link PaymentTransactionType#AUTHORIZATION} at all
	 */
	@Override
	public boolean isValid(final PaymentTransactionEntryModel entry)
	{
		if (entry.getType().equals(PaymentTransactionType.AUTHORIZATION))
		{
			final Calendar calendar = Calendar.getInstance();
			calendar.setTime(entry.getTime());
			calendar.add(Calendar.HOUR, 24);
			return calendar.after(Calendar.getInstance());
		}
		return true;
	}

	protected boolean check(final PaymentTransactionEntryModel entry, final TransactionStatus status)
	{
		final String transactionStatus = entry.getTransactionStatus() != null ? entry.getTransactionStatus().trim() : "";
		return transactionStatus.equalsIgnoreCase(status.name());
	}

}
