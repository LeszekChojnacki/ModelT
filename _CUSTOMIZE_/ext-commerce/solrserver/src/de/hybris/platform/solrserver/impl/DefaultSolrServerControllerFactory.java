/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.solrserver.impl;

import de.hybris.platform.solrserver.SolrServerController;
import de.hybris.platform.solrserver.SolrServerControllerFactory;
import de.hybris.platform.solrserver.SolrServerRuntimeException;


/**
 * Default implementation of {@link SolrServerControllerFactory}.
 *
 * @deprecated Since 18.08, no longer used.
 */
@Deprecated
public class DefaultSolrServerControllerFactory implements SolrServerControllerFactory
{
	private SolrServerController unixSolrServerController;
	private SolrServerController windowsSolrServerController;

	public SolrServerController getUnixSolrServerController()
	{
		return unixSolrServerController;
	}

	public void setUnixSolrServerController(final SolrServerController unixSolrServerController)
	{
		this.unixSolrServerController = unixSolrServerController;
	}

	public SolrServerController getWindowsSolrServerController()
	{
		return windowsSolrServerController;
	}

	public void setWindowsSolrServerController(final SolrServerController windowsSolrServerController)
	{
		this.windowsSolrServerController = windowsSolrServerController;
	}

	@Override
	public SolrServerController getController()
	{
		if (isUnix())
		{
			return unixSolrServerController;
		}
		else if (isWindows())
		{
			return windowsSolrServerController;
		}

		throw new SolrServerRuntimeException("Factory is able to create controllers only for Windows and Unix systems");
	}

	protected boolean isUnix()
	{
		final String operatingSystem = System.getProperty("os.name");
		return (!(operatingSystem.startsWith("Windows") || operatingSystem.startsWith("OS/2")
				|| operatingSystem.startsWith("NetWare")));
	}

	protected boolean isWindows()
	{
		final String operatingSystem = System.getProperty("os.name");
		return operatingSystem.startsWith("Windows");
	}
}
