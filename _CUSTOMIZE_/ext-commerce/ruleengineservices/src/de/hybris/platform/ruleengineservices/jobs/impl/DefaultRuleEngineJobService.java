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

import com.google.common.base.Preconditions;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.cronjob.model.TriggerModel;
import de.hybris.platform.ruleengineservices.jobs.RuleEngineCronJobDAO;
import de.hybris.platform.ruleengineservices.jobs.RuleEngineJobService;
import de.hybris.platform.ruleengineservices.model.RuleEngineCronJobModel;
import de.hybris.platform.ruleengineservices.model.RuleEngineJobModel;
import de.hybris.platform.servicelayer.cronjob.CronJobService;
import de.hybris.platform.servicelayer.model.ModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;


/**
 * Default implementation of the {@link RuleEngineJobService}
 */
public class DefaultRuleEngineJobService implements RuleEngineJobService
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultRuleEngineJobService.class);

	private ModelService modelService;
	private RuleEngineCronJobDAO ruleEngineCronJobDAO;
	private String triggerCronExpression;
	private CronJobService cronJobService;

	@Override
	public RuleEngineJobModel getRuleEngineJob(final String jobCode, final String springBeanName)
	{
		return getRuleEngineJob(jobCode)
				.orElseGet(() -> createNewRuleEngineJob(jobCode, springBeanName));
	}

	@Override
	public boolean isRunning(final String ruleEngineJobCode)
	{
		return countRunningJobs(ruleEngineJobCode) > 0;
	}

	@Override
	public int countRunningJobs(final String ruleEngineJobCode)
	{
		return getRuleEngineCronJobDAO().countCronJobsByJob(ruleEngineJobCode, CronJobStatus.RUNNING, CronJobStatus.RUNNINGRESTART,
				CronJobStatus.UNKNOWN);
	}

	@Override
	public RuleEngineCronJobModel triggerCronJob(final String ruleEngineJobCode, final String jobPerformableBeanName,
			final Supplier<RuleEngineCronJobModel> cronJobSupplier)
	{
		Preconditions
				.checkArgument(Objects.nonNull(cronJobSupplier), "Cron Job supplier for RuleEngineCronJobModel should be provided");

		final RuleEngineJobModel ruleEngineJob = getRuleEngineJob(ruleEngineJobCode, jobPerformableBeanName);
		final RuleEngineCronJobModel ruleEngineCronJob = cronJobSupplier.get();

		LOG.debug("Triggering cron job [{}] for [{}]", ruleEngineCronJob.getCode(), ruleEngineJobCode);

		ruleEngineCronJob.setJob(ruleEngineJob);
		getModelService().save(ruleEngineCronJob);

		if (getCronJobService().isPerformable(ruleEngineCronJob))
		{
			getCronJobService().performCronJob(ruleEngineCronJob);
		}
		else
		{
			createTriggerForCronJob(ruleEngineCronJob);
		}

		return ruleEngineCronJob;
	}

	protected Optional<RuleEngineJobModel> getRuleEngineJob(final String jobCode)
	{
		return Optional.ofNullable(getRuleEngineCronJobDAO().findRuleEngineJobByCode(jobCode));
	}

	protected RuleEngineJobModel createNewRuleEngineJob(final String jobCode, final String springBeanName)
	{
		final RuleEngineJobModel ruleEngineJob = getModelService().create(RuleEngineJobModel.class);
		ruleEngineJob.setCode(jobCode);
		ruleEngineJob.setSpringId(springBeanName);
		ruleEngineJob.setLogToDatabase(false);
		ruleEngineJob.setLogToFile(false);
		getModelService().save(ruleEngineJob);

		return ruleEngineJob;
	}

	protected void createTriggerForCronJob(final CronJobModel cronJob)
	{
		LOG.debug("Creating trigger with cron expression [{}] for the cron job [{}]", getTriggerCronExpression(),
				cronJob.getCode());

		final TriggerModel trigger = getModelService().create(TriggerModel.class);
		trigger.setActivationTime(new Date());
		trigger.setCronJob(cronJob);
		trigger.setCronExpression(getTriggerCronExpression());
		getModelService().save(trigger);
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	protected RuleEngineCronJobDAO getRuleEngineCronJobDAO()
	{
		return ruleEngineCronJobDAO;
	}

	@Required
	public void setRuleEngineCronJobDAO(final RuleEngineCronJobDAO ruleEngineCronJobDAO)
	{
		this.ruleEngineCronJobDAO = ruleEngineCronJobDAO;
	}

	public void setTriggerCronExpression(final String triggerCronExpression)
	{
		this.triggerCronExpression = triggerCronExpression;
	}

	protected String getTriggerCronExpression()
	{
		return triggerCronExpression;
	}

	protected CronJobService getCronJobService()
	{
		return cronJobService;
	}

	@Required
	public void setCronJobService(final CronJobService cronJobService)
	{
		this.cronJobService = cronJobService;
	}
}
