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
package de.hybris.platform.warehousing.sourcing.impl;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.warehousing.data.sourcing.SourcingContext;
import de.hybris.platform.warehousing.data.sourcing.SourcingResults;
import de.hybris.platform.warehousing.sourcing.SourcingService;
import de.hybris.platform.warehousing.sourcing.context.SourcingContextFactory;
import de.hybris.platform.warehousing.sourcing.context.grouping.OrderEntryGroup;
import de.hybris.platform.warehousing.sourcing.context.grouping.OrderEntryGroupingService;
import de.hybris.platform.warehousing.sourcing.context.grouping.OrderEntryMatcher;
import de.hybris.platform.warehousing.sourcing.filter.SourcingFilterProcessor;
import de.hybris.platform.warehousing.sourcing.result.SourcingResultFactory;
import de.hybris.platform.warehousing.sourcing.strategy.SourcingStrategy;
import de.hybris.platform.warehousing.sourcing.strategy.SourcingStrategyMapper;
import de.hybris.platform.warehousing.sourcing.strategy.SourcingStrategyService;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;


/**
 * Service to evaluate the best way to source an order using some defined sourcing strategies. Its result will determine
 * the number of consignments to create.
 */
public class DefaultSourcingService implements SourcingService
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSourcingService.class);

	private SourcingContextFactory sourcingContextFactory;
	private SourcingFilterProcessor sourcingFilterProcessor;
	private OrderEntryGroupingService orderEntryGroupingService;
	private Collection<OrderEntryMatcher<?>> orderEntryMatchers;
	private SourcingStrategyService sourcingStrategyService;
	private Collection<SourcingStrategyMapper> sourcingStrategyMappers;
	private SourcingResultFactory sourcingResultFactory;

	@Override
	public SourcingResults sourceOrder(final AbstractOrderModel order)
	{
		validateParameterNotNullStandardMessage("order", order);
		Preconditions.checkArgument(Objects.nonNull(order), "Parameter order cannot be null.");

		LOGGER.debug("Starting sourcing Order [{}]", order.getCode());

		final Set<WarehouseModel> locations = Sets.newHashSet();
		sourcingFilterProcessor.filterLocations(order, locations);

		LOGGER.debug("> Total filtered sourcing locations found: {}", locations.size());

		final Set<OrderEntryGroup> groups = orderEntryGroupingService.splitOrderByMatchers(order, orderEntryMatchers);

		LOGGER.debug("> Total order entry groups found: {}", groups.size());

		final Collection<SourcingContext> contexts = sourcingContextFactory.create(groups, locations);

		final Collection<SourcingResults> results = Lists.newArrayList();
		for (final SourcingContext context : contexts)
		{
			final List<String> productNames = Lists.newArrayList();
			context.getOrderEntries().forEach(e -> productNames.add(e.getProduct().getCode()));
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("Start sourcing products [{}]", StringUtils.join(productNames, ", "));
			}
			List<SourcingStrategy> strategies = sourcingStrategyService.getStrategies(context, sourcingStrategyMappers);
			if (strategies.isEmpty())
			{
				strategies = sourcingStrategyService.getDefaultStrategies();
			}

			final List<String> strategyNames = Lists.newArrayList();
			strategies.forEach(s -> strategyNames.add(s.getClass().getSimpleName()));
			LOGGER.debug("> Total sourcing strategies found for context: {} :: {}", strategies.size(),
					StringUtils.join(strategyNames, ", "));

			for (final SourcingStrategy strategy : strategies)
			{
				LOGGER.debug("------ Apply sourcing strategy: {}", strategy.getClass().getSimpleName());
				strategy.source(context);

				context.getResult().getResults()
						.forEach(result -> LOGGER.debug("Warehouse found by sourcing strategy: {}", result.getWarehouse().getCode()));

				results.add(context.getResult());
				if (LOGGER.isDebugEnabled())
				{
					LOGGER.debug("Sourcing strategy '" + strategy.getClass().getSimpleName() + "' applied" + (context.getResult()
							.isComplete() ? "" : " not") + " successfully");
				}
				if (strategy.isTerminal() || context.getResult().isComplete())
				{
					break;
				}
			}
		}

		// aggregate all sourcing results
		return sourcingResultFactory.create(results);
	}

	protected SourcingContextFactory getSourcingContextFactory()
	{
		return sourcingContextFactory;
	}

	@Required
	public void setSourcingContextFactory(final SourcingContextFactory sourcingContextFactory)
	{
		this.sourcingContextFactory = sourcingContextFactory;
	}

	protected SourcingFilterProcessor getSourcingFilterProcessor()
	{
		return sourcingFilterProcessor;
	}

	@Required
	public void setSourcingFilterProcessor(final SourcingFilterProcessor sourcingFilterProcessor)
	{
		this.sourcingFilterProcessor = sourcingFilterProcessor;
	}

	protected OrderEntryGroupingService getOrderEntryGroupingService()
	{
		return orderEntryGroupingService;
	}

	@Required
	public void setOrderEntryGroupingService(final OrderEntryGroupingService orderEntryGroupingService)
	{
		this.orderEntryGroupingService = orderEntryGroupingService;
	}

	protected Collection<OrderEntryMatcher<?>> getOrderEntryMatchers()
	{
		return orderEntryMatchers;
	}

	@Required
	public void setOrderEntryMatchers(final Collection<OrderEntryMatcher<?>> orderEntryMatchers)
	{
		this.orderEntryMatchers = orderEntryMatchers;
	}

	protected SourcingStrategyService getSourcingStrategyService()
	{
		return sourcingStrategyService;
	}

	@Required
	public void setSourcingStrategyService(final SourcingStrategyService sourcingStrategyService)
	{
		this.sourcingStrategyService = sourcingStrategyService;
	}

	protected Collection<SourcingStrategyMapper> getSourcingStrategyMappers()
	{
		return sourcingStrategyMappers;
	}

	@Required
	public void setSourcingStrategyMappers(final Collection<SourcingStrategyMapper> sourcingStrategyMappers)
	{
		this.sourcingStrategyMappers = sourcingStrategyMappers;
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

}
