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
package de.hybris.platform.ordersplitting.jalo;

import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.product.Product;


public class StockLevel extends GeneratedStockLevel
{

	/**
	 * Getter of product code.
	 * 
	 * use #getProductCode(String code), we will store the stock level not directly on Product, because we don't want to
	 * deal with the stages (versions) which a product can be part of.
	 * @deprecated Since ages
	 */
	@Deprecated
	@Override
	public Product getProduct(final SessionContext ctx) //NOSONAR
	{
		for (final Product product : this.getProducts()) //NOSONAR
		{
			return product;
		}

		return null;
	}

	/**
	 * Setter of product code.
	 * 
	 * use #setProductCode(String code).<br>
	 * We will store the stock level not directly on Product, because we don't want to deal with the stages (versions)
	 * which a product can be part of.
	 * @deprecated Since ages
	 */
	@Deprecated
	@Override
	public void setProduct(final SessionContext ctx, final Product value) //NOSONAR
	{

		if (null != value && !getProducts(ctx).contains(value))
		{
			addToProducts(value);
		}
	}

}
