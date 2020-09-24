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
package de.hybris.platform.ruleengine.init;

import de.hybris.platform.ruleengine.model.DroolsRuleModel;

import java.util.Collection;

import org.kie.api.builder.ReleaseId;


/**
 * Incremental rules engine update strategy interface.
 */
public interface IncrementalRuleEngineUpdateStrategy
{
	/**
	 * Given the rules module and the list (per kie base) of rules to add and remove, returns true if the incremental update would
	 * be the better strategy, false otherwise
	 *
	 * @param releaseId
	 * 		instance of currently deployed kie container {@link ReleaseId}
	 * @param moduleName
	 * 		name of the rules module
	 * @param rulesToAdd
	 * 		collection of drool rules to be added or updated
	 * @param rulesToRemove
	 * 		collection of drool rules to be removed
	 * @return true if the incremental rule engine update should be applied, false otherwise
	 */
	boolean shouldUpdateIncrementally(ReleaseId releaseId, String moduleName, Collection<DroolsRuleModel> rulesToAdd,
			Collection<DroolsRuleModel> rulesToRemove);
}
