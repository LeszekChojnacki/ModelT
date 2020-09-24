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

import de.hybris.platform.ruleengine.concurrency.SuspendResumeTaskManager;
import de.hybris.platform.ruleengine.concurrency.TaskContext;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerService;

import java.util.concurrent.atomic.AtomicLong;


/**
 * The interface for the rule compilation context
 */
public interface RuleCompilationContext extends TaskContext
{

	/**
	 * get the instance of {@link RuleCompilerService}
	 *
	 * @return instance of {@link RuleCompilerService}
	 */
	RuleCompilerService getRuleCompilerService();

	/**
	 * Resets the compilation context version to the value associated with rules module
	 *
	 * @param moduleName
	 * 		the module name for the series of rules to compile
	 * @return the instance of {@link AtomicLong} keeping the module rules version
	 */
	AtomicLong resetRuleEngineRuleVersion(String moduleName);

	/**
	 * get the next available rule version for the module
	 *
	 * @param moduleName
	 * 		the module name for the series of rules to compile
	 * @return a version number
	 */
	Long getNextRuleEngineRuleVersion(String moduleName);

	/**
	 * Clean up the context (usually called before the object destroy)
	 *
	 * @param moduleName
	 * 		the rules module name to be monitored by listeners
	 */
	void cleanup(String moduleName);

	/**
	 * register aplication listeners related to rule compilation process
	 *
	 * @param moduleName
	 * 		the rules module name to be monitored by listeners
	 */
	void registerCompilationListeners(String moduleName);

	/**
	 * get the suspend/resume task manager
	 *
	 * @return instance of {@link SuspendResumeTaskManager}
	 */
	SuspendResumeTaskManager getSuspendResumeTaskManager();

}
