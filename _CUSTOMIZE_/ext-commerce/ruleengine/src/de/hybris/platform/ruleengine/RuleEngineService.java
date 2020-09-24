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
package de.hybris.platform.ruleengine;

import de.hybris.platform.ruleengine.event.RuleEngineInitializedEvent;
import de.hybris.platform.ruleengine.init.InitializationFuture;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengine.model.AbstractRulesModuleModel;
import de.hybris.platform.ruleengine.model.DroolsRuleModel;

import java.util.Collection;
import java.util.List;
import java.util.Optional;


/**
 * The interface provides with method that allows initialization and evaluation of rules modules.
 */
public interface RuleEngineService
{

	/**
	 * Since 6.0
	 * Executes rule evaluation for given RuleEvaluationContext. Executes all rules contained within the context
	 *
	 * @param context
	 * 		the rule engine context containing data about rule engine execution
	 * @return results of rule engine execution
	 */
	RuleEvaluationResult evaluate(RuleEvaluationContext context);

	/**
	 * Since 6.4
	 * Asynchronously initializes the given rules module (and if {@code propagateToOtherNodes} is set to {@code true} propagates a
	 * {@link RuleEngineInitializedEvent} to other nodes).
	 *
	 * @param module
	 * 		the module to be initialized
	 * @param deployedMvnVersion
	 * 		currently deployed version of the Kie Module
	 * @param propagateToOtherNodes
	 * 		if true propagates a {@link RuleEngineInitializedEvent} to other nodes.
	 * @param enableIncrementalUpdate
	 * 		flag, if true, enables for incremental updates of the rule engine kie module
	 * @param result
	 * 		the instance of {@link RuleEngineActionResult} accumulating the results of initialization
	 */
	<M extends AbstractRulesModuleModel> void initializeNonBlocking(final M module,
			String deployedMvnVersion,
			final boolean propagateToOtherNodes, boolean enableIncrementalUpdate, final RuleEngineActionResult result);

	/**
	 * Since 6.4
	 * Initializes the given rules module in blocking/async mode (depending on blocking param) (and if {@code
	 * propagateToOtherNodes} is set to {@code true} propagates a
	 * {@link RuleEngineInitializedEvent} to other nodes).
	 *
	 * @param modules
	 * 		the modules to be initialized
	 * @param propagateToOtherNodes
	 * 		if true propagates a {@link RuleEngineInitializedEvent} to other nodes.
	 * @param enableIncrementalUpdate
	 * 		flag, if true, enables for incremental updates of the rule engine kie module
	 * @return instance of {@link InitializationFuture} to be used for eventual blocking on multiple parallel swapping
	 */
	InitializationFuture initialize(List<AbstractRulesModuleModel> modules, boolean propagateToOtherNodes,
			boolean enableIncrementalUpdate);

	/**
	 * Since 6.7
	 * Initializes the given rules module in blocking/async mode (depending on blocking param) (and if {@code
	 * propagateToOtherNodes} is set to {@code true} propagates a
	 * {@link RuleEngineInitializedEvent} to other nodes).
	 *
	 * @param modules
	 * 		the modules to be initialized
	 * @param propagateToOtherNodes
	 * 		if true propagates a {@link RuleEngineInitializedEvent} to other nodes.
	 * @param enableIncrementalUpdate
	 * 		flag, if true, enables for incremental updates of the rule engine kie module
	 * @param executionContext
	 * 		instance of {@link ExecutionContext} that serves as a storage for initialization related data
	 * @return instance of {@link InitializationFuture} to be used for eventual blocking on multiple parallel swapping
	 */
	InitializationFuture initialize(List<AbstractRulesModuleModel> modules, boolean propagateToOtherNodes,
			boolean enableIncrementalUpdate, final ExecutionContext executionContext);

	/**
	 * Since 6.4
	 * Initializes the given rules module (and if {@code propagateToOtherNodes} is set to {@code true} propagates a
	 * {@link RuleEngineInitializedEvent} to other nodes).
	 *
	 * @param module
	 * 		the module to be initialized
	 * @param deployedMvnVersion
	 * 		currently deployed mvn version of the rule engine module
	 * @param propagateToOtherNodes
	 * 		if true propagates a {@link RuleEngineInitializedEvent} to other nodes.
	 * @param enableIncrementalUpdate
	 * 		flag, if true, enables for incremental updates of the rule engine kie module
	 * @param result
	 * 		the instance of {@link RuleEngineActionResult} accumulating the results of initialization
	 */
	void initialize(AbstractRulesModuleModel module,
			String deployedMvnVersion, boolean propagateToOtherNodes,
			boolean enableIncrementalUpdate, RuleEngineActionResult result);

