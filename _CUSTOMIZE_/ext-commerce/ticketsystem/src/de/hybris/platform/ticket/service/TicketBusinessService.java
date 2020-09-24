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
package de.hybris.platform.ticket.service;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.ticket.enums.CsEventReason;
import de.hybris.platform.ticket.enums.CsInterventionType;
import de.hybris.platform.ticket.enums.CsResolutionType;
import de.hybris.platform.ticket.enums.CsTicketState;
import de.hybris.platform.ticket.events.model.CsCustomerEventModel;
import de.hybris.platform.ticket.events.model.CsTicketEventModel;
import de.hybris.platform.ticket.events.model.CsTicketResolutionEventModel;
import de.hybris.platform.ticket.model.CsAgentGroupModel;
import de.hybris.platform.ticket.model.CsTicketModel;
import de.hybris.platform.ticketsystem.data.CsTicketParameter;

import java.util.Collection;
import java.util.List;


/**
 * Service to support the business processes used for creating and managing tickets.
 *
 * @author Rick Hobbs (rick@neoworks.com)
 */
public interface TicketBusinessService
{

	/**
	 * Create a new ticket with the parameters defined in {@link CsTicketParameter}
	 *
	 * @param ticketParameter
	 *           Ticker creation parameters
	 * @return The newly created ticket
	 */
	CsTicketModel createTicket(CsTicketParameter ticketParameter);

	/**
	 * Create a new ticket with the given ticket model and creation event. The ticket will get validated and assigned a
	 * ticket id and stored in the DB. The creation event will get associated with the ticket.
	 *
	 * @param ticket
	 *           The new ticket
	 * @param creationEvent
	 *           The creation event to associate with the new ticket
	 * @return The newly created ticket
	 *
	 * @deprecated since 6.0 use {@link #createTicket(CsTicketParameter)} instead
	 */
	@Deprecated
	CsTicketModel createTicket(CsTicketModel ticket, CsCustomerEventModel creationEvent);

	/**
	 * Update the details of a ticket. This will generate an event to capture all changed fields on the ticket and attach
	 * it to the ticket. Business rules for changes to specific fields (state, assignment etc.) must also be run by the
	 * implementation of this method.
	 *
	 * @param ticket
	 *           The ticket to update the details of
	 * @return The updated ticket
	 * @throws TicketException
 *  			 If the ticket is null or the update is not permitted
	 */
	CsTicketModel updateTicket(CsTicketModel ticket) throws TicketException;

	/**
	 * Update the details of a ticket. This will generate an event to capture all changed fields on the ticket and
	 * attached to the ticket. A note can also be passed which will be added as a separate event to the ticket. Business
	 * rules for changes to specific fields (state, assignment etc.) must also be run by the implementation of this
	 * method.
	 *
	 * @param ticket
	 *           The ticket to update the details of
	 * @param note
	 *           The note to associate with the update
	 * @return The updated ticket
	 * @throws TicketException TicketException
	 * 			 In case if:
	 * 			 	1)the ticket is null
	 * 			 	2)the note is null or empty
	 * 			 	3)the update is not permitted
	 */
	CsTicketModel updateTicket(CsTicketModel ticket, String note) throws TicketException;

	/**
	 * Set the state of a ticket. Note that this is a separate method to updateTicket and will throw an exception if the
	 * ticket is in any other way changed.
	 *
	 * @param ticket
	 *           The ticket to change the state of
	 * @param newState
	 *           The new state of the ticket
	 * @return The updated ticket
	 * @throws TicketException
	 *            If the ticket has been modified
	 */
	CsTicketModel setTicketState(CsTicketModel ticket, CsTicketState newState) throws TicketException;

	/**
	 * Set the state of a ticket. Note that this is a separate method to updateTicket and will throw an exception if the
	 * ticket is in any other way changed.
	 *
	 * @param ticket
	 *           The ticket to change the state of
	 * @param newState
	 *           The new state of the ticket
	 * @param note
	 *           The note to associate with the state change
	 * @return The updated ticket
	 * @throws TicketException
	 *            If the ticket has been modified
	 */
	CsTicketModel setTicketState(CsTicketModel ticket, CsTicketState newState, String note) throws TicketException;

	/**
	 * Sets the assignedAgent of the Ticket, i.e. assigns the ticket to the specified agent. Note that this is a separate
	 * method to updateTicket and will throw an exception if the ticket is in any other way changed.
	 *
	 * @param ticket
	 *           The Ticket to assign
	 * @param agent
	 *           The Agent to assign the ticket to
	 * @return The updated ticket
	 * @throws TicketException
	 *            If the ticket has been modified
	 */
	CsTicketModel assignTicketToAgent(CsTicketModel ticket, EmployeeModel agent) throws TicketException;

	/**
	 * Sets the assignedGroup of the Ticket, i.e. assigns the ticket to the specified group. Note that this is a separate
	 * method to updateTicket and will throw an exception if the ticket is in any other way changed.
	 *
	 * @param ticket
	 *           The Ticket to assign
	 * @param group
	 *           The Group to assign the ticket to
	 * @return The updated ticket
	 * @throws TicketException
	 *            If the ticket has been modified
	 */
	CsTicketModel assignTicketToGroup(CsTicketModel ticket, CsAgentGroupModel group) throws TicketException;

