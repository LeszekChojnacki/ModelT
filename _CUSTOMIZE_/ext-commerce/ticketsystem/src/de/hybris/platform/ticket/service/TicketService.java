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

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.comments.model.CommentTypeModel;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.ticket.enums.CsEventReason;
import de.hybris.platform.ticket.enums.CsInterventionType;
import de.hybris.platform.ticket.enums.CsResolutionType;
import de.hybris.platform.ticket.enums.CsTicketCategory;
import de.hybris.platform.ticket.enums.CsTicketPriority;
import de.hybris.platform.ticket.enums.CsTicketState;
import de.hybris.platform.ticket.events.model.CsTicketEventModel;
import de.hybris.platform.ticket.model.CsAgentGroupModel;
import de.hybris.platform.ticket.model.CsTicketModel;

import java.util.List;


/**
 * Service to provide mechanism to find CsTickets by various attributes. Also provides methods to retrieve lists of
 * Agents, AgentGroups, Priorities, States, Intervention Types and Reasons based on the available instances at runtime.
 *
 * @spring.bean ticketService
 */
public interface TicketService
{
	/**
	 * Retrieves a list of the AgentGroups.
	 *
	 * @return The list of AgentGroups
	 */
	List<CsAgentGroupModel> getAgentGroups();

	/**
	 * Retrieves a list of the AgentGroups for the provided BaseStore
	 *
	 * @param store
	 *           The BaseStore to retrieve AgentGroups for
	 * @return The list of AgentGroups
	 */
	List<CsAgentGroupModel> getAgentGroupsForBaseStore(BaseStoreModel store);

	/**
	 * Retrieves a list of the Agents as Employees.
	 *
	 * @return The list of Agents
	 */
	List<EmployeeModel> getAgents();

	/**
	 * Retrieves a list of the Agents as Employees for the provided BaseStore
	 *
	 * @param store
	 *           The BaseStore to retrieve Agents for
	 * @return The list of Agents
	 */
	List<EmployeeModel> getAgentsForBaseStore(BaseStoreModel store);

	/**
	 * Retrieves all available Event Reasons in the system.
	 *
	 * @return The list of Event Reasons
	 */
	List<CsEventReason> getEventReasons();

	/**
	 * Gets all ticket events for <code>CsTicketModel</code> object.
	 *
	 * @param ticket
	 *           the <code>CsTicketModel</code> for which to return ticket events.
	 * @return A list of ticket events.
	 */
	List<CsTicketEventModel> getEventsForTicket(CsTicketModel ticket);

	/**
	 * Gets all ticket events for <code>CsTicketModel</code> object excluding private messages.
	 *
	 * @param ticket
	 *           the <code>CsTicketModel</code> for which to return ticket events.
	 * @return A list of ticket events.
	 */
	List<CsTicketEventModel> getTicketEventsForCustomerByTicket(CsTicketModel ticket);

	/**
	 * Retrieves all available Intervention Types in the system.
	 *
	 * @return The list of Intervention Types
	 */
	List<CsInterventionType> getInterventionTypes();

	/**
	 * Retrieves all available Resolution Types in the system.
	 *
	 * @return The list of Resolution Types
	 */
	List<CsResolutionType> getResolutionTypes();

	/**
	 * Retrieves all available Ticket Categories in the system.
	 *
	 * @return The list of Ticket Categories
	 */
	List<CsTicketCategory> getTicketCategories();

	/**
	 * Gets the ticket for given <code>CsTicketEventModel</code> object.
	 *
	 * @param ticketEvent
	 *           the ticket event
	 * @return the ticket
	 */
	CsTicketModel getTicketForTicketEvent(CsTicketEventModel ticketEvent);

	/**
	 * Lookup a specific ticket by ticket ID
	 *
	 * @param ticketId
	 *           The id of the ticket to lookup
	 * @return The ticket with that Id
	 */
	CsTicketModel getTicketForTicketId(String ticketId);

	/**
	 * Retrieves all available Ticket Priorities in the system.
	 *
	 * @return The list of Ticket Priorities
	 */
	List<CsTicketPriority> getTicketPriorities();

