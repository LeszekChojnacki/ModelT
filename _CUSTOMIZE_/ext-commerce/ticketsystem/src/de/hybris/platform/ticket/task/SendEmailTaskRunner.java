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
package de.hybris.platform.ticket.task;

import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.task.TaskModel;
import de.hybris.platform.task.TaskRunner;
import de.hybris.platform.task.TaskService;
import de.hybris.platform.ticket.events.model.CsTicketEventModel;
import de.hybris.platform.ticket.model.CsTicketModel;
import de.hybris.platform.ticket.strategies.TicketEventEmailStrategy;

import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Required;


/**
 * Send email task runner.
 */
public class SendEmailTaskRunner implements TaskRunner<TaskModel>
{
	public static final String TICKET_MODEL_KEY = "ticketEventModelKey";
	public static final String TICKET_EVENT_MODEL_KEY = "ticketEventKey";
	public static final String LOCALE_MODEL_KEY = "localeKey";

	private TicketEventEmailStrategy ticketEventEmailStrategy;
	private I18NService i18NService;

	@Override
	public void run(final TaskService taskService, final TaskModel taskModel)
	{
		final Map<String, Object> context = (Map<String, Object>) taskModel.getContext();
		final CsTicketModel ticket = (CsTicketModel) context.get(TICKET_MODEL_KEY);
		final CsTicketEventModel event = (CsTicketEventModel) context.get(TICKET_EVENT_MODEL_KEY);
		getI18NService().setCurrentLocale((Locale) context.get(LOCALE_MODEL_KEY));
		getTicketEventEmailStrategy().sendEmailsForEvent(ticket, event);
	}

	@Override
	public void handleError(final TaskService taskService, final TaskModel taskModel, final Throwable throwable)
	{
		// no error handling currently.
		// Exception loggging happens in DefaultTickentEventEmailStrategy
	}

	protected TicketEventEmailStrategy getTicketEventEmailStrategy()
	{
		return ticketEventEmailStrategy;
	}

	@Required
	public void setTicketEventEmailStrategy(final TicketEventEmailStrategy ticketEventEmailStrategy)
	{
		this.ticketEventEmailStrategy = ticketEventEmailStrategy;
	}

	protected I18NService getI18NService()
	{
		return i18NService;
	}

	@Required
	public void setI18NService(final I18NService i18NService)
	{
		this.i18NService = i18NService;
	}
}
