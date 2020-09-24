/*
 *  
 * [y] hybris Platform
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.chinesecommerceorgaddressfacades.jalo;

import de.hybris.platform.chinesecommerceorgaddressfacades.constants.ChinesecommerceorgaddressfacadesConstants;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.extension.ExtensionManager;
import org.apache.log4j.Logger;

@SuppressWarnings("PMD")
public class ChinesecommerceorgaddressfacadesManager extends GeneratedChinesecommerceorgaddressfacadesManager
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger( ChinesecommerceorgaddressfacadesManager.class.getName() );
	
	public static final ChinesecommerceorgaddressfacadesManager getInstance()
	{
		ExtensionManager em = JaloSession.getCurrentSession().getExtensionManager();
		return (ChinesecommerceorgaddressfacadesManager) em.getExtension(ChinesecommerceorgaddressfacadesConstants.EXTENSIONNAME);
	}
	
}
