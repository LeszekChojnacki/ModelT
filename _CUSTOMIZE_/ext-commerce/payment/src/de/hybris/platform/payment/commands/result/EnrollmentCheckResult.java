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

import de.hybris.platform.payment.commands.EnrollmentCheckCommand;


/**
 * Reply for {@link EnrollmentCheckCommand}
 */
public class EnrollmentCheckResult extends AbstractResult
{
	private String acsURL;
	private String eci;
	private String commerceIndicator;
	private String ucafCollectionIndicator;
	private String xid;
	private String proofXml;
	private String paReq;
	private String proxyPAN;
	private String veresEnrolled;
	private String paymentProvider;

	public EnrollmentCheckResult(final String merchantTransactionCode)
	{
		super();
		this.setMerchantTransactionCode(merchantTransactionCode); // NOSONAR
	}

	public String getAcsURL()
	{
		return acsURL;
	}

	public void setAcsURL(final String acsURL)
	{
		this.acsURL = acsURL;
	}

	public String getEci()
	{
		return eci;
	}

	public void setEci(final String eci)
	{
		this.eci = eci;
	}

	public String getCommerceIndicator()
	{
		return commerceIndicator;
	}

	public void setCommerceIndicator(final String commerceIndicator)
	{
		this.commerceIndicator = commerceIndicator;
	}

	public String getUcafCollectionIndicator()
	{
		return ucafCollectionIndicator;
	}

	public void setUcafCollectionIndicator(final String ucafCollectionIndicator)
	{
		this.ucafCollectionIndicator = ucafCollectionIndicator;
	}

	public String getXid()
	{
		return xid;
	}

	public void setXid(final String xid)
	{
		this.xid = xid;
	}

	public String getProofXml()
	{
		return proofXml;
	}

	public void setProofXml(final String proofXml)
	{
		this.proofXml = proofXml;
	}

	public String getPaReq()
	{
		return paReq;
	}

	public void setPaReq(final String paReq)
	{
		this.paReq = paReq;
	}

	public String getProxyPAN()
	{
		return proxyPAN;
	}

	public void setProxyPAN(final String proxyPAN)
	{
		this.proxyPAN = proxyPAN;
	}

	public String getVeresEnrolled()
	{
		return veresEnrolled;
	}

	public void setVeresEnrolled(final String veresEnrolled)
	{
		this.veresEnrolled = veresEnrolled;
	}

	public void setPaymentProvider(final String paymentProvider)
	{
		this.paymentProvider = paymentProvider;
	}

	public String getPaymentProvider()
	{
		return paymentProvider;
	}
}
