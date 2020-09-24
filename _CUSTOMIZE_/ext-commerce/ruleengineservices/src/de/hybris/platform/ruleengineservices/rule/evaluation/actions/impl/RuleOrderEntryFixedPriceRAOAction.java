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
package de.hybris.platform.ruleengineservices.rule.evaluation.actions.impl;

import de.hybris.platform.ruleengineservices.rao.DiscountRAO;
import de.hybris.platform.ruleengineservices.rao.OrderEntryRAO;
import de.hybris.platform.ruleengineservices.rao.RuleEngineResultRAO;
import de.hybris.platform.ruleengineservices.rule.evaluation.RuleActionContext;
import de.hybris.platform.ruleengineservices.rule.evaluation.actions.AbstractRuleExecutableSupport;
import de.hybris.platform.ruleengineservices.util.SharedParametersProvider;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static de.hybris.platform.ruleengineservices.util.RAOConstants.VALUE_PARAM;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;


public class RuleOrderEntryFixedPriceRAOAction extends AbstractRuleExecutableSupport
{

	@Override
	public boolean performActionInternal(final RuleActionContext context)
	{
		if (context.getParameters().containsKey(SharedParametersProvider.IS_DISCOUNTED_PRICE_INCLUDED))
		{
			Boolean discountedPriceIncluded = (Boolean) context.getParameter(SharedParametersProvider.IS_DISCOUNTED_PRICE_INCLUDED);
			return processWithCartTotalThreshold(context, discountedPriceIncluded);
		}
		else
		{
			return processWithoutCartTotalThreshold(context);
		}

	}

	protected boolean processWithCartTotalThreshold(final RuleActionContext context, final boolean discountedPriceIncluded)
	{
		final Set<OrderEntryRAO> orderEntries = context.getValues(OrderEntryRAO.class);
		//noinspection unchecked
		final Map<String, BigDecimal> values = (Map<String, BigDecimal>) context.getParameter(VALUE_PARAM);
		for (final OrderEntryRAO orderEntryRao : orderEntries)
		{
			final BigDecimal valueForCurrency = values.get(orderEntryRao.getCurrencyIsoCode());
			if (isNull(valueForCurrency))
			{
				break;
			}
			//noinspection unchecked
			final BigDecimal cartThreshold = ((Map<String, BigDecimal>) context
					.getParameter(SharedParametersProvider.CART_THRESHOLD)).get(orderEntryRao.getCurrencyIsoCode());
			final int consumableQuantity = getConsumableQuantity(orderEntryRao);
			if (consumableQuantity > 0)
			{
				final Map<Integer, Integer> selectedMapOrderEntriesMap = newHashMap();
				selectedMapOrderEntriesMap.put(orderEntryRao.getEntryNumber(), 1);
				final Set<OrderEntryRAO> selectedOrderEntryRaos = newHashSet();
				selectedOrderEntryRaos.add(orderEntryRao);
				final BigDecimal total = context.getCartRao().getSubTotal();
				final List<DiscountRAO> discounts = getRuleEngineCalculationService().addFixedPriceEntriesDiscount(
						context.getCartRao(), selectedMapOrderEntriesMap, selectedOrderEntryRaos, valueForCurrency);

				if (isDiscountApplicable(discounts, orderEntryRao, cartThreshold, total, discountedPriceIncluded))
				{
					addDiscount(context, orderEntryRao, 1, discounts.get(0));
					return true;
				}
			}
		}
		return false;
	}

	protected boolean isDiscountApplicable(final List<DiscountRAO> discounts, final OrderEntryRAO orderEntryRao,
			final BigDecimal cartThreshold, final BigDecimal total, final boolean discountedPriceIncluded)
	{
		return discounts.stream()
				.findFirst()
				.map(discount -> discountedPriceIncluded ? getDiscountedPrice(discount) : orderEntryRao.getPrice())
				.map(total::subtract)
				.map(diff -> diff.compareTo(cartThreshold) >= 0)
				.orElse(false);
	}

	protected BigDecimal getDiscountedPrice(final DiscountRAO discount)
	{
		return discount.getValue().multiply(new BigDecimal(discount.getAppliedToQuantity()));
	}

	protected boolean processWithoutCartTotalThreshold(final RuleActionContext context)
	{
		final Set<OrderEntryRAO> orderEntries = context.getValues(OrderEntryRAO.class);
		//noinspection unchecked
		final Map<String, BigDecimal> values = (Map<String, BigDecimal>) context.getParameter(VALUE_PARAM);
		boolean isPerformed = false;
		for (final OrderEntryRAO orderEntry : orderEntries)
		{
			final BigDecimal valueForCurrency = values.get(orderEntry.getCurrencyIsoCode());
			if (nonNull(valueForCurrency))
			{
				isPerformed |= processOrderEntry(context, orderEntry, valueForCurrency);
			}
		}
		return isPerformed;
	}

	protected boolean processOrderEntry(final RuleActionContext context, final OrderEntryRAO orderEntryRao,
			final BigDecimal valueForCurrency)
	{
		boolean isPerformed = false;
		final int consumableQuantity = getConsumableQuantity(orderEntryRao);
		if (consumableQuantity > 0)
		{
			isPerformed = true;
			final DiscountRAO discount = getRuleEngineCalculationService().addFixedPriceEntryDiscount(orderEntryRao,
					valueForCurrency);
			if (nonNull(discount))
			{
				addDiscount(context, orderEntryRao, discount);
			}
		}
		return isPerformed;
	}

	protected void addDiscount(final RuleActionContext context, final OrderEntryRAO orderEntryRao, final DiscountRAO discount)
	{
		addDiscount(context, orderEntryRao, orderEntryRao.getQuantity(), discount);
	}

	protected void addDiscount(final RuleActionContext context, final OrderEntryRAO orderEntryRao, final int quantity,
			final DiscountRAO discount)
	{
		final RuleEngineResultRAO result = context.getRuleEngineResultRao();
		result.getActions().add(discount);
		setRAOMetaData(context, discount);
		consumeOrderEntry(orderEntryRao, quantity, discount);

		context.scheduleForUpdate(orderEntryRao, orderEntryRao.getOrder(), result);
		context.insertFacts(discount);
		context.insertFacts(discount.getConsumedEntries());
	}
}
