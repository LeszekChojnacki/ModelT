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
package de.hybris.platform.ruleengineservices.util;

import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.variants.model.VariantProductModel;

import java.util.HashSet;
import java.util.Set;


/**
 * The class provides utility methods related to Product.
 */
public class ProductUtils
{
	/**
	 * Returns all the base products from the product tree or empty list if none exists.
	 *
	 * @param productModel
	 * @return
	 */
	public Set<ProductModel> getAllBaseProducts(final ProductModel productModel)
	{
		final Set<ProductModel> allBaseProducts = new HashSet<>();

		ProductModel currentProduct = productModel;

		while (currentProduct instanceof VariantProductModel)
		{
			currentProduct = ((VariantProductModel) currentProduct).getBaseProduct();

			if (currentProduct == null)
			{
				break;
			}
			else
			{
				allBaseProducts.add(currentProduct);
			}
		}
		return allBaseProducts;
	}
}
