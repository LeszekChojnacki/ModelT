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
package de.hybris.platform.warehousing.sourcing.fitness.normalize;

/**
 * The normalizer will ensure that all evaluated fitness values are adjusted in order to be measured on a common scale.
 * By default, we use 100 as the scale.
 */
public interface FitnessNormalizer
{
	/**
	 * Normalize the fitness out of 100.
	 *
	 * @param fitness
	 *           - the evaluated fitness value or {@link Double#NaN} if undefined
	 * @param total
	 *           - the sum of all evaluated fitness values for a given sourcing factor or {@link Double#NaN} if
	 *           undefined; must be greater than 0
	 * @return the normalized fitness or zero if the fitness is {@link Double#NaN}
	 */
	Double normalize(Double fitness, Double total);
}
