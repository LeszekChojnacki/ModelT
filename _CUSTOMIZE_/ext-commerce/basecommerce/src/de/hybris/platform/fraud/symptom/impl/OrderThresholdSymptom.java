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

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.fraud.impl.FraudServiceResponse;
import de.hybris.platform.fraud.impl.FraudSymptom;
import de.hybris.platform.fraud.strategy.AbstractOrderFraudSymptomDetection;


/**
 * OrderThresholdSymptom implements a symptom for detecting whether an order exceeds a defined threshold.
 */
public class OrderThresholdSymptom extends AbstractOrderFraudSymptomDetection
{

	private double thresholdLimit = 1000;
	private double thresholdDelta = 100;

	public double getThresholdLimit()
	{
		return thresholdLimit;
	}

	public void setThresholdLimit(final double thresholdLimit)
	{
		this.thresholdLimit = thresholdLimit;
	}

	public double getThresholdDelta()
	{
		return thresholdDelta;
	}

	public void setThresholdDelta(final double thresholdDelta)
	{
		this.thresholdDelta = thresholdDelta;
	}

	@Override
	public FraudServiceResponse recognizeSymptom(final FraudServiceResponse fraudResponse, final AbstractOrderModel order)
	{
		if (order.getTotalPrice().compareTo(Double.valueOf(getThresholdLimit())) > 0)
		{
			final double difference = order.getTotalPrice().doubleValue() - getThresholdLimit();
			fraudResponse.addSymptom(new FraudSymptom(getSymptomName(), getIncrement(difference)));
		}
		else
		{
			fraudResponse.addSymptom(createSymptom(false));
		}
		return fraudResponse;
	}

	public double getIncrement(final double orderDelta)
	{
		final double stepIncrement = super.getIncrement();
		return Math.ceil(orderDelta / getThresholdDelta()) * stepIncrement;
	}
}
