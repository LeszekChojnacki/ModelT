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
package de.hybris.platform.ruleengineservices.jobs.events;

import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.ruleengineservices.jobs.RuleEngineJobExecutionSynchronizer;
import de.hybris.platform.ruleengineservices.model.RuleEngineCronJobModel;
import de.hybris.platform.ruleengineservices.model.RuleEngineJobModel;
import de.hybris.platform.servicelayer.event.events.AfterCronJobFinishedEvent;
import de.hybris.platform.servicelayer.event.impl.AbstractEventListener;
import de.hybris.platform.servicelayer.model.ModelService;
import org.springframework.beans.factory.annotation.Required;

/**
 * Event listener for the {@link AfterCronJobFinishedEvent} type of events. In case of {@link RuleEngineJobModel} job type
 * performs release of locks associated to the given cron job via
 * {@link RuleEngineJobExecutionSynchronizer#releaseLock(RuleEngineCronJobModel)}.
 */
public class RuleEngineAfterCronJobFinishedEventListener extends AbstractEventListener<AfterCronJobFinishedEvent>
{
	private ModelService modelService;

	private RuleEngineJobExecutionSynchronizer ruleEngineJobExecutionSynchronizer;

	@Override
	protected void onEvent(final AfterCronJobFinishedEvent event)
	{
		if (RuleEngineJobModel._TYPECODE.equals(event.getJobType()))
		{
			final CronJobModel cronJob = getModelService().get(event.getCronJobPK());
			getRuleEngineJobExecutionSynchronizer().releaseLock(((RuleEngineCronJobModel) cronJob));
		}
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

	protected RuleEngineJobExecutionSynchronizer getRuleEngineJobExecutionSynchronizer()
	{
		return ruleEngineJobExecutionSynchronizer;
	}

	@Required
	public void setRuleEngineJobExecutionSynchronizer(final RuleEngineJobExecutionSynchronizer ruleEngineJobExecutionSynchronizer)
	{
		this.ruleEngineJobExecutionSynchronizer = ruleEngineJobExecutionSynchronizer;
	}
}
