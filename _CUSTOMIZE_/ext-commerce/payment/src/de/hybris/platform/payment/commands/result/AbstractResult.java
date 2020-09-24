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
package de.hybris.platform.payment.commands.result;

import de.hybris.platform.payment.dto.TransactionStatus;
import de.hybris.platform.payment.dto.TransactionStatusDetails;


public abstract class AbstractResult
{
	private String merchantTransactionCode;
	private String requestId;
	private String requestToken;
	private String reconciliationId;
	private TransactionStatus transactionStatus;
	private TransactionStatusDetails transactionStatusDetails;

	public String getMerchantTransactionCode()
	{
		return merchantTransactionCode;
	}

	public void setMerchantTransactionCode(final String merchantTransactionCode)
	{
		this.merchantTransactionCode = merchantTransactionCode;
	}

	public String getRequestId()
	{
		return requestId;
	}

	public void setRequestId(final String requestId)
	{
		this.requestId = requestId;
	}

	public String getRequestToken()
	{
		return requestToken;
	}

	public void setRequestToken(final String requestToken)
	{
		this.requestToken = requestToken;
	}

	public TransactionStatus getTransactionStatus()
	{
		return transactionStatus;
	}

	public void setTransactionStatus(final TransactionStatus transactionStatus)
	{
		this.transactionStatus = transactionStatus;
	}

	public TransactionStatusDetails getTransactionStatusDetails()
	{
		return transactionStatusDetails;
	}

	public void setTransactionStatusDetails(final TransactionStatusDetails transactionStatusDetails)
	{
		this.transactionStatusDetails = transactionStatusDetails;
	}

	public String getReconciliationId()
	{
		return reconciliationId;
	}

	public void setReconciliationId(final String reconciliationId)
	{
		this.reconciliationId = reconciliationId;
	}
}
