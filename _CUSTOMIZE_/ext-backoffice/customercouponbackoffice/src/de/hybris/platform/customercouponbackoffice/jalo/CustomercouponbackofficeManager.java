/*
 *  
 * [y] hybris Platform
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.customercouponbackoffice.jalo;

import de.hybris.platform.customercouponbackoffice.constants.CustomercouponbackofficeConstants;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.extension.ExtensionManager;
import org.apache.log4j.Logger;

@SuppressWarnings("PMD")
public class CustomercouponbackofficeManager extends GeneratedCustomercouponbackofficeManager
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger( CustomercouponbackofficeManager.class.getName() );
	
	public static final CustomercouponbackofficeManager getInstance()
	{
		ExtensionManager em = JaloSession.getCurrentSession().getExtensionManager();
		return (CustomercouponbackofficeManager) em.getExtension(CustomercouponbackofficeConstants.EXTENSIONNAME);
	}
	
}
