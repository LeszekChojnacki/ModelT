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
package de.hybris.platform.ruleengineservices.jobs.impl;

import static com.google.common.collect.Lists.newArrayList;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.ArrayUtils.isNotEmpty;
import static org.fest.util.Arrays.array;

import de.hybris.platform.ruleengine.dao.RulesModuleDao;
import de.hybris.platform.ruleengine.model.AbstractRulesModuleModel;
import de.hybris.platform.ruleengineservices.jobs.RuleEngineCronJobLauncher;
import de.hybris.platform.ruleengineservices.jobs.RuleEngineCronJobSupplierFactory;
import de.hybris.platform.ruleengineservices.jobs.RuleEngineJobService;
import de.hybris.platform.ruleengineservices.model.RuleEngineCronJobModel;
import de.hybris.platform.ruleengineservices.model.SourceRuleModel;
import de.hybris.platform.servicelayer.i18n.L10NService;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of the rule engine cron job launcher
 */
public class DefaultRuleEngineCronJobLauncher implements RuleEngineCronJobLauncher
{

	protected static final String COMPILE_PUBLISH_PERFORMABLE_BEAN_NAME = "ruleEngineCompilePublishJobPerformable";
	protected static final String COMPILE_PUBLISH_JOB_CODE_TEMPLATE = "rules -> Compilation and Publishing for [%s]";
	protected static final String UNDEPLOY_PERFORMABLE_BEAN_NAME = "ruleEngineUndeployJobPerformable";
	protected static final String UNDEPLOY_JOB_CODE_TEMPLATE = "rules -> Undeploy for [%s]";
	protected static final String MODULES_SYNCH_PERFORMABLE_BEAN_NAME = "ruleEngineModuleSyncJobPerformable";
	protected static final String MODULES_SYNCH_JOB_CODE_TEMPLATE = "rules -> Modules Sync from [%s] to [%s]";
	protected static final String MODULE_INIT_PERFORMABLE_BEAN_NAME = "ruleEngineModuleInitJobPerformable";
	protected static final String MODULE_INIT_JOB_CODE_TEMPLATE = "rules -> Module Init for [%s]";
	protected static final String ALL_MODULES_INIT_PERFORMABLE_BEAN_NAME = "ruleEngineAllModulesInitJobPerformable";
	protected static final String ALL_MODULES_INIT_JOB_CODE = "rules -> All Modules Init";
	/**
	 * @deprecated since 1811
	 */
	@Deprecated
	protected static final String ARCHIVE_JOB_CODE = "rules -> Archive rule";
	/**
	 * @deprecated since 1811
	 */
	@Deprecated
	protected static final String ARCHIVE_JOB_PERFORMABLE_BEAN_NAME = "ruleEngineArchiveJobPerformable";

	private RuleEngineJobService ruleEngineJobService;
	private RulesModuleDao rulesModuleDao;
	private RuleEngineCronJobSupplierFactory ruleEngineCronJobSupplierFactory;
	private L10NService l10nService;
	private int maximumNumberOfParallelCronJobs;

	@Override
	public RuleEngineCronJobModel triggerCompileAndPublish(final List<SourceRuleModel> rules, final String moduleName,
			final boolean enableIncrementalUpdate)
	{
		validateParameterNotNullStandardMessage("rules", rules);
		validateParameterNotNullStandardMessage("moduleName", moduleName);

		final String compilePublishJobCode = format(COMPILE_PUBLISH_JOB_CODE_TEMPLATE, moduleName);
		verifyIfJobsAreNotRunning(Stream.of(newArrayList(compilePublishJobCode,
				format(UNDEPLOY_JOB_CODE_TEMPLATE, moduleName),
				format(MODULE_INIT_JOB_CODE_TEMPLATE, moduleName), ALL_MODULES_INIT_JOB_CODE),
				getPossibleModuleSyncJobCodes(moduleName)).flatMap(Collection::stream).toArray(String[]::new));
		return getRuleEngineJobService()
				.triggerCronJob(compilePublishJobCode, COMPILE_PUBLISH_PERFORMABLE_BEAN_NAME,
						getRuleEngineCronJobSupplierFactory().createCompileAndPublishSupplier(rules, moduleName,
								enableIncrementalUpdate));
	}

