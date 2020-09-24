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
package com.hybris.samlssobackoffice.jalo;

import de.hybris.platform.core.Registry;

import com.hybris.samlssobackoffice.constants.SamlssobackofficeConstants;



/**
 * This is the extension manager of the Ybackoffice extension.
 */
public class SamlssobackofficeManager extends GeneratedSamlssobackofficeManager
{
	/**
	 * Get the valid instance of this manager.
	 *
	 * @return the current instance of this manager
	 */
	public static SamlssobackofficeManager getInstance()
	{
		return (SamlssobackofficeManager) Registry.getCurrentTenant().getJaloConnection().getExtensionManager()
				.getExtension(SamlssobackofficeConstants.EXTENSIONNAME);
		}

}
