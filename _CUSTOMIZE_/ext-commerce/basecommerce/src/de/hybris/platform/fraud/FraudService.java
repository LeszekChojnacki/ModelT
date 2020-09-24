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
package de.hybris.platform.fraud;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.fraud.impl.FraudServiceResponse;

import java.util.Collection;


/**
 * Service which manages a number of {@link FraudServiceProvider} and fetches results from them.
 */
public interface FraudService
{

	/**
	 * Analyzes the given order data in the aspect of fraud detection
	 *
	 * @param providerName
	 * 		name of provider
	 * @param order
	 * 		instance of {@link AbstractOrderModel}
	 * @return instance of {@link FraudServiceResponse}
	 */
	FraudServiceResponse recognizeOrderSymptoms(String providerName, AbstractOrderModel order);

	/**
	 * Analyzes the user activities in the aspect of fraud detection
	 *
	 * @param providerName
	 * 		name of provider
	 * @param user
	 * 		instance of {@link UserModel}
	 * @return instance of {@link FraudServiceResponse}
	 */
	FraudServiceResponse recognizeActivitySymptoms(String providerName, UserModel user);

	/**
	 * Fetches a specific fraud detection provider.
	 *
	 * @param name
	 * 		name of provider to search
	 * @return instance of {@link FraudServiceResponse}
	 */
	FraudServiceProvider getProvider(final String name);

	/**
	 * Returns all configured fraud detection services.
	 *
	 * @return collection of {@link FraudServiceProvider} for all detected providers
	 */
	Collection<FraudServiceProvider> getProviders();

}
