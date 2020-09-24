/*
 *  
 * [y] hybris Platform
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.stocknotificationservices.jalo;

import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.extension.ExtensionManager;
import de.hybris.platform.stocknotificationservices.constants.StocknotificationservicesConstants;
import org.apache.log4j.Logger;

@SuppressWarnings("PMD")
public class StocknotificationservicesManager extends GeneratedStocknotificationservicesManager
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger( StocknotificationservicesManager.class.getName() );
	
	public static final StocknotificationservicesManager getInstance()
	{
		ExtensionManager em = JaloSession.getCurrentSession().getExtensionManager();
		return (StocknotificationservicesManager) em.getExtension(StocknotificationservicesConstants.EXTENSIONNAME);
	}
	
}
