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
package de.hybris.platform.ruleengineservices.maintenance.systemsetup;

import de.hybris.platform.ruleengine.jalo.AbstractRulesModule;
import de.hybris.platform.ruleengineservices.jalo.SourceRule;
import de.hybris.platform.ruleengineservices.model.SourceRuleModel;

import java.util.Collection;


/**
 * RuleEngineSystemSetup defines methods for registering SourceRules for compilation and publication (i.e. deployment)
 * for the next system initialization (system initialization or system update).
 */
public interface RuleEngineSystemSetup
{
	/**
	 * registers a collection of SourceRules to be compiled and deployed for the given moduleNames after the next
	 * initialization. This call will register <em>all</em> given source rules for <em>all</em> given module names.
	 *
	 * @param sourceRules
	 *           the source rules to be registered for deployment
	 * @param moduleNames
	 *           the target modules by name
	 */
	void registerSourceRulesForDeployment(Collection<SourceRuleModel> sourceRules, Collection<String> moduleNames);

	/**
	 * registers a (jalo) SourceRule to be compiled and deployed for the given moduleNames after the next initialization.
	 * This method is intended to be used from within impex scripts.
	 *
	 * @param sourceRule
	 *           the source rule to be registered for deployment
	 * @param moduleNames
	 *           the target module(s) by name
	 */
	void registerSourceRuleForDeployment(SourceRule sourceRule, String... moduleNames);

	/**
	 * Performs initialization of the provided (jalo) rules module. This method is intended to be used from within impex
	 * scripts.
	 *
	 * @param module
	 *           Jalo representation of rules module to be initialized
	 */
	<T extends AbstractRulesModule> void initializeModule(T module);
}
