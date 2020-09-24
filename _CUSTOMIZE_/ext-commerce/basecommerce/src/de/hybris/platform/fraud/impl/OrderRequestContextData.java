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
package de.hybris.platform.fraud.impl;

import de.hybris.platform.fraud.constants.FrauddetectionConstants;

import java.util.HashMap;
import java.util.Map;


/**
 * 
 */
public class OrderRequestContextData
{

	private String ipAddress;
	private String domain;

	/**
	 * @return the ipAddress
	 */
	public String getIpAddress()
	{
		return ipAddress;
	}

	/**
	 * @param ipAddress
	 *           the ipAddress to set
	 */
	public void setIpAddress(final String ipAddress)
	{
		this.ipAddress = ipAddress;
	}

	/**
	 * @return the domain
	 */
	public String getDomain()
	{
		return domain;
	}

	/**
	 * @param domain
	 *           the domain to set
	 */
	public void setDomain(final String domain)
	{
		this.domain = domain;
	}

	public Map<String, String> asMap()
	{
		final Map<String, String> result = new HashMap<String, String>();
		result.put(FrauddetectionConstants.PARAM_IP, this.ipAddress);
		result.put(FrauddetectionConstants.PARAM_DOMAIN, this.domain);
		return result;
	}

}
