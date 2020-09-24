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
package de.hybris.platform.ticket.service.impl;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.comments.model.CommentAttachmentModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.time.TimeService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.ticket.enums.CsEmailRecipients;
import de.hybris.platform.ticket.enums.CsEventReason;
import de.hybris.platform.ticket.enums.CsInterventionType;
import de.hybris.platform.ticket.enums.CsResolutionType;
import de.hybris.platform.ticket.enums.CsTicketCategory;
import de.hybris.platform.ticket.enums.CsTicketPriority;
import de.hybris.platform.ticket.enums.CsTicketState;
import de.hybris.platform.ticket.events.model.CsCustomerEventModel;
import de.hybris.platform.ticket.events.model.CsTicketEventModel;
import de.hybris.platform.ticket.events.model.CsTicketResolutionEventModel;
import de.hybris.platform.ticket.model.CsAgentGroupModel;
import de.hybris.platform.ticket.model.CsTicketModel;
import de.hybris.platform.ticket.service.TicketAttachmentsService;
import de.hybris.platform.ticket.service.TicketBusinessService;
import de.hybris.platform.ticket.service.TicketException;
import de.hybris.platform.ticket.strategies.TicketEventEmailStrategy;
import de.hybris.platform.ticket.strategies.TicketEventStrategy;
import de.hybris.platform.ticket.strategies.TicketRenderStrategy;
import de.hybris.platform.ticket.strategies.TicketResolutionStrategy;
import de.hybris.platform.ticket.strategies.TicketUpdateStrategy;
import de.hybris.platform.ticketsystem.data.CsTicketParameter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.multipart.MultipartFile;


/**
 * Implementation of TicketBusinessService
 */
public class DefaultTicketBusinessService implements TicketBusinessService
{
	private static final Logger LOG = Logger.getLogger(DefaultTicketBusinessService.class);

	private static final String CANNOT_UPDATE_TICKET_TO_NULL_STATE = "Cannot update ticket to null state";
	private static final String CANNOT_UPDATE_NULL_TICKET = "Cannot update null ticket";
	private static final String DEFAULT_SUBJECT_MESSAGE = "Support Ticket Status Updated from %s to  %s";

	private TicketUpdateStrategy ticketUpdateStrategy;
	private TicketEventStrategy ticketEventStrategy;
	private TicketEventEmailStrategy ticketEventEmailStrategy;
	private TicketResolutionStrategy ticketResolutionStrategy;
	private TicketRenderStrategy ticketRenderStrategy;
	private BaseSiteService baseSiteService;
	private Converter<CsTicketParameter, CsTicketModel> ticketParameterConverter;
	private TicketAttachmentsService ticketAttachmentsService;
	private UserService userService;
	private TimeService timeService;
	private ModelService modelService;

	@Override
	public CsTicketModel createTicket(final CsTicketParameter ticketParameter)
	{
		//create attachments first and later assign them to the creation event info
		final List<CommentAttachmentModel> attachments = new ArrayList<>();

		if (CollectionUtils.isNotEmpty(ticketParameter.getAttachments()))
		{
			for (final MultipartFile file : ticketParameter.getAttachments())
			{
				try
				{
					final CommentAttachmentModel attachmentModel = getModelService().create(CommentAttachmentModel.class);

					attachmentModel.setItem(getTicketAttachmentsService().createAttachment(file.getOriginalFilename(),
							file.getContentType(), file.getBytes(), getUserService().getCurrentUser()));
					attachments.add(attachmentModel);
				}
				catch (final IOException e)
				{
					LOG.error(e.getMessage(), e);
					return null;
				}
			}
		}

		final CsTicketModel ticket = ticketParameterConverter.convert(ticketParameter);
		final CsCustomerEventModel creationEvent = ticketEventStrategy.createCreationEventForTicket(ticket,
				ticketParameter.getReason(), ticketParameter.getInterventionType(), ticketParameter.getCreationNotes());

		attachments.forEach(attachmentModel -> attachmentModel.setAbstractComment(creationEvent));

		getModelService().saveAll(attachments);
		return createTicketInternal(ticket, creationEvent);
	}

	/**
	 * {@inheritDoc}
	 * @deprecated since 6.0 use {@link #createTicket(CsTicketParameter)} instead
	 */
	@Deprecated
	@Override
	public CsTicketModel createTicket(final CsTicketModel ticket, final CsCustomerEventModel creationEvent)
	{
		return createTicketInternal(ticket, creationEvent);
	}

