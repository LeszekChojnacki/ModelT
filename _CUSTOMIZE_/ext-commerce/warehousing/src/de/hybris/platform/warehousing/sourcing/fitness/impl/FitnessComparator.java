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
package de.hybris.platform.warehousing.sourcing.fitness.impl;

import de.hybris.platform.warehousing.data.sourcing.FitSourcingLocation;

import java.math.BigDecimal;
import java.util.Comparator;


/**
 * Compares two locations according to their fitness value
 */
public class FitnessComparator implements Comparator<FitSourcingLocation>
{
	@Override
	public int compare(final FitSourcingLocation sourcingLocation, final FitSourcingLocation nextSourcingLocation)
	{
		if (sourcingLocation.getFitness().doubleValue() < nextSourcingLocation.getFitness().doubleValue())
		{
			return 1;
		}

		if (BigDecimal.valueOf(sourcingLocation.getFitness().doubleValue())
				.equals(BigDecimal.valueOf(nextSourcingLocation.getFitness().doubleValue())))
		{
			return 0;
		}

		return -1;
	}

}
