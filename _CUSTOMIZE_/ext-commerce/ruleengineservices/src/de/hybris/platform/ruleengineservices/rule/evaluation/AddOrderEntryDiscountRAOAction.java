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
 * AddOrderEntryDiscountRAOAction adds a discount on order/cart entry level.
 */
public interface AddOrderEntryDiscountRAOAction
{

	/**
	 * Adds an order entry level discount to the given {@code orderEntryRao}, recalculates the cart totals and returns
	 * the discount. The {@code absolute} flag determines whether the discount is absolute or percentage based. For
	 * absolute values, the cart's currency is used. The {@code amount} specifies the amount used, e.g. $20 or 10%. The
	 * {@code ruleContext} can be used to enhance the returned DiscountRAO.
	 *
	 * @param orderEntryRao
	 *           the OrderEntryRAO to apply the discount to
	 * @param absolute
	 *           the type of discount
	 * @param amount
	 *           the amount of the discount
	 * @param result
	 *           the result rao
	 * @param ruleContext
	 *           an optional context object that can be used to provide additional information for the returned
	 *           DiscountRAO
	 */
	DiscountRAO addOrderEntryLevelDiscount(OrderEntryRAO orderEntryRao, boolean absolute, BigDecimal amount,
			RuleEngineResultRAO result, Object ruleContext);


	/**
	 * Adds an order entry level discount for each order entry element in {@code orderEntryList}, recalculates the cart
	 * totals and returns the discount. The {@code absolute} flag determines whether the discount is absolute or
	 * percentage based. For absolute values, the cart's currency is used. The {@code amount} specifies the amount used,
	 * e.g. $20 or 10%. The {@code ruleContext} can be used to enhance the returned DiscountRAO.
	 *
	 * @param orderEntryList
	 *           Collection of OrderEntryRAO to apply the discount to
	 * @param absolute
	 *           the type of discount
	 * @param amount
	 *           the amount of the discount
	 * @param result
	 *           the result rao
	 * @param ruleContext
	 *           an optional context object that can be used to provide additional information for the returned
	 *           DiscountRAO
	 */
	List<DiscountRAO> addOrderEntryLevelDiscounts(Collection<OrderEntryRAO> orderEntryList, boolean absolute, BigDecimal amount,
			RuleEngineResultRAO result, Object ruleContext);

	/**
	 * Adds order entry level discounts to the order entries selected by strategies from
	 * {@code entriesSelectionStrategyRPDs} which are marked to be target for the action. The discount is either absolute
	 * or percentage.
	 *
	 * @param entriesSelectionStrategyRPDs
	 *           list of strategies used to select order entries and their applicable quantities. Discounts are created
	 *           for the entries from the strategies which have {@code isTargetOfAction} = true. Other entries are just
	 *           consumed, but not discounted.
	 * @param absolute
	 *           type of discount to be applied. Absolute discount will provide a currency amount discount, rather than a
	 *           percentage.
	 * @param amount
	 *           amount of discount to apply. Can be currency amount of percentage
	 * @param result
	 *           RuleEngineResultRAO that will be updated with discounts
	 * @param ruleContext
	 *           an optional context object that can be used to provide additional information for the returned
	 *           DiscountRAO
	 * @return List of created discounts
	 */
	List<DiscountRAO> addOrderEntryLevelDiscount(List<EntriesSelectionStrategyRPD> entriesSelectionStrategyRPDs, boolean absolute,
			BigDecimal amount, RuleEngineResultRAO result, Object ruleContext);

}
