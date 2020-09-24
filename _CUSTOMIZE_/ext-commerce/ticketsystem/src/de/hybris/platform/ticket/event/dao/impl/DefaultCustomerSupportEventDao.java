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
package de.hybris.platform.ticket.event.dao.impl;

import de.hybris.platform.servicelayer.time.TimeService;
import de.hybris.platform.ticket.enums.EventType;
import de.hybris.platform.ticket.event.dao.CustomerSupportEventDao;
import de.hybris.platform.commerceservices.search.dao.impl.DefaultPagedGenericDao;
import de.hybris.platform.commerceservices.search.flexiblesearch.data.SortQueryData;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hybris.platform.ticketsystem.events.model.SessionEventModel;
import de.hybris.platform.ticketsystem.events.model.SessionStartEventModel;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * @see CustomerSupportEventDao
 */
public class DefaultCustomerSupportEventDao extends DefaultPagedGenericDao<SessionEventModel> implements CustomerSupportEventDao
{
	private static final String CURRENTDATE = "currentDate";
	private static final String LOGINDISABLED_PARAMETER = "loginDisabled";

	private static final String SEARCH_ALL =
			"SELECT {c:" + SessionEventModel.PK + "} FROM {" + SessionEventModel._TYPECODE + " AS c}";

	private static final String SEARCH_STARTED_SESSIONS =
		"SELECT {c:" + SessionEventModel.PK + "} FROM {"+ SessionStartEventModel._TYPECODE + " AS c}";

	private static final String SEARCH_CUSTOMERS_BY_STARTED_SESSIONS =
		"SELECT {c:" + SessionStartEventModel.CUSTOMER+ "},MAX({c:" + SessionEventModel.CREATIONTIME + "})"
		+" FROM {" + SessionStartEventModel._TYPECODE + " AS c}";

	private static final String SEARCH_CUSTOMERS_BY_STARTED_SESSIONS_FILTER_DISABLED_ACC =
		"SELECT  {c:" + SessionStartEventModel.CUSTOMER + "} , MAX({c:" + SessionEventModel.CREATIONTIME + "})"
		+ " FROM {" + SessionStartEventModel._TYPECODE + " AS c"
		+ " JOIN " + CustomerModel._TYPECODE + " as cu ON {c:" + SessionStartEventModel.CUSTOMER + "} = {cu:" + CustomerModel.PK + "}} ";

	private static final String SORT_SESSIONS_BY_DATE_ASC_CUSTOMER_GROUPING = " GROUP BY {c:" + SessionStartEventModel.CUSTOMER
			+ "} ORDER BY MAX({" + SessionEventModel.CREATIONTIME + "}) ASC";

	private static final String SORT_SESSIONS_BY_DATE_DESC_CUSTOMER_GROUPING = " GROUP BY {c:" + SessionStartEventModel.CUSTOMER
			+ "} ORDER BY MAX({" + SessionEventModel.CREATIONTIME + "}) DESC";

	private static final String SORT_SESSIONS_BY_DATE_ASC = " ORDER BY {" + SessionEventModel.CREATIONTIME + "} ASC";

	private static final String SORT_SESSIONS_BY_DATE_DESC = " ORDER BY {" + SessionEventModel.CREATIONTIME + "} DESC";

	private static final String START_DATE_PARAMETER = "startDate";
	private static final String END_DATE_PARAMETER = "endDate";
	private static final String BEFORE_DATE_PARAMETER = "beforeDate";
	private static final String AGENT_PARAMETER = "agent";

	private static final String SORT_BY_DATE_ASC = "byDateAsc";
	private static final String SORT_BY_DATE_DESC = "byDateDesc";

	private static final String WHERE_CLAUSE_KEY = "whereClause";
	private static final String WHERE_CLAUSE_PARAMETERS_KEY = "queryParam";
	private TimeService timeService;

	public DefaultCustomerSupportEventDao(final String typeCode)
	{
		super(typeCode);
	}

	/**
	 * @see CustomerSupportEventDao#findAllCustomersByEventsAndAgent(EmployeeModel, EventType, Date, Date, PageableData, int)
	 */
	@Override
	public SearchPageData<SessionEventModel> findAllEventsByAgent(final EmployeeModel agent, final EventType eventType,
			final Date startDate, final Date endDate, final PageableData pageableData, final int limit)
	{
		final List<SortQueryData> sortQueries;

		final Map preparedResultsMap = validateAndPrepareWhereClause(agent, startDate, endDate, limit, true);

		String queryToExecute;

		if (eventType.equals(EventType.START_SESSION_EVENT))
		{
			queryToExecute = SEARCH_STARTED_SESSIONS;
		}
		else
		{
			queryToExecute = SEARCH_ALL;
		}

		sortQueries = Arrays.asList(createSortQueryData(SORT_BY_DATE_ASC, createQuery(queryToExecute,
				preparedResultsMap.get(WHERE_CLAUSE_KEY).toString(), SORT_SESSIONS_BY_DATE_ASC)),

				createSortQueryData(SORT_BY_DATE_DESC, createQuery(queryToExecute,
						preparedResultsMap.get(WHERE_CLAUSE_KEY).toString(), SORT_SESSIONS_BY_DATE_DESC)));

		return getPagedFlexibleSearchService().search(sortQueries, SORT_BY_DATE_DESC,
				(Map) preparedResultsMap.get(WHERE_CLAUSE_PARAMETERS_KEY), pageableData);
	}

