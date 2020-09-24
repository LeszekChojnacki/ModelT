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
package de.hybris.platform.ruleengineservices.jalo;

import de.hybris.platform.core.Registry;
import de.hybris.platform.cronjob.jalo.CronJob;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.ruleengineservices.jobs.RuleEngineJobPerformable;
import de.hybris.platform.servicelayer.internal.jalo.ServicelayerJob;
import de.hybris.platform.servicelayer.model.ModelService;


/**
 * Extending on the existing ServicelayerJob functionality by performing call to cronjob aware method
 * {@link RuleEngineJobPerformable#isPerformable(CronJobModel)}. This is required for a more dynamic decision making process
 * whether a job can perform based on the state of the given cronjob
 */
public class RuleEngineJob extends ServicelayerJob
{
	@Override
	protected boolean canPerform(final CronJob cronJob)
	{
		final CronJobModel model = getCronJob(cronJob);
		return Registry.getApplicationContext().containsBean(getSpringId()) && getPerformable().isPerformable(model);
	}

	@Override
	protected RuleEngineJobPerformable getPerformable()
	{
		return (RuleEngineJobPerformable) super.getPerformable();
	}

	protected CronJobModel getCronJob(final CronJob cronJob)
	{
		return Registry.getApplicationContext().getBean("modelService", ModelService.class).get(cronJob);
	}
}
