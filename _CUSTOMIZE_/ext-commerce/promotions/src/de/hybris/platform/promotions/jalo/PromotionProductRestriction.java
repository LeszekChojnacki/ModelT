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
package de.hybris.platform.promotions.jalo;


import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.order.AbstractOrder;
import de.hybris.platform.jalo.product.Product;
import de.hybris.platform.promotions.util.Helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;


/**
 * PromotionProductRestriction. Prevents the promotion from considering the products in the restricted products
 * collection.
 *
 *
 */
public class PromotionProductRestriction extends GeneratedPromotionProductRestriction // NOSONAR
{
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(PromotionProductRestriction.class.getName());

	@Override
	public RestrictionResult evaluate(final SessionContext ctx, final Collection<Product> products, final Date date, // NOSONAR
			final AbstractOrder order)
	{
		final Collection<Product> restrictedProducts = super.getProducts(ctx); // NOSONAR

		if (restrictedProducts != null && products != null && !restrictedProducts.isEmpty() && !products.isEmpty())
		{
			final ArrayList<Product> productsToRemove = new ArrayList<Product>(); // NOSONAR

			for (final Product testProduct : products) // NOSONAR
			{
				if (isRestrictedProduct(restrictedProducts, testProduct))
				{
					productsToRemove.add(testProduct);
				}
			}

			if (!productsToRemove.isEmpty())
			{
				products.removeAll(productsToRemove);
				return RestrictionResult.ADJUSTED_PRODUCTS;
			}
		}

		return RestrictionResult.ALLOW;
	}

	/**
	 * Test if a product is in the collection of restricted products. If the product or its base product (if it is a
	 * variant or composite product) is in the collection of restricted products then this returns true.
	 */
	protected boolean isRestrictedProduct(final Collection<Product> restrictedProducts, final Product testProduct) // NOSONAR
	{
		boolean result = restrictedProducts.contains(testProduct);

		if (!result)
		{
			final List<Product> baseProducts = Helper.getBaseProducts(getSession().getSessionContext(), testProduct); // NOSONAR
			for (final Product baseProduct : baseProducts) // NOSONAR
			{
				if (restrictedProducts.contains(baseProduct))
				{
					result = true;
					break;
				}
			}
		}
		return result;
	}

	@Override
	protected void buildDataUniqueKey(final SessionContext ctx, final StringBuilder builder)
	{
		super.buildDataUniqueKey(ctx, builder);

		final Collection<Product> products = getProducts(ctx); // NOSONAR
		if (products != null && !products.isEmpty())
		{
			for (final Product p : products) // NOSONAR
			{
				builder.append(p.getCode(ctx)).append(',');
			}
		}
		builder.append('|');
	}

	@Override
	protected Object[] getDescriptionPatternArguments(final SessionContext ctx)
	{
		return new Object[]
		{ getRestrictionType(ctx), getProductNames(ctx) };
	}

	public String getProductNames(final SessionContext ctx)
	{
		final StringBuilder productNames = new StringBuilder();

		final Collection<Product> products = getProducts(ctx); // NOSONAR
		if (products != null && !products.isEmpty())
		{
			for (final Iterator<Product> iterator = products.iterator(); iterator.hasNext();) // NOSONAR
			{
				final Product product = iterator.next(); // NOSONAR
				productNames.append(product.getName());

				if (iterator.hasNext())
				{
					productNames.append(", ");
				}
			}
		}

		return productNames.toString();
	}
}
