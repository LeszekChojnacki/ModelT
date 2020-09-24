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
package de.hybris.platform.ruleengineservices.jobs;

import de.hybris.platform.ruleengineservices.model.RuleEngineCronJobModel;
import de.hybris.platform.ruleengineservices.model.SourceRuleModel;

import java.util.List;


/**
 * Interface for the launcher of rule-engine specific tasks
 */
public interface RuleEngineCronJobLauncher
{
	/**
	 * Trigger the compilation and publishing of specified rules for a module
	 *
	 * @param rules
	 * 		a list of {@link SourceRuleModel} to compile and publish
	 * @param moduleName
	 * 		a kie module name
	 * @param enableIncrementalUpdate
	 * 		boolean indicating whether the incremental update of rule engine must be employed
	 * @return the instance of triggered {@link RuleEngineCronJobModel}
	 */
	RuleEngineCronJobModel triggerCompileAndPublish(List<SourceRuleModel> rules, String moduleName, boolean enableIncrementalUpdate);

	/**
	 * Trigger the specified rules undeployment process
	 *
	 * @param rules
	 * 		a list of {@link SourceRuleModel} to undeploy
	 * @param moduleName
	 * 		a kie module name to undeploy the rules for
	 * @return the instance of triggered {@link RuleEngineCronJobModel}
	 */
	RuleEngineCronJobModel triggerUndeployRules(List<SourceRuleModel> rules, String moduleName);

	/**
	 * Trigger archive process for the specified rule
	 *
	 *	@deprecated since 1811
	 *
	 * @param rule
	 * 		 {@link SourceRuleModel} to archive
	 * @return the instance of triggered {@link RuleEngineCronJobModel}
	 */
	@Deprecated
	RuleEngineCronJobModel triggerArchiveRule(SourceRuleModel rule);

	/**
	 * Trigger the modules synchronization task
	 *
	 * @param srcModuleName
	 * 		a name of the kie module to synchronise the rules from
	 * @param targetModuleName
	 * 		a name of the kie module to synchronise the rules to
	 * @return the instance of triggered {@link RuleEngineCronJobModel}
	 */
	RuleEngineCronJobModel triggerSynchronizeModules(String srcModuleName, String targetModuleName);


	/**
	 * Trigger the specified module initialization task
	 *
	 * @param moduleName
	 * 		a name of the kie module to initialize
	 * @return the instance of triggered {@link RuleEngineCronJobModel}
	 */
	RuleEngineCronJobModel triggerModuleInitialization(String moduleName);

	/**
	 * Trigger the whole rule engine (for all modules) initialization
	 *
	 * @return the instance of triggered {@link RuleEngineCronJobModel}
	 */
	RuleEngineCronJobModel triggerAllModulesInitialization();
}
