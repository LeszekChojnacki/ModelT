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
package de.hybris.platform.permissionsfacades.jalo;

import de.hybris.platform.core.Registry;
import de.hybris.platform.permissionsfacades.constants.PermissionsfacadesConstants;



/**
 * This is the extension manager of the Permissionsfacades extension.
 */
public class PermissionsfacadesManager extends GeneratedPermissionsfacadesManager
{
	/**
	 * Get the valid instance of this manager.
	 *
	 * @return the current instance of this manager
	 */
	public static PermissionsfacadesManager getInstance()
	{
		return (PermissionsfacadesManager) Registry.getCurrentTenant().getJaloConnection().getExtensionManager()
				.getExtension(PermissionsfacadesConstants.EXTENSIONNAME);
	}
}
