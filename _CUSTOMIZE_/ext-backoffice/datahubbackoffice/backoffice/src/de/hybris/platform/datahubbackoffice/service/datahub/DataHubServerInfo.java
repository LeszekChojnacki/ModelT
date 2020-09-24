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

import java.io.Serializable;
import java.util.Objects;

/**
 * Describes a DataHub instance to be accessed.
 */
public class DataHubServerInfo implements Serializable
{
	private final String name;
	private final String location;
	private final String userName;
	private final String password;

	/**
	 * Instantiates a configuration entry
	 *
	 * @param name name of the DataHub instance. It will be used to refer to this instance everywhere in the user interface.
	 * @param location location of the DataHub instance. It will be used to access and exchange data with the DataHub server.
	 */
	public DataHubServerInfo(final String name, final String location, final String userName, final String password)
	{
		this.name = name;
		this.location = location;
		this.userName = userName;
		this.password = password;
	}

	/**
	 * Retrieves name of this configuration entry.
	 *
	 * @return name of the DataHub server configuration
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Retrieves location of the DataHub server described by this configuration entry.
	 *
	 * @return some URI that can be interpreted by the program in order to access the DataHub server.
	 */
	public String getLocation()
	{
		return location;
	}

	public String getUserName()
	{
		return userName;
	}

	public String getPassword()
	{
		return password;
	}

	@Override
	public int hashCode()
	{
		return name != null ? name.hashCode() : -1;
	}

	@Override
	public boolean equals(final Object obj)
	{
		return obj instanceof DataHubServerInfo && Objects.equals(name, ((DataHubServerInfo) obj).name);
	}

	@Override
	public String toString()
	{
		return name + " @ " + location;
	}
}
