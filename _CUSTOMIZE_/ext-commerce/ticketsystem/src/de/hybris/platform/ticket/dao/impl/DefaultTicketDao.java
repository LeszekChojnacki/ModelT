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
package de.hybris.platform.ticket.dao.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.comments.constants.CommentsConstants;
import de.hybris.platform.commerceservices.search.flexiblesearch.PagedFlexibleSearchService;
import de.hybris.platform.commerceservices.search.flexiblesearch.data.SortQueryData;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.core.model.link.LinkModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.ticket.dao.TicketDao;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of <code>TicketDao</code> interface.
 */
public class DefaultTicketDao extends AbstractItemDao implements TicketDao
{
	private static final String COMMENT_ITEM_RELATION = "CommentItemRelation";
	private static final String SEARCH_TICKET = "SELECT {t:" + CsTicketModel.PK + "} FROM {" + CsTicketModel._TYPECODE + " AS t} ";

	private static final String FIND_TICKETS_BY_CUSTOMER_SITE_QUERY_AND_ORDERBY_MODIFIEDTIME = "SELECT {pk} FROM {"
			+ CsTicketModel._TYPECODE + "} WHERE {" + CsTicketModel.CUSTOMER + "} = ?user AND {" + CsTicketModel.BASESITE
			+ "} = ?baseSite";

	private static final String FIND_TICKETS_BY_CUSTOMER_SITE_QUERY_AND_ORDERBY_TICKETID = "SELECT {pk} FROM {"
			+ CsTicketModel._TYPECODE + "} WHERE {" + CsTicketModel.CUSTOMER + "} = ?user AND {" + CsTicketModel.BASESITE
			+ "} = ?baseSite";

	private static final String SORT_TICKETS_BY_MODIFIED_DATE = " ORDER BY {" + CsTicketModel.MODIFIEDTIME + "} DESC";

	private static final String SORT_TICKETS_BY_TICKETID = " ORDER BY {" + CsTicketModel.TICKETID + "} DESC";

