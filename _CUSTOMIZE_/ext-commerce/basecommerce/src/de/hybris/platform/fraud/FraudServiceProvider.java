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


/**
 * Interface to be fulfilled by all fraud detection providers.
 */
public interface FraudServiceProvider
{
	String getProviderName();

	FraudServiceResponse recognizeOrderFraudSymptoms(AbstractOrderModel order);

	FraudServiceResponse recognizeUserActivitySymptoms(UserModel order);

}
