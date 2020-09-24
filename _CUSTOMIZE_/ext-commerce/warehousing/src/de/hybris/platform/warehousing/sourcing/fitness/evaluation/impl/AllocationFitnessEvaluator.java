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

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.warehousing.data.sourcing.SourcingLocation;
import de.hybris.platform.warehousing.sourcing.fitness.evaluation.FitnessEvaluator;

import java.util.ArrayList;
import java.util.List;


/**
 * Allocation implementation of FitnessEvaluator interface evaluates a location's ability to allocate ordered items.
 */
public class AllocationFitnessEvaluator implements FitnessEvaluator
{
	private static final long ZERO_LONG = 0;

	@Override
	public Double evaluate(final SourcingLocation sourcingLocation)
	{
		final List<AbstractOrderEntryModel> entries = new ArrayList<>(sourcingLocation.getContext().getOrderEntries());

		long totalAvailableQuantity = ZERO_LONG;
		for (final AbstractOrderEntryModel entry : entries)
		{
			final long availableQuantity = (sourcingLocation.getAvailability().get(entry.getProduct()) == null) ?
					ZERO_LONG :
					sourcingLocation.getAvailability().get(entry.getProduct()).longValue();

			totalAvailableQuantity += availableQuantity;
		}

		return Double.valueOf(totalAvailableQuantity);
	}

}
