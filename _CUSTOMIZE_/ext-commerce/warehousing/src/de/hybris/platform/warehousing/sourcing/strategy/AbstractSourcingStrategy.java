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
package de.hybris.platform.warehousing.sourcing.strategy;

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.warehousing.data.sourcing.SourcingContext;
import de.hybris.platform.warehousing.data.sourcing.SourcingLocation;
import de.hybris.platform.warehousing.data.sourcing.SourcingResult;
import de.hybris.platform.warehousing.sourcing.result.SourcingResultFactory;

import java.util.Set;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * Abstract sourcing strategy. This will delegate the post-processing.
 */
public abstract class AbstractSourcingStrategy implements SourcingStrategy
{
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSourcingStrategy.class);
	private Boolean terminal;
	private SourcingResultFactory sourcingResultFactory;

	/**
	 * Retrieve the stock available from a sourcing location
	 *
	 * @param productModel
	 * 		the {@link ProductModel}
	 * @param sourcingLocation
	 * 		the {@link SourcingLocation}
	 * @return the stock available
	 */
	protected Long getAvailabilityForProduct(final ProductModel productModel, final SourcingLocation sourcingLocation)
	{
		Long stockLevel = 0L;

		if (sourcingLocation.getAvailability() != null && sourcingLocation.getAvailability().get(productModel) != null)
		{
			stockLevel = sourcingLocation.getAvailability().get(productModel);
		}
		return stockLevel;
	}

	/**
	 * Retrieve the quantity already sourced
	 *
	 * @param sourcingResults
	 * 		{@link SourcingContext}
	 * @param orderEntry
	 * 		the {@link AbstractOrderEntryModel}
	 * @return
	 */
	protected long getQuantitySourced(final Set<SourcingResult> sourcingResults, final AbstractOrderEntryModel orderEntry)
	{
		return sourcingResults.stream().filter(result -> result.getAllocation() != null)
				.mapToLong(obj -> obj.getAllocation().get(orderEntry) == null ? 0L : obj.getAllocation().get(orderEntry).longValue())
				.sum();
	}

	/**
	 * Check if the order was sourced.
	 *
	 * @param sourcingContext
	 * 		{@link SourcingContext}
	 * @return true if the all order entries was fully sourced
	 */
	protected boolean checkSourceCompleted(final SourcingContext sourcingContext)
	{

		final Predicate<? super AbstractOrderEntryModel> predicate = entry ->
				getQuantitySourced(sourcingContext.getResult().getResults(), entry) == ((OrderEntryModel) entry)
						.getQuantityUnallocated().longValue();

		final boolean allMatch;
		allMatch = sourcingContext.getOrderEntries().stream().allMatch(predicate);
		LOGGER.debug("The order has been completely sourced :: {}", (allMatch ? "YES" : "NO"));
		return allMatch;
	}


	@Override
	public Boolean isTerminal()
	{
		return terminal;
	}

	protected SourcingResultFactory getSourcingResultFactory()
	{
		return sourcingResultFactory;
	}

	@Required
	public void setSourcingResultFactory(final SourcingResultFactory sourcingResultFactory)
	{
		this.sourcingResultFactory = sourcingResultFactory;
	}


	@Required
	public void setTerminal(final Boolean terminal)
	{
		this.terminal = terminal;
	}
}