	private PagedFlexibleSearchService pagedFlexibleSearchService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CsTicketModel> findTicketsByAgentGroupState(final EmployeeModel agent, final CsAgentGroupModel group,
			final CsTicketState state)
	{
		if ((agent == null) && (group == null) && (state == null))
		{
			return Collections.emptyList();
		}
		else
		{
			final Map<String, Object> params = new HashMap<String, Object>();

			final StringBuilder query = new StringBuilder(SEARCH_TICKET);
			query.append(" WHERE ");

			//poor implementation, sorry.
			if (agent != null)
			{
				params.put("agent", agent);
				query.append("{" + CsTicketModel.ASSIGNEDAGENT + "} = ?agent ");
				if (group != null || state != null)
				{
					query.append(" AND ");
				}
			}

			if (group != null)
			{
				params.put("group", group);
				query.append("{" + CsTicketModel.ASSIGNEDGROUP + "} = ?group ");
				if (state != null)
				{
					query.append(" AND ");
				}
			}

			if (state != null)
			{
				params.put("state", state);
				query.append("{" + CsTicketModel.STATE + "} = ?state ");
			}

			final SearchResult<CsTicketModel> result = getFlexibleSearchService().search(query.toString(), params);
			return result.getResult();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CsTicketModel> findTicketsByStringInTicketOrEvent(final String searchString)
	{
		if (searchString == null)
		{
			throw new IllegalArgumentException("searchString must not be null");
		}

		final String query = "SELECT DISTINCT {t:" + CsTicketModel.PK + "}, {t:" + CsTicketModel.CREATIONTIME + "} FROM {"
				+ CsTicketModel._TYPECODE + " AS t LEFT JOIN " + COMMENT_ITEM_RELATION + " AS e2t ON {t:" + CsTicketModel.PK
				+ "}={e2t:" + LinkModel.TARGET + "} LEFT JOIN " + CsTicketEventModel._TYPECODE + " AS e ON {e2t:" + LinkModel.SOURCE
				+ "}={e:" + CsTicketEventModel.PK + "} } WHERE {t:" + CsTicketModel.TICKETID + "} LIKE ?searchText OR {t:"
				+ CsTicketModel.HEADLINE + "} LIKE ?searchText OR {e:" + CsTicketEventModel.TEXT + "} LIKE ?searchText ORDER BY {t:"
				+ CsTicketModel.CREATIONTIME + "} DESC";

		final SearchResult<CsTicketModel> resultTickets = getFlexibleSearchService().search(query,
				Collections.singletonMap("searchText", "%" + searchString + "%"));

		return resultTickets.getResult();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CsTicketModel> findTicketsByStringInTicketOrEventAndStates(final String searchString,
			final Set<CsTicketState> states)
	{
		if (searchString == null)
		{
			throw new IllegalArgumentException("searchString must not be null");
		}

		if (states == null || states.isEmpty())
		{
			throw new IllegalArgumentException("states must not be null and not be empty");
		}

		final String query = "SELECT DISTINCT {t:" + CsTicketModel.PK + "}, {t:" + CsTicketModel.CREATIONTIME + "} FROM {"
				+ CsTicketModel._TYPECODE + " AS t LEFT JOIN " + COMMENT_ITEM_RELATION + " AS e2t ON {t:" + CsTicketModel.PK
				+ "}={e2t:" + LinkModel.TARGET + "} LEFT JOIN " + CsTicketEventModel._TYPECODE + " AS e ON {e2t:" + LinkModel.SOURCE
				+ "}={e:" + CsTicketEventModel.PK + "} } WHERE ({t:" + CsTicketModel.TICKETID + "} LIKE ?searchText OR {t:"
				+ CsTicketModel.HEADLINE + "} LIKE ?searchText OR {e:" + CsTicketEventModel.TEXT + "} LIKE ?searchText) AND {t:"
				+ CsTicketModel.STATE + "} IN (?states) " + "ORDER BY {t:" + CsTicketModel.CREATIONTIME + "} DESC";

		final Map<String, Object> params = new HashMap<>(2);
		params.put("searchText", "%" + searchString + "%");
		params.put("states", states);

		final SearchResult<CsTicketModel> resultTickets = getFlexibleSearchService().search(query, params);

		return resultTickets.getResult();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CsTicketEventModel> findTicketEventsByTicket(final CsTicketModel ticket)
	{
		// YTODO: use constant for relation in SL when it will be available
		final String query = "SELECT {e:" + CsTicketEventModel.PK + "}, {c2i:" + LinkModel.REVERSESEQUENCENUMBER + "} " + "FROM {"
				+ CsTicketEventModel._TYPECODE + " AS e " + "JOIN " + CommentsConstants.Relations.COMMENTITEMRELATION + " AS c2i "
				+ "ON {c2i:" + LinkModel.SOURCE + "}={e:" + CsTicketEventModel.PK + "} }" + "WHERE {c2i:" + LinkModel.TARGET
				+ "}=?ticket " + "ORDER BY {c2i:" + LinkModel.REVERSESEQUENCENUMBER + "} ASC";

		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query);
		fQuery.addQueryParameter("ticket", ticket);

		final SearchResult<CsTicketEventModel> result = getFlexibleSearchService().search(fQuery);
		return result.getResult();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CsTicketEventModel> findTicketEventsForCustomerByTicket(final CsTicketModel ticket)
	{
		final String query = "SELECT {ce:" + CsCustomerEventModel.PK + "}, {c2i:" + LinkModel.REVERSESEQUENCENUMBER + "} "
				+ "FROM {" + CsCustomerEventModel._TYPECODE + " AS ce " + "JOIN " + CommentsConstants.Relations.COMMENTITEMRELATION
				+ " AS c2i " + "ON {c2i:" + LinkModel.SOURCE + "}={ce:" + CsCustomerEventModel.PK + "} AND {ce:"
				+ CsCustomerEventModel.INTERVENTIONTYPE + "} != ?interventionType }" + "WHERE {c2i:" + LinkModel.TARGET + "}=?ticket "
				+ "ORDER BY {c2i:" + LinkModel.REVERSESEQUENCENUMBER + "} ASC";

		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query);
		fQuery.addQueryParameter("interventionType", CsInterventionType.PRIVATE);
		fQuery.addQueryParameter("ticket", ticket);

		final SearchResult<CsTicketEventModel> result = getFlexibleSearchService().search(fQuery);
		return result.getResult();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CsTicketModel> findTicketsByAgent(final EmployeeModel agent)
	{
		if (agent == null)
		{
			throw new IllegalArgumentException("agent must not be null");
		}

		final SearchResult<CsTicketModel> result = getFlexibleSearchService()
				.search(
						"SELECT {pk} FROM {" + CsTicketModel._TYPECODE + "} WHERE {" + CsTicketModel.ASSIGNEDAGENT
								+ "} = ?agent ORDER BY {" + CsTicketModel.CREATIONTIME + "} DESC",
						Collections.singletonMap("agent", agent));
		return result.getResult();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CsTicketModel> findTicketsByAgentGroup(final CsAgentGroupModel agentGroup)
	{
		if (agentGroup == null)
		{
			throw new IllegalArgumentException("agentGroup must not be null");
		}

		final SearchResult<CsTicketModel> result = getFlexibleSearchService()
				.search(
						"SELECT {pk} FROM {" + CsTicketModel._TYPECODE + "} WHERE {" + CsTicketModel.ASSIGNEDGROUP
								+ "} = ?group ORDER BY {" + CsTicketModel.CREATIONTIME + "} DESC",
						Collections.singletonMap("group", agentGroup));
		return result.getResult();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CsTicketModel> findTicketsByCategory(final CsTicketCategory... category)
	{
		if (category == null || category.length == 0)
		{
			throw new IllegalArgumentException("category must not be null or empty");
		}

		final SearchResult<CsTicketModel> result = getFlexibleSearchService()
				.search(
						"SELECT {pk} FROM {" + CsTicketModel._TYPECODE + "} WHERE {" + CsTicketModel.CATEGORY
								+ "} IN (?category) ORDER BY {" + CsTicketModel.CREATIONTIME + "} DESC",
						Collections.singletonMap("category", Arrays.asList(category)));
		return result.getResult();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CsTicketModel> findTicketsByCustomer(final UserModel customer)
	{
		if (customer == null)
		{
			throw new IllegalArgumentException("customer must not be null");
		}

		final SearchResult<CsTicketModel> result = getFlexibleSearchService()
				.search(
						"SELECT {pk} FROM {" + CsTicketModel._TYPECODE + "} WHERE {" + CsTicketModel.CUSTOMER
								+ "} = ?customer ORDER BY {" + CsTicketModel.CREATIONTIME + "} DESC",
						Collections.singletonMap("customer", customer));
		return result.getResult();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CsTicketModel> findTicketsById(final String ticketId)
	{
		if (ticketId == null)
		{
			throw new IllegalArgumentException("ticketId must not be null");
		}

		final SearchResult<CsTicketModel> result = getFlexibleSearchService().search(
				"SELECT {pk} FROM {" + CsTicketModel._TYPECODE + "} WHERE {" + CsTicketModel.TICKETID + "} = ?id",
				Collections.singletonMap("id", ticketId));

		return result.getResult();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CsTicketModel> findTicketsByOrder(final OrderModel order)
	{
		if (order == null)
		{
			throw new IllegalArgumentException("order must not be null");
		}

		final SearchResult<CsTicketModel> result = getFlexibleSearchService().search("SELECT {pk} FROM {" + CsTicketModel._TYPECODE
				+ "} WHERE {" + CsTicketModel.ORDER + "} = ?order ORDER BY {" + CsTicketModel.CREATIONTIME + "} DESC",
				Collections.singletonMap("order", order));

		return result.getResult();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CsTicketModel> findTicketsByPriority(final CsTicketPriority... priority)
	{
		if (priority == null || priority.length == 0)
		{
			throw new IllegalArgumentException("priority must not be null or empty");
		}

		final SearchResult<CsTicketModel> result = getFlexibleSearchService()
				.search(
						"SELECT {pk} FROM {" + CsTicketModel._TYPECODE + "} WHERE {" + CsTicketModel.PRIORITY
								+ "} IN (?priority) ORDER BY {" + CsTicketModel.CREATIONTIME + "} DESC",
						Collections.singletonMap("priority", Arrays.asList(priority)));
		return result.getResult();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CsTicketModel> findTicketsByResolutionType(final CsResolutionType... resolutionType)
	{
		if (resolutionType == null || resolutionType.length == 0)
		{
			throw new IllegalArgumentException("resolutionType must not be null or empty");
		}

		final String query = "SELECT {t:" + CsTicketModel.PK + "} FROM {" + CsTicketModel._TYPECODE + " AS t JOIN "
				+ CsTicketResolutionEventModel._TYPECODE + " AS r ON {t:" + CsTicketModel.RESOLUTION + "}={r:"
				+ CsTicketResolutionEventModel.PK + "}} WHERE {r:" + CsTicketResolutionEventModel.RESOLUTIONTYPE
				+ "} IN (?resolutionType) ORDER BY {" + CsTicketModel.CREATIONTIME + "} DESC";

		final SearchResult<CsTicketModel> result = getFlexibleSearchService().search(query,
				Collections.singletonMap("resolutionType", Arrays.asList(resolutionType)));

		return result.getResult();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CsTicketModel> findTicketsByState(final CsTicketState... state)
	{
		if (state == null || state.length == 0)
		{
			throw new IllegalArgumentException("state must not be null or empty");
		}

		final SearchResult<CsTicketModel> result = getFlexibleSearchService().search("SELECT {pk} FROM {" + CsTicketModel._TYPECODE
				+ "} WHERE {" + CsTicketModel.STATE + "} IN (?state) ORDER BY {" + CsTicketModel.CREATIONTIME + "} DESC",
				Collections.singletonMap("state", Arrays.asList(state)));
		return result.getResult();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CsTicketModel> findTicketsWithNullAgent()
	{
		final SearchResult<CsTicketModel> result = getFlexibleSearchService().search("SELECT {pk} FROM {" + CsTicketModel._TYPECODE
				+ "} WHERE {" + CsTicketModel.ASSIGNEDAGENT + "} IS NULL ORDER BY {" + CsTicketModel.CREATIONTIME + "} DESC");
		return result.getResult();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CsTicketModel> findTicketsWithNullAgentGroup()
	{
		final SearchResult<CsTicketModel> result = getFlexibleSearchService().search("SELECT {pk} FROM {" + CsTicketModel._TYPECODE
				+ "} WHERE {" + CsTicketModel.ASSIGNEDGROUP + "} IS NULL ORDER BY {" + CsTicketModel.CREATIONTIME + "} DESC");
		return result.getResult();
	}

	@Override
	public List<CsTicketModel> findTicketsByCustomerOrderByModifiedTime(final UserModel customer)
	{
		if (customer == null)
		{
			throw new IllegalArgumentException("customer must not be null");
		}

		final SearchResult<CsTicketModel> result = getFlexibleSearchService()
				.search(
						"SELECT {pk} FROM {" + CsTicketModel._TYPECODE + "} WHERE {" + CsTicketModel.CUSTOMER
								+ "} = ?customer ORDER BY {" + CsTicketModel.MODIFIEDTIME + "} DESC",
						Collections.singletonMap("customer", customer));
		return result.getResult();
	}

	@Override
	public SearchPageData<CsTicketModel> findTicketsByCustomerOrderByModifiedTime(final UserModel user,
			final BaseSiteModel baseSite, final PageableData pageableData)
	{
		validateParameterNotNull(user, "Customer must not be null");
		validateParameterNotNull(baseSite, "Store must not be null");

		final Map<String, Object> queryParams = new HashMap<>();
		queryParams.put("user", user);
		queryParams.put("baseSite", baseSite);

		final List<SortQueryData> sortQueries;
		sortQueries = Arrays.asList(
				createSortQueryData("byDate",
						FIND_TICKETS_BY_CUSTOMER_SITE_QUERY_AND_ORDERBY_MODIFIEDTIME + SORT_TICKETS_BY_MODIFIED_DATE),
				createSortQueryData("byTicketId",
						FIND_TICKETS_BY_CUSTOMER_SITE_QUERY_AND_ORDERBY_TICKETID + SORT_TICKETS_BY_TICKETID));

		return getPagedFlexibleSearchService().search(sortQueries, "byDate", queryParams, pageableData);
	}

	protected SortQueryData createSortQueryData(final String sortCode, final String query)
	{
		final SortQueryData sortQueryData = new SortQueryData();
		sortQueryData.setSortCode(sortCode);
		sortQueryData.setQuery(query);
		return sortQueryData;
	}

	/**
	 * @return the pagedFlexibleSearchService
	 */
	public PagedFlexibleSearchService getPagedFlexibleSearchService()
	{
		return pagedFlexibleSearchService;
	}

	/**
	 * @param pagedFlexibleSearchService
	 *           the pagedFlexibleSearchService to set
	 */
	@Required
	public void setPagedFlexibleSearchService(final PagedFlexibleSearchService pagedFlexibleSearchService)
	{
		this.pagedFlexibleSearchService = pagedFlexibleSearchService;
	}
}
