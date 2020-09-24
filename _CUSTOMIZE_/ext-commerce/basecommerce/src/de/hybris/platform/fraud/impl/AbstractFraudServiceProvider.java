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

import de.hybris.platform.fraud.FraudServiceProvider;

import org.springframework.beans.factory.annotation.Required;


/**
 * 
 */
public abstract class AbstractFraudServiceProvider implements FraudServiceProvider
{

	private String providerName;

	@Required
	public void setProviderName(final String providerName)
	{
		this.providerName = providerName;
	}

	@Override
	public String getProviderName()
	{
		return providerName;
	}

	// Mocked Commercial Provider

}
