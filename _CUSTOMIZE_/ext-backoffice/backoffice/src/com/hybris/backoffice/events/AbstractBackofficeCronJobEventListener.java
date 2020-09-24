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
package com.hybris.backoffice.events;

import de.hybris.platform.servicelayer.cronjob.CronJobHistoryInclude;
import de.hybris.platform.servicelayer.event.EventService;
import de.hybris.platform.servicelayer.event.events.AbstractCronJobPerformEvent;
import de.hybris.platform.servicelayer.event.impl.AbstractEventListener;
import de.hybris.platform.servicelayer.type.TypeService;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;


public abstract class AbstractBackofficeCronJobEventListener<T extends AbstractCronJobPerformEvent>
		extends AbstractEventListener<T>
{
	private EventService eventService;
	private TypeService typeService;
	private Map<String, CronJobHistoryInclude> processesIncludes;

	protected boolean isProcessUpdateEvent(final T event)
	{
		return processesIncludes.values().stream().anyMatch(include -> eventMatchesInclude(event, include));
	}

	protected boolean eventMatchesInclude(final T event, final CronJobHistoryInclude include)
	{
		return (include.getJobCodes() != null && include.getJobCodes().contains(event.getJob()))
				|| typesMatch(include.getJobTypeCode(), event.getJobType())
				|| typesMatch(include.getCronJobTypeCode(), event.getCronJobType());
	}

	protected boolean typesMatch(final String superType, final String subType)
	{
		if (StringUtils.isEmpty(superType) || StringUtils.isEmpty(subType))
		{
			return false;
		}
		return typeService.isAssignableFrom(superType, subType);
	}

	public EventService getEventService()
	{
		return eventService;
	}

	@Required
	public void setEventService(final EventService eventService)
	{
		this.eventService = eventService;
	}

	public TypeService getTypeService()
	{
		return typeService;
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}

	public Map<String, CronJobHistoryInclude> getProcessesIncludes()
	{
		return processesIncludes;
	}

	@Required
	public void setProcessesIncludes(final Map<String, CronJobHistoryInclude> processesIncludes)
	{
		this.processesIncludes = processesIncludes;
	}
}
