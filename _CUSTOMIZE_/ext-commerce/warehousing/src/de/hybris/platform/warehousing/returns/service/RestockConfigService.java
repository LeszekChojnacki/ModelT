/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 *
 */
package de.hybris.platform.warehousing.returns.service;

import de.hybris.platform.warehousing.model.RestockConfigModel;
import de.hybris.platform.warehousing.returns.RestockException;


/**
 * Service for handling  {@link RestockConfigModel}.
 */
public interface RestockConfigService
{
	/**
	 * gets restock config
	 * @return restock config
	 */
	RestockConfigModel getRestockConfig() throws RestockException;
	
	/**
	 * Get returned bin code from restock config
	 * 
	 * @return returned bin code
	 */	
	String getReturnedBinCode();
}
