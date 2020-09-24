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
package de.hybris.platform.ruleengineservices.rao.providers.impl;

import de.hybris.platform.ruleengineservices.calculation.RuleEngineCalculationService;
import de.hybris.platform.ruleengineservices.rao.CartRAO;
import de.hybris.platform.ruleengineservices.rao.OrderEntryRAO;
import de.hybris.platform.ruleengineservices.rao.ProductConsumedRAO;
import de.hybris.platform.ruleengineservices.rao.providers.RAOFactsExtractor;
import org.springframework.beans.factory.annotation.Required;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;


/**
 * Extension of Default cart RAO provider through facts extractor, adding the consumed-awareness functionality
 */
public class ConsumedCartRAOExtractor implements RAOFactsExtractor
{
	public static final String EXPAND_CONSUMED = "EXPAND_CONSUMED";

	private RuleEngineCalculationService ruleEngineCalculationService;

	private boolean enabled;

	@Override
	public Set expandFact(final Object fact)
	{
		checkArgument(fact instanceof CartRAO, "CartRAO type is expected here");
		final Set<Object> facts = new HashSet<>();
		final CartRAO cartRAO = (CartRAO) fact;
		addConsumed(facts, cartRAO, cartRAO.getEntries());
		return facts;
	}

	protected void addConsumed(final Set<Object> facts, final CartRAO cart, final Set<OrderEntryRAO> entries)
	{
		if (isNotEmpty(entries))
		{
			facts.addAll(entries.stream().map(this::createProductConsumedRAO).collect(Collectors.toSet()));
		}
	}

	protected ProductConsumedRAO createProductConsumedRAO(final OrderEntryRAO orderEntryRAO)
	{
		final ProductConsumedRAO productConsumedRAO = new ProductConsumedRAO();
		productConsumedRAO.setOrderEntry(orderEntryRAO);
		productConsumedRAO.setAvailableQuantity(
				getRuleEngineCalculationService().getProductAvailableQuantityInOrderEntry(orderEntryRAO));
		return productConsumedRAO;
	}

	@Override
	public String getTriggeringOption()
	{
		return EXPAND_CONSUMED;
	}

	@Override
	public boolean isMinOption()
	{
		return false;
	}

	@Override
	public boolean isDefault()
	{
		return isEnabled();
	}

	protected boolean isEnabled()
	{
		return enabled;
	}

	@Required
	public void setEnabled(final boolean enabled)
	{
		this.enabled = enabled;
	}

	protected RuleEngineCalculationService getRuleEngineCalculationService()
	{
		return ruleEngineCalculationService;
	}

	@Required
	public void setRuleEngineCalculationService(final RuleEngineCalculationService ruleEngineCalculationService)
	{
		this.ruleEngineCalculationService = ruleEngineCalculationService;
	}

}