	protected CsTicketModel createTicketInternal(final CsTicketModel ticket, final CsCustomerEventModel creationEvent)
	{
		final BaseSiteModel currentBaseSite = getBaseSiteService().getCurrentBaseSite();
		if (currentBaseSite != null)
		{
			ticket.setBaseSite(currentBaseSite);
		}

		ticketEventStrategy.ensureTicketEventSetupForCreationEvent(ticket, creationEvent);

		getModelService().saveAll(ticket, creationEvent);
		getModelService().refresh(ticket);

		ticketEventEmailStrategy.sendEmailsForEvent(ticket, creationEvent);

		return ticket;
	}

	protected CsTicketModel populateTicketDetails(final UserModel customer, final AbstractOrderModel abstractOrder,
			final CsTicketCategory category, final CsTicketPriority priority, final EmployeeModel assignedAgent,
			final CsAgentGroupModel assignedGroup, final String headline)
	{
		final CsTicketModel ticket = getModelService().create(CsTicketModel.class);
		ticket.setCustomer(customer);
		ticket.setOrder(abstractOrder);
		ticket.setCategory(category);
		ticket.setPriority(priority);
		ticket.setAssignedAgent(assignedAgent);
		ticket.setAssignedGroup(assignedGroup);
		ticket.setHeadline(headline);

		final BaseSiteModel currentBaseSite = getBaseSiteService().getCurrentBaseSite();

		if (currentBaseSite != null)
		{
			ticket.setBaseSite(currentBaseSite);
		}
		else if (abstractOrder != null && abstractOrder.getSite() != null)
		{
			ticket.setBaseSite(abstractOrder.getSite());
		}

		return ticket;
	}

	@Override
	public CsTicketModel updateTicket(final CsTicketModel ticket) throws TicketException
	{
		if (ticket == null)
		{
			throw new IllegalArgumentException(CANNOT_UPDATE_NULL_TICKET);
		}

		final CsTicketModel ret = ticketUpdateStrategy.updateTicket(ticket);
		getModelService().refresh(ticket);

		ticketEventEmailStrategy.sendEmailsForEvent(ret, getLastEvent(ret));

		return ret;
	}

	@Override
	public CsTicketModel updateTicket(final CsTicketModel ticket, final String note) throws TicketException
	{
		if (ticket == null)
		{
			throw new IllegalArgumentException(CANNOT_UPDATE_NULL_TICKET);
		}
		if (note == null || "".equals(note))
		{
			throw new IllegalArgumentException("No note text found");
		}

		final CsTicketModel ret = ticketUpdateStrategy.updateTicket(ticket, note);
		getModelService().save(ret);

		ticketEventEmailStrategy.sendEmailsForEvent(ret, getLastEvent(ret));

		return ret;
	}

	@Override
	public CsTicketModel setTicketState(final CsTicketModel ticket, final CsTicketState newState) throws TicketException
	{
		return setTicketState(ticket, newState, "");
	}

	@Override
	public CsTicketModel setTicketState(final CsTicketModel ticket, final CsTicketState newState, final String note)
			throws TicketException
	{
		if (ticket == null)
		{
			throw new IllegalArgumentException(CANNOT_UPDATE_NULL_TICKET);
		}
		if (newState == null)
		{
			throw new IllegalArgumentException(CANNOT_UPDATE_TICKET_TO_NULL_STATE);
		}
		final String oldState = ticket.getState().getCode();
		ticketUpdateStrategy.setTicketState(ticket, newState, note);
		getModelService().refresh(ticket);

		final CsTicketEventModel lastEvent = getLastEvent(ticket);
		lastEvent.setSubject(String.format(DEFAULT_SUBJECT_MESSAGE, oldState, newState));
		ticketEventEmailStrategy.sendEmailsForEvent(ticket, getLastEvent(ticket));

		return ticket;
	}

	@Override
	public List<CsTicketState> getTicketNextStates(final CsTicketModel ticket)
	{
		if (ticket == null)
		{
			throw new IllegalArgumentException("ticket must not be null");
		}

		return ticketResolutionStrategy
				.filterTicketStatesToRemovedClosedStates(ticketUpdateStrategy.getTicketNextStates(ticket.getState()));
	}

	@Override
	public List<CsTicketState> getTicketNextStates(final CsTicketState state)
	{
		if (state == null)
		{
			throw new IllegalArgumentException("state must not be null");
		}

		return ticketResolutionStrategy.filterTicketStatesToRemovedClosedStates(ticketUpdateStrategy.getTicketNextStates(state));
	}

