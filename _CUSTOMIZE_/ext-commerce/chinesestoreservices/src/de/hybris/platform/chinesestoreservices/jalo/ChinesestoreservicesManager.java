/*
 *  
 * [y] hybris Platform
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.chinesestoreservices.jalo;

import de.hybris.platform.chinesestoreservices.constants.ChinesestoreservicesConstants;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.extension.ExtensionManager;
import org.apache.log4j.Logger;

@SuppressWarnings("PMD")
public class ChinesestoreservicesManager extends GeneratedChinesestoreservicesManager
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger( ChinesestoreservicesManager.class.getName() );
	
	public static final ChinesestoreservicesManager getInstance()
	{
		ExtensionManager em = JaloSession.getCurrentSession().getExtensionManager();
		return (ChinesestoreservicesManager) em.getExtension(ChinesestoreservicesConstants.EXTENSIONNAME);
	}
	
}
