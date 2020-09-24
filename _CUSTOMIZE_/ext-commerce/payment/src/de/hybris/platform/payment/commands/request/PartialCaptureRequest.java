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
package de.hybris.platform.payment.commands.request;

import de.hybris.platform.payment.commands.PartialCaptureCommand;

import java.math.BigDecimal;
import java.util.Currency;


/**
 * Request for {@link PartialCaptureCommand}
 */
public class PartialCaptureRequest extends CaptureRequest
{
	private final String partialPaymentID;

	/**
	 * @param merchantTransactionCode
	 * @param requestId
	 * @param requestToken
	 * @param currency
	 * @param totalAmount
	 * @param partialPaymentID
	 *           'current time in millis' will be used if <null> was submitted
	 */
	public PartialCaptureRequest(final String merchantTransactionCode, final String requestId, final String requestToken,
			final Currency currency, final BigDecimal totalAmount, final String partialPaymentID, final String paymentProvider)
	{
		super(merchantTransactionCode, requestId, requestToken, currency, totalAmount, paymentProvider);
		this.partialPaymentID = partialPaymentID == null ? String.valueOf(System.currentTimeMillis()) : partialPaymentID;
	}

	/**
	 * @return the partialPaymentID
	 */
	public String getPartialPaymentID()
	{
		return partialPaymentID;
	}
}
