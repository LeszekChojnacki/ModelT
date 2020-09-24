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

import de.hybris.platform.warehousing.data.sourcing.SourcingFactorIdentifiersEnum;


/**
 * Defines the services around registering and getting appropriate fitness evaluators for corresponding factors.
 * 
 * @see FitnessEvaluator
 */
public interface FitnessEvaluatorFactory
{
	/**
	 * Gets the appropriate evaluator implementation for a sourcing factor
	 *
	 * @param factorId
	 *           the sourcing factor id
	 * @return the evaluator
	 */
	FitnessEvaluator getEvaluator(SourcingFactorIdentifiersEnum factorId);
}
