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
package de.hybris.platform.fraud.symptom.impl;

import static java.util.Objects.nonNull;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.fraud.impl.FraudServiceResponse;
import de.hybris.platform.fraud.impl.FraudSymptom;
import de.hybris.platform.fraud.strategy.AbstractOrderFraudSymptomDetection;


/**
 * FirstTimeOrderSymptom implements a symptom for detecting whether it is a first time order.
 */
public class FirstTimeOrderSymptom extends AbstractOrderFraudSymptomDetection
{
	@Override
	public FraudServiceResponse recognizeSymptom(final FraudServiceResponse fraudResponse, final AbstractOrderModel order)
	{
		final boolean firstOrder = nonNull(order.getUser().getOrders()) && order.getUser().getOrders().size() == 1;
		fraudResponse.addSymptom(new FraudSymptom(getSymptomName(), firstOrder ? getIncrement() : 0));
		return fraudResponse;
	}
}
