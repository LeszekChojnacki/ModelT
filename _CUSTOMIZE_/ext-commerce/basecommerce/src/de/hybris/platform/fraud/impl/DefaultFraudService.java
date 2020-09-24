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

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.fraud.FraudService;
import de.hybris.platform.fraud.FraudServiceProvider;

import java.util.Collection;
import java.util.Collections;


/**
 * Default {@link FraudService} implementation
 */
public class DefaultFraudService implements FraudService
{
	private Collection<FraudServiceProvider> providers;

	/**
	 * @return the providers
	 */
	@Override
	public Collection<FraudServiceProvider> getProviders()
	{
		return providers == null ? Collections.emptyList() : providers;
	}

	/**
	 * @param providers
	 *           the providers to set
	 */
	public void setProviders(final Collection<FraudServiceProvider> providers)
	{
		this.providers = providers;
	}

	@Override
	public FraudServiceProvider getProvider(final String name)
	{
		// YTODO cache by name
		for (final FraudServiceProvider p : getProviders())
		{
			if (name.equalsIgnoreCase(p.getProviderName()))
			{
				return p;
			}
		}
		throw new IllegalArgumentException("got no configured provider " + name + " within " + getProviders());
	}

	@Override
	public FraudServiceResponse recognizeActivitySymptoms(final String providerName, final UserModel user)
	{
		return getProvider(providerName).recognizeUserActivitySymptoms(user);
	}

	@Override
	public FraudServiceResponse recognizeOrderSymptoms(final String providerName, final AbstractOrderModel order)
	{
		return getProvider(providerName).recognizeOrderFraudSymptoms(order);
	}

}
