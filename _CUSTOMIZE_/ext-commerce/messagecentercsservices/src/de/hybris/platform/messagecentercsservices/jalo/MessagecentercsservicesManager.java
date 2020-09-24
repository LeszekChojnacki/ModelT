/*
 *  
 * [y] hybris Platform
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.messagecentercsservices.jalo;

import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.extension.ExtensionManager;
import de.hybris.platform.messagecentercsservices.constants.MessagecentercsservicesConstants;
import org.apache.log4j.Logger;

@SuppressWarnings("PMD")
public class MessagecentercsservicesManager extends GeneratedMessagecentercsservicesManager
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger( MessagecentercsservicesManager.class.getName() );
	
	public static final MessagecentercsservicesManager getInstance()
	{
		ExtensionManager em = JaloSession.getCurrentSession().getExtensionManager();
		return (MessagecentercsservicesManager) em.getExtension(MessagecentercsservicesConstants.EXTENSIONNAME);
	}
	
}
