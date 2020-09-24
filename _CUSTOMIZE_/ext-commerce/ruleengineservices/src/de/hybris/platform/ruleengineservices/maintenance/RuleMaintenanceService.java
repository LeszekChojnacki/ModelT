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
package de.hybris.platform.ruleengineservices.maintenance;

import de.hybris.platform.cronjob.jalo.CronJobProgressTracker;
import de.hybris.platform.ruleengineservices.model.AbstractRuleModel;
import de.hybris.platform.ruleengineservices.model.SourceRuleModel;

import java.util.List;
import java.util.Optional;


/**
 * The interface provides with method that allows archiving of rules.
 */
public interface RuleMaintenanceService
{
	/**
	 * Archives given rule
	 *
	 * @deprecated since 1811
	 * @param rule
	 * 		- rule to archive
	 *
	 * @return instance of {@link RuleCompilerPublisherResult} containing the status of rule archiving
	 */
	@Deprecated
	RuleCompilerPublisherResult archiveRule(AbstractRuleModel rule);

	/**
	 * Compiles and publishes rules
	 *
	 * @param rules
	 * 		- list of rules to compile and publish
	 * @param moduleName
	 * 		Rules module name
	 * @param enableIncrementalUpdate
	 * 		flag, if true, enables for incremental updates of the rule engine kie module
	 * @return instance of {@link RuleCompilerPublisherResult} containing the status of rules compilation and publication
	 */
	<T extends SourceRuleModel> RuleCompilerPublisherResult compileAndPublishRules(List<T> rules, String moduleName,
			boolean enableIncrementalUpdate);

	/**
	 * Publish/re-publish drools rules for a specific module
	 *
	 * @param moduleName
	 * 		Rules module name to publish the rules for
	 * @param enableIncrementalUpdate
	 * 		flag, if true, enables for incremental updates of the rule engine kie module
	 * @return instance of {@link RuleCompilerPublisherResult} containing the status of rules compilation and publication
	 */
	RuleCompilerPublisherResult initializeModule(String moduleName,
			boolean enableIncrementalUpdate);

	/**
	 * Publish/re-publish drools rules for a all available modules
	 *
	 * @param enableIncrementalUpdate
	 * 		flag, if true, enables for incremental updates of the rule engine kie module
	 * @return instance of {@link RuleCompilerPublisherResult} containing the status of rules compilation and publication
	 */
	RuleCompilerPublisherResult initializeAllModules(boolean enableIncrementalUpdate);

	/**
	 * Compiles and publishes rules with blocking on initialization
	 *
	 * @param rules
	 * 		- list of rules to compile and publish
	 * @param moduleName
	 * 		Rules module name
	 * @param enableIncrementalUpdate
	 * 		flag, if true, enables for incremental updates of the rule engine kie module
	 * @return instance of {@link RuleCompilerPublisherResult} containing the status of rules compilation and publication
	 */
	<T extends SourceRuleModel> RuleCompilerPublisherResult compileAndPublishRulesWithBlocking(final List<T> rules,
			String moduleName, boolean enableIncrementalUpdate);

	/**
	 * Compiles and publishes rules with blocking on initialization
	 *
	 * @param rules
	 * 		- list of rules to compile and publish
	 * @param moduleName
	 * 		Rules module name
	 * @param enableIncrementalUpdate
	 * 		flag, if true, enables for incremental updates of the rule engine kie module
	 * @param cronJobProgressTracker
	 * 		instance of {@link CronJobProgressTracker} used by cron job framework to track the job execution
	 * @return instance of {@link RuleCompilerPublisherResult} containing the status of rules compilation and publication
	 */
	<T extends SourceRuleModel> RuleCompilerPublisherResult compileAndPublishRulesWithBlocking(List<T> rules,
			String moduleName, boolean enableIncrementalUpdate, CronJobProgressTracker cronJobProgressTracker);

	/**
	 * "Undeploy" (deactivate) the rules in the rule engine
	 *
	 * @param rules
	 * 		instances of {@link SourceRuleModel} to undeploy from module if they were deployed
	 * @param moduleName
	 * 		the name of the rules module to undeploy the rules from
	 * @param <T>
	 * 		type of the {@link SourceRuleModel}
	 * @return Optional of {@link RuleCompilerPublisherResult} containing the status of rules compilation and publication
	 */
	<T extends SourceRuleModel> Optional<RuleCompilerPublisherResult> undeployRules(List<T> rules, String moduleName);

	/**
	 * Synchronize the deployed rules between two modules. The result after synchronisation should be a complete alignment of
	 * source rules (including their versions) deployed for target module with those deployed for source module
	 *
	 * @param sourceModuleName
	 * 		The name of the module to be used as a source for synchronisation
	 * @param targetModuleName
	 * 		The name of the module to be used as a target for synchronisation
	 * @return Optional of {@link RuleCompilerPublisherResult} containing the status of rules compilation and publication for
	 * target module
	 */
	Optional<RuleCompilerPublisherResult> synchronizeModules(String sourceModuleName, String targetModuleName);

}
