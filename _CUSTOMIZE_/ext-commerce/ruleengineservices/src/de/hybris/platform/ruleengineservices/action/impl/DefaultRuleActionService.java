/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.ruleengineservices.action.impl;

import static java.util.Objects.isNull;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.ruleengineservices.action.RuleActionService;
import de.hybris.platform.ruleengineservices.action.RuleActionStrategy;
import de.hybris.platform.ruleengineservices.rao.AbstractRuleActionRAO;
import de.hybris.platform.ruleengineservices.rao.DiscountRAO;
import de.hybris.platform.ruleengineservices.rao.FreeProductRAO;
import de.hybris.platform.ruleengineservices.rao.RuleEngineResultRAO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.assertj.core.util.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Default implementation of {@link RuleActionService}
 */
public class DefaultRuleActionService implements RuleActionService
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRuleActionService.class);

	private Map<String, RuleActionStrategy> actionStrategiesMapping;

	/**
	 * Takes a RuleEngineResultRAO as argument and gets list of its Actions, then for each Action applies corresponding
	 * RuleActionStrategy specified by its strategy field.
	 */
	@Override
	public List<ItemModel> applyAllActions(final RuleEngineResultRAO ruleEngineResultRAO)
	{
		final List<ItemModel> actionResults = Lists.newArrayList();
		if (ruleEngineResultRAO != null && ruleEngineResultRAO.getActions() != null)
		{
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("applyAllActions triggered for actions: [{}]",
						ruleEngineResultRAO.getActions().stream().map(AbstractRuleActionRAO::getFiredRuleCode)
								.collect(Collectors.joining(", ")));
			}

			for (final AbstractRuleActionRAO action : ruleEngineResultRAO.getActions())
			{
				final RuleActionStrategy strategy = getRuleActionStrategy(action.getActionStrategyKey());

				if (isNull(strategy))
				{
					LOGGER.error("Strategy bean for key '{}' not found!", action.getActionStrategyKey());
					continue;
				}

				if (isActionApplicable(action, ruleEngineResultRAO.getActions()))
				{
					actionResults.addAll(strategy.apply(action));
				}
			}
		}
		else
		{
			LOGGER.warn("applyAllActions called for undefined action set!");
		}

		return actionResults;
	}

	protected boolean isActionApplicable(final AbstractRuleActionRAO action, final Set<AbstractRuleActionRAO> actions)
	{
		if (!(action instanceof DiscountRAO))
		{
			return true;
		}

		final DiscountRAO discount = (DiscountRAO) action;
		if (discount.getValue() == null || discount.getValue().compareTo(BigDecimal.ZERO) > 0)
		{
			return true;
		}

		return (discount.getValue().intValue() == 0 &&
			actions.stream().filter(FreeProductRAO.class::isInstance)
						.anyMatch(d -> ((FreeProductRAO) d).getAddedOrderEntry().equals(action.getAppliedToObject())));
	}

	/**
	 * returns the {@code RuleActionStrategy} defined in the {@code actionStrategiesMapping} attribute of this service by
	 * looking up it's hey.
	 *
	 * @param strategyKey
	 * 		the key of the RuleActionStrategy to look up
	 *
	 * @return the found bean id
	 *
	 * @throws IllegalArgumentException
	 * 		if the requested strategy cannot be found
	 * @throws IllegalStateException
	 * 		if this method is called but no strategies are configured
	 */
	protected RuleActionStrategy getRuleActionStrategy(final String strategyKey)
	{
		if (getActionStrategiesMapping() != null)
		{
			final RuleActionStrategy strategy = getActionStrategiesMapping().get(strategyKey);
			if (strategy != null)
			{
				return strategy;
			}
			throw new IllegalArgumentException("cannot find RuleActionStrategy for given action: " + strategyKey);
		}
		throw new IllegalStateException("cannot call getActionStrategiesMapping(\"" + strategyKey
				+ "\"), no strategy mapping defined! Please configure your DefaultRuleActionService bean to contain actionStrategiesMapping.");

	}

	public Map<String, RuleActionStrategy> getActionStrategiesMapping()
	{
		return actionStrategiesMapping;
	}

	public void setActionStrategiesMapping(final Map<String, RuleActionStrategy> actionStrategiesMapping)
	{
		this.actionStrategiesMapping = actionStrategiesMapping;
	}
}
