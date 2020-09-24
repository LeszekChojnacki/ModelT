/*
 *  
 * [y] hybris Platform
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.marketplaceservices.jalo;

import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.extension.ExtensionManager;
import de.hybris.platform.marketplaceservices.constants.MarketplaceservicesConstants;
import org.apache.log4j.Logger;

@SuppressWarnings("PMD")
public class MarketplaceservicesManager extends GeneratedMarketplaceservicesManager
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger( MarketplaceservicesManager.class.getName() );
	
	public static final MarketplaceservicesManager getInstance()
	{
		ExtensionManager em = JaloSession.getCurrentSession().getExtensionManager();
		return (MarketplaceservicesManager) em.getExtension(MarketplaceservicesConstants.EXTENSIONNAME);
	}
	
}
