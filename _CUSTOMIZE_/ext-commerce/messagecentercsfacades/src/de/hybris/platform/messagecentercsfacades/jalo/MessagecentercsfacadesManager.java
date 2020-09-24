/*
 *  
 * [y] hybris Platform
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.messagecentercsfacades.jalo;

import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.extension.ExtensionManager;
import de.hybris.platform.messagecentercsfacades.constants.MessagecentercsfacadesConstants;
import org.apache.log4j.Logger;

@SuppressWarnings("PMD")
public class MessagecentercsfacadesManager extends GeneratedMessagecentercsfacadesManager
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger( MessagecentercsfacadesManager.class.getName() );
	
	public static final MessagecentercsfacadesManager getInstance()
	{
		ExtensionManager em = JaloSession.getCurrentSession().getExtensionManager();
		return (MessagecentercsfacadesManager) em.getExtension(MessagecentercsfacadesConstants.EXTENSIONNAME);
	}
	
}
