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
import de.hybris.platform.comments.model.CommentTypeModel;
import de.hybris.platform.comments.model.ComponentModel;
import de.hybris.platform.comments.model.DomainModel;
import de.hybris.platform.comments.services.CommentService;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.ticket.constants.TicketsystemConstants;
import de.hybris.platform.ticket.dao.AgentDao;
import de.hybris.platform.ticket.dao.TicketDao;
import de.hybris.platform.ticket.enums.CsEventReason;
import de.hybris.platform.ticket.enums.CsInterventionType;
import de.hybris.platform.ticket.enums.CsResolutionType;
import de.hybris.platform.ticket.enums.CsTicketCategory;
import de.hybris.platform.ticket.enums.CsTicketPriority;
import de.hybris.platform.ticket.enums.CsTicketState;
import de.hybris.platform.ticket.events.model.CsTicketEventModel;
import de.hybris.platform.ticket.model.CsAgentGroupModel;
import de.hybris.platform.ticket.model.CsTicketModel;
import de.hybris.platform.ticket.resolver.TicketAssociatedObjectResolver;
import de.hybris.platform.ticket.service.TicketService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;

/**
 * This service class provides mechanism to find CsTickets by various attributes. Also provides methods to retrieve
 * lists of Agents, AgentGroups, Priorities, States, Intervention Types and Reasons based on the available instances at
 * runtime.
 */
