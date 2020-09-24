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
package de.hybris.platform.ruleengine.dao;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.ruleengine.enums.RuleType;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineContextModel;
import de.hybris.platform.ruleengine.model.CatalogVersionToRuleEngineContextMappingModel;

import java.util.Collection;


/**
 * DAO for retrieving the mapping items of catalog version to rule engine context
 */
public interface CatalogVersionToRuleEngineContextMappingDao
{

	/**
	 * returns all mappings for the given catalog versions and rule type.
	 *
	 * @param catalogVersions
	 * 		the versions to lookup mappings for (can be empty/null)
	 * @param ruleType
	 * 		the ruleType: filters to return only mappings which rules module is of the given rule type (can be null)
	 * @return a collection of mappings
	 */
	Collection<CatalogVersionToRuleEngineContextMappingModel> findMappingsByCatalogVersion(
			Collection<CatalogVersionModel> catalogVersions, RuleType ruleType);

	/**
	 * find catalog versions mapped by rule engine contexts
	 *
	 * @param contexts
	 * 		the collection of {@link AbstractRuleEngineContextModel}
	 * @return the collection of {@link CatalogVersionToRuleEngineContextMappingModel}
	 */
	Collection<CatalogVersionToRuleEngineContextMappingModel> findByContext(
			final Collection<AbstractRuleEngineContextModel> contexts);

}
