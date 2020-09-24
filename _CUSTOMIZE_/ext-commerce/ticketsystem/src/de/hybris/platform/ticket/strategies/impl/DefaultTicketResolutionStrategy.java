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

import de.hybris.platform.comments.model.CommentAttachmentModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.ticket.enums.CsEventReason;
import de.hybris.platform.ticket.enums.CsInterventionType;
import de.hybris.platform.ticket.enums.CsResolutionType;
import de.hybris.platform.ticket.enums.CsTicketState;
import de.hybris.platform.ticket.events.model.CsCustomerEventModel;
import de.hybris.platform.ticket.events.model.CsTicketChangeEventEntryModel;
import de.hybris.platform.ticket.events.model.CsTicketEventModel;
import de.hybris.platform.ticket.events.model.CsTicketResolutionEventModel;
import de.hybris.platform.ticket.factory.TicketEventFactory;
import de.hybris.platform.ticket.model.CsTicketModel;
import de.hybris.platform.ticket.service.TicketException;
import de.hybris.platform.ticket.strategies.TicketAttributeChangeEventStrategy;
import de.hybris.platform.ticket.strategies.TicketResolutionStrategy;
import de.hybris.platform.ticket.strategies.TicketUpdateStrategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

/**
 * Implementation of Ticket Resolution Strategy
 */
public class DefaultTicketResolutionStrategy implements TicketResolutionStrategy
{
	@SuppressWarnings(
	{ "unused" })
	private static final Logger LOG = Logger.getLogger(DefaultTicketResolutionStrategy.class);

	private CsTicketState defaultResolvedTicketState;
	private CsTicketState defaultUnresolvedTicketState;

	private String resolveEventType;

	private String unresolveEventType;

	private String agentNoteEventType;

	private TicketUpdateStrategy ticketUpdateStrategy;
	private TicketAttributeChangeEventStrategy ticketAttributeChangeEventStrategy;
	private TicketEventFactory ticketEventFactory;
	private ModelService modelService;

	@Override
	public CsTicketResolutionEventModel resolveTicket(final CsTicketModel ticket, final CsInterventionType intervention,
			final CsResolutionType resolutionType, final String note) throws TicketException
	{
		return this.resolveTicket(ticket, intervention, resolutionType, note);
	}

	@Override
	public CsTicketResolutionEventModel resolveTicket(final CsTicketModel ticket, final CsInterventionType intervention,
			final CsResolutionType resolutionType, final String note, final Collection<MediaModel> attachments)
			throws TicketException
	{
		if (ticketUpdateStrategy.getTicketNextStates(ticket.getState()).contains(defaultResolvedTicketState))
		{
			final String oldState = ticket.getState().getCode();
			final CsTicketResolutionEventModel resolutionEvent = (CsTicketResolutionEventModel) ticketEventFactory
					.createEvent(resolveEventType);
			resolutionEvent.setText(note);
			resolutionEvent.setTicket(ticket);
			resolutionEvent.setResolutionType(resolutionType);
			resolutionEvent.setInterventionType(intervention);
			resolutionEvent.setReason(CsEventReason.UPDATE);
			ticket.setState(defaultResolvedTicketState);
			resolutionEvent
					.setSubject("Support Ticket Status Updated from " + oldState + " to " + defaultResolvedTicketState.getCode());

			final Set<CsTicketChangeEventEntryModel> changedValues = ticketAttributeChangeEventStrategy
					.getEntriesForChangedAttributes(ticket);

			resolutionEvent.setEntries(changedValues);

			this.addAttachmentsToEvent(resolutionEvent, attachments);

			getModelService().save(ticket);
			getModelService().save(resolutionEvent);

			if (attachments == null)
			{
				return resolutionEvent;
			}

			for (final MediaModel attachment : attachments)
			{
				if (getModelService().isNew(attachment))
				{
					getModelService().save(attachment);
				}
			}
			return resolutionEvent;
		}
		else
		{
			throw new TicketException(
					"Cannot resolve Ticket " + ticket.getTicketID() + ": configuation does not allow Tickets in state "
							+ ticket.getState() + " to be moved to state " + defaultResolvedTicketState);
		}
	}

	protected void addAttachmentsToEvent(final CsTicketEventModel event, final Collection<MediaModel> attachments)
	{
		if (attachments != null && !attachments.isEmpty())
		{
			for (final MediaModel m : attachments)
			{
				final CommentAttachmentModel attachment = getModelService().create(CommentAttachmentModel.class);
				attachment.setAbstractComment(event);
				attachment.setItem(m);

				getModelService().save(attachment);
			}
		}
	}

