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
import de.hybris.platform.ticket.enums.CsResolutionType;
import de.hybris.platform.ticket.enums.CsTicketState;
import de.hybris.platform.ticket.events.model.CsCustomerEventModel;
import de.hybris.platform.ticket.events.model.CsTicketResolutionEventModel;
import de.hybris.platform.ticket.model.CsTicketModel;
import de.hybris.platform.ticket.service.TicketException;

import java.util.Collection;
import java.util.List;


/**
 * Interface to support the strategies to be considered when moving a note in and out of resolved state and when finding
 * out if a ticket can be considered 'closed'.
 *
 * @author Rick Hobbs (rick@neoworks.com)
 */
public interface TicketResolutionStrategy
{
	/**
	 * Create a resolution event against a ticket and associate it with the ticket. This will also set the resolution
	 * event on the ticket itself
	 *
	 * @param ticket
	 *           The ticket to mark as resolved
	 * @param intervention
	 *           The intervention of the resolution
	 * @param resolutionType
	 *           The type of the resolution
	 * @param note
	 *           The text note to add agsint the resolution
	 * @return The resolution event
	 */
	CsTicketResolutionEventModel resolveTicket(CsTicketModel ticket, CsInterventionType intervention,
			CsResolutionType resolutionType, String note) throws TicketException;

	/**
	 * Create a resolution event against a ticket and associate it with the ticket. This will also set the resolution
	 * event on the ticket itself
	 *
	 * @param ticket
	 *           The ticket to mark as resolved
	 * @param intervention
	 *           The intervention of the resolution
	 * @param resolutionType
	 *           The type of the resolution
	 * @param note
	 *           The text note to add agsint the resolution
	 * @return The resolution event
	 */
	CsTicketResolutionEventModel resolveTicket(CsTicketModel ticket, CsInterventionType intervention,
			CsResolutionType resolutionType, String note, Collection<MediaModel> attachments) throws TicketException;

	/**
	 * Remove the resolution event from the ticket (although keep in in the list of events) and add a note to a ticket to
	 * indicate that it is no longer resolved. This method should also update the ticket state to indicate it is no
	 * longer resolved if appropriate.
	 *
	 * @param ticket
	 *           The ticket to un-resolve
	 * @param intervention
	 *           The intervention that caused the ticket's resolution to be removed
	 * @param reason
	 *           The reason for unresolving the ticket
	 * @param note
	 *           The note to add when the ticket is unresolved.
	 * @return The note created against the ticket.
	 */
	CsCustomerEventModel unResolveTicket(CsTicketModel ticket, CsInterventionType intervention, CsEventReason reason, String note)
			throws TicketException;

	/**
	 * Remove the resolution event from the ticket (although keep in in the list of events) and add a note to a ticket to
	 * indicate that it is no longer resolved. This method should also update the ticket state to indicate it is no
	 * longer resolved if appropriate.
	 *
	 * @param ticket
	 *           The ticket to un-resolve
	 * @param intervention
	 *           The intervention that caused the ticket's resolution to be removed
	 * @param reason
	 *           The reason for unresolving the ticket
	 * @param note
	 *           The note to add when the ticket is unresolved.
	 * @param attachments
	 * @return The note created against the ticket.
	 */
	CsCustomerEventModel unResolveTicket(CsTicketModel ticket, CsInterventionType intervention, CsEventReason reason, String note,
			Collection<MediaModel> attachments) throws TicketException;

	/**
	 * Find out if a ticket is considered 'closed'. This allows for specific implementations to use different rules
	 * around reporting a closed ticket.
	 *
	 * @param ticket
	 *           The ticket to check
	 * @return true if the ticket is closed, false otherwise
	 */
	boolean isTicketClosed(CsTicketModel ticket);

	/**
	 * Find out whether a ticket is resolvable. This allows for specific implementations to use different rules.
	 *
	 * @param ticket
	 *           The ticket to check
	 * @return true if the ticket is resolvable, false otherwise
	 */
	boolean isTicketResolvable(CsTicketModel ticket);

	/**
	 * Filter a given list of ticket states to remove any which represent a resolved state and should only be used by the
	 * ticket resolution strategies. This method must not return a new states.
	 *
	 * @param states
	 *           The states to filter
	 * @return A list with any states removed
	 */
	List<CsTicketState> filterTicketStatesToRemovedClosedStates(List<CsTicketState> states);
}
