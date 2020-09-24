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
package de.hybris.platform.b2b.jalo;

import de.hybris.platform.b2b.constants.B2BConstants;
import de.hybris.platform.core.Registry;


/**
 * This is the extension manager of the B2bcommerce extension.
 */
public class B2BCommerceManager extends GeneratedB2BCommerceManager
{
	/**
	 * Get the valid instance of this manager.
	 *
	 * @return the current instance of this manager
	 */
	public static B2BCommerceManager getInstance()
	{
		return (B2BCommerceManager) Registry.getCurrentTenant().getJaloConnection().getExtensionManager()
				.getExtension(B2BConstants.EXTENSIONNAME);
	}
}