	/**
	 * Since 18.08
	 * Initializes the given rules module (and if {@code propagateToOtherNodes} is set to {@code true} propagates a
	 * {@link RuleEngineInitializedEvent} to other nodes).
	 *
	 * @param module
	 * 		the module to be initialized
	 * @param deployedMvnVersion
	 * 		currently deployed mvn version of the rule engine module
	 * @param propagateToOtherNodes
	 * 		if true propagates a {@link RuleEngineInitializedEvent} to other nodes.
	 * @param enableIncrementalUpdate
	 * 		flag, if true, enables for incremental updates of the rule engine kie module
	 * @param result
	 * 		the instance of {@link RuleEngineActionResult} accumulating the results of initialization
	 * @param executionContext
	 * 		the instance of {@link ExecutionContext} to be set as executionContext of {@link RuleEngineActionResult} if not null
	 */
	void initialize(AbstractRulesModuleModel module,
				String deployedMvnVersion, boolean propagateToOtherNodes,
				boolean enableIncrementalUpdate, RuleEngineActionResult result, ExecutionContext executionContext);
	/**
	 * Initializes all {@code AbstractRulesModuleModel}s that are marked as active (in case of cluster mode - on all the
	 * nodes).
	 *
	 * @return the results of the initialize action (use {@link RuleEngineActionResult#isActionFailed()} to check if the
	 * action succeeded)
	 */
	List<RuleEngineActionResult> initializeAllRulesModules();

	/**
	 * Initializes all {@code AbstractRulesModuleModel}s that are marked as active.
	 *
	 * @param propagateToOtherNodes
	 * 		if true on all the nodes of cluster
	 * @return the activation results
	 */
	List<RuleEngineActionResult> initializeAllRulesModules(boolean propagateToOtherNodes);

	/**
	 * Updates the given rule if it already exists.
	 *
	 * @param ruleEngineRule
	 * 		Rule to be updated
	 * @param rulesModule
	 * 		{@code AbstractRulesModuleModel} where the Rule be updated
	 * @return the result of the update action (use {@link RuleEngineActionResult#isActionFailed()} to check if the
	 * action succeeded)
	 */
	RuleEngineActionResult updateEngineRule(AbstractRuleEngineRuleModel ruleEngineRule, AbstractRulesModuleModel rulesModule);

	/**
	 * Archives the given rule if it already exists.
	 *
	 * @deprecated since 1811
	 * 
	 * @param ruleEngineRule
	 * 		Rule to be archived
	 * @return the result of the archive action (use {@link RuleEngineActionResult#isActionFailed()} to check if the
	 * action succeeded)
	 */
	@Deprecated
	RuleEngineActionResult archiveRule(AbstractRuleEngineRuleModel ruleEngineRule);

	/**
	 * Archives the given rule if it already exists.
	 *
	 * @deprecated since 1811
	 * 
	 * @param ruleEngineRule
	 * 		Rule to be archived
	 * @param rulesModule
	 * 		{@code AbstractRulesModuleModel} where the Rule is archived
	 * @return the result of the archive action (use {@link RuleEngineActionResult#isActionFailed()} to check if the
	 * action succeeded)
	 */
	@Deprecated
	RuleEngineActionResult archiveRule(AbstractRuleEngineRuleModel ruleEngineRule, AbstractRulesModuleModel rulesModule);

	/**
	 * Archive a collection of rules
	 *
	 * @param engineRules
	 * 		collection of {@link DroolsRuleModel} instances
	 * @param <T>
	 * 		subtype of {@link DroolsRuleModel}
	 * @return optional of {@link InitializationFuture} keeping the status of initialization
	 */
	<T extends DroolsRuleModel> Optional<InitializationFuture> archiveRules(Collection<T> engineRules);

	/**
	 * Finds AbstractRuleEngineRuleModel for given code and module.
	 *
	 * @param code
	 * 		the rule code
	 * @param moduleName
	 * 		the rules module name
	 * @return AbstractRuleEngineRuleModel for given code.
	 */
	AbstractRuleEngineRuleModel getRuleForCodeAndModule(String code, String moduleName);

	/**
	 * Finds AbstractRuleEngineRuleModel for given uuid.
	 * 
	 * @param uuid
	 * 		the rule uuid
	 * @return AbstractRuleEngineRuleModel for given uuid.
	 *
	 */
	AbstractRuleEngineRuleModel getRuleForUuid(final String uuid);

	/**
	 * make the specified collection of engine rules belonging to a specified module inactive, so that the engine wouln't take them
	 * in consideration during init
	 *
	 * @param moduleName
	 * 		Name of the module the rules should refer to when applying changes
	 * @param engineRules
	 * 		A collection of {@link AbstractRuleEngineRuleModel} to be deactivated
	 * @param <T>
	 * 		a type of engine rules
	 */
	<T extends AbstractRuleEngineRuleModel> void deactivateRulesModuleEngineRules(String moduleName, Collection<T> engineRules);


}
