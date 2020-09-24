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
import de.hybris.platform.warehousing.data.sourcing.SourcingResult;
import de.hybris.platform.warehousing.sourcing.fitness.FitnessService;
import de.hybris.platform.warehousing.sourcing.strategy.AbstractSourcingStrategy;

import java.util.Collection;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;


/**
 * Strategy allows an order to be split into multiple locations. The strategy creates sources for order entry based on
 * location availability.
 */
public class NoRestrictionsStrategy extends AbstractSourcingStrategy
{
	private static Logger LOGGER = LoggerFactory.getLogger(NoRestrictionsStrategy.class);

	private FitnessService fitnessService;

	@Override
	public void source(final SourcingContext sourcingContext)
	{
		validateParameterNotNullStandardMessage("sourcingContext", sourcingContext);

		final Collection<SourcingLocation> fitnessResult = getFitnessService().sortByFitness(sourcingContext);

		fitnessResult.forEach(fitnessSourcingLocation -> sourcingContext.getOrderEntries()
				.forEach(orderEntry -> createSourcingResult(sourcingContext, orderEntry, fitnessSourcingLocation)));

		final boolean checkSourceCompleted = checkSourceCompleted(sourcingContext);
		sourcingContext.getResult().setComplete(checkSourceCompleted);

		LOGGER.debug("Total order entries sourceable using No Restrictions Strategy: {}",
				sourcingContext.getResult().getResults().size());
	}

	/**
	 * It creates a sourcing result
	 *
	 * @param sourcingContext
	 * 		the {@link SourcingContext}
	 * @param orderEntry
	 * 		the {@link AbstractOrderEntryModel}
	 * @param fitnessSourcingLocation
	 * 		the {@link SourcingLocation}
	 */
	protected void createSourcingResult(final SourcingContext sourcingContext, final AbstractOrderEntryModel orderEntry,
			final SourcingLocation fitnessSourcingLocation)
	{
		final Long totalQtySourced = Long.valueOf(getQuantitySourced(sourcingContext.getResult().getResults(), orderEntry));
		final OrderEntryModel orderEntryModel = (OrderEntryModel) orderEntry;

		if (totalQtySourced.longValue() < orderEntryModel.getQuantityUnallocated().longValue())
		{
			final Long stockLevel = getAvailabilityForProduct(orderEntry.getProduct(), fitnessSourcingLocation);
			final Long remainingQty = Long
					.valueOf(orderEntryModel.getQuantityUnallocated().longValue() - totalQtySourced.longValue());

			Long orderQty;
			if ((stockLevel == null || stockLevel.longValue() > 0) && remainingQty.longValue() > 0)
			{
				if (stockLevel == null || stockLevel.longValue() >= remainingQty.longValue())
				{
					orderQty = remainingQty;
				}
				else
				{
					orderQty = stockLevel;
				}


				final Optional<SourcingResult> result = sourcingContext.getResult().getResults().stream()
						.filter(predicate -> predicate.getWarehouse().equals(fitnessSourcingLocation.getWarehouse())).findFirst();

				if (result.isPresent())
				{

					result.get().getAllocation().put(orderEntry, orderQty);

					LOGGER.debug("Updated sourcing result for product [{}]: requested qty [{}] at location [{}]",
							orderEntry.getProduct().getCode(), orderQty, fitnessSourcingLocation.getWarehouse().getCode());

				}
				else
				{
					sourcingContext.getResult().getResults()
							.add(getSourcingResultFactory().create(orderEntry, fitnessSourcingLocation, orderQty));

					LOGGER.debug("Created sourcing result for product [{}]: requested qty [{}] at location [{}] ",
							orderEntry.getProduct().getCode(), orderQty, fitnessSourcingLocation.getWarehouse().getCode());
				}

			}
		}
	}

	protected FitnessService getFitnessService()
	{
		return fitnessService;
	}

	@Required
	public void setFitnessService(final FitnessService fitnessService)
	{
		this.fitnessService = fitnessService;
	}
}
