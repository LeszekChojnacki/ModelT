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

import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.ordersplitting.jalo.StockLevel;
import de.hybris.platform.stock.StockService;


/**
 * This strategy will be used by the {@link StockService} for generating a product related ID, which will be stored
 * at {@link StockLevel}.
 */
public interface StockLevelProductStrategy
{
	/**
	 * Returns a product related ID for {@link StockLevel}.
	 * 
	 * @param model
	 *           the product we need a StockLelevel 'productCode' for.
	 * @return String the StckLevel#productCode
	 */
	String convert(ProductModel model);

	/**
	 * Returns a product model based of the assigned ID.
	 * 
	 * @param productCode
	 *           the product code
	 * @return ProductModel the product with the specified code.
	 */
	ProductModel convert(final String productCode);
}