	/**
	 * @deprecated since 6.6
	 */
	@Override
	@Deprecated
	public <T extends CustomerModel> SearchPageData<T> findAllCustomersByEventsAndAgent(final EmployeeModel agent,
			final EventType eventType, final Date startDate, final Date endDate, final PageableData pageableData, final int limit)
	{
		return findAllCustomersByEventsAndAgent(agent, eventType, startDate, endDate, pageableData, limit, true);
	}

	@Override
	public <T extends CustomerModel> SearchPageData<T> findAllCustomersByEventsAndAgent(final EmployeeModel agent, final EventType eventType,
		final Date startDate, final Date endDate, final PageableData pageableData, final int limit, final boolean includeDisabledAccounts)
	{
		final List<SortQueryData> sortQueries;

		final Map preparedResultsMap = validateAndPrepareWhereClause(agent, startDate, endDate, limit, includeDisabledAccounts);

		if (eventType.equals(EventType.START_SESSION_EVENT))
		{
			String query;
			if (includeDisabledAccounts)
			{
				query = SEARCH_CUSTOMERS_BY_STARTED_SESSIONS;
		    } else {
				query = SEARCH_CUSTOMERS_BY_STARTED_SESSIONS_FILTER_DISABLED_ACC;
			}

			sortQueries = Arrays.asList(createSortQueryData(SORT_BY_DATE_ASC,
				createQuery(query, preparedResultsMap.get(WHERE_CLAUSE_KEY).toString(),
						SORT_SESSIONS_BY_DATE_ASC_CUSTOMER_GROUPING)),

				createSortQueryData(SORT_BY_DATE_DESC,
						createQuery(query, preparedResultsMap.get(WHERE_CLAUSE_KEY).toString(),
								SORT_SESSIONS_BY_DATE_DESC_CUSTOMER_GROUPING)));

			return getPagedFlexibleSearchService().search(sortQueries, SORT_BY_DATE_DESC,
					(Map) preparedResultsMap.get(WHERE_CLAUSE_PARAMETERS_KEY), pageableData);
		}
		return new SearchPageData<T>();
	}

	protected Map<String, Object> validateAndPrepareWhereClause(final EmployeeModel agent, final Date startDate,
			final Date endDate, final int limit, final boolean includeDisabledAcc)
	{
		final Map<String, Object> preparedResultsMap = new HashMap<String, Object>();

		final List<String> whereClause = new ArrayList<String>();

		final Map<String, Object> queryParameters = new HashMap<String, Object>();

		if (limit < 1)
		{
			throw new IllegalArgumentException("Query limit shouldn't be less than 1");
		}

		if (null != agent)
		{
			whereClause.add("{" + SessionEventModel.AGENT + "}=?agent");
			queryParameters.put(AGENT_PARAMETER, agent);
		}

		if (null != startDate && null != endDate && startDate.before(endDate))
		{
			whereClause.add("{" + SessionEventModel.CREATIONTIME + "} between ?startDate AND ?endDate");
			queryParameters.put(START_DATE_PARAMETER, startDate);
			queryParameters.put(END_DATE_PARAMETER, endDate);
		}
		if (!includeDisabledAcc)
		{
			whereClause.add(" {cu:" + CustomerModel.LOGINDISABLED + "} = ?" + LOGINDISABLED_PARAMETER
					+ " AND ({cu:" + CustomerModel.DEACTIVATIONDATE + "} IS NULL"
					+ " OR {cu:" + CustomerModel.DEACTIVATIONDATE + "} > ?" + CURRENTDATE + ") ");
			queryParameters.put(CURRENTDATE, getTimeService().getCurrentTime());
			queryParameters.put(LOGINDISABLED_PARAMETER, Boolean.FALSE);
		}

		String whereClauseStr = "";

		if (!whereClause.isEmpty())
		{
			whereClauseStr = " WHERE " + StringUtils.join(whereClause, " AND ");
		}

		preparedResultsMap.put(WHERE_CLAUSE_KEY, whereClauseStr);
		preparedResultsMap.put(WHERE_CLAUSE_PARAMETERS_KEY, queryParameters);

		return preparedResultsMap;
	}

	@Override
	public List<SessionEventModel> findAllEventsBeforeDate(final EventType eventType, final Date beforeDate)
	{
		FlexibleSearchQuery query;

		final String whereClauseStr = " WHERE {" + SessionEventModel.CREATIONTIME + "} < ?beforeDate";

		query = new FlexibleSearchQuery(createQuery(
				null != eventType && eventType.equals(EventType.START_SESSION_EVENT) ? SEARCH_STARTED_SESSIONS : SEARCH_ALL,
				whereClauseStr), Collections.singletonMap(BEFORE_DATE_PARAMETER, beforeDate));

		final PageableData pageableData = new PageableData();
		pageableData.setPageSize(500);

		final SearchPageData<SessionEventModel> searchPageData = getPagedFlexibleSearchService().search(query, pageableData);

		return searchPageData.getResults();
	}

	protected String createQuery(final String... queryClauses)
	{
		final StringBuilder queryBuilder = new StringBuilder();

		for (final String queryClause : queryClauses)
		{
			queryBuilder.append(queryClause);
		}

		return queryBuilder.toString();
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
}
