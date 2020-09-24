/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.ticket.cronjob;

import de.hybris.platform.ticket.enums.EventType;
import de.hybris.platform.ticket.constants.TicketsystemConstants;
import de.hybris.platform.ticket.event.dao.CustomerSupportEventDao;
import de.hybris.platform.ticketsystem.events.model.SessionEventModel;
import de.hybris.platform.ticketsystem.model.SessionEventsRemovalCronJobModel;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.util.Config;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * A job responsible for removing old session events based on survival duration which by default is set to 24 hours
 */
public class SessionEventsRemovalJob extends AbstractJobPerformable<SessionEventsRemovalCronJobModel>
{

	private static final Logger LOG = LogManager.getLogger(SessionEventsRemovalJob.class);

	private CustomerSupportEventDao customerSupportEventDao;

	@Override
	public PerformResult perform(final SessionEventsRemovalCronJobModel arg0)
	{
		try
		{
			//set page size to 1000
			final PageableData pagableData = new PageableData();
			pagableData.setPageSize(1000);

			final LocalDateTime now = LocalDateTime.now();

			final int eventsSurvivalAge = Config.getInt(
					TicketsystemConstants.DEFAULT_RECENT_SESSIONS_SURVIVAL_DURATION_KEY,
					TicketsystemConstants.DEFAULT_RECENT_SESSIONS_SURVIVAL_DURATION);

			//survival duration date will be the number of hours a session can survive
			final Date survivalDuration = Date.from(now.minusHours(eventsSurvivalAge).atZone(ZoneId.systemDefault()).toInstant());

			//fetch the list of matching events
			final List<SessionEventModel> doomedEvents = getCustomerSupportEventDao().findAllEventsBeforeDate(EventType.EVENTS,
					survivalDuration);

			//remove these events
			for (final SessionEventModel doomedEvent : doomedEvents)
			{
				getModelService().remove(doomedEvent);
			}

			return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
		}
		catch (final Exception e)
		{
			LOG.error("Exception occurred during events cleanup", e);
			return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
		}
	}

	protected CustomerSupportEventDao getCustomerSupportEventDao()
	{
		return customerSupportEventDao;
	}

	@Required
	public void setCustomerSupportEventDao(final CustomerSupportEventDao customerSupportEventDao)
	{
		this.customerSupportEventDao = customerSupportEventDao;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}
}
