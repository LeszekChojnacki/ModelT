/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.datahubbackoffice.service.datahub;

import java.util.Collection;

/**
 * A service for managing DataHub server instance configurations that can be accessed from the user interface.
 */
public interface DataHubNameService
{
	/**
	 * Retrieves all DataHub instances configured in the system.
	 * @return a collection of DataHub servers or an empty collection, if there is no a single DataHub configured
	 * yet.
	 */
	Collection<DataHubServerInfo> getAllServers();

	/**
	 * Retrieves a specific DataHub instance configuration.
	 * @param name name of the DataHub server configuration to retrieve.
	 * @return the corresponding server information or <code>null</code>, if such configuration does not exist.
	 */
	DataHubServerInfo getServer(String name);
}
