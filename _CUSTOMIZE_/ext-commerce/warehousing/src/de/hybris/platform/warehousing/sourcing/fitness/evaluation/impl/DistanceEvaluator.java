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
package de.hybris.platform.warehousing.sourcing.fitness.evaluation.impl;

import de.hybris.platform.warehousing.data.sourcing.SourcingLocation;
import de.hybris.platform.warehousing.sourcing.fitness.evaluation.FitnessEvaluator;


/**
 * Distance implementation of fitness evaluator interface. This simply returns the distance provided in the sourcing
 * location.
 */
public class DistanceEvaluator implements FitnessEvaluator
{

	@Override
	public Double evaluate(final SourcingLocation sourcingLocation)
	{
		if(sourcingLocation.getDistance() == null)
		{
			return Double.NaN;
		}
		return sourcingLocation.getDistance();
	}

}
