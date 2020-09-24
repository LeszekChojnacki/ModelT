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

import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.ticket.enums.CsTicketState;
import de.hybris.platform.ticket.events.model.CsTicketEventModel;
import de.hybris.platform.ticket.model.CsAgentGroupModel;
import de.hybris.platform.ticket.model.CsTicketModel;
import de.hybris.platform.ticket.service.TicketException;

import java.util.List;


/**
 * Interface to support the strategies to be considered when changing a ticket. This strategy should implement any
 * business rules to be used changing a ticket.
 *
 * @author Rick Hobbs (rick@neoworks.com)
 */
public interface TicketUpdateStrategy
{
	/**
	 * Store all updated attributes on a ticket. This method should run business rules to confirm that the changes made
	 * are legal. If the update is not permitted an exception is thrown.
	 *
	 * @param ticket
	 *           The ticket to be updated
	 * @return The updated ticket
	 * @throws TicketException
	 *            If the update is not permitted
	 */
	CsTicketModel updateTicket(CsTicketModel ticket) throws TicketException;

	/**
	 * Store all updated attributes on a ticket. This method also allows a note to be added to the ticket with the
	 * update. This method should run business rules to confirm that the changes made are legal. If the update is not
	 * permitted an exception is thrown.
	 *
	 * @param ticket
	 *           The ticket to be updated
	 * @param note
	 *           The text of the note to add along with the update
	 * @return The updated ticket
	 * @throws TicketException
	 *            If the update is not permitted
	 */
	CsTicketModel updateTicket(CsTicketModel ticket, String note) throws TicketException;

	/**
	 * Set the state of the ticket. This method should run any business rules associated with state change. This method
	 * should also guarantee that it won't have the side effect of storing any other changes to the ticket and should
	 * throw an exception if the ticket has been otherwise updated.
	 *
	 * @param ticket
	 *           The ticket to update the state of
	 * @param newState
	 *           The state to set the ticket to
	 * @throws TicketException
	 *            If the ticket has been modified or the state transition is illegal
	 * @see TicketUpdateStrategy#getTicketNextStates getTicketNextStates to get list of possible state transitions
	 */
	void setTicketState(CsTicketModel ticket, CsTicketState newState) throws TicketException;

	/**
	 * Set the state of the ticket. This method should run any business rules associated with state change. This method
	 * should also guarantee that it won't have the side effect of storing any other changes to the ticket and should
	 * throw an exception if the ticket has been otherwise updated.
	 *
	 * @param ticket
	 *           The ticket to update the state of
	 * @param newState
	 *           The state to set the ticket to
	 * @param note
	 *           The text of the note to add along with the state change
	 * @throws TicketException
	 *            If the ticket has been modified or the state transition is illegal
	 * @see TicketUpdateStrategy#getTicketNextStates getTicketNextStates to get list of possible state transitions
	 */
	void setTicketState(CsTicketModel ticket, CsTicketState newState, String note) throws TicketException;

	/**
	 * Assign the ticket to a new agent, or null to make the ticket unassigned. This method should run any business rules
	 * associated with the assignment change. This method should also guarantee that it won't have the side effect of
	 * storing any other changes to the ticket and should throw an exception if the ticket has been otherwise updated.
	 *
	 * @param ticket
	 *           The ticket to update the state of
	 * @param agent
	 *           The agent to assign the ticket to or null to mark the ticket as unassigned.
	 * @return CsTicketEventModel
	 * @throws TicketException
	 *            If the ticket has been modified or the assignment is illegal
	 */
	CsTicketEventModel assignTicketToAgent(CsTicketModel ticket, EmployeeModel agent) throws TicketException;

	/**
	 * Assign the ticket to a new group, or null to make the ticket unassigned. This method should run any business rules
	 * associated with the assignment change. This method should also guarantee that it won't have the side effect of
	 * storing any other changes to the ticket and should throw an exception if the ticket has been otherwise updated.
	 *
	 * @param ticket
	 *           The ticket to update the state of
	 * @param group
	 *           The agent group to assign the ticket to or null to mark the ticket as unassigned.
	 * @return CsTicketEventModel
	 * @throws TicketException
	 *            If the ticket has been modified or the assignment is illegal
	 */
	CsTicketEventModel assignTicketToGroup(CsTicketModel ticket, CsAgentGroupModel group) throws TicketException;


	/**
	 * Get a list of legal states that a ticket can move into.
	 *
	 * @param currentState
	 *           The starting state
	 * @return a list of states that the ticket can move to next.
	 */
	List<CsTicketState> getTicketNextStates(CsTicketState currentState);
}
