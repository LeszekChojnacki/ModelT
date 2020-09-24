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
package de.hybris.platform.stock.strategy.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.ordersplitting.jalo.StockLevel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.model.ItemModelContextImpl;
import de.hybris.platform.servicelayer.model.ModelContextUtils;
import de.hybris.platform.stock.strategy.StockLevelProductStrategy;


/**
 * This impl. will use the configured 'qualifier' for code generation.
 */
public class DefaultStockLevelProductStrategy implements StockLevelProductStrategy
{

	private String qualifier;
	private ProductService productService;

	/**
	 * Returns a product related ID for {@link StockLevel}.
	 *
	 * @param model
	 *           the product we need a StockLelevel 'productCode' for.
	 * @return String the code of the assigned product.
	 */
	@Override
	public String convert(final ProductModel model)
	{
		validateParameterNotNull(model, "Parameter 'model' was null.");
		final ItemModelContextImpl internalContext = (ItemModelContextImpl) ModelContextUtils.getItemModelContext(model);
		final Object value = internalContext.getAttributeProvider().getAttribute(qualifier);
		return value == null ? null : value.toString();
	}

	/**
	 * Returns a product model based of the assigned ID.
	 *
	 * @param productCode
	 *           the product code
	 * @return ProductModel the product with the specified code.
	 */
	@Override
	public ProductModel convert(final String productCode)
	{
		validateParameterNotNull(productCode, "Parameter 'productCode' was null.");
		return productService.getProductForCode(productCode);
	}

	/**
	 * @param qualifier
	 *           the qualifier to set
	 */
	public void setQualifier(final String qualifier)
	{
		this.qualifier = qualifier;
	}

}