	@Override
	public CsCustomerEventModel unResolveTicket(final CsTicketModel ticket, final CsInterventionType intervention,
			final CsEventReason reason, final String note) throws TicketException
	{
		return this.unResolveTicket(ticket, intervention, reason, note);
	}

	@Override
	public CsCustomerEventModel unResolveTicket(final CsTicketModel ticket, final CsInterventionType intervention,
			final CsEventReason reason, final String note, final Collection<MediaModel> attachments) throws TicketException
	{
		if (ticketUpdateStrategy.getTicketNextStates(ticket.getState()).contains(defaultUnresolvedTicketState))
		{
			final String oldState = ticket.getState().getCode();
			final CsCustomerEventModel unResolveEvent = (CsCustomerEventModel) ticketEventFactory
					.createEvent(CsInterventionType.PRIVATE.equals(intervention) ? agentNoteEventType : unresolveEventType);
			unResolveEvent.setText(note);
			unResolveEvent.setTicket(ticket);
			unResolveEvent.setReason(reason);
			unResolveEvent.setInterventionType(intervention);

			ticket.setState(defaultUnresolvedTicketState);
			unResolveEvent
					.setSubject("Support Ticket Status Updated from " + oldState + " to " + defaultUnresolvedTicketState.getCode());

			final Set<CsTicketChangeEventEntryModel> changedValues = ticketAttributeChangeEventStrategy
					.getEntriesForChangedAttributes(ticket);

			unResolveEvent.setEntries(changedValues);

			this.addAttachmentsToEvent(unResolveEvent, attachments);

			getModelService().save(ticket);
			getModelService().save(unResolveEvent);

			if (attachments == null || attachments.isEmpty())
			{
				return unResolveEvent;
			}

			for (final MediaModel attachment : attachments)
			{
				if (getModelService().isNew(attachment))
				{
					getModelService().save(attachment);
				}
			}

			return unResolveEvent;
		}
		else
		{
			throw new TicketException(
					"Cannot resolve Ticket " + ticket.getTicketID() + ": configuation does not allow Tickets in state "
							+ ticket.getState() + " to be moved to state " + defaultUnresolvedTicketState);
		}
	}

	@Override
	public List<CsTicketState> filterTicketStatesToRemovedClosedStates(final List<CsTicketState> states)
	{
		final List<CsTicketState> ret = new ArrayList<>(states);
		ret.remove(defaultResolvedTicketState);

		return ret;
	}

	@Override
	public boolean isTicketClosed(final CsTicketModel ticket)
	{
		return ticket.getResolution() != null && CsTicketState.CLOSED.equals(ticket.getState());
	}

	@Override
	public boolean isTicketResolvable(final CsTicketModel ticket)
	{
		return ticket.getResolution() == null
				&& ticketUpdateStrategy.getTicketNextStates(ticket.getState()).contains(defaultResolvedTicketState);
	}

	@Required
	public void setDefaultResolvedTicketState(final CsTicketState defaultResolvedTicketState)
	{
		this.defaultResolvedTicketState = defaultResolvedTicketState;
	}

	@Required
	public void setDefaultUnresolvedTicketState(final CsTicketState defaultUnresolvedTicketState)
	{
		this.defaultUnresolvedTicketState = defaultUnresolvedTicketState;
	}

	@Required
	public void setTicketUpdateStrategy(final TicketUpdateStrategy ticketUpdateStrategy)
	{
		this.ticketUpdateStrategy = ticketUpdateStrategy;
	}

	@Required
	public void setTicketEventFactory(final TicketEventFactory ticketEventFactory)
	{
		this.ticketEventFactory = ticketEventFactory;
	}

	@Required
	public void setResolveEventType(final String resolveEventType)
	{
		this.resolveEventType = resolveEventType;
	}

	@Required
	public void setUnresolveEventType(final String unresolveEventType)
	{
		this.unresolveEventType = unresolveEventType;
	}

	/**
	 * @param agentNoteEventType
	 *           the agentNoteEventType to set
	 */
	@Required
	public void setAgentNoteEventType(final String agentNoteEventType)
	{
		this.agentNoteEventType = agentNoteEventType;
	}

	@Required
	public void setTicketAttributeChangeEventStrategy(final TicketAttributeChangeEventStrategy ticketAttributeChangeEventStrategy)
	{
		this.ticketAttributeChangeEventStrategy = ticketAttributeChangeEventStrategy;
	}

	/**
	 * @return the agentNoteEventType
	 */
	public String getAgentNoteEventType()
	{
		return agentNoteEventType;
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
