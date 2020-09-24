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
package de.hybris.platform.datahubbackoffice.service;

import de.hybris.platform.datahubbackoffice.daos.DataHubInstanceDAO;
import de.hybris.platform.datahubbackoffice.model.DataHubInstanceModelModel;
import de.hybris.platform.datahubbackoffice.service.datahub.DataHubNameService;
import de.hybris.platform.datahubbackoffice.service.datahub.DataHubServerInfo;
import de.hybris.platform.datahubbackoffice.service.datahub.UserContext;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Value;

/**
 * A service for managing DataHub server instance configurations that can be accessed from the user interface.
 */
public class TypeSystemBasedDataHubNameService implements DataHubNameService
{
	private final Map<String, DataHubServerInfo> servers;
	private DataHubInstanceDAO dataHubInstanceDAO;


	private UserContext userContext;

	@Value("${datahub.backoffice.rest.client.username.developer}")
	private String developerClientUsername;
	@Value("${datahub.backoffice.rest.client.password.developer}")
	private String developerClientPassword;
	@Value("${datahub.backoffice.rest.client.username.admin}")
	private String adminClientUsername;
	@Value("${datahub.backoffice.rest.client.password.admin}")
	private String adminClientPassword;

	/**
	 * Creates en empty map of servers.
	 */
	public TypeSystemBasedDataHubNameService()
	{
		this.servers = new LinkedHashMap<>();
	}

	@Override
	public Collection<DataHubServerInfo> getAllServers()
	{
		dataHubInstanceDAO.findDataHubInstances().forEach(this::addServerConfiguration);
		return Collections.unmodifiableCollection(servers.values());
	}

	@Override
	public DataHubServerInfo getServer(final String instanceName)
	{
		if (servers.isEmpty())
		{
			getAllServers();
		}
		return servers.get(instanceName);
	}

	private void addServerConfiguration(final DataHubInstanceModelModel dataHubInstanceModel)
	{
		final String clientUsername = userContext.isUserDataHubAdmin() ? adminClientUsername : developerClientUsername;
		final String clientPassword = userContext.isUserDataHubAdmin() ? adminClientPassword : developerClientPassword;

		servers.put(dataHubInstanceModel.getInstanceName(), new DataHubServerInfo(
				dataHubInstanceModel.getInstanceName(),
				dataHubInstanceModel.getInstanceLocation(),
				clientUsername,
				clientPassword));
	}

	@Required
	public void setDataHubInstanceDAO(final DataHubInstanceDAO dataHubInstanceDAO)
	{
		this.dataHubInstanceDAO = dataHubInstanceDAO;
	}

	@Required
	public void setUserContext(final UserContext userContext)
	{
		this.userContext = userContext;
	}
}
