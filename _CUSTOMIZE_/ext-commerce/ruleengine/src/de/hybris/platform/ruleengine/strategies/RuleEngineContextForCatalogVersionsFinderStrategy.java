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
import de.hybris.platform.ruleengine.enums.RuleType;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineContextModel;

import java.util.Collection;
import java.util.List;


/**
 * Strategy for retrieving rule engine contexts based on catalog versions (and rule type)
 */
public interface RuleEngineContextForCatalogVersionsFinderStrategy
{

	/**
	 * finds rule engine contexts for the given catalog versions and rule type
	 *
	 * @param catalogVersions
	 *           the catalog version(s) to look up rule engine contexts for
	 * @param ruleType
	 *           filters to return only mappings which rules module is of the given rule type
	 * @return a list of rule engine contexts for the given catalog versions and rule type
	 */
	<T extends AbstractRuleEngineContextModel> List<T> findRuleEngineContexts(Collection<CatalogVersionModel> catalogVersions,
			RuleType ruleType);

}
