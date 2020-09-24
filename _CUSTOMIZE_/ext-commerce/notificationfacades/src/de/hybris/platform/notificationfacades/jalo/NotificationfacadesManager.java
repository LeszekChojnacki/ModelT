/*
 *  
 * [y] hybris Platform
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.notificationfacades.jalo;

import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.extension.ExtensionManager;
import de.hybris.platform.notificationfacades.constants.NotificationfacadesConstants;
import org.apache.log4j.Logger;

@SuppressWarnings("PMD")
public class NotificationfacadesManager extends GeneratedNotificationfacadesManager
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger( NotificationfacadesManager.class.getName() );
	
	public static final NotificationfacadesManager getInstance()
	{
		ExtensionManager em = JaloSession.getCurrentSession().getExtensionManager();
		return (NotificationfacadesManager) em.getExtension(NotificationfacadesConstants.EXTENSIONNAME);
	}
	
}
