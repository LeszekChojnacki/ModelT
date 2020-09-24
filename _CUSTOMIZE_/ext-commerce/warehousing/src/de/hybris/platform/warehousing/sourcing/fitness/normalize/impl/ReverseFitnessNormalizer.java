/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 *
 */
package de.hybris.platform.warehousing.sourcing.fitness.normalize.impl;

import de.hybris.platform.warehousing.sourcing.fitness.normalize.FitnessNormalizer;

import com.google.common.base.Preconditions;


/**
 * Normalizes the fitness value by dividing it by the given total and multiplying by 100 to produce a percentage. Then
 * subtracting that percentage from 100 to produce a reverse value. This is useful for when a smaller fitness value
 * should be better than a large fitness value.</br>
 * <p>
 * Example</br> <tt>normalize(5.0, 20.0);</tt> means (100 - ((5.0 / 20.0) * 100)) = 75
 * </p>
 */
public class ReverseFitnessNormalizer implements FitnessNormalizer
{
	private static final Double ZERO = 0.0;
	private static final Integer ONE_HUNDRED = 100;

	@Override
	public Double normalize(final Double fitness, final Double total) throws IllegalArgumentException
	{
		Preconditions.checkArgument(total != null, "Total cannot be null.");
		Preconditions.checkArgument(fitness != null, "Fitness cannot be null.");

		if (fitness.equals(Double.NaN) || total.equals(Double.NaN))
		{
			return ZERO;
		}

		Preconditions.checkArgument(fitness >= 0D, "Fitness cannot be negative.");
		Preconditions.checkArgument(total > 0D, "Total cannot be zero or negative.");
		return ONE_HUNDRED - ((fitness / total) * ONE_HUNDRED);
	}
}
