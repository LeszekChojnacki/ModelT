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
package de.hybris.platform.ticket.event.dao;

import de.hybris.platform.ticket.enums.EventType;
import de.hybris.platform.ticketsystem.events.model.SessionEventModel;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.EmployeeModel;

import java.util.Date;
import java.util.List;


/**
 * DAO for retrieving sc-events
 */
public interface CustomerSupportEventDao
{
	/**
	 * Search for agent-specific events
	 *
	 * @param agent
	 *           - can be empty or null
	 * @param eventType
	 *           event type to search for
	 * @param pageableData
	 *           paging and sorting information
	 * @param limit
	 *           limit of cs session events returned
	 * @return search page data for CS Session Event Model
	 */
	SearchPageData<SessionEventModel> findAllEventsByAgent(EmployeeModel agent, EventType eventType, Date startDate,
		Date endDate, PageableData pageableData, int limit);

	/**
	 * Searching customers based on event type
	 *
	 * @param agent
	 *           - can be empty or null
	 *
	 * @param eventType
	 *           event type to search for
	 * @param pageableData
	 *           paging and sorting information
	 * @param limit
	 *           limit of cs session events returned
	 * @return search page data for CS Customer Model
	 * @deprecated since 6.7
	 */
	@Deprecated
	<T extends CustomerModel> SearchPageData<T> findAllCustomersByEventsAndAgent(final EmployeeModel agent, final EventType eventType,
		 final Date startDate, final Date endDate, final PageableData pageableData, final int limit);

	/**
	 * Searching customers based on event type
	 *
	 * @param agent
	 *           - can be empty or null
	 *
	 * @param eventType
	 *           event type to search for
	 * @param pageableData
	 *           paging and sorting information
	 * @param limit
	 *           limit of cs session events returned
	 * @param includeDisabledAccounts
	 * 			include disabled account or no
	 * @return search page data for CS Customer Model
	 */
	<T extends CustomerModel> SearchPageData<T> findAllCustomersByEventsAndAgent(final EmployeeModel agent, final EventType eventType,
		 final Date startDate, final Date endDate, final PageableData pageableData, final int limit, final boolean includeDisabledAccounts);

	/**
	 * Get all events before specific date
	 *
	 * @param eventType
	 *           event type to retrieve events for
	 *
	 * @param beforeDate
	 *           the date to be used as end date for events retrival
	 */
	List<SessionEventModel> findAllEventsBeforeDate(EventType eventType, Date beforeDate);
}