	/**
	 * Add a note to the specified ticket. This will initiate all business rules around adding a note to a ticket. Note
	 * that any changes to the ticket itself will be ignored and should be saved using updateTicket.
	 *
	 * @param ticket
	 *           The ticket to add the note to
	 * @param intervention
	 *           The interventionType of the note
	 * @param reason
	 *           The reason for the intervention
	 * @param note
	 *           The note to add
	 * @param attachments
	 *           The attachments to add to the note
	 * @return The note created and attached to the ticket.
	 */
	CsCustomerEventModel addNoteToTicket(CsTicketModel ticket, CsInterventionType intervention, CsEventReason reason, String note,
			Collection<MediaModel> attachments);

	/**
	 * Add a customer email to the specified ticket. This will add the email to the ticket and send it via email to the
	 * customer using the business rules specified. Note that this is for sending an email to a customer and not for
	 * adding an email received from the customer.
	 *
	 * @param ticket
	 *           The ticket to add the email to
	 * @param reason
	 *           The reason for the email
	 * @param subject
	 *           The subject of the email
	 * @param emailBody
	 *           The body of the email
	 * @param attachments
	 *           The attachments to add to the email
	 * @return The note created and attached to the ticket.
	 */
	CsCustomerEventModel addCustomerEmailToTicket(CsTicketModel ticket, CsEventReason reason, String subject, String emailBody,
			Collection<MediaModel> attachments);

	/**
	 * Resolve a ticket and add the associated resolution event. The ticket will be validated to check it is in an
	 * appropriate state for resolution and will then be resolved if the state check passes
	 *
	 * @param ticket
	 *           The ticket to be resolved
	 * @param intervention
	 *           Intervention Type
	 * @param resolutionType
	 *           The resolution reason for the ticket
	 * @param note
	 *           message
	 * @param attachments
	 *           as attachments
	 * @return The resolution event created and attached to the ticket.
	 *
	 * @throws TicketException
	 *           if the ticket is null or missing arguments required to resolve ticket
	 *
	 */
	CsTicketResolutionEventModel resolveTicket(CsTicketModel ticket, CsInterventionType intervention,
			CsResolutionType resolutionType, String note, Collection<MediaModel> attachments) throws TicketException;


	/**
	 *
	 * Resolve a ticket and add the associated resolution event. The ticket will be validated to check it is in an
	 * appropriate state for resolution and will then be resolved if the state check passes
	 *
	 * @param ticket
	 *           The ticket to be resolved
	 * @param intervention
	 *           Intervention Type
	 * @param resolutionType
	 *           The resolution reason for the ticket
	 * @param note
	 *           message
	 * @return The resolution event created and attached to the ticket.
	 *
	 * @throws TicketException
	 *           if the ticket is null or missing arguments required to resolve ticket
	 *
	 */
	CsTicketResolutionEventModel resolveTicket(CsTicketModel ticket, CsInterventionType intervention,
			CsResolutionType resolutionType, String note) throws TicketException;

	/**
	 * Reverse the resolution of a ticket and add a note to it.
	 *
	 * @param ticket
	 *           The ticket to 'unresolve'
	 * @param intervention
	 *           The interventionType for the event
	 * @param reason
	 *           The reason for the event
	 * @param note
	 *           The content of the note to add to the ticket
	 * @param attachments
	 *           as ticket attachments
	 * @return The note created and attached to the ticket.
	 *
	 * @throws TicketException
	 *           if the ticket is null or missing arguments required to resolve ticket
	 *
	 */
	CsCustomerEventModel unResolveTicket(CsTicketModel ticket, CsInterventionType intervention, CsEventReason reason, String note,
			Collection<MediaModel> attachments) throws TicketException;

	/**
	 * Reverse the resolution of a ticket and add a note to it.
	 *
	 * @param ticket
	 *           The ticket to 'unresolve'
	 * @param intervention
	 *           The interventionType for the event
	 * @param reason
	 *           The reason for the event
	 * @param note
	 *           The content of the note to add to the ticket
	 * @return The note created and attached to the ticket.
	 *
	 * @throws TicketException
	 *           if the ticket is null or missing arguments required to unresolve ticket
	 */
	CsCustomerEventModel unResolveTicket(CsTicketModel ticket, CsInterventionType intervention, CsEventReason reason, String note)
			throws TicketException;

	/**
	 * Find out whether a ticket is closed. This is dependent on the business rules of when a ticket is open or closed.
	 *
	 * @param ticket
	 *           The ticket to check
	 * @return true if the ticket is closed, false otherwise
	 */
	boolean isTicketClosed(CsTicketModel ticket);

	/**
	 * Find out whether a ticket is resolvable. This is dependent on the business rules.
	 *
	 * @param ticket
	 *           The ticket to check
	 * @return true if the ticket is resolvable, false otherwise
	 */
	boolean isTicketResolvable(CsTicketModel ticket);

	/**
	 * Returns a list of states that the passed ticket can move to according to configured rules.
	 *
	 * @param ticket
	 *           The current state of the ticket
	 * @return The list of available states that the ticket can move to
	 */
	List<CsTicketState> getTicketNextStates(CsTicketModel ticket);

	/**
	 * Get the list of states which can come after the passed on
	 *
	 * @param state
	 *           The state to get the next states from
	 * @return A list of states that at ticket in the specified state can move into
	 */
	List<CsTicketState> getTicketNextStates(CsTicketState state);


	/**
	 * Returns the last event on the ticket.
	 *
	 * @param ticket
	 *           The ticket to retrieve the last event for
	 * @return The last event on the ticket
	 */
	CsTicketEventModel getLastEvent(CsTicketModel ticket);

	/**
	 * Get a description of a ticket for rendering on front ends
	 *
	 * @param ticket
	 *           The ticket to get the description of
	 * @return A string description of the ticket
	 */
	String renderTicketEventText(CsTicketEventModel ticket);
}
