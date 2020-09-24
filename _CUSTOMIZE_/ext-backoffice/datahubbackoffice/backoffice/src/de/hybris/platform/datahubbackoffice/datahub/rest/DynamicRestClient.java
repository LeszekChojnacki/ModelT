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
package de.hybris.platform.datahubbackoffice.datahub.rest;

import de.hybris.platform.datahubbackoffice.service.datahub.DataHubServer;


/**
 * A REST client, which can connect to different servers.
 */
public interface DynamicRestClient
{
	/**
	 * Specifies DataHub server to use for uploading data. The server will be used until another server is not specified
	 * or this client is instructed to use the default context server.
	 * 
	 * @param server
	 *           a server, against which the REST calls should be executed.
	 */
	void useServer(DataHubServer server);

	/**
	 * Instructs this client to stop using a server explicitly specified previously and to start using the server
	 * selected in the DataHub UI.
	 * 
	 * @see #useServer(DataHubServer)
	 */
	void useContextServer();

	/**
	 * Returns URL of the API root for the current server to use.
	 * 
	 * @return URL string for the REST API base. All request should be constructed relative to this base URL.
	 */
	String getBaseApiUrl();
}
