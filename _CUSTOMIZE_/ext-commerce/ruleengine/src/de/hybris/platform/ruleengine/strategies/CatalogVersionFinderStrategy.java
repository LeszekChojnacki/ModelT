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
import de.hybris.platform.ruleengine.model.AbstractRulesModuleModel;

import java.util.List;


/**
 * The finder strategy for catalog version
 */
public interface CatalogVersionFinderStrategy
{
	/**
	 * Find catalog versions by rules module
	 *
	 * @param rulesModule
	 * 		rules module   	
	 * @return list of qualifying catalog versions
	 */
	List<CatalogVersionModel> findCatalogVersionsByRulesModule(AbstractRulesModuleModel rulesModule);

}
