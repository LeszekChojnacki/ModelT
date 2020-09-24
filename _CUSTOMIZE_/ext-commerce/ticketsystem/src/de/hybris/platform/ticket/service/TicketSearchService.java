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

import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.ticket.enums.CsTicketState;
import de.hybris.platform.ticket.model.CsAgentGroupModel;
import de.hybris.platform.ticket.model.CsTicketModel;

import java.util.List;
import java.util.Set;


/**
 * Service for finding tickets
 */
public interface TicketSearchService
{
	/**
	 * Search for all tickets where the searchString appears in the headline or comment bodies of the ticket.
	 * 
	 * @param searchString
	 *           The string to search for
	 * @return A list of tickets that contain the searchString text in their headline or comment bodies
	 */
	List<CsTicketModel> searchForTickets(String searchString);

	/**
	 * Search for all tickets where the searchString appears in the headline or comment bodies of the ticket. The
	 * resulting tickets must also be in one of the specified states.
	 * 
	 * @param searchString
	 *           The string to search for
	 * @param states
	 *           The states that a ticket must be in to match the criteria
	 * @return A list of tickets that contain the searchString text in their headline or comment bodies
	 */
	List<CsTicketModel> searchForTickets(String searchString, Set<CsTicketState> states);

	/**
	 * Search for all the tickets with matching assigned Agent and assigned AgentGroup and Ticket State. If any arguments
	 * are provided as null then the argument will not be used in the search.
	 * 
	 * @param agent
	 *           The assigned Agent to match
	 * @param group
	 *           The assigned AgentGroup to match
	 * @param state
	 *           The Ticket State to match
	 * @return A list of Tickets matching the search criteria.
	 */
	List<CsTicketModel> searchForTickets(EmployeeModel agent, CsAgentGroupModel group, CsTicketState state);
}
