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

import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.servicelayer.model.AbstractItemModel;
import de.hybris.platform.servicelayer.model.ItemModelContextImpl;
import de.hybris.platform.servicelayer.model.ModelContextUtils;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.ticket.enums.CsTicketState;
import de.hybris.platform.ticket.events.model.CsTicketChangeEventCsTicketStateEntryModel;
import de.hybris.platform.ticket.events.model.CsTicketChangeEventEntryModel;
import de.hybris.platform.ticket.events.model.CsTicketEventModel;
import de.hybris.platform.ticket.factory.TicketEventFactory;
import de.hybris.platform.ticket.model.CsAgentGroupModel;
import de.hybris.platform.ticket.model.CsTicketModel;
import de.hybris.platform.ticket.service.TicketException;
import de.hybris.platform.ticket.strategies.TicketAttributeChangeEventStrategy;
import de.hybris.platform.ticket.strategies.TicketUpdateStrategy;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

/**
 *
 */
public class DefaultTicketUpdateStrategy implements TicketUpdateStrategy
{
	@SuppressWarnings(
	{ "unused" })
	private static final Logger LOG = Logger.getLogger(DefaultTicketUpdateStrategy.class);

	private TicketAttributeChangeEventStrategy ticketAttributeChangeEventStrategy;
	private Map<CsTicketState, List<CsTicketState>> availableNextStates;

	private String ticketChangeEventType;
	private String ticketAssigndEventType;
	private TicketEventFactory ticketEventFactory;
	private ModelService modelService;

	@Override
	public CsTicketModel updateTicket(final CsTicketModel ticket) throws TicketException
	{
		return updateTicket(ticket, "");
	}

	@Override
	public CsTicketModel updateTicket(final CsTicketModel ticket, final String note) throws TicketException
	{
		final Set<CsTicketChangeEventEntryModel> changedValues = ticketAttributeChangeEventStrategy
				.getEntriesForChangedAttributes(ticket);

		for (final CsTicketChangeEventEntryModel changedValue : changedValues)
		{
			if (changedValue instanceof CsTicketChangeEventCsTicketStateEntryModel)
			{
				final CsTicketChangeEventCsTicketStateEntryModel changedValueImpl = (CsTicketChangeEventCsTicketStateEntryModel) changedValue;
				if (!isValidStateTransition(changedValueImpl.getOldValue(), changedValueImpl.getNewValue()))
				{
					throw new TicketException("Ticket " + ticket.getTicketID() + " in state " + changedValueImpl.getOldValue()
							+ " cannot be changed to state " + changedValueImpl.getNewValue() + " according to configured rules");
				}
			}
		}

		if (!changedValues.isEmpty())
		{
			final CsTicketEventModel changeEvent = createChangeEvent(ticket, changedValues);
			changeEvent.setText(note);

			getModelService().saveAll(ticket, changeEvent);
			getModelService().saveAll(changedValues);
		}

		return ticket;
	}

	@Override
	public void setTicketState(final CsTicketModel ticket, final CsTicketState newState) throws TicketException
	{
		setTicketState(ticket, newState, "");
	}

	@Override
	public void setTicketState(final CsTicketModel ticket, final CsTicketState newState, final String note) throws TicketException
	{
		if (getContext(ticket).getValueHistory().isDirty())
		{
			throw new TicketException("The ticket must not have been previously updated before specifically changing the state");
		}

		preSetTicketState(ticket, newState);

		ticket.setState(newState);

		final Set<CsTicketChangeEventEntryModel> changedValues = ticketAttributeChangeEventStrategy
				.getEntriesForChangedAttributes(ticket);

		if (!changedValues.isEmpty())
		{
			final CsTicketEventModel changeEvent = createChangeEvent(ticket, changedValues, note);

			getModelService().saveAll(ticket, changeEvent);
			getModelService().saveAll(changedValues);
		}
	}

	@Override
	public List<CsTicketState> getTicketNextStates(final CsTicketState currentState)
	{
		if (availableNextStates.get(currentState) == null)
		{
			return Collections.emptyList();
		}

		return availableNextStates.get(currentState);
	}

	protected boolean isValidStateTransition(final CsTicketState oldState, final CsTicketState newState)
	{
		return availableNextStates.get(oldState) != null && availableNextStates.get(oldState).contains(newState);
	}

