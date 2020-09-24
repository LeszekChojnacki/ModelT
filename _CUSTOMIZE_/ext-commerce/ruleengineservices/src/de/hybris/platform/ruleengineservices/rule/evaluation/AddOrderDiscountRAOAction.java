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

import de.hybris.platform.ruleengineservices.rao.CartRAO;
import de.hybris.platform.ruleengineservices.rao.DiscountRAO;
import de.hybris.platform.ruleengineservices.rao.RuleEngineResultRAO;

import java.math.BigDecimal;



/**
 * AddOrderDiscountRAOAction adds a discount on order/cart level.
 */
public interface AddOrderDiscountRAOAction
{

	/**
	 * Adds an order level discount to the given {@code cartRao}, recalculates the cart totals and returns the discount.
	 * The {@code absolute} flag determines whether the discount is absolute or percentage based. For absolute values,
	 * the cart's currency is used. The {@code amount} specifies the amount used, e.g. $20 or 10%. The
	 * {@code ruleContext} can be used to enhance the returned DiscountRAO.
	 *
	 * @param cartRao
	 *           the CartRAO to apply the discount to
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
	DiscountRAO addOrderLevelDiscount(CartRAO cartRao, boolean absolute, BigDecimal amount, RuleEngineResultRAO result,
			Object ruleContext);

	/**
	 * Adds an order-level discount to the {@code cartRao} and recalculates cart totals. Takes into account the excluded
	 * products so that non-absolute discounts are calculated relative to the total excluding these products
	 *
	 * @param cartRao
	 *           Cart to apply the discount to.
	 * @param absolute
	 *           true if the discount is a definite currency amount, rather than a percentage discount
	 * @param amount
	 *           currency amount or percentage to be discounted
	 * @param result
	 *           resultRAO containing information from evaluation action
	 * @param ruleContext
	 *           additional info for the rule execution environment
	 * @return order-level DiscountRAO
	 */
	DiscountRAO addOrderLevelDiscount(CartRAO cartRao, boolean absolute, BigDecimal amount, String[] excludedProducts,
			RuleEngineResultRAO result, Object ruleContext);

	/**
	 * Calculates the cart sub total for CartRao excluding a set of products.
	 *
	 * @param cartRao
	 *           CartRAO object to be calculated
	 * @param excludedProductCodes
	 *           Array of product codes that identify cart entries to be excluded
	 * @return result of the cartSubTotals (without charges like payment or delivery) minus the excluded products
	 */
	BigDecimal calculateSubTotals(final CartRAO cartRao, final String[] excludedProductCodes);

}
