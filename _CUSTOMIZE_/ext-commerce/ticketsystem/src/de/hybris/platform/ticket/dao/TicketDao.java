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
package de.hybris.platform.ticket.dao;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.ticket.enums.CsResolutionType;
import de.hybris.platform.ticket.enums.CsTicketCategory;
import de.hybris.platform.ticket.enums.CsTicketPriority;
import de.hybris.platform.ticket.enums.CsTicketState;
import de.hybris.platform.ticket.events.model.CsTicketEventModel;
import de.hybris.platform.ticket.model.CsAgentGroupModel;
import de.hybris.platform.ticket.model.CsTicketModel;

import java.util.List;
import java.util.Set;


/**
 * The Interface TicketDao responsible for searching <code>CsTicketModel</code> objects.
 *
 * @spring.bean ticketDao
 */
public interface TicketDao
{
	/**
	 * Find tickets by the specified agent, group and state. If any of these parameters are null, they should be
	 * considered "don't care" - i.e. don't include in the query
	 *
	 * @param agent
	 *           The agent to search for or null if none
	 * @param group
	 *           The group to search for or null if none
	 * @param state
	 *           The state to search for or null if any
	 * @return The list of tickets matching the specified critera
	 */
	List<CsTicketModel> findTicketsByAgentGroupState(EmployeeModel agent, CsAgentGroupModel group, CsTicketState state);

	/**
	 * Find tickets which contain the specified search string in the headline or any of the events
	 *
	 * @param searchString
	 *           The string to find in the ticket
	 * @return The list of tickets matching the criteria
	 */
	List<CsTicketModel> findTicketsByStringInTicketOrEvent(String searchString);

	/**
	 * Find tickets which contain the specified search string in the headline or any of the events and that are also in
	 * the given list of states. If the list of states is empty or null then the state should not be considered in the
	 * query.
	 *
	 * @param searchString
	 *           The string to find in the ticket
	 * @param states
	 *           The states that any returned tickets should be in
	 * @return The list of tickets matching the criteria
	 */
	List<CsTicketModel> findTicketsByStringInTicketOrEventAndStates(String searchString, Set<CsTicketState> states);

	/**
	 * Find all ticket events for <code>CsTicketModel</code> object.
	 *
	 * @param ticket
	 *           the <code>CsTicketModel</code> for which to return ticket events.
	 * @return A list of ticket events matching search criteria.
	 */
	List<CsTicketEventModel> findTicketEventsByTicket(CsTicketModel ticket);

	/**
	 * Find all ticket events for <code>CsTicketModel</code> object excluding private messages.
	 *
	 * @param ticket
	 *           the <code>CsTicketModel</code> for which to return ticket events.
	 * @return A list of ticket events matching search criteria.
	 */
	List<CsTicketEventModel> findTicketEventsForCustomerByTicket(CsTicketModel ticket);

	/**
	 * Find all the tickets that are assigned to the Agent.
	 *
	 * @param agent
	 *           The agent to find assigned tickets for
	 * @return A list of tickets matching search criteria
	 */
	List<CsTicketModel> findTicketsByAgent(EmployeeModel agent);

	/**
	 * Find all the tickets assigned to the AgentGroup.
	 *
	 * @param agentGroup
	 *           The AgentGroup to find assigned tickets for
	 * @return A list of tickets matching search criteria
	 */
	List<CsTicketModel> findTicketsByAgentGroup(CsAgentGroupModel agentGroup);

	/**
	 * Find all the tickets that have the provided categories.
	 *
	 * @param category
	 *           The category/categories for which to return tickets. The number of arguments is variable and may be
	 *           zero, although zero arguments will return an empty list.
	 * @return A list of tickets matching search criteria
	 */
	List<CsTicketModel> findTicketsByCategory(CsTicketCategory... category);

	/**
	 * Find all tickets that are associated with the customer.
	 *
	 * @param customer
	 *           The customer to find tickets for
	 * @return A list of tickets matching search criteria
	 */
	List<CsTicketModel> findTicketsByCustomer(UserModel customer);

	/**
	 * Lookup tickets with the specified ticket id
	 *
	 * @param ticketId
	 *           The id of the ticket to lookup
	 * @return The tickets with that Id
	 */
	List<CsTicketModel> findTicketsById(String ticketId);

	/**
	 * Find all the tickets that are associated with the order.
	 *
	 * @param order
	 *           The order to find tickets for
	 * @return A list of tickets matching search criteria
	 */
	List<CsTicketModel> findTicketsByOrder(OrderModel order);

	/**
	 * Find all the tickets that have the provided priorities.
	 *
	 * @param priority
	 *           The priority/priorities for which to return tickets. The number of arguments is variable and may be
	 *           zero, although zero arguments will return an empty list.
	 * @return A list of tickets matching search criteria
	 */
	List<CsTicketModel> findTicketsByPriority(CsTicketPriority... priority);

	/**
	 * Find all the tickets that have the provided Resolution Type(s).
	 *
	 * @param resolutionType
	 *           The Resolution Type(s) for which to return tickets. The number of arguments is variable and may be zero,
	 *           although zero arguments will return an empty list.
	 * @return A list of tickets matching search criteria
	 */
	List<CsTicketModel> findTicketsByResolutionType(CsResolutionType... resolutionType);

	/**
	 * Find all the tickets that are in the provided states.
	 *
	 * @param state
	 *           The state(s) for which to return tickets. The number of arguments is variable and may be zero, although
	 *           zero arguments will return an empty list.
	 * @return A list of tickets matching search criteria
	 */
	List<CsTicketModel> findTicketsByState(CsTicketState... state);

	/**
	 * Find all the tickets that are not assigned to an agent.
	 *
	 * @return A list of tickets not assigned to an agent
	 */
	List<CsTicketModel> findTicketsWithNullAgent();

	/**
	 * Find all the tickets that are not assigne to an agent group.
	 *
	 * @return A list of tickets not assigned to an agent group
	 */
	List<CsTicketModel> findTicketsWithNullAgentGroup();

	/**
	 * Find all tickets that are associated with the customer in descending and order by Modified date time.
	 *
	 * @param user
	 * @return List<CsTicketModel>
	 */
	List<CsTicketModel> findTicketsByCustomerOrderByModifiedTime(UserModel user);

	/**
	 * Find all tickets that are associated with the customer and current site, in descending and order by Modified date
	 * time.
	 *
	 * @param user
	 * @param baseSite
	 * @param pageableData
	 * @return List<CsTicketModel>
	 */
	SearchPageData<CsTicketModel> findTicketsByCustomerOrderByModifiedTime(UserModel user, BaseSiteModel baseSite,
			PageableData pageableData);
}
