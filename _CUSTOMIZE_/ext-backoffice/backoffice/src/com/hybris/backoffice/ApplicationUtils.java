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
package com.hybris.backoffice;

import de.hybris.platform.core.Registry;
import de.hybris.platform.util.RedeployUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Global level Backoffice application utilities methods
 */
public class ApplicationUtils
{

	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationUtils.class);

	private ApplicationUtils()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Checks whether platform is ready for business process engine events
	 *
	 * @return true if events can be processed
	 */
	public static boolean isPlatformReady()
	{
		try
		{
			final boolean notShuttingDown = !RedeployUtilities.isShutdownInProgress();
			final boolean isSystemInitialized = Registry.hasCurrentTenant()
					&& Registry.getCurrentTenant().getJaloConnection().isSystemInitialized();
			return notShuttingDown && isSystemInitialized;
		}
		catch (final IllegalStateException e)
		{
			LOGGER.debug("Platform check failed", e);
			return false;
		}
	}

}
