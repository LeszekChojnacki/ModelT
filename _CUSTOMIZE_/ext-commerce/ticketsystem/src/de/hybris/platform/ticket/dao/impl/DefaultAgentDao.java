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

import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.ticket.constants.TicketsystemConstants;
import de.hybris.platform.ticket.dao.AgentDao;
import de.hybris.platform.ticket.model.CsAgentGroupModel;

import java.util.Collections;
import java.util.List;


/**
 * The Class DefaultAgentDao. Default implementation of {@link de.hybris.platform.ticket.dao.AgentDao} interface.
 */
public class DefaultAgentDao extends AbstractItemDao implements AgentDao
{

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CsAgentGroupModel> findAgentGroups()
	{
		final SearchResult<CsAgentGroupModel> result = getFlexibleSearchService().search(
				"SELECT {pk} FROM {" + CsAgentGroupModel._TYPECODE + "}");
		return result.getResult();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CsAgentGroupModel> findAgentGroupsByBaseStore(final BaseStoreModel baseStore)
	{
		final String query = "	SELECT {relation:source} " + "	FROM {" + TicketsystemConstants.Relations.CSAGENTGROUP2BASESTORE
				+ " as relation }" + "	WHERE {relation:target} = ?store";
		final SearchResult<CsAgentGroupModel> result = getFlexibleSearchService().search(query,
				Collections.singletonMap("store", baseStore));
		return result.getResult();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<EmployeeModel> findAgents()
	{
		//some indication of which employees are agents?
		final SearchResult<EmployeeModel> result = getFlexibleSearchService().search(
				"SELECT {" + EmployeeModel.PK + "} FROM {" + EmployeeModel._TYPECODE + "}");
		return result.getResult();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<EmployeeModel> findAgentsByBaseStore(final BaseStoreModel baseStore)
	{

		final String query = "	SELECT {relation:source} " + "	FROM {" + TicketsystemConstants.Relations.AGENT2BASESTORE
				+ " as relation }" + "	WHERE {relation:target} = ?store";


		final SearchResult<EmployeeModel> result = getFlexibleSearchService().search(query,
				Collections.singletonMap("store", baseStore));
		return result.getResult();
	}

}
