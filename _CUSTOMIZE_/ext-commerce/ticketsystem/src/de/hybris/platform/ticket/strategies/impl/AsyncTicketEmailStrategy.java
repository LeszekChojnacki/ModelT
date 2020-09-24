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
package de.hybris.platform.ticket.strategies.impl;

import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.task.TaskModel;
import de.hybris.platform.task.TaskService;
import de.hybris.platform.ticket.enums.CsEmailRecipients;
import de.hybris.platform.ticket.events.model.CsTicketEventModel;
import de.hybris.platform.ticket.model.CsTicketModel;
import de.hybris.platform.ticket.strategies.TicketEventEmailStrategy;
import de.hybris.platform.ticket.task.SendEmailTaskRunner;

import java.util.Date;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Required;

public class AsyncTicketEmailStrategy implements TicketEventEmailStrategy
{
	private TaskService taskService;
	private I18NService i18NService;
	private ModelService modelService;

	@Override
	public void sendEmailsForEvent(final CsTicketModel ticket, final CsTicketEventModel event)
	{
		sendEmailsForAssignAgentTicketEvent(ticket, event, null);
	}

	@Override
	public void sendEmailsForAssignAgentTicketEvent(final CsTicketModel ticket, final CsTicketEventModel event,
			final CsEmailRecipients recipientType)
	{
		// Send email asynchronously using task engine
		final TaskModel sendEmailTask = getModelService().create(TaskModel.class);
		sendEmailTask.setRunnerBean("sendEmailTaskRunner");
		sendEmailTask.setExecutionDate(new Date());
		final HashMap<String, Object> taskContext = new HashMap<>();
		taskContext.put(SendEmailTaskRunner.TICKET_MODEL_KEY, ticket);
		taskContext.put(SendEmailTaskRunner.TICKET_EVENT_MODEL_KEY, event);
		taskContext.put(SendEmailTaskRunner.LOCALE_MODEL_KEY, getI18NService().getCurrentLocale());

		sendEmailTask.setContext(taskContext);
		getTaskService().scheduleTask(sendEmailTask);
	}

	protected TaskService getTaskService()
	{
		return taskService;
	}

	@Required
	public void setTaskService(final TaskService taskService)
	{
		this.taskService = taskService;

	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(ModelService modelService)
	{
		this.modelService = modelService;
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
