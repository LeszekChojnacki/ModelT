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

import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.ticket.model.CsAgentGroupModel;

import java.util.List;


/**
 * Dao for finding agets and agent groups.
 * 
 * @spring.bean agentDao
 */
public interface AgentDao
{

	/**
	 * Find all agent groups.
	 * 
	 * @return the list of found <code>CsAgentGroupModel</code> objects.
	 */
	List<CsAgentGroupModel> findAgentGroups();

	/**
	 * Find agent groups by base store.
	 * 
	 * @param baseStore
	 *           the base store for which search will be executed
	 * @return the list of found <code>CsAgentGroupModel</code> objects.
	 */
	List<CsAgentGroupModel> findAgentGroupsByBaseStore(BaseStoreModel baseStore);

	/**
	 * Find all agents.
	 * 
	 * @return the list of found <code>EmployeeModel</code> objects.
	 */
	List<EmployeeModel> findAgents();

	/**
	 * Find agents by base store.
	 * 
	 * @param baseStore
	 *           the base store for which search will be executed
	 * @return the list of found <code>EmployeeModel</code> objects.
	 */
	List<EmployeeModel> findAgentsByBaseStore(BaseStoreModel baseStore);
}
