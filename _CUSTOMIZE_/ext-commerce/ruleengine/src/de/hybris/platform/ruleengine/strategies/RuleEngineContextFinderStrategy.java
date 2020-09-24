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
package de.hybris.platform.ruleengine.strategies;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.ruleengine.enums.RuleType;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineContextModel;

import java.util.Collection;
import java.util.Optional;


/**
 * Strategy for retrieving rule engine contexts based on rule type
 */
public interface RuleEngineContextFinderStrategy
{

	/**
	 * Finds rule engine contexts for the given rule type. The method only works in rule environments where only 1 rules
	 * module exists for the given ruleType. If there are multiple rules modules for the given ruleType it will throw an
	 * IllegalStateException exception.
	 * 
	 * @param ruleType
	 *           the rule type to look up rule engine contexts for
	 * @return an optional of unique rule engine context for the given rule type. Otherwise throw
	 *         {@link IllegalStateException}
	 */
	<T extends AbstractRuleEngineContextModel> Optional<T> findRuleEngineContext(RuleType ruleType);

	/**
	 * Given the order, finds the rule engine context, compatible with specified rule type
	 *
	 * @param order
	 *           the order, for which we need to find the rule engine context
	 * @param ruleType
	 *           filters to return only mappings which rules module is of the given rule type
	 * @return a rule engine context, compatible with given rule type. Empty optional if did not find any
	 */
	<T extends AbstractRuleEngineContextModel, O extends AbstractOrderModel> Optional<T> findRuleEngineContext(final O order,
			RuleType ruleType);

	/**
	 * Given the product, finds the rule engine context, compatible with specified rule type
	 *
	 * @param product
	 *           the product, for which we need to find the rule engine context
	 * @param ruleType
	 *           filters to return only mappings which rules module is of the given rule type
	 * @return a rule engine context, compatible with given rule type. Empty optional if did not find any
	 */
	<T extends AbstractRuleEngineContextModel> Optional<T> findRuleEngineContext(final ProductModel product, RuleType ruleType);

	/**
	 * Finds rule engine contexts for the given catalog versions and rule type. If there are multiple rules modules for
	 * the provided parameters it will throw an {@link IllegalStateException} exception.
	 * 
	 * @param catalogVersions
	 *           collection of catalog versions to perform look up of engine context for
	 * @param ruleType
	 *           the rule type to look up rule engine context for
	 * @return a rule engine context compatible with the given rule type and catalog versions
	 * @throws {@link
	 *            IllegalStateException} in case multiple rule engine contexts have been found in the system that are
	 *            compatible with the provided parameter set
	 */
	<T extends AbstractRuleEngineContextModel> Optional<T> getRuleEngineContextForCatalogVersions(
			final Collection<CatalogVersionModel> catalogVersions, final RuleType ruleType);

}