	protected void preSetTicketState(final CsTicketModel ticket, final CsTicketState newState) throws TicketException
	{
		if (!isValidStateTransition(ticket.getState(), newState))
		{
			throw new TicketException("Ticket " + ticket.getTicketID() + " in state " + ticket.getState()
					+ " cannot be changed to state " + newState + " according to configured rules");
		}
	}

	@Override
	public CsTicketEventModel assignTicketToAgent(final CsTicketModel ticket, final EmployeeModel agent) throws TicketException
	{
		preChangeTicketAssignment(ticket, agent);

		ticket.setAssignedAgent(agent);

		final Set<CsTicketChangeEventEntryModel> changedValues = ticketAttributeChangeEventStrategy
				.getEntriesForChangedAttributes(ticket);

		if (!changedValues.isEmpty())
		{
			final CsTicketEventModel changeEvent = createAssignTicketEvent(ticket, changedValues);

			getModelService().saveAll(ticket, changeEvent);
			getModelService().saveAll(changedValues);
			return changeEvent;
		}
		return null;
	}

	@SuppressWarnings(
	{ "unused" })
	protected void preChangeTicketAssignment(final CsTicketModel ticket, final EmployeeModel agent) throws TicketException
	{
		if (getContext(ticket).getValueHistory().isDirty())
		{
			throw new TicketException("The ticket must not have been previously updated before specifically changing the agent");
		}
	}

	@Override
	public CsTicketEventModel assignTicketToGroup(final CsTicketModel ticket, final CsAgentGroupModel group)
			throws TicketException
	{
		preChangeTicketGroup(ticket, group);

		ticket.setAssignedGroup(group);

		final Set<CsTicketChangeEventEntryModel> changedValues = ticketAttributeChangeEventStrategy
				.getEntriesForChangedAttributes(ticket);

		if (!changedValues.isEmpty())
		{
			final CsTicketEventModel changeEvent = createAssignTicketEvent(ticket, changedValues);

			getModelService().saveAll(ticket, changeEvent);
			getModelService().saveAll(changedValues);
			return changeEvent;
		}
		return null;
	}

	@SuppressWarnings(
	{ "unused" })
	protected void preChangeTicketGroup(final CsTicketModel ticket, final CsAgentGroupModel group) throws TicketException
	{
		if (getContext(ticket).getValueHistory().isDirty())
		{
			throw new TicketException("The ticket must not have been previously updated before specifically changing the agent");
		}
	}

	protected CsTicketEventModel createAssignTicketEvent(final CsTicketModel ticket,
			final Set<CsTicketChangeEventEntryModel> changedValues)
	{
		final CsTicketEventModel event = ticketEventFactory.createEvent(ticketAssigndEventType);
		event.setText("");
		event.setTicket(ticket);
		event.setEntries(changedValues);

		return event;
	}

	protected CsTicketEventModel createChangeEvent(final CsTicketModel ticket,
			final Set<CsTicketChangeEventEntryModel> changedValues)
	{
		return createChangeEvent(ticket, changedValues, "");
	}

	protected CsTicketEventModel createChangeEvent(final CsTicketModel ticket,
			final Set<CsTicketChangeEventEntryModel> changedValues, final String note)
	{
		final CsTicketEventModel event = ticketEventFactory.createEvent(ticketChangeEventType);
		event.setText(note);
		event.setTicket(ticket);
		event.setEntries(changedValues);

		return event;
	}

	@Required
	public void setTicketAttributeChangeEventStrategy(final TicketAttributeChangeEventStrategy ticketAttributeChangeEventStrategy)
	{
		this.ticketAttributeChangeEventStrategy = ticketAttributeChangeEventStrategy;
	}

	@Required
	public void setAvailableNextStates(final Map<CsTicketState, List<CsTicketState>> availableNextStates)
	{
		this.availableNextStates = availableNextStates;
	}

	@Required
	public void setTicketEventFactory(final TicketEventFactory ticketEventFactory)
	{
		this.ticketEventFactory = ticketEventFactory;
	}

	@Required
	public void setTicketChangeEventType(final String ticketChangeEventType)
	{
		this.ticketChangeEventType = ticketChangeEventType;
	}

	protected ItemModelContextImpl getContext(final AbstractItemModel model)
	{
		return (ItemModelContextImpl) ModelContextUtils.getItemModelContext(model);
	}

	public String getTicketAssigndEventType()
	{
		return ticketAssigndEventType;
	}

	@Required
	public void setTicketAssigndEventType(final String ticketAssigndEventType)
	{
		this.ticketAssigndEventType = ticketAssigndEventType;
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
}