	/**
	 * Find all the tickets that are assigned to the Agent.
	 *
	 * @param agent
	 *           The agent to find assigned tickets for
	 * @return A list of tickets matching search criteria
	 */
	List<CsTicketModel> getTicketsForAgent(EmployeeModel agent);

	/**
	 * Find all the tickets assigned to the AgentGroup.
	 *
	 * @param agentGroup
	 *           The AgentGroup to find assigned tickets for
	 * @return A list of tickets matching search criteria
	 */
	List<CsTicketModel> getTicketsForAgentGroup(CsAgentGroupModel agentGroup);

	/**
	 * Find all the tickets that have the provided categories.
	 *
	 * @param category
	 *           The category/categories for which to return tickets. The number of arguments is variable and may be
	 *           zero, although zero arguments will return an empty list.
	 * @return A list of tickets matching search criteria
	 */
	List<CsTicketModel> getTicketsForCategory(CsTicketCategory... category);

	/**
	 * Find all tickets that are associated with the customer.
	 *
	 * @param customer
	 *           The customer to find tickets for
	 * @return A list of tickets matching search criteria
	 */
	List<CsTicketModel> getTicketsForCustomer(UserModel customer);

	/**
	 * Find all the tickets that are associated with the order.
	 *
	 * @param order
	 *           The order to find tickets for
	 * @return A list of tickets matching search criteria
	 */
	List<CsTicketModel> getTicketsForOrder(OrderModel order);

	/**
	 * Find all the tickets that have the provided priorities.
	 *
	 * @param priority
	 *           The priority/priorities for which to return tickets. The number of arguments is variable and may be
	 *           zero, although zero arguments will return an empty list.
	 * @return A list of tickets matching search criteria
	 */
	List<CsTicketModel> getTicketsForPriority(CsTicketPriority... priority);

	/**
	 * Find all the tickets that have the provided Resolution Type(s).
	 *
	 * @param resolutionType
	 *           The Resolution Type(s) for which to return tickets. The number of arguments is variable and may be zero,
	 *           although zero arguments will return an empty list.
	 * @return A list of tickets matching search criteria
	 */
	List<CsTicketModel> getTicketsForResolutionType(CsResolutionType... resolutionType);

	/**
	 * Find all the tickets that are in the provided states.
	 *
	 * @param state
	 *           The state(s) for which to return tickets. The number of arguments is variable and may be zero, although
	 *           zero arguments will return an empty list.
	 * @return A list of tickets matching search criteria
	 */
	List<CsTicketModel> getTicketsForState(CsTicketState... state);

	/**
	 * Retrieves all available Ticket States in the system.
	 *
	 * @return The list of Ticket States
	 */
	List<CsTicketState> getTicketStates();

	/**
	 * Get the comment type given the code of that type
	 *
	 * @param type
	 *           The type code of comment type to get
	 * @return The comment type represented by the type code
	 */
	CommentTypeModel getTicketType(String type);

	/**
	 *
	 * Find all tickets that are associated with the customer in order by Modified date and time.
	 *
	 * @param user the UserModel whose tickets to be returned
	 * @return List<CsTicketModel>
	 */
	List<CsTicketModel> getTicketsForCustomerOrderByModifiedTime(UserModel user);


	/**
	 *
	 * Find all tickets that are associated with the customer in order by Modified date and time.
	 *
	 * @param user the UserModel whose tickets to be returned
	 * @param baseSite the base site
	 * @param pageableData the pageable data
	 * @return List<CsTicketModel>
	 */
	SearchPageData<CsTicketModel> getTicketsForCustomerOrderByModifiedTime(UserModel user, BaseSiteModel baseSite,
			PageableData pageableData);

	/**
	 * Extract associatedTo object as AbstractOrderModel
	 *
	 * @param associatedCode
	 *           code of associated object
	 * @param userUid
	 *           optional
	 * @param siteUid
	 *           optional
	 * @return AbstractOrderModel, nullable
	 */
	AbstractOrderModel getAssociatedObject(final String associatedCode, final String userUid, final String siteUid);
}
