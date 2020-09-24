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
package de.hybris.platform.wishlist2.jalo;

import de.hybris.platform.core.Registry;
import de.hybris.platform.wishlist2.constants.Wishlist2Constants;

import org.apache.log4j.Logger;



/**
 * This is the extension manager of the Wishlist extension.
 */
public class Wishlist2Manager extends GeneratedWishlist2Manager
{
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(Wishlist2Manager.class.getName());


	/**
	 * Get the valid instance of this manager.
	 * 
	 * @return the current instance of this manager
	 */
	public static Wishlist2Manager getInstance()
	{
		return (Wishlist2Manager) Registry.getCurrentTenant().getJaloConnection().getExtensionManager().getExtension(
				Wishlist2Constants.EXTENSIONNAME);
	}
}
