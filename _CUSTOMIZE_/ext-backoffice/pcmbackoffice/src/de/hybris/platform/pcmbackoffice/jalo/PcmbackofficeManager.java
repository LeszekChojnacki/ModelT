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
package de.hybris.platform.pcmbackoffice.jalo;

import de.hybris.platform.core.Registry;
import de.hybris.platform.pcmbackoffice.constants.PcmbackofficeConstants;

import org.apache.log4j.Logger;



/**
 * This is the extension manager of the PCM Backoffice extension.
 */
public class PcmbackofficeManager extends GeneratedPcmbackofficeManager
{
	/** Edit the local|project.properties to change logging behavior (properties 'log4j.*'). */
	private static final Logger LOG = Logger.getLogger(PcmbackofficeManager.class.getName());

	/**
	 * Never call the constructor of any manager directly, call getInstance() You can place your business logic here -
	 * like registering a jalo session listener. Each manager is created once for each tenant.
	 */
	public PcmbackofficeManager()
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("constructor of PcmbackofficeManager called.");
		}
	}

	/**
	 * Get the valid instance of this manager.
	 *
	 * @return the current instance of this manager
	 */
	public static PcmbackofficeManager getInstance()
	{
		return (PcmbackofficeManager) Registry.getCurrentTenant().getJaloConnection().getExtensionManager()
				.getExtension(PcmbackofficeConstants.EXTENSIONNAME);
	}

	/**
	 * Use this method to do some basic work only ONCE in the lifetime of a tenant resp. "deployment". This method is
	 * called after manager creation (for example within startup of a tenant). Note that if you have more than one tenant
	 * you have a manager instance for each tenant.
	 *
	 * @deprecated since 6.5
	 */
	@Deprecated
	@Override
	public void init()
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("init() of YbackofficeManager called. " + getTenant().getTenantID());
		}
	}

	/**
	 * Use this method as a callback when the manager instance is being destroyed (this happens before system
	 * initialization, at redeployment or if you shutdown your VM). Note that if you have more than one tenant you have a
	 * manager instance for each tenant.
	 *
	 * @deprecated since 6.5
	 */
	@Deprecated
	@Override
	public void destroy()
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("destroy() of YbackofficeManager called, current tenant: " + getTenant().getTenantID());
		}
	}
}
