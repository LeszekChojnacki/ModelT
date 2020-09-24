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
package de.hybris.platform.voucher.jalo;

import de.hybris.platform.category.jalo.Category;
import de.hybris.platform.category.jalo.CategoryManager;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.order.AbstractOrder;
import de.hybris.platform.jalo.order.AbstractOrderEntry;
import de.hybris.platform.jalo.product.Product;
import de.hybris.platform.util.localization.Localization;
import de.hybris.platform.variants.jalo.VariantProduct;
import de.hybris.platform.voucher.jalo.util.VoucherEntry;
import de.hybris.platform.voucher.jalo.util.VoucherEntrySet;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;


/**
 * This restriction restricts vouchers to product categories
 *
 */
public class ProductCategoryRestriction extends GeneratedProductCategoryRestriction //NOSONAR
{
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(ProductCategoryRestriction.class.getName());

	/**
	 * Gets the categories for this restriction is valid for.
	 */
	@Override
	public Collection getCategories(final SessionContext ctx)
	{
		final Collection categories = super.getCategories(ctx);
		return categories != null ? categories : Collections.emptyList();
	}

	/**
	 * Returns the names of all categories restricted by this restriction.
	 *
	 * @return a String representing a comma separated list of category names.
	 */
	protected String getCategoryNames()
	{
		final StringBuilder categoryNames = new StringBuilder();
		for (final Iterator iterator = getCategories().iterator(); iterator.hasNext();)
		{
			final Category category = (Category) iterator.next();
			categoryNames.append(category.getName());
			if (iterator.hasNext())
			{
				categoryNames.append(", ");
			}
		}
		return categoryNames.toString();
	}

	@Override
	public VoucherEntrySet getApplicableEntries(final AbstractOrder anOrder)
	{
		final Set<Product> fulfilledProducts = new HashSet<>(); //NOSONAR
		final Set<Product> unfulfilledProducts = new HashSet<>(); //NOSONAR
		final Collection<Category> restrictCategories = this.getCategories();
		final VoucherEntrySet entries = new VoucherEntrySet();
		for (final AbstractOrderEntry entry : anOrder.getEntries()) //NOSONAR
		{
			final Product product = entry.getProduct(); //NOSONAR
			if (fulfilledProducts.contains(product))
			{
				entries.add(new VoucherEntry(entry, entry.getQuantity().longValue(), entry.getUnit()));
				continue;
			}
			if (unfulfilledProducts.contains(product))
			{
				continue;
			}
			final boolean fulfilledProduct = isFulfilled(product, restrictCategories);
			if (fulfilledProduct)
			{
				entries.add(new VoucherEntry(entry, entry.getQuantity().longValue(), entry.getUnit()));
				fulfilledProducts.add(product);
			}
			else
			{
				unfulfilledProducts.add(product);
			}
		}
		return entries;
	}

	@Override
	protected boolean isFulfilledInternal(final Product product) //NOSONAR
	{
		final Collection<Category> restrictCategories = this.getCategories();
		return isFulfilled(product, restrictCategories);
	}

	private boolean isFulfilled(final Product product, final Collection<Category> restrictCategories) //NOSONAR
	{
		final boolean contained = containsProductCategory(product, restrictCategories);
		return contained == isPositiveAsPrimitive();
	}

	/**
	 * Checks if the product is included in the restricted categories.
	 *
	 * @return true if the product is included, false otherwise
	 */
	private boolean containsProductCategory(final Product product, final Collection<Category> restrictCategories) //NOSONAR
	{

		Collection<Category> categories;
		final CategoryManager catManager = CategoryManager.getInstance();
		if (product instanceof VariantProduct)
		{
			final Product baseProduct = ((VariantProduct) product).getBaseProduct(); //NOSONAR
			categories = new HashSet<>(catManager.getSupercategories(baseProduct));
			categories.addAll(catManager.getSupercategories(product));
		}
		else
		{
			categories = catManager.getSupercategories(product);
		}
		return containsCategory(categories, restrictCategories);
	}

	//recursive check for super categories
	@SuppressWarnings("squid:S2325")
	private boolean containsCategory(final Collection<Category> categories, final Collection<Category> restrictCategories)
	{
		if (categories.isEmpty())
		{
			return false;
		}
		boolean result = false;
		for (final Category category : categories)
		{
			if (restrictCategories.contains(category))
			{
				result = true;
				break;
			}
		}
		//only when the category is not found, the super categories will be checked
		if (!result)
		{
			for (final Category category : categories)
			{
				result = containsCategory(category.getSupercategories(), restrictCategories);
				if (result)
				{
					break;
				}
			}
		}
		return result;
	}

	@Override
	protected String[] getMessageAttributeValues()
	{
		return new String[]
		{ Localization.getLocalizedString("type.restriction.positive." + isPositiveAsPrimitive()), getCategoryNames() };
	}

	@Override
	public Collection getProducts(final SessionContext ctx)
	{
		return getProducts(ctx, new HashSet(), getCategories(ctx));
	}

	@SuppressWarnings("squid:S2325")
	private Collection getProducts(final SessionContext ctx, final Collection crawledCategories,
			final Collection uncrawledCategories)
	{
		final Collection products = new HashSet();
		if (uncrawledCategories != null)
		{
			for (final Iterator iterator = uncrawledCategories.iterator(); iterator.hasNext();)
			{
				final Category nextCategory = (Category) iterator.next();
				if (crawledCategories.add(nextCategory))
				{
					products.addAll(nextCategory.getProducts(ctx));
					products.addAll(getProducts(ctx, crawledCategories, nextCategory.getSubcategories(ctx))); //NOSONAR
				}
			}
		}
		return products;
	}

}
