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
import de.hybris.platform.ruleengineservices.rao.EntriesSelectionStrategyRPD;
import de.hybris.platform.ruleengineservices.rao.FreeProductRAO;
import de.hybris.platform.ruleengineservices.rao.OrderEntryRAO;
import de.hybris.platform.ruleengineservices.rao.RuleEngineResultRAO;

import java.util.Collection;



/**
 * AddProductToCartRAOAction adds a product to the cart.
 */
public interface AddProductToCartRAOAction
{

	/**
	 * Adds free product items to the cart.
	 *
	 * @param cartRao
	 *           the cartRao where a product is added
	 * @param productCode
	 *           code of a product to add
	 * @param quantity
	 *           quantity of product items to add
	 * @param triggeringEntry
	 *           order entry which triggered the rule
	 * @param triggeringEntryQuantity
	 *           quantity of triggering entry to consume
	 * @param result
	 *           the result rao
	 * @param ruleContext
	 *           an optional context object that can be used to provide additional information for the returned
	 *           DiscountRAO
	 */
	FreeProductRAO addFreeProductsToCart(CartRAO cartRao, String productCode, int quantity, OrderEntryRAO triggeringEntry,
			int triggeringEntryQuantity, RuleEngineResultRAO result, Object ruleContext);

	/**
	 * Adds free product items to the cart.
	 *
	 * @param cartRao
	 *           the cartRao where a product is added
	 * @param productCode
	 *           code of a product to add
	 * @param quantity
	 *           quantity of product items to add
	 * @param strategies
	 *           the selection strategies containing the order entries
	 * @param result
	 *           the result rao
	 * @param ruleContext
	 *           an optional context object that can be used to provide additional information for the returned
	 *           DiscountRAO
	 */
	FreeProductRAO addFreeProductsToCart(CartRAO cartRao, String productCode, int quantity,
			Collection<EntriesSelectionStrategyRPD> strategies, RuleEngineResultRAO result, Object ruleContext);

}