	@Override
	public CsTicketModel assignTicketToAgent(final CsTicketModel ticket, final EmployeeModel agent) throws TicketException
	{
		if (ticket == null)
		{
			throw new IllegalArgumentException(CANNOT_UPDATE_NULL_TICKET);
		}

		final CsTicketEventModel csTicketEventModel = ticketUpdateStrategy.assignTicketToAgent(ticket, agent);
		getModelService().refresh(ticket);
		if (csTicketEventModel != null)
		{
			ticketEventEmailStrategy.sendEmailsForAssignAgentTicketEvent(ticket, csTicketEventModel,
					CsEmailRecipients.ASSIGNEDAGENT);
		}

		return ticket;
	}

	@Override
	public CsTicketModel assignTicketToGroup(final CsTicketModel ticket, final CsAgentGroupModel group) throws TicketException
	{
		if (ticket == null)
		{
			throw new IllegalArgumentException(CANNOT_UPDATE_NULL_TICKET);
		}

		final CsTicketEventModel csTicketEventModel = ticketUpdateStrategy.assignTicketToGroup(ticket, group);

		getModelService().refresh(ticket);
		if (csTicketEventModel != null)
		{
			ticketEventEmailStrategy.sendEmailsForAssignAgentTicketEvent(ticket, csTicketEventModel,
					CsEmailRecipients.ASSIGNEDGROUP);
		}

		return ticket;
	}

	@Override
	public CsCustomerEventModel addNoteToTicket(final CsTicketModel ticket, final CsInterventionType intervention,
			final CsEventReason reason, final String note, final Collection<MediaModel> attachments)
	{
		if (ticket == null)
		{
			throw new IllegalArgumentException("Cannot add note to null ticket");
		}
		if (intervention == null || reason == null || note == null || "".equals(note))
		{
			throw new IllegalArgumentException("Missing arguments required to create note");
		}

		final CsCustomerEventModel ret = ticketEventStrategy.createNoteForTicket(ticket, intervention, reason, note, attachments);
		getModelService().save(ret);
		getModelService().saveAll(ret.getAttachments());
		getModelService().refresh(ticket);

		ticketEventEmailStrategy.sendEmailsForEvent(ticket, ret);

		return ret;
	}

	@Override
	public CsCustomerEventModel addCustomerEmailToTicket(final CsTicketModel ticket, final CsEventReason reason,
			final String subject, final String emailBody, final Collection<MediaModel> attachments)
	{
		if (ticket == null)
		{
			throw new IllegalArgumentException("Cannot add email to null ticket");
		}
		if (StringUtils.isBlank(subject) || reason == null || StringUtils.isBlank(emailBody))
		{
			throw new IllegalArgumentException("Missing arguments required to create email");
		}

		final CsCustomerEventModel ret = ticketEventStrategy.createCustomerEmailForTicket(ticket, reason, subject, emailBody,
				attachments);
		getModelService().save(ret);
		getModelService().saveAll(ret.getAttachments());
		getModelService().refresh(ticket);

		ticketEventEmailStrategy.sendEmailsForEvent(ticket, ret);

		return ret;
	}

	@Override
	public CsTicketResolutionEventModel resolveTicket(final CsTicketModel ticket, final CsInterventionType intervention,
			final CsResolutionType resolutionType, final String note) throws TicketException
	{
		return this.resolveTicket(ticket, intervention, resolutionType, note, Collections.emptyList());
	}

	@Override
	public CsTicketResolutionEventModel resolveTicket(final CsTicketModel ticket, final CsInterventionType intervention,
			final CsResolutionType resolutionType, final String note, final Collection<MediaModel> attachments)
			throws TicketException
	{
		if (ticket == null)
		{
			throw new IllegalArgumentException("Cannot resolve null ticket");
		}
		if (intervention == null || resolutionType == null || note == null || "".equals(note))
		{
			throw new IllegalArgumentException("Missing arguments required to resolve ticket");
		}

		final CsTicketResolutionEventModel ret = ticketResolutionStrategy.resolveTicket(ticket, intervention, resolutionType, note,
				attachments);
		getModelService().save(ret);

		ticket.setResolution(ret);
		ticket.setRetentionDate(getTimeService().getCurrentTime());
		getModelService().save(ticket);

		getModelService().refresh(ticket);

		LOG.info("Ticket [" + ticket.getTicketID() + "] has been marked as resolved with resolution type ["
				+ resolutionType.getCode() + "]");

		ticketEventEmailStrategy.sendEmailsForEvent(ticket, ret);

		return ret;
	}

	@Override
	public CsCustomerEventModel unResolveTicket(final CsTicketModel ticket, final CsInterventionType intervention,
			final CsEventReason reason, final String note) throws TicketException
	{
		return this.unResolveTicket(ticket, intervention, reason, note, Collections.emptyList());
	}