	@Override
	public RuleEngineCronJobModel triggerUndeployRules(final List<SourceRuleModel> rules, final String moduleName)
	{
		validateParameterNotNullStandardMessage("rules", rules);
		validateParameterNotNullStandardMessage("moduleName", moduleName);

		final String undeployJobCode = format(UNDEPLOY_JOB_CODE_TEMPLATE, moduleName);
		verifyIfJobsAreNotRunning(Stream.of(newArrayList(undeployJobCode,
				format(COMPILE_PUBLISH_JOB_CODE_TEMPLATE, moduleName),
				format(MODULE_INIT_JOB_CODE_TEMPLATE, moduleName),
				ALL_MODULES_INIT_JOB_CODE),
				getPossibleModuleSyncJobCodes(moduleName)).flatMap(Collection::stream).toArray(String[]::new));
		return getRuleEngineJobService()
				.triggerCronJob(undeployJobCode, UNDEPLOY_PERFORMABLE_BEAN_NAME,
						getRuleEngineCronJobSupplierFactory().createUndeploySupplier(rules, moduleName));
	}

	/**
	 * @deprecated since 1811
	 */
	@Override
	@Deprecated
	public RuleEngineCronJobModel triggerArchiveRule(final SourceRuleModel rule)
	{
		validateParameterNotNullStandardMessage("rule", rule);

		final String[] jobCodes = Stream.of(newArrayList(ARCHIVE_JOB_CODE, ALL_MODULES_INIT_JOB_CODE),
				getPossibleJobCodes(MODULE_INIT_JOB_CODE_TEMPLATE),
				getPossibleJobCodes(COMPILE_PUBLISH_JOB_CODE_TEMPLATE),
				getPossibleJobCodes(UNDEPLOY_JOB_CODE_TEMPLATE),
				getPossibleModuleSyncJobCodes(),
				getPossibleJobCodes(MODULE_INIT_JOB_CODE_TEMPLATE)).flatMap(Collection::stream).toArray(String[]::new);
		verifyIfJobsAreNotRunning(jobCodes);

		return getRuleEngineJobService()
				.triggerCronJob(ARCHIVE_JOB_CODE, ARCHIVE_JOB_PERFORMABLE_BEAN_NAME,
						getRuleEngineCronJobSupplierFactory().createArchiveSupplier(rule));
	}

	@Override
	public RuleEngineCronJobModel triggerSynchronizeModules(final String srcModuleName, final String targetModuleName)
	{
		validateParameterNotNullStandardMessage("srcModuleName", srcModuleName);
		validateParameterNotNullStandardMessage("targetModuleName", targetModuleName);

		final String moduleSyncJobCode = format(MODULES_SYNCH_JOB_CODE_TEMPLATE, srcModuleName, targetModuleName);
		verifyIfJobsAreNotRunning(moduleSyncJobCode,
				format(MODULES_SYNCH_JOB_CODE_TEMPLATE, targetModuleName, srcModuleName),
				format(COMPILE_PUBLISH_JOB_CODE_TEMPLATE, srcModuleName),
				format(COMPILE_PUBLISH_JOB_CODE_TEMPLATE, targetModuleName),
				format(MODULE_INIT_JOB_CODE_TEMPLATE, srcModuleName),
				format(MODULE_INIT_JOB_CODE_TEMPLATE, targetModuleName),
				format(UNDEPLOY_JOB_CODE_TEMPLATE, srcModuleName),
				format(UNDEPLOY_JOB_CODE_TEMPLATE, targetModuleName),
				ALL_MODULES_INIT_JOB_CODE);
		return getRuleEngineJobService()
				.triggerCronJob(moduleSyncJobCode, MODULES_SYNCH_PERFORMABLE_BEAN_NAME,
						getRuleEngineCronJobSupplierFactory().createSynchronizeSupplier(srcModuleName, targetModuleName));
	}

	@Override
	public RuleEngineCronJobModel triggerModuleInitialization(final String moduleName)
	{
		validateParameterNotNullStandardMessage("moduleName", moduleName);

		final String moduleInitSyncJobCode = format(MODULE_INIT_JOB_CODE_TEMPLATE, moduleName);
		verifyIfJobsAreNotRunning(Stream.of(newArrayList(moduleInitSyncJobCode,
				format(COMPILE_PUBLISH_JOB_CODE_TEMPLATE, moduleName),
				format(UNDEPLOY_JOB_CODE_TEMPLATE, moduleName),
				format(MODULE_INIT_JOB_CODE_TEMPLATE, moduleName),
				ALL_MODULES_INIT_JOB_CODE),
				getPossibleModuleSyncJobCodes(moduleName)).flatMap(Collection::stream).toArray(String[]::new));
		return getRuleEngineJobService()
				.triggerCronJob(moduleInitSyncJobCode, MODULE_INIT_PERFORMABLE_BEAN_NAME,
						getRuleEngineCronJobSupplierFactory().createModuleInitializationSupplier(moduleName));
	}

