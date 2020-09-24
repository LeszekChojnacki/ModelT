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

package de.hybris.platform.configurablebundleservices.jalo;

import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.extension.ExtensionManager;
import de.hybris.platform.configurablebundleservices.constants.ConfigurableBundleServicesConstants;
import de.hybris.platform.configurablebundleservices.jalo.GeneratedConfigurableBundleServicesManager;

import org.apache.log4j.Logger;


@SuppressWarnings("PMD")
public class ConfigurableBundleServicesManager extends GeneratedConfigurableBundleServicesManager
{
	@SuppressWarnings("unused")
	private static Logger LOG = Logger.getLogger(ConfigurableBundleServicesManager.class.getName());

	public static ConfigurableBundleServicesManager getInstance()
	{
		final ExtensionManager em = JaloSession.getCurrentSession().getExtensionManager();
		return (ConfigurableBundleServicesManager) em.getExtension(ConfigurableBundleServicesConstants.EXTENSIONNAME);
	}

}
