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
package de.hybris.platform.solrfacetsearch.provider.impl;

import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.provider.IdentityProvider;
import de.hybris.platform.variants.model.VariantProductModel;

import java.io.Serializable;


/**
 * Resolves unique product's identity. Respects multi-catalog versions
 */
public class ShardAwareProductIdentityProvider extends AbstractProductIdentityProvider
		implements IdentityProvider<ProductModel>, Serializable
{

	public static final String GROUPING_SEPARATOR = "!";

	@Override
	public String getIdentifier(final IndexConfig indexConfig, final ProductModel product)
	{
		final StringBuilder sb = new StringBuilder();

		if (product instanceof VariantProductModel)
		{
			sb.append(getTopBaseProductCode(product)).append(GROUPING_SEPARATOR);
		}

		sb.append(getIdentifierForProduct(product));
		return sb.toString();
	}

	protected String getTopBaseProductCode(final ProductModel product)
	{
		if (product instanceof VariantProductModel)
		{
			final ProductModel baseProduct = ((VariantProductModel) product).getBaseProduct();
			return getTopBaseProductCode(baseProduct);
		}
		return product.getCode();
	}

}
