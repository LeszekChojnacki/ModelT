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

import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.ticket.dao.TicketDao;
import de.hybris.platform.ticket.enums.CsTicketState;
import de.hybris.platform.ticket.model.CsAgentGroupModel;
import de.hybris.platform.ticket.model.CsTicketModel;
import de.hybris.platform.ticket.service.TicketSearchService;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Required;

/**
 * This service class is used for finding tickets
 *
 */
public class DefaultTicketSearchService implements TicketSearchService
{
	private TicketDao ticketDao;

	@Override
	public List<CsTicketModel> searchForTickets(final String searchString)
	{
		if ("".equals(searchString) || searchString == null)
		{
			return Collections.emptyList();
		}

		return ticketDao.findTicketsByStringInTicketOrEvent(searchString);
	}

	@Override
	public List<CsTicketModel> searchForTickets(final String searchString, final Set<CsTicketState> states)
	{
		if ("".equals(searchString) || searchString == null || states == null || states.isEmpty())
		{
			return Collections.emptyList();
		}

		return ticketDao.findTicketsByStringInTicketOrEventAndStates(searchString, states);
	}

	@Override
	public List<CsTicketModel> searchForTickets(final EmployeeModel agent, final CsAgentGroupModel group, final CsTicketState state)
	{
		if ((agent == null) && (group == null) && (state == null))
		{
			return Collections.emptyList();
		}

		return ticketDao.findTicketsByAgentGroupState(agent, group, state);
	}

	@Required
	public void setTicketDao(final TicketDao ticketDao)
	{
		this.ticketDao = ticketDao;
	}
}
