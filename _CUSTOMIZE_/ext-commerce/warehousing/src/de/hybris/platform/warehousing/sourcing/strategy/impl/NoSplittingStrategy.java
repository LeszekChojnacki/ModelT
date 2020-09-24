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
package de.hybris.platform.warehousing.sourcing.strategy.impl;

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.warehousing.data.sourcing.SourcingContext;
import de.hybris.platform.warehousing.data.sourcing.SourcingLocation;
import de.hybris.platform.warehousing.sourcing.fitness.FitnessService;
import de.hybris.platform.warehousing.sourcing.strategy.AbstractSourcingStrategy;

import java.util.Collection;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;


/**
 * Strategy to apply when the order is not allowed to be split into multiple shipments.
 */
public class NoSplittingStrategy extends AbstractSourcingStrategy
{

	private static final Logger LOGGER = LoggerFactory.getLogger(NoSplittingStrategy.class);

	private FitnessService fitnessService;

	@Override
	public void source(final SourcingContext sourcingContext)
	{
		validateParameterNotNullStandardMessage("sourcingContext", sourcingContext);

		final Collection<SourcingLocation> sourcingLocations = getFitnessCalculationService().sortByFitness(sourcingContext);

		final Optional<SourcingLocation> bestMatch = sourcingLocations.stream()
				.filter(sourcingLocation -> isSourcingNoSplittingPossible(sourcingContext.getOrderEntries(), sourcingLocation))
				.findFirst();

		bestMatch.ifPresent(bestLocation -> sourcingContext.getResult().getResults()
				.add(getSourcingResultFactory().create(sourcingContext.getOrderEntries(), bestLocation)));

		final boolean checkSourceCompleted = checkSourceCompleted(sourcingContext);
		sourcingContext.getResult().setComplete(checkSourceCompleted);

		LOGGER.debug("Total order entries sourceable using No Splitting Strategy: {}",
				sourcingContext.getResult().getResults().size());
	}

	/**
	 * Check if it is possible to source.
	 *
	 * @param entries
	 * 		the order entries to check if it is sourceable or not.
	 * @param sourcingLocation
	 * 		the destination location to test the sourcing.
	 * @return true if we can source the entire order from this location, otherwise false
	 */
	protected boolean isSourcingNoSplittingPossible(final Collection<AbstractOrderEntryModel> entries,
			final SourcingLocation sourcingLocation)
	{
		return entries.stream().allMatch(
				entry -> ((OrderEntryModel) entry).getQuantityUnallocated().longValue() <= getAvailabilityForProduct(
						entry.getProduct(), sourcingLocation).longValue());
	}

	/**
	 * @return the {@link FitnessService}
	 */
	protected FitnessService getFitnessCalculationService()
	{
		return fitnessService;
	}

	/**
	 * set the {@link FitnessService}
	 *
	 * @param fitnessCalculationService
	 * 		the {@link FitnessService}
	 */
	@Required
	public void setFitnessCalculationService(final FitnessService fitnessCalculationService)
	{
		this.fitnessService = fitnessCalculationService;
	}
}
