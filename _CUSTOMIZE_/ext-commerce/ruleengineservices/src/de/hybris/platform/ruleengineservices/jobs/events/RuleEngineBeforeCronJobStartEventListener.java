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
import de.hybris.platform.ruleengineservices.model.RuleEngineJobModel;
import de.hybris.platform.servicelayer.event.events.BeforeCronJobStartEvent;
import de.hybris.platform.servicelayer.event.impl.AbstractEventListener;
import de.hybris.platform.servicelayer.model.ModelService;
import org.springframework.beans.factory.annotation.Required;


/**
 * Event listener for the {@link BeforeCronJobStartEvent} type of events. In case of {@link RuleEngineJobModel} job type
 * performs removal of all of the {@link de.hybris.platform.cronjob.model.TriggerModel}s associated to the given cron job.
 * The purpose is to ensure that once the cron job has been started, it should not be triggered anymore, as rule engine cron jobs\
 * are expected to fire only once.
 */
public class RuleEngineBeforeCronJobStartEventListener extends AbstractEventListener<BeforeCronJobStartEvent>
{
	private ModelService modelService;

	@Override
	protected void onEvent(final BeforeCronJobStartEvent event)
	{
		if (RuleEngineJobModel._TYPECODE.equals(event.getJobType()))
		{
			final CronJobModel cronJob = getModelService().get(event.getCronJobPK());
			getModelService().removeAll(cronJob.getTriggers());
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
}
