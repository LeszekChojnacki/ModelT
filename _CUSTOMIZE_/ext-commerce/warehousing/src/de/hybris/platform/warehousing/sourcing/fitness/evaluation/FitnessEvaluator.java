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
package de.hybris.platform.warehousing.sourcing.fitness.evaluation;

import de.hybris.platform.warehousing.data.sourcing.SourcingLocation;


/**
 *	Defines the services for fitness evaluation
 */
public interface FitnessEvaluator
{
	/**
	 * Calculate the fitness value
	 *
	 * @param sourcingLocation
	 *           the location to be evaluated
	 * @return the fitness value or {@link Double#NaN} if it cannot be evaluated
	 */
	Double evaluate(SourcingLocation sourcingLocation);
}