	@Override
	public CsCustomerEventModel unResolveTicket(final CsTicketModel ticket, final CsInterventionType intervention,
			final CsEventReason reason, final String note, final Collection<MediaModel> attachments) throws TicketException
	{
		if (ticket == null)
		{
			throw new IllegalArgumentException("Cannot unresolve null ticket");
		}
		if (intervention == null || reason == null || note == null || "".equals(note))
		{
			throw new IllegalArgumentException("Missing arguments required to unresolve ticket");
		}

		final CsCustomerEventModel ret = ticketResolutionStrategy.unResolveTicket(ticket, intervention, reason, note, attachments);
		getModelService().save(ret);

		ticket.setResolution(null);
		ticket.setRetentionDate(null);
		getModelService().save(ticket);

		getModelService().refresh(ticket);

		LOG.info("Ticket [" + ticket.getTicketID() + "] has been marked as unresolved");

		ticketEventEmailStrategy.sendEmailsForEvent(ticket, ret);

		return ret;
	}

	@Override
	public boolean isTicketClosed(final CsTicketModel ticket)
	{
		if (ticket == null)
		{
			throw new IllegalArgumentException("Cannot close null ticket");
		}
		return ticketResolutionStrategy.isTicketClosed(ticket);
	}

	@Override
	public boolean isTicketResolvable(final CsTicketModel ticket)
	{
		if (ticket == null)
		{
			throw new IllegalArgumentException("Cannot resolve null ticket");
		}
		return ticketResolutionStrategy.isTicketResolvable(ticket);
	}

	@Override
	public CsTicketEventModel getLastEvent(final CsTicketModel ticket)
	{
		if (ticket == null)
		{
			throw new IllegalArgumentException("ticket must not be null.");
		}

		if (!ticket.getEvents().isEmpty())
		{
			return ticket.getEvents().get(ticket.getEvents().size() - 1);
		}

		// No events found - this should not ever happen in reality
		LOG.error("Not events found for ticket [" + ticket + "] when looking for last event");
		return null;
	}

	@Override
	public String renderTicketEventText(final CsTicketEventModel ticketEvent)
	{
		return ticketRenderStrategy.renderTicketEvent(ticketEvent);
	}

	@Required
	public void setTicketUpdateStrategy(final TicketUpdateStrategy ticketUpdateStrategy)
	{
		this.ticketUpdateStrategy = ticketUpdateStrategy;
	}

	@Required
	public void setTicketEventStrategy(final TicketEventStrategy ticketEventStrategy)
	{
		this.ticketEventStrategy = ticketEventStrategy;
	}

	@Required
	public void setTicketEventEmailStrategy(final TicketEventEmailStrategy ticketEventEmailStrategy)
	{
		this.ticketEventEmailStrategy = ticketEventEmailStrategy;
	}

	@Required
	public void setTicketResolutionStrategy(final TicketResolutionStrategy ticketResolutionStrategy)
	{
		this.ticketResolutionStrategy = ticketResolutionStrategy;
	}

	@Required
	public void setTicketRenderStrategy(final TicketRenderStrategy ticketRenderStrategy)
	{
		this.ticketRenderStrategy = ticketRenderStrategy;
	}

	public TicketEventEmailStrategy getTicketEventEmailStrategy()
	{
		return ticketEventEmailStrategy;
	}

	/**
	 * @return the baseSiteService
	 */
	public BaseSiteService getBaseSiteService()
	{
		return baseSiteService;
	}

	/**
	 * @param baseSiteService
	 *           the baseSiteService to set
	 */
	@Required
	public void setBaseSiteService(final BaseSiteService baseSiteService)
	{
		this.baseSiteService = baseSiteService;
	}

	protected Converter<CsTicketParameter, CsTicketModel> getTicketParameterConverter()
	{
		return ticketParameterConverter;
	}

	@Required
	public void setTicketParameterConverter(final Converter<CsTicketParameter, CsTicketModel> ticketParameterConverter)
	{
		this.ticketParameterConverter = ticketParameterConverter;
	}

	protected UserService getUserService()
	{
		return userService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	protected TicketAttachmentsService getTicketAttachmentsService()
	{
		return ticketAttachmentsService;
	}

	@Required
	public void setTicketAttachmentsService(final TicketAttachmentsService ticketAttachmentsService)
	{
		this.ticketAttachmentsService = ticketAttachmentsService;
	}

	protected TimeService getTimeService()
	{
		return timeService;
	}

	@Required
	public void setTimeService(TimeService timeService)
	{
		this.timeService = timeService;
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
