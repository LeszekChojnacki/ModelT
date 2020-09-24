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
package de.hybris.platform.fraud.strategy;

import static de.hybris.platform.fraud.constants.FrauddetectionConstants.STRATEGY_SUFFIX;

import de.hybris.platform.fraud.impl.FraudSymptom;

import org.springframework.beans.factory.annotation.Required;


/**
 * 
 */
public abstract class AbstractOrderFraudSymptomDetection implements OrderFraudSymptomDetection
{

	public static final double DEFAULT_INCREMENT = 50;

	private double increment = DEFAULT_INCREMENT;
	private String symptomName;


	protected FraudSymptom createSymptom(final boolean positive)
	{
		return createSymptom(null, positive);
	}

	protected FraudSymptom createSymptom(final String explanation, final boolean positive)
	{
		return new FraudSymptom(explanation, positive ? getIncrement() : 0, getSymptomName());
	}

	public String getStrategyName()
	{
		return getSymptomName() + STRATEGY_SUFFIX;
	}

	public String getSymptomName()
	{
		return symptomName;
	}

	@Required
	public void setSymptomName(final String name)
	{
		this.symptomName = name;
	}

	/**
	 * @return the value to be used as score in case the symptom has been tested positively.
	 */
	public double getIncrement()
	{
		return increment;
	}

	public void setIncrement(final double increment)
	{
		this.increment = increment;
	}
}
