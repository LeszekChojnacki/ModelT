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
package de.hybris.platform.ruleengineservices.rule.evaluation;

import de.hybris.platform.ruleengineservices.rao.DiscountRAO;
import de.hybris.platform.ruleengineservices.rao.EntriesSelectionStrategyRPD;
import de.hybris.platform.ruleengineservices.rao.OrderEntryRAO;
import de.hybris.platform.ruleengineservices.rao.RuleEngineResultRAO;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;



/**
 * AddFixedPriceEntryDiscount adds a discount to an cart entry so that the cart entry has a fixed target price.
 */
public interface AddFixedPriceEntryDiscountRAOAction
{

	/**
	 * Adds an order entry level discount to the given {@code orderEntryRao} such that the final price of the discounted
	 * order entry will be the {@code fixedPrice}.
	 *
	 * @param orderEntryRao
	 *           the OrderEntryRAO to apply the discount to
	 * @param fixedPrice
	 *           the target unit price for the order entry
	 * @param result
	 *           the result rao
	 * @param ruleContext
	 *           an optional context object that can be used to provide additional information for the returned
	 *           DiscountRAO
	 */
	DiscountRAO addFixedPriceEntryDiscount(OrderEntryRAO orderEntryRao, BigDecimal fixedPrice, RuleEngineResultRAO result,
			Object ruleContext);

	/**
	 * Adds an order entry level discount to each of the the given {@code orderEntryRao} such that the final price of the
	 * discounted order entry will be the {@code fixedPrice}.
	 *
	 * @param orderEntryRao
	 *           collection of OrderEntryRAO to apply the discount to
	 * @param fixedPrice
	 *           the target unit price for the order entry
	 * @param result
	 *           the result rao
	 * @param ruleContext
	 *           an optional context object that can be used to provide additional information for the returned
	 *           DiscountRAO
	 */
	List<DiscountRAO> addFixedPriceEntryDiscounts(Collection<OrderEntryRAO> orderEntryRao, BigDecimal fixedPrice,
			RuleEngineResultRAO result, Object ruleContext);

	/**
	 * Adds order entry level discounts to the order entries selected by strategies from
	 * {@code entriesSelectionStrategyRPDs} which are marked to be target for the action. The discount is such that the
	 * final price of the discounted order entry will be {@code fixedPrice}.
	 *
	 * @param entriesSelectionStrategyRPDs
	 *           list of strategies used to select order entries and their applicable quantities. Discounts are created
	 *           for the entries from the strategies which have {@code isTargetOfAction} = true. Other entries are just
	 *           consumed, but not discounted.
	 * @param fixedPrice
	 *           the target unit price for the order entry
	 * @param result
	 *           RuleEngineResultRAO that will be updated with discounts
	 * @param ruleContext
	 *           an optional context object that can be used to provide additional information for the returned
	 *           DiscountRAO
	 * @return List of created discounts
	 */
	List<DiscountRAO> addFixedPriceEntriesDiscount(List<EntriesSelectionStrategyRPD> entriesSelectionStrategyRPDs,
			BigDecimal fixedPrice, int maxQuantity, RuleEngineResultRAO result, Object ruleContext);

}
