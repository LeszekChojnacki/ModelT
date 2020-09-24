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
package de.hybris.platform.ticket.strategies;

import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.ticket.enums.CsEventReason;
import de.hybris.platform.ticket.enums.CsInterventionType;
import de.hybris.platform.ticket.events.model.CsCustomerEventModel;
import de.hybris.platform.ticket.events.model.CsTicketEventModel;
import de.hybris.platform.ticket.model.CsTicketModel;

import java.util.Collection;


/**
 * Interface to support the creation of events for a ticket. Allows a plugable strategy for checking specific critera on
 * the ticket etc. before an event can be created.
 *
 * @author Rick Hobbs (rick@neoworks.com)
 */
public interface TicketEventStrategy
{
	/**
	 * Create and internal note against the ticket with the specified information
	 *
	 * @param ticket
	 *           The ticket to create the note for
	 * @param intervention
	 *           The intervention type of the note
	 * @param reason
	 *           The reason for the note's creation
	 * @param note
	 *           The text of the note
	 * @param attachments
	 *           Any attachments which should be added to the note
	 * @return The newly create note
	 */
	CsCustomerEventModel createNoteForTicket(CsTicketModel ticket, CsInterventionType intervention, CsEventReason reason,
			String note, Collection<MediaModel> attachments);

	/**
	 * Create a note against the ticket which is sent as an email to the customer the ticket relates to
	 *
	 * @param ticket
	 *           The ticket to create the note email for
	 * @param reason
	 *           The reason for the email
	 * @param subject
	 *           The subject of the email
	 * @param emailBody
	 *           The body of the email
	 * @param attachments
	 *           Any attachments should be included with the email
	 * @return The newly create email
	 */
	CsCustomerEventModel createCustomerEmailForTicket(CsTicketModel ticket, CsEventReason reason, String subject,
			String emailBody, Collection<MediaModel> attachments);

	/**
	 * Create a creation event for the specified ticket.
	 *
	 * @param ticket
	 *           The ticket to create the creation event for
	 * @param reason
	 *           The reson for the ticket event
	 * @param interventionType
	 *           The intervention type for the ticket event
	 * @param text
	 *           The text to add to the creation event
	 * @return The newly created creation event
	 */
	CsCustomerEventModel createCreationEventForTicket(CsTicketModel ticket, CsEventReason reason,
			CsInterventionType interventionType, String text);

	/**
	 * Ensure that the specified creation event is correctly setup and bound to the ticket. This ensures that all of the
	 * manadatory underlying fields on the event are populated.
	 *
	 * @param ticket
	 *           The ticket the creation event is for
	 * @param creationEvent
	 *           The creation event
	 * @return The updated creation event
	 */
	CsCustomerEventModel ensureTicketEventSetupForCreationEvent(CsTicketModel ticket, CsCustomerEventModel creationEvent);

	/**
	 * Create a creation event for the specified ticket TO THE CS Agent.
	 *
	 * @param ticket
	 *           The ticket the creation event is for
	 * @return CsTicketEventModel
	 * 
	 */
	CsTicketEventModel createAssignAgentToTicket(CsTicketModel ticket);
}
