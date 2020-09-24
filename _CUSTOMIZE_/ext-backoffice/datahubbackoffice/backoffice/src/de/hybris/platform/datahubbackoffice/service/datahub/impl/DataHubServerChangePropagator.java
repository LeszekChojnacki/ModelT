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

import de.hybris.platform.datahubbackoffice.service.datahub.DataHubServer;
import de.hybris.platform.datahubbackoffice.service.datahub.DataHubServerAware;

import java.util.Collection;

import javax.validation.constraints.NotNull;

/**
 * Propagates DataHub server context to multiple Spring beans.
 */
public class DataHubServerChangePropagator implements DataHubServerAware
{
	private Collection<DataHubServerAware> services;

	@Override
	public void setDataHubServer(final DataHubServer s)
	{
		if (services != null)
		{
			for (final DataHubServerAware service : services)
			{
				service.setDataHubServer(s);
			}
		}
	}

	public void setServices(@NotNull final Collection<DataHubServerAware> c)
	{
		services = c;
	}
}
