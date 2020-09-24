/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.futurestock;

import de.hybris.platform.core.model.product.ProductModel;

import java.util.Date;
import java.util.Map;


/**
 * Service for 'Future Stock Management'.
 * 
 * @spring.bean stockService
 */
public interface FutureStockService
{
	String BEAN_NAME = "futureStockService";

	/**
	 * Gets the future product availability for the specified product, for each future date.
	 * 
	 * @param product
	 *           the product
	 * @return A map of quantity for each available date.
	 */
	Map<Date, Integer> getFutureAvailability(ProductModel product);

}
