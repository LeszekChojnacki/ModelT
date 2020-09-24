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

import de.hybris.platform.payment.commands.AuthorizationCommand;
import de.hybris.platform.payment.dto.AvsStatus;
import de.hybris.platform.payment.dto.CvnStatus;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;


/**
 * Result for {@link AuthorizationCommand}
 */
public class AuthorizationResult extends AbstractResult
{
	private Currency currency;
	private BigDecimal totalAmount;
	private AvsStatus avsStatus;
	private CvnStatus cvnStatus;
	private BigDecimal accountBalance;
	private String authorizationCode;
	private Date authorizationTime;
	private String paymentProvider;

	/**
	 * @return the currency
	 */
	public Currency getCurrency()
	{
		return currency;
	}

	/**
	 * @param currency
	 *           the currency to set
	 */
	public void setCurrency(final Currency currency)
	{
		this.currency = currency;
	}

	/**
	 * @return the totalAmount
	 */
	public BigDecimal getTotalAmount()
	{
		return totalAmount;
	}

	/**
	 * @param totalAmount
	 *           the totalAmount to set
	 */
	public void setTotalAmount(final BigDecimal totalAmount)
	{
		this.totalAmount = totalAmount;
	}

	/**
	 * @return the avsStatus
	 */
	public AvsStatus getAvsStatus()
	{
		return avsStatus;
	}

	/**
	 * @param avsStatus
	 *           the avsStatus to set
	 */
	public void setAvsStatus(final AvsStatus avsStatus)
	{
		this.avsStatus = avsStatus;
	}

	/**
	 * @return the getCvnStatus
	 */
	public CvnStatus getCvnStatus()
	{
		return cvnStatus;
	}

	/**
	 * @param cvnStatus
	 *           the cvnStatus to set
	 */
	public void setCvnStatus(final CvnStatus cvnStatus)
	{
		this.cvnStatus = cvnStatus;
	}

	/**
	 * @return the accountBalance
	 */
	public BigDecimal getAccountBalance()
	{
		return accountBalance;
	}

	/**
	 * @param accountBalance
	 *           the accountBalance to set
	 */
	public void setAccountBalance(final BigDecimal accountBalance)
	{
		this.accountBalance = accountBalance;
	}

	/**
	 * @return the authorizationCode
	 */
	public String getAuthorizationCode()
	{
		return authorizationCode;
	}

	/**
	 * @param authorizationCode
	 *           the authorizationCode to set
	 */
	public void setAuthorizationCode(final String authorizationCode)
	{
		this.authorizationCode = authorizationCode;
	}

	/**
	 * @return the authorizationTime
	 */
	public Date getAuthorizationTime()
	{
		return authorizationTime;
	}

	/**
	 * @param authorizationTime
	 *           the authorizationTime to set
	 */
	public void setAuthorizationTime(final Date authorizationTime)
	{
		this.authorizationTime = authorizationTime;
	}

	/**
	 * @param paymentProvider
	 *           the paymentProvider to set
	 */
	public void setPaymentProvider(final String paymentProvider)
	{
		this.paymentProvider = paymentProvider;
	}

	/**
	 * @return the paymentProvider
	 */
	public String getPaymentProvider()
	{
		return paymentProvider;
	}
}
