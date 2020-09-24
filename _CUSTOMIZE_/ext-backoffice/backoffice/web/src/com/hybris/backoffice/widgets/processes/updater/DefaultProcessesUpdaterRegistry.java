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
package com.hybris.backoffice.widgets.processes.updater;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;

import com.hybris.cockpitng.components.Widgetslot;
import com.hybris.cockpitng.core.events.CockpitEvent;
import com.hybris.cockpitng.util.WidgetUtils;


/**
 * Default implementation which registers {@link #getProcessesUpdaters()} as global events listeners.
 * 
 * @deprecated since 6.6 - not used anymore
 */
@Deprecated
public class DefaultProcessesUpdaterRegistry implements ProcessesUpdatersRegistry
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultProcessesUpdaterRegistry.class);
	private List<ProcessesUpdater> processesUpdaters;
	private WidgetUtils widgetUtils;

	@Override
	public void registerGlobalEventListeners(final Widgetslot widgetslot, final Consumer<String> updateCronJob)
	{
		if (CollectionUtils.isNotEmpty(processesUpdaters))
		{
			final Map<String, List<ProcessesUpdater>> evtNameToListeners = processesUpdaters.stream()
					.collect(Collectors.groupingBy(ProcessesUpdater::getEventName));

			evtNameToListeners
					.forEach((eventName, listeners) -> addGlobalEventListener(eventName, listeners, updateCronJob, widgetslot));
		}
	}

	protected void addGlobalEventListener(final String eventName, final List<ProcessesUpdater> listeners,
			final Consumer<String> updateCronJob, final Widgetslot widgetslot)
	{
		final String eventScope = getCommonEventScope(listeners);
		final ComposedEventListener eventListener = new ComposedEventListener(updateCronJob, listeners);
		widgetUtils.addGlobalEventListener(eventName, widgetslot, eventListener, eventScope);
	}

	protected String getCommonEventScope(final List<ProcessesUpdater> listeners)
	{
		if (CollectionUtils.isNotEmpty(listeners))
		{
			final ProcessesUpdater firstUpdater = listeners.get(0);

			final Optional<ProcessesUpdater> anyWithDifferentScope = listeners.stream()
					.filter(updater -> !StringUtils.equals(updater.getEventScope(), firstUpdater.getEventScope())).findAny();

			anyWithDifferentScope.ifPresent(updater -> LOG.warn(
					"Different event scopes '{}' and '{}' have been found for global event '{}', the first one will be used",
					firstUpdater.getEventScope(), updater.getEventScope(), updater.getEventName()));

			return firstUpdater.getEventScope();
		}
		return StringUtils.EMPTY;
	}

	static class ComposedEventListener implements EventListener<Event>
	{
		private final List<ProcessesUpdater> listeners;
		private final Consumer<String> updateCronJob;

		public ComposedEventListener(final Consumer<String> updateCronJob, final List<ProcessesUpdater> listeners)
		{
			this.updateCronJob = updateCronJob;
			this.listeners = listeners;
		}

		@Override
		public void onEvent(final Event event) throws Exception
		{
			if (event.getData() instanceof CockpitEvent)
			{

				listeners.stream().flatMap(listener -> listener.onEvent(((CockpitEvent) event.getData())).stream()).distinct()
						.forEach(updateCronJob);
			}
		}
	}

	@Required
	public void setProcessesUpdaters(final List<ProcessesUpdater> processesUpdaters)
	{
		this.processesUpdaters = processesUpdaters;
	}

	public List<ProcessesUpdater> getProcessesUpdaters()
	{
		return processesUpdaters;
	}

	@Required
	public void setWidgetUtils(final WidgetUtils widgetUtils)
	{
		this.widgetUtils = widgetUtils;
	}

	public WidgetUtils getWidgetUtils()
	{
		return widgetUtils;
	}
}
