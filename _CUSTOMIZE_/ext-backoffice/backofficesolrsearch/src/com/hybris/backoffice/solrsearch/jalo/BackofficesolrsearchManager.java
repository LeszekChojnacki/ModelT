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
package com.hybris.backoffice.solrsearch.jalo;

import de.hybris.platform.core.Registry;

import com.hybris.backoffice.solrsearch.constants.BackofficesolrsearchConstants;


public class BackofficesolrsearchManager extends GeneratedBackofficesolrsearchManager
{
	/**
	 * Get the valid instance of this manager.
	 *
	 * @return the current instance of this manager
	 */
	public static BackofficesolrsearchManager getInstance()
	{
		return (BackofficesolrsearchManager) Registry.getCurrentTenant().getJaloConnection().getExtensionManager()
				.getExtension(BackofficesolrsearchConstants.EXTENSIONNAME);
	}
}
