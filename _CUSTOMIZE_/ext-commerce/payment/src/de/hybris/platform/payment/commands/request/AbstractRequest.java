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




/**
 * base class for all requests
 */
public abstract class AbstractRequest
{
	private final String merchantTransactionCode;

	/**
	 * @param merchantTransactionCode
	 */
	protected AbstractRequest(final String merchantTransactionCode)
	{
		super();
		this.merchantTransactionCode = merchantTransactionCode;
	}

	/**
	 * @return the merchantTransactionCode
	 */
	public String getMerchantTransactionCode()
	{
		return merchantTransactionCode;
	}
}