public class DefaultTicketService implements TicketService
{
	private TicketDao ticketDao;
	private AgentDao agentDao;
	private CommentService commentService;
	private EnumerationService enumerationService;
	private String ticketSystemDomain;
	private String ticketSystemComponent;
	private Map<String, TicketAssociatedObjectResolver> associatedTicketObjectResolverMap;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CsAgentGroupModel> getAgentGroups()
	{
		return agentDao.findAgentGroups();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CsAgentGroupModel> getAgentGroupsForBaseStore(final BaseStoreModel store)
	{
		if (store == null)
		{
			throw new IllegalArgumentException("store must not be null");
		}

		return agentDao.findAgentGroupsByBaseStore(store);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<EmployeeModel> getAgents()
	{
		return agentDao.findAgents();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<EmployeeModel> getAgentsForBaseStore(final BaseStoreModel store)
	{
		if (store == null)
		{
			throw new IllegalArgumentException("store must not be null");
		}

		return agentDao.findAgentsByBaseStore(store);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CsEventReason> getEventReasons()
	{
		return enumerationService.getEnumerationValues(TicketsystemConstants.TC.CSEVENTREASON);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CsTicketEventModel> getEventsForTicket(final CsTicketModel ticket)
	{
		return ticketDao.findTicketEventsByTicket(ticket);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CsTicketEventModel> getTicketEventsForCustomerByTicket(final CsTicketModel ticket)
	{
		return ticketDao.findTicketEventsForCustomerByTicket(ticket);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CsInterventionType> getInterventionTypes()
	{
		return enumerationService.getEnumerationValues(TicketsystemConstants.TC.CSINTERVENTIONTYPE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CsResolutionType> getResolutionTypes()
	{
		return enumerationService.getEnumerationValues(TicketsystemConstants.TC.CSRESOLUTIONTYPE);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CsTicketCategory> getTicketCategories()
	{
		return enumerationService.getEnumerationValues(TicketsystemConstants.TC.CSTICKETCATEGORY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CsTicketModel getTicketForTicketEvent(final CsTicketEventModel ticketEvent)
	{
		// TODO change getRelatedItems call when proper service method in comments extension will be available
		final Collection<ItemModel> relatedItems = ticketEvent.getRelatedItems();
		if (relatedItems != null)
		{
			if (relatedItems.size() > 1)
			{
				throw new IllegalStateException(
						"A ticket event should only associated with a single ticket. Error occurred on event [" + ticketEvent.getPk()
								+ "]");
			}
			else if (relatedItems.size() == 1)
			{
				final ItemModel item = relatedItems.iterator().next();
				if (item instanceof CsTicketModel)
				{
					return (CsTicketModel) item;
				}
				else
				{
					throw new IllegalStateException("A ticket event must be associated with a ticket. Error occurred on event ["
							+ ticketEvent.getPk() + "] found related item [" + item + "]");
				}
			}
		}
		return null; // Not yet related to a ticket
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CsTicketModel getTicketForTicketId(final String ticketId)
	{
		if (ticketId == null || "".equals(ticketId))
		{
			return null;
		}

		final List<CsTicketModel> tickets = ticketDao.findTicketsById(ticketId);
		if (tickets.isEmpty())
		{
			return null;
		}

		if (tickets.size() > 1)
		{
			throw new AmbiguousIdentifierException(
					"CsTicket with ticketId'" + ticketId + "' is not unique, " + tickets.size() + " results!");
		}

		return tickets.get(0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CsTicketPriority> getTicketPriorities()
	{
		return enumerationService.getEnumerationValues(TicketsystemConstants.TC.CSTICKETPRIORITY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CsTicketModel> getTicketsForAgent(final EmployeeModel agent)
	{
		if (agent == null)
		{
			return ticketDao.findTicketsWithNullAgent();
		}
		else
		{
			return ticketDao.findTicketsByAgent(agent);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CsTicketModel> getTicketsForAgentGroup(final CsAgentGroupModel agentGroup)
	{
		if (agentGroup == null)
		{
			return ticketDao.findTicketsWithNullAgentGroup();
		}
		else
		{
			return ticketDao.findTicketsByAgentGroup(agentGroup);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CsTicketModel> getTicketsForCategory(final CsTicketCategory... category)
	{
		if (category == null || category.length == 0)
		{
			return Collections.emptyList();
		}

		return ticketDao.findTicketsByCategory(category);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CsTicketModel> getTicketsForCustomer(final UserModel customer)
	{
		if (customer == null)
		{
			return Collections.emptyList();
		}

		return ticketDao.findTicketsByCustomer(customer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CsTicketModel> getTicketsForCustomerOrderByModifiedTime(final UserModel user)
	{
		if (user == null)
		{
			return Collections.emptyList();
		}

		return ticketDao.findTicketsByCustomerOrderByModifiedTime(user);
	}

	@Override
	public SearchPageData<CsTicketModel> getTicketsForCustomerOrderByModifiedTime(final UserModel user,
			final BaseSiteModel baseSite, final PageableData pageableData)
	{
		return ticketDao.findTicketsByCustomerOrderByModifiedTime(user, baseSite, pageableData);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CsTicketModel> getTicketsForOrder(final OrderModel order)
	{
		if (order == null)
		{
			return Collections.emptyList();
		}

		return ticketDao.findTicketsByOrder(order);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CsTicketModel> getTicketsForPriority(final CsTicketPriority... priority)
	{
		if (priority == null || priority.length == 0)
		{
			return Collections.emptyList();
		}

		return ticketDao.findTicketsByPriority(priority);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CsTicketModel> getTicketsForResolutionType(final CsResolutionType... resolutionType)
	{
		if (resolutionType == null || resolutionType.length == 0)
		{
			return Collections.emptyList();
		}

		return ticketDao.findTicketsByResolutionType(resolutionType);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CsTicketModel> getTicketsForState(final CsTicketState... state)
	{
		if (state == null || state.length == 0)
		{
			return Collections.emptyList();
		}

		return ticketDao.findTicketsByState(state);
	}


	@Override
	public List<CsTicketState> getTicketStates()
	{
		return enumerationService.getEnumerationValues(TicketsystemConstants.TC.CSTICKETSTATE);
	}

	@Override
	public CommentTypeModel getTicketType(final String type)
	{
		final DomainModel domain = commentService.getDomainForCode(ticketSystemDomain);
		final ComponentModel component = commentService.getComponentForCode(domain, ticketSystemComponent);
		return commentService.getCommentTypeForCode(component, type);
	}


	/**
	 * @param associatedCode
	 *           code of associated object
	 * @param siteUid
	 *           optional
	 * @param siteUid
	 *           optional
	 * @return AbstractOrderModel
	 */
	@Override
	public AbstractOrderModel getAssociatedObject(final String associatedCode, final String userUid, final String siteUid)
	{
		if (!StringUtils.isEmpty(associatedCode))
		{
			final String[] associatedToTokens = associatedCode.split("=");

			if (associatedTicketObjectResolverMap.containsKey(associatedToTokens[0]))
			{
				final TicketAssociatedObjectResolver ticketAssociatedObjectResolver = associatedTicketObjectResolverMap
						.get(associatedToTokens[0]);
				return ticketAssociatedObjectResolver.getObject(associatedToTokens[1], userUid, siteUid);
			}
		}
		return null;
	}

	protected Map<String, TicketAssociatedObjectResolver> getAssociatedTicketObjectResolverMap()
	{
		return associatedTicketObjectResolverMap;
	}

	@Required
	public void setAssociatedTicketObjectResolverMap(
			final Map<String, TicketAssociatedObjectResolver> associatedTicketObjectResolverMap)
	{
		this.associatedTicketObjectResolverMap = associatedTicketObjectResolverMap;
	}

	public void setAgentDao(final AgentDao agentDao)
	{
		this.agentDao = agentDao;
	}

	@Required
	public void setCommentService(final CommentService commentService)
	{
		this.commentService = commentService;
	}

	@Required
	public void setEnumerationService(final EnumerationService enumerationService)
	{
		this.enumerationService = enumerationService;
	}

	@Required
	public void setTicketDao(final TicketDao ticketDao)
	{
		this.ticketDao = ticketDao;
	}

	@Required
	public void setTicketSystemComponent(final String ticketSystemComponent)
	{
		this.ticketSystemComponent = ticketSystemComponent;
	}

	@Required
	public void setTicketSystemDomain(final String ticketSystemDomain)
	{
		this.ticketSystemDomain = ticketSystemDomain;
	}
}
