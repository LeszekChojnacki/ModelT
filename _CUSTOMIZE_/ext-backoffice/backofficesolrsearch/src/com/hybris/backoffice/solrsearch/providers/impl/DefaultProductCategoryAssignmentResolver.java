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
package com.hybris.backoffice.solrsearch.providers.impl;

import com.hybris.backoffice.solrsearch.providers.ProductCategoryAssignmentResolver;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.variants.model.VariantProductModel;

import java.util.LinkedHashSet;
import java.util.Set;


public class DefaultProductCategoryAssignmentResolver implements ProductCategoryAssignmentResolver
{

	public Set<CategoryModel> getIndexedCategories(final ProductModel product)
	{
		final Set<CategoryModel> categories = new LinkedHashSet<>(product.getSupercategories());
		if (product instanceof VariantProductModel)
		{
			final ProductModel baseProduct = ((VariantProductModel) product).getBaseProduct();
			categories.addAll(baseProduct.getSupercategories());
		}
		return categories;
	}
}
