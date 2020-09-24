/*
 *  
 * [y] hybris Platform
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.chineselogisticservices.jalo;

import de.hybris.platform.chineselogisticservices.constants.ChineselogisticservicesConstants;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.extension.ExtensionManager;
import org.apache.log4j.Logger;

@SuppressWarnings("PMD")
public class ChineselogisticservicesManager extends GeneratedChineselogisticservicesManager
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger( ChineselogisticservicesManager.class.getName() );
	
	public static final ChineselogisticservicesManager getInstance()
	{
		ExtensionManager em = JaloSession.getCurrentSession().getExtensionManager();
		return (ChineselogisticservicesManager) em.getExtension(ChineselogisticservicesConstants.EXTENSIONNAME);
	}
	
}
