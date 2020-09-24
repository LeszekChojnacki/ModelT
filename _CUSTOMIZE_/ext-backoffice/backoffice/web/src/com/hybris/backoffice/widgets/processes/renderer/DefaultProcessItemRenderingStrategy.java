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
package com.hybris.backoffice.widgets.processes.renderer;

import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobHistoryModel;
import de.hybris.platform.servicelayer.cronjob.CronJobService;
import de.hybris.platform.servicelayer.security.permissions.PermissionCheckingService;
import de.hybris.platform.servicelayer.security.permissions.PermissionsConstants;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.Ordered;
import org.zkoss.lang.Strings;

import com.hybris.backoffice.widgets.processes.ProcessItemRenderingStrategy;
import com.hybris.cockpitng.labels.LabelService;


public class DefaultProcessItemRenderingStrategy implements ProcessItemRenderingStrategy
{
	private LabelService labelService;
	private PermissionCheckingService permissionCheckingService;
	private CronJobService cronJobService;
	private int order = Ordered.LOWEST_PRECEDENCE;

	@Override
	public boolean canHandle(final CronJobHistoryModel cronJobHistory)
	{
		return true;
	}

	@Override
	public boolean isRerunApplicable(final CronJobHistoryModel cronJobHistory)
	{
		return (CronJobStatus.ABORTED.equals(cronJobHistory.getStatus()) || isFailed(cronJobHistory)) && permissionCheckingService
				.checkItemPermission(cronJobHistory.getCronJob(), PermissionsConstants.CHANGE).isGranted();
	}

	protected boolean isFailed(final CronJobHistoryModel cronJobHistoryModel)
	{
		return CronJobStatus.FINISHED.equals(cronJobHistoryModel.getStatus())
				&& (CronJobResult.ERROR.equals(cronJobHistoryModel.getResult())
						|| CronJobResult.FAILURE.equals(cronJobHistoryModel.getResult()));
	}

	@Override
	public void rerunCronJob(final CronJobHistoryModel cronJobHistory)
	{
		cronJobService.performCronJob(cronJobHistory.getCronJob());
	}

	@Override
	public boolean isProgressSupported(final CronJobHistoryModel cronJobHistory)
	{
		return false;
	}

	@Override
	public String getTitle(final CronJobHistoryModel cronJobHistory)
	{
		return labelService.getObjectLabel(cronJobHistory);
	}

	@Override
	public String getJobTitle(final CronJobHistoryModel cronJobHistory)
	{
		if (cronJobHistory.getCronJob() != null && cronJobHistory.getCronJob().getJob() != null)
		{
			return labelService.getObjectLabel(cronJobHistory.getCronJob().getJob());
		}
		return Strings.EMPTY;
	}

	@Override
	public int getOrder()
	{
		return order;
	}

	public void setOrder(final int order)
	{
		this.order = order;
	}

	public LabelService getLabelService()
	{
		return labelService;
	}

	@Required
	public void setLabelService(final LabelService labelService)
	{
		this.labelService = labelService;
	}

	public PermissionCheckingService getPermissionCheckingService()
	{
		return permissionCheckingService;
	}

	@Required
	public void setPermissionCheckingService(final PermissionCheckingService permissionCheckingService)
	{
		this.permissionCheckingService = permissionCheckingService;
	}

	public CronJobService getCronJobService()
	{
		return cronJobService;
	}

	@Required
	public void setCronJobService(final CronJobService cronJobService)
	{
		this.cronJobService = cronJobService;
	}
}
