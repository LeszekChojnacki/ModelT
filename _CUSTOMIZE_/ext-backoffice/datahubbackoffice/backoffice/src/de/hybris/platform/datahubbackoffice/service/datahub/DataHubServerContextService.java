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
 * A service containing context of the DataHub server the user is working with now.
 */
public interface DataHubServerContextService
{
	/**
	 * Returns the DataHub server the user is working with.
	 * @return DataHub server currently being in the context of the user operations.
	 */
	DataHubServer getContextDataHubServer();

	/**
	 * Retrieves all DataHub Servers available.
	 * @return a collection of all servers available for the user to choose or an empty collection, if no servers configured.
	 */
	Collection<DataHubServer> getAllServers();
}