	@Override
	public RuleEngineCronJobModel triggerAllModulesInitialization()
	{
		final String[] jobCodes = Stream.of(newArrayList(ALL_MODULES_INIT_JOB_CODE),
				getPossibleJobCodes(MODULE_INIT_JOB_CODE_TEMPLATE),
				getPossibleJobCodes(COMPILE_PUBLISH_JOB_CODE_TEMPLATE),
				getPossibleJobCodes(UNDEPLOY_JOB_CODE_TEMPLATE),
				getPossibleModuleSyncJobCodes(),
				getPossibleJobCodes(MODULE_INIT_JOB_CODE_TEMPLATE)).flatMap(Collection::stream).toArray(String[]::new);
		verifyIfJobsAreNotRunning(jobCodes);
		return getRuleEngineJobService()
				.triggerCronJob(ALL_MODULES_INIT_JOB_CODE, ALL_MODULES_INIT_PERFORMABLE_BEAN_NAME,
						getRuleEngineCronJobSupplierFactory().createAllModulesInitializationSupplier());
	}

	protected List<String> getPossibleModuleSyncJobCodes()
	{
		final List<String> moduleNames = getAllRuleModuleNames();
		final List<String> possibleModuleSyncJobNames = newArrayList();

		for (final String moduleName : moduleNames)
		{
			for (final String moduleName2 : moduleNames)
			{
				if (!moduleName2.equals(moduleName))
				{
					possibleModuleSyncJobNames.add(format(MODULES_SYNCH_JOB_CODE_TEMPLATE, moduleName, moduleName2));
				}
			}
		}
		return possibleModuleSyncJobNames;
	}

	protected List<String> getPossibleModuleSyncJobCodes(final String moduleName)
	{
		final List<String> moduleNames = getAllRuleModuleNames();
		final List<String> possibleModuleSyncJobNames = newArrayList();

		for (final String moduleName1 : moduleNames)
		{
			if (!moduleName1.equals(moduleName))
			{
				possibleModuleSyncJobNames.add(format(MODULES_SYNCH_JOB_CODE_TEMPLATE, moduleName, moduleName1));
				possibleModuleSyncJobNames.add(format(MODULES_SYNCH_JOB_CODE_TEMPLATE, moduleName1, moduleName));
			}
		}

		return possibleModuleSyncJobNames;
	}

	protected List<String> getPossibleJobCodes(final String template)
	{
		final List<String> moduleNames = getAllRuleModuleNames();
		return moduleNames.stream().map(m -> format(template, m)).collect(toList());
	}

	protected List<String> getAllRuleModuleNames()
	{
		return getRulesModuleDao().findAll().stream().filter(AbstractRulesModuleModel::getActive)
				.map(AbstractRulesModuleModel::getName).collect(toList());
	}

	protected void verifyIfJobsAreNotRunning(final String... jobCodes)
	{
		if (isNotEmpty(jobCodes))
		{
			final int numberOfCronJobsRunning = stream(jobCodes).mapToInt(getRuleEngineJobService()::countRunningJobs).sum();
			if (numberOfCronJobsRunning >= getMaximumNumberOfParallelCronJobs())
			{
				throw new IllegalStateException(getL10nService().getLocalizedString("rule.cronjob.launcher.limit.error",
						array(getMaximumNumberOfParallelCronJobs())));
			}
		}
	}

	protected RuleEngineJobService getRuleEngineJobService()
	{
		return ruleEngineJobService;
	}

	@Required
	public void setRuleEngineJobService(final RuleEngineJobService ruleEngineJobService)
	{
		this.ruleEngineJobService = ruleEngineJobService;
	}

	protected RulesModuleDao getRulesModuleDao()
	{
		return rulesModuleDao;
	}

	@Required
	public void setRulesModuleDao(final RulesModuleDao rulesModuleDao)
	{
		this.rulesModuleDao = rulesModuleDao;
	}

	protected RuleEngineCronJobSupplierFactory getRuleEngineCronJobSupplierFactory()
	{
		return ruleEngineCronJobSupplierFactory;
	}

	@Required
	public void setRuleEngineCronJobSupplierFactory(final RuleEngineCronJobSupplierFactory ruleEngineCronJobSupplierFactory)
	{
		this.ruleEngineCronJobSupplierFactory = ruleEngineCronJobSupplierFactory;
	}

	protected int getMaximumNumberOfParallelCronJobs()
	{
		return maximumNumberOfParallelCronJobs;
	}

	@Required
	public void setMaximumNumberOfParallelCronJobs(final int maximumNumberOfParallelCronJobs)
	{
		this.maximumNumberOfParallelCronJobs = maximumNumberOfParallelCronJobs;
	}

	protected L10NService getL10nService()
	{
		return l10nService;
	}

	@Required
	public void setL10nService(final L10NService l10nService)
	{
		this.l10nService = l10nService;
	}
}
