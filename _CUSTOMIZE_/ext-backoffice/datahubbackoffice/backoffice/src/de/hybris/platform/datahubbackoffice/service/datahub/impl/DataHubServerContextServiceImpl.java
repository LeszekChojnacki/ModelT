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
package de.hybris.platform.datahubbackoffice.service.datahub.impl;

import de.hybris.platform.datahubbackoffice.exception.NoDataHubInstanceAvailableException;
import de.hybris.platform.datahubbackoffice.service.datahub.DataHubNameService;
import de.hybris.platform.datahubbackoffice.service.datahub.DataHubServer;
import de.hybris.platform.datahubbackoffice.service.datahub.DataHubServerAware;
import de.hybris.platform.datahubbackoffice.service.datahub.DataHubServerContextService;
import de.hybris.platform.datahubbackoffice.service.datahub.DataHubServerInfo;

import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Required;

import com.google.common.base.Preconditions;

/**
 * A factory for creating REST clients based on the DataHub instance selected by the user.
 */
public class DataHubServerContextServiceImpl implements DataHubServerAware, DataHubServerContextService
{
	private DataHubServer dataHubServer;
	private Collection<DataHubServer> allServers;
	private DataHubNameService nameService;

	@Override
	public DataHubServer getContextDataHubServer()
	{
		return dataHubServer != null ? dataHubServer : getDefaultServer();
	}

	@Override
	public Collection<DataHubServer> getAllServers()
	{
		if (allServers == null)
		{
			allServers = initializeServers();
		}
		return allServers;
	}

	private Collection<DataHubServer> initializeServers()
	{
		return nameService.getAllServers().stream().map(DataHubServer::new).collect(Collectors.toList());
	}

	private DataHubServer getDefaultServer()
	{
		for (final DataHubServer server : getAllServers())
		{
			if (server.isAccessibleWithTimeout())
			{
				return server;
			}
		}

		throw new NoDataHubInstanceAvailableException();
	}

	/**
	 * Sets DataHub server the user will operate with.
	 *
	 * @param server - instance of {@link DataHubServerInfo}
	 * @throws IllegalArgumentException - when {@link DataHubServerInfo} is null or
	 * {@link DataHubServerInfo#getLocation} method returns null.
	 */
	@Override
	public void setDataHubServer(final DataHubServer server)
	{
		Preconditions.checkArgument(server != null);
		dataHubServer = server;
	}

	/**
	 * Injects DataHub server name service to be used.
	 *
	 * @param service a service implementation to use.
	 */
	@Required
	public void setNameService(final DataHubNameService service)
	{
		nameService = service;
	}
}
