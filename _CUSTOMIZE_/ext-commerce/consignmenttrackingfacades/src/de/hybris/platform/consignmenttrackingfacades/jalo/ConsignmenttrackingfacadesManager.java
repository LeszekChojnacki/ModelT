/*
 *  
 * [y] hybris Platform
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.consignmenttrackingfacades.jalo;

import de.hybris.platform.consignmenttrackingfacades.constants.ConsignmenttrackingfacadesConstants;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.extension.ExtensionManager;
import org.apache.log4j.Logger;

@SuppressWarnings("PMD")
public class ConsignmenttrackingfacadesManager extends GeneratedConsignmenttrackingfacadesManager
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger( ConsignmenttrackingfacadesManager.class.getName() );
	
	public static final ConsignmenttrackingfacadesManager getInstance()
	{
		ExtensionManager em = JaloSession.getCurrentSession().getExtensionManager();
		return (ConsignmenttrackingfacadesManager) em.getExtension(ConsignmenttrackingfacadesConstants.EXTENSIONNAME);
	}
	
}
