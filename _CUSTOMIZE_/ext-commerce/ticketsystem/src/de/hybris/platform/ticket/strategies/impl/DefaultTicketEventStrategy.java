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
import de.hybris.platform.ticket.constants.TicketsystemConstants;
import de.hybris.platform.ticket.enums.CsEventReason;
import de.hybris.platform.ticket.enums.CsInterventionType;
import de.hybris.platform.ticket.enums.CsTicketState;
import de.hybris.platform.ticket.events.model.CsCustomerEventModel;
import de.hybris.platform.ticket.events.model.CsTicketEventModel;
import de.hybris.platform.ticket.factory.TicketEventFactory;
import de.hybris.platform.ticket.model.CsTicketModel;
import de.hybris.platform.ticket.strategies.TicketEventStrategy;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

/**
 *
 */
public class DefaultTicketEventStrategy implements TicketEventStrategy
{
	private static final Logger LOG = Logger.getLogger(DefaultTicketEventStrategy.class);

	private ModelService modelService;
	private TicketEventFactory ticketEventFactory;
	private String ticketCreatedType;
	private String emailCommentType;
	private String customerNoteCommentType;
	private String agentNoteCommentType;
	private CsTicketState openState;

	@Override
	public CsCustomerEventModel createNoteForTicket(final CsTicketModel ticket, final CsInterventionType intervention,
			final CsEventReason reason, final String note, final Collection<MediaModel> attachments)
	{
		final String msgCommentType = CsInterventionType.PRIVATE.equals(intervention) ? getAgentNoteCommentType()
				: getCustomerNoteCommentType();
		final CsCustomerEventModel noteEvent = (CsCustomerEventModel) ticketEventFactory.createEvent(msgCommentType);
		noteEvent.setText(note);
		noteEvent.setTicket(ticket);
		noteEvent.setSubject(TicketsystemConstants.SUPPORT_TICKET_UPDATED);
		noteEvent.setInterventionType(intervention);
		noteEvent.setReason(reason);

		addAttachmentsToEvent(noteEvent, attachments);

		getModelService().save(noteEvent);

		if (attachments != null && !attachments.isEmpty())
		{
			for (final MediaModel attachment : attachments)
			{
				if (getModelService().isNew(attachment))
				{
					getModelService().save(attachment);
				}
			}
		}

		return noteEvent;
	}

	@Override
	public CsCustomerEventModel createAssignAgentToTicket(final CsTicketModel ticket)
	{
		final CsCustomerEventModel csAgentEvent = (CsCustomerEventModel) ticketEventFactory.createEvent(ticketCreatedType);
		csAgentEvent.setTicket(ticket);
		csAgentEvent.setSubject(TicketsystemConstants.SUPPORT_TICKET_ASSIGNED);
		csAgentEvent.setText("Assigned");
		getModelService().save(csAgentEvent);
		return csAgentEvent;
	}

	@Override
	public CsCustomerEventModel createCustomerEmailForTicket(final CsTicketModel ticket, final CsEventReason reason,
			final String subject, final String emailBody, final Collection<MediaModel> attachments)
	{

		final CsCustomerEventModel emailEvent = (CsCustomerEventModel) ticketEventFactory.createEvent(emailCommentType);
		emailEvent.setSubject(subject);
		emailEvent.setText(emailBody);
		emailEvent.setTicket(ticket);
		emailEvent.setInterventionType(CsInterventionType.EMAIL);
		emailEvent.setReason(reason);

		addAttachmentsToEvent(emailEvent, attachments);

		getModelService().save(emailEvent);

		if (attachments != null)
		{
			for (final MediaModel attachment : attachments)
			{
				if (getModelService().isNew(attachment))
				{
					getModelService().save(attachment);
				}
			}
		}

		return emailEvent;
	}

	@Override
	public CsCustomerEventModel createCreationEventForTicket(final CsTicketModel ticket, final CsEventReason reason,
			final CsInterventionType interventionType, final String text)
	{

		final CsCustomerEventModel creationEvent = (CsCustomerEventModel) ticketEventFactory.createEvent(ticketCreatedType);
		creationEvent.setSubject(StringUtils.EMPTY);
		creationEvent.setText(text);
		creationEvent.setTicket(ticket);
		creationEvent.setInterventionType(interventionType);
		creationEvent.setReason(reason);

		getModelService().save(creationEvent);

		return creationEvent;
	}

	@Override
	public CsCustomerEventModel ensureTicketEventSetupForCreationEvent(final CsTicketModel ticket,
			final CsCustomerEventModel creationEvent)
	{
		creationEvent.setTicket(ticket);
		final CsCustomerEventModel ret = (CsCustomerEventModel) ticketEventFactory.ensureTicketSetup(creationEvent,
				ticketCreatedType);

		onTicketCreation(ticket, ret);

		return ret;
	}

	/**
	 * Placeholder for post ticket creation business rules
	 */
	protected void onTicketCreation(final CsTicketModel ticket, final CsCustomerEventModel creationEvent)
	{
		if (ticket.getAssignedAgent() != null)
		{
			LOG.info("Newly created ticket is assigned, setting it to open state [" + openState + "]");
			ticket.setState(openState);
		}
		else if (ticket.getAssignedGroup() != null && ticket.getAssignedGroup().getDefaultAssignee() != null)
		{
			LOG.info("Newly created ticket is unassign but there is a default assignee, assigning to ["
					+ ticket.getAssignedGroup().getDefaultAssignee() + "]");
			ticket.setAssignedAgent(ticket.getAssignedGroup().getDefaultAssignee());
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

	@Required
	public void setEmailCommentType(final String emailCommentType)
	{
		this.emailCommentType = emailCommentType;
	}

	@Required
	public void setTicketCreatedType(final String ticketCreatedType)
	{
		this.ticketCreatedType = ticketCreatedType;
	}

	@Required
	public void setTicketEventFactory(final TicketEventFactory ticketEventFactory)
	{
		this.ticketEventFactory = ticketEventFactory;
	}

	@Required
	public void setOpenState(final CsTicketState openState)
	{
		this.openState = openState;
	}

	/**
	 * @param customerNoteCommentType
	 *           the customerNoteCommentType to set
	 */
	@Required
	public void setCustomerNoteCommentType(final String customerNoteCommentType)
	{
		this.customerNoteCommentType = customerNoteCommentType;
	}

	/**
	 * @param agentNoteCommentType
	 *           the agentNoteCommentType to set
	 */
	@Required
	public void setAgentNoteCommentType(final String agentNoteCommentType)
	{
		this.agentNoteCommentType = agentNoteCommentType;
	}

	/**
	 * @return the ticketEventFactory
	 */
	protected TicketEventFactory getTicketEventFactory()
	{
		return ticketEventFactory;
	}

	/**
	 * @return the ticketCreatedType
	 */
	protected String getTicketCreatedType()
	{
		return ticketCreatedType;
	}

	/**
	 * @return the emailCommentType
	 */
	protected String getEmailCommentType()
	{
		return emailCommentType;
	}

	/**
	 * @return the openState
	 */
	protected CsTicketState getOpenState()
	{
		return openState;
	}


	/**
	 * @return the customerNoteCommentType
	 */
	public String getCustomerNoteCommentType()
	{
		return customerNoteCommentType;
	}

	/**
	 * @return the agentNoteCommentType
	 */
	public String getAgentNoteCommentType()
	{
		return agentNoteCommentType;
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
