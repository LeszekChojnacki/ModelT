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

import de.hybris.platform.ruleengineservices.rao.AbstractOrderRAO;
import de.hybris.platform.ruleengineservices.rao.DiscountRAO;
import de.hybris.platform.ruleengineservices.rao.EntriesSelectionStrategyRPD;
import de.hybris.platform.ruleengineservices.rao.RuleEngineResultRAO;

import java.math.BigDecimal;
import java.util.List;



/**
 * AddOrderEntryGroupFixedPriceDiscountRAOAction adds a discount to a bundle of products.
 */
public interface AddOrderEntryGroupFixedPriceDiscountRAOAction
{

	/**
	 * Sets the target price {@code targetPrice} to a group of products, selecting them from order entries according to
	 * the given {@code entriesSelectionStrategyRPDs}.
	 *
	 * @param order
	 *           the order / cart
	 * @param entriesSelectionStrategyRPDs
	 *           list of {@link EntriesSelectionStrategyRPD} defining which and how many products should take part in the
	 *           promotion
	 * @param targetPrice
	 *           target price for the product group
	 * @param result
	 *           the result rao
	 * @param ruleContext
	 *           an optional context object that can be used to provide additional information for the returned
	 *           DiscountRAO
	 * @return list of discounts that should be applied to gain the target price.
	 */
	List<DiscountRAO> addOrderEntryGroupFixedPriceDiscount(AbstractOrderRAO order,
			List<EntriesSelectionStrategyRPD> entriesSelectionStrategyRPDs, BigDecimal targetPrice, RuleEngineResultRAO result,
			Object ruleContext);

}
