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

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;

import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.enums.JobLogLevel;
import de.hybris.platform.cronjob.jalo.CronJobProgressTracker;
import de.hybris.platform.cronjob.model.JobLogModel;
import de.hybris.platform.ruleengine.ResultItem;
import de.hybris.platform.ruleengine.RuleEngineActionResult;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerProblem;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerResult;
import de.hybris.platform.ruleengineservices.jobs.RuleEngineJobExecutionSynchronizer;
import de.hybris.platform.ruleengineservices.jobs.RuleEngineJobPerformable;
import de.hybris.platform.ruleengineservices.maintenance.RuleCompilerPublisherResult;
import de.hybris.platform.ruleengineservices.maintenance.RuleMaintenanceService;
import de.hybris.platform.ruleengineservices.model.RuleEngineCronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * Abstract implementation of {@link de.hybris.platform.servicelayer.cronjob.JobPerformable} for rule engine tasks
 */
public abstract class AbstractRuleEngineJob extends AbstractJobPerformable<RuleEngineCronJobModel> implements
		RuleEngineJobPerformable<RuleEngineCronJobModel>
{
	private static final Logger LOG = LoggerFactory.getLogger(AbstractRuleEngineJob.class);

	protected static final PerformResult SUCCESS_RESULT = new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);

	protected static final PerformResult FAILURE_RESULT = new PerformResult(CronJobResult.FAILURE, CronJobStatus.FINISHED);

	protected static final PerformResult ABORTED_RESULT = new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);

	private RuleMaintenanceService ruleMaintenanceService;

	private RuleEngineJobExecutionSynchronizer ruleEngineJobExecutionSynchronizer;

	@Override
	public PerformResult perform(final RuleEngineCronJobModel cronJob)
	{
		checkArgument(nonNull(cronJob), "CronJob should be specified");

		final CronJobProgressTracker tracker = createCronJobProgressTracker(cronJob);

		try
		{
			if (clearAbortRequestedIfNeeded(cronJob))
			{
				return ABORTED_RESULT;
			}

			logOnJobStart();

			final Optional<RuleCompilerPublisherResult> result = performInternal(cronJob, tracker);
			return result.isPresent() ? getPerformResult(cronJob, result.get()) : SUCCESS_RESULT;
		}
		catch (final Exception e)
		{
			LOG.error("Exception caught: {}", e);
			return FAILURE_RESULT;
		}
		finally
		{
			tracker.close();
		}
	}

	@Override
	public boolean isAbortable()
	{
		return true;
	}

	@Override
	public boolean isPerformable(final RuleEngineCronJobModel cronJob)
	{
		return toBoolean(cronJob.getLockAcquired()) ? true : getRuleEngineJobExecutionSynchronizer().acquireLock(cronJob);
	}

	protected abstract Optional<RuleCompilerPublisherResult> performInternal(final RuleEngineCronJobModel cronJob,
			final CronJobProgressTracker tracker);

	protected CronJobProgressTracker createCronJobProgressTracker(final RuleEngineCronJobModel cronJob)
	{
		return new CronJobProgressTracker(modelService.getSource(cronJob));
	}

	protected abstract String getJobName();

	protected PerformResult getPerformResult(final RuleEngineCronJobModel cronJob, final RuleCompilerPublisherResult result)
	{
		if (hasErrors(result))
		{
			try
			{
				onError(cronJob, result);
			}
			catch (final Exception e)
			{
				LOG.error("Error occurred: {}", e);
			}
			finally
			{
				logOnFailedJobFinish();
			}
			return FAILURE_RESULT;
		}
		if (result.getResult().equals(RuleCompilerPublisherResult.Result.SUCCESS))
		{
			logOnSuccessfulJobFinish();

			return SUCCESS_RESULT;
		}
		return FAILURE_RESULT;
	}

	protected boolean hasErrors(final RuleCompilerPublisherResult result)
	{
		return RuleCompilerPublisherResult.Result.COMPILER_ERROR.equals(result.getResult())
				|| RuleCompilerPublisherResult.Result.PUBLISHER_ERROR.equals(result.getResult()) || hasPublisherErrors(result);
	}

	protected boolean hasPublisherErrors(final RuleCompilerPublisherResult result)
	{
		return nonNull(result.getPublisherResults()) &&
				result.getPublisherResults().stream().anyMatch(RuleEngineActionResult::isActionFailed);
	}

	protected void onError(final RuleEngineCronJobModel cronJob, final RuleCompilerPublisherResult ruleResults)
	{
		if (ruleResults.getResult() == RuleCompilerPublisherResult.Result.COMPILER_ERROR)
		{
			logCompilerErrorMessages(cronJob, ruleResults);
		}
		else if (ruleResults.getResult() == RuleCompilerPublisherResult.Result.PUBLISHER_ERROR)
		{
			logPublisherErrorMessages(cronJob, ruleResults);
		}
		else
		{
			throw new IllegalArgumentException("Unsupported error type " + ruleResults.getResult());
		}
	}

	protected void logCompilerErrorMessages(final RuleEngineCronJobModel cronJob, final RuleCompilerPublisherResult ruleResults)
	{
		final Set<String> errorMessages = ruleResults
				.getCompilerResults()
				.stream()
				.filter(result -> RuleCompilerResult.Result.ERROR.equals(result.getResult()))
				.flatMap(compilerResult -> compilerResult.getProblems().stream())
				.map(RuleCompilerProblem::getMessage).collect(Collectors.toSet());
		errorMessages.forEach(LOG::error);
		errorMessages.forEach(message -> logToDatabase(cronJob, message));
	}


	protected void logPublisherErrorMessages(final RuleEngineCronJobModel cronJob, final RuleCompilerPublisherResult ruleResults)
	{
		final Set<String> errorMessages = ruleResults
				.getPublisherResults()
				.stream().flatMap(result -> result.getResults().stream())
				.map(ResultItem::getMessage).collect(Collectors.toSet());
		errorMessages.forEach(LOG::error);
		errorMessages.forEach(message -> logToDatabase(cronJob, message));
	}

	protected void logToDatabase(final RuleEngineCronJobModel cronJob, final String error)
	{
		if (Boolean.TRUE.equals(cronJob.getLogToDatabase()))
		{
			final JobLogModel log = getModelService().create(JobLogModel.class);
			log.setLevel(JobLogLevel.ERROR);
			log.setMessage(error);
			log.setCronJob(cronJob);
			getModelService().save(log);
		}
	}

	protected void logOnJobStart()
	{
		LOG.info("*************************************");
		LOG.info("Starting {}", getJobName());
		LOG.info("*************************************");
	}

	protected void logOnSuccessfulJobFinish()
	{
		LOG.info("*************************************");
		LOG.info("{} successfully finished", getJobName());
		LOG.info("*************************************");
	}

	protected void logOnFailedJobFinish()
	{
		LOG.info("*************************************");
		LOG.info("{} finished with errors", getJobName());
		LOG.info("*************************************");
	}

	protected void setTrackerProgress(final CronJobProgressTracker tracker, final double progress)
	{
		tracker.setProgress(progress);
	}

	protected RuleMaintenanceService getRuleMaintenanceService()
	{
		return ruleMaintenanceService;
	}

	@Required
	public void setRuleMaintenanceService(final RuleMaintenanceService ruleMaintenanceService)
	{
		this.ruleMaintenanceService = ruleMaintenanceService;
	}

	protected RuleEngineJobExecutionSynchronizer getRuleEngineJobExecutionSynchronizer()
	{
		return ruleEngineJobExecutionSynchronizer;
	}

	@Required
	public void setRuleEngineJobExecutionSynchronizer(final RuleEngineJobExecutionSynchronizer ruleEngineJobExecutionSynchronizer)
	{
		this.ruleEngineJobExecutionSynchronizer = ruleEngineJobExecutionSynchronizer;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

}
