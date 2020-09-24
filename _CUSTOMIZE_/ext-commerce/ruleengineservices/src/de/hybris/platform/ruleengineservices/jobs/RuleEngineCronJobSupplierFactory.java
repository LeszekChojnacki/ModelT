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
import java.util.function.Supplier;

/**
 * Interface for the rule-engine job supplier factory
 */
public interface RuleEngineCronJobSupplierFactory
{
	/**
	 * Create supplier of {@link RuleEngineCronJobModel} for compilation and publishing of specified rules for a module
	 *
	 * @param rules
	 *           list of {@link SourceRuleModel} entities
	 * @param moduleName
	 *           kie module name
	 * @param enableIncrementalUpdate boolean flag that identifies whether to follow incremental update process
	 * @return supplier of {@link RuleEngineCronJobModel} instance
	 */
	Supplier<RuleEngineCronJobModel> createCompileAndPublishSupplier(List<SourceRuleModel> rules, String moduleName,
			boolean enableIncrementalUpdate);

	/**
	 * Create supplier of {@link RuleEngineCronJobModel} for undeployment of specified rules for a module
	 *
	 * @param rules
	 *           list of {@link SourceRuleModel} entities
	 * @param moduleName
	 *           kie module name
	 * @return supplier of {@link RuleEngineCronJobModel} instance
	 */
	Supplier<RuleEngineCronJobModel> createUndeploySupplier(List<SourceRuleModel> rules, String moduleName);

	/**
	 * Create supplier of {@link RuleEngineCronJobModel} for archive process for the specified rule
	 *
	 * @deprecated since 1811
	 * 
	 * @param rule
	 *           instance of {@link SourceRuleModel}
	 * @return supplier of {@link RuleEngineCronJobModel} instance
	 */
	@Deprecated
	Supplier<RuleEngineCronJobModel> createArchiveSupplier(SourceRuleModel rule);

	/**
	 * Create supplier of {@link RuleEngineCronJobModel} for modules synchronization process
	 *
	 * @param srcModuleName
	 *           source kie module name
	 * @param targetModuleName
	 *           name of the target kie module
	 * @return supplier of {@link RuleEngineCronJobModel} instance
	 */
	Supplier<RuleEngineCronJobModel> createSynchronizeSupplier(String srcModuleName, String targetModuleName);

	/**
	 * Create supplier of {@link RuleEngineCronJobModel} for module initialization process
	 *
	 * @param moduleName
	 *           kie module name
	 * @return supplier of {@link RuleEngineCronJobModel} instance
	 */
	Supplier<RuleEngineCronJobModel> createModuleInitializationSupplier(String moduleName);

	/**
	 * Create supplier of {@link RuleEngineCronJobModel} for all modules initialization process
	 *
	 * @return supplier of {@link RuleEngineCronJobModel} instance
	 */
	Supplier<RuleEngineCronJobModel> createAllModulesInitializationSupplier();
}
