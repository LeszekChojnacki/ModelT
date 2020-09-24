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

import org.apache.commons.lang3.tuple.Pair;


/**
 * Rules filter interface base on rules content match logic
 */
public interface ContentMatchRulesFilter extends RulesFilter<DroolsRuleModel>
{
	/**
	 * Apply the filter to a given set of rules UUIDs
	 *
	 * @param ruleUuids
	 * 			 a collection of rule UUIDs of the rules to be filtered out
	 * @return the pair with a lhs, containing the rules to add and rhs, containing the rules to remove
	 */
	@Override
	Pair<Collection<DroolsRuleModel>, Collection<DroolsRuleModel>> apply(Collection<String> ruleUuids);
	


	@Override
	Pair<Collection<DroolsRuleModel>, Collection<DroolsRuleModel>> apply(Collection<String> ruleUuids, Long newModuleVersion);

}
