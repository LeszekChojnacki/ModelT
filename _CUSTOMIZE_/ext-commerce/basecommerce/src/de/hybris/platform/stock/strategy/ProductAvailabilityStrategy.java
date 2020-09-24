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
package de.hybris.platform.stock.strategy;

import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;

import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * This strategy will be used for calculating the availability of the specified product.
 * 
 * @spring.bean productAvailabilityStrategy
 */
public interface ProductAvailabilityStrategy
{
	/**
	 * Calculates product availability, passing the product, quantity and the warehouses for which the availability
	 * should be returned.
	 * 
	 * @param productCode
	 *           code of the product
	 * @param warehouses
	 *           the warehouses
	 * @param date
	 *           the date the specified quantity has to be available at least.
	 * @return the mapped quantities
	 */
	Map<WarehouseModel, Integer> getAvailability(String productCode, List<WarehouseModel> warehouses, Date date);

	/**
	 * Gets availability date by invoking strategy for calculating product availability, passing product, quantity and
	 * warehouses as parameters.
	 * 
	 * @param productCode
	 *           code of the product
	 * @param warehouses
	 *           the warehouses
	 * @param quantity
	 *           the asked quantity
	 * @return Returns mapped availability dates
	 */
	Map<WarehouseModel, Date> getAvailability(String productCode, List<WarehouseModel> warehouses, int quantity);

	/**
	 * Converted the mapped quantities in a textual representation.
	 * 
	 * @param quantities
	 *           the mapped quantities
	 */
	String parse(final Map<WarehouseModel, Integer> quantities, final String productCode, final Date date, LanguageModel language);

	/**
	 * Converted the mapped availability in a textual representation.
	 * 
	 * @param quantities
	 *           the mapped availability dates
	 */
	String parse(final Map<WarehouseModel, Date> quantities, final String productCode, final int quantity, LanguageModel language);

	WarehouseModel getBestMatchOfQuantity(final Map<WarehouseModel, Integer> map);

	WarehouseModel getBestMatchOfAvailability(final Map<WarehouseModel, Date> map);
}
