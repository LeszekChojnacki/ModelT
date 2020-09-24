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

import de.hybris.platform.cronjob.jalo.CronJobProgressTracker;
import de.hybris.platform.ruleengineservices.maintenance.RuleCompilerPublisherResult;
import de.hybris.platform.ruleengineservices.model.RuleEngineCronJobModel;

import java.util.Optional;


/**
 * Implementation of the {@link de.hybris.platform.servicelayer.cronjob.JobPerformable} for compile&publish rule engine task
 */
public class RuleEngineCompilePublishJobPerformable extends AbstractRuleEngineJob
{

	@Override
	protected Optional<RuleCompilerPublisherResult> performInternal(final RuleEngineCronJobModel cronJob, final CronJobProgressTracker tracker)
	{
		final RuleCompilerPublisherResult result = getRuleMaintenanceService()
				.compileAndPublishRulesWithBlocking(cronJob.getSourceRules(), cronJob.getTargetModuleName(),
						cronJob.getEnableIncrementalUpdate().booleanValue(), tracker);
		return Optional.ofNullable(result);
	}

	@Override
	protected String getJobName()
	{
		return "RuleEngineCompilePublishJob";
	}
}
