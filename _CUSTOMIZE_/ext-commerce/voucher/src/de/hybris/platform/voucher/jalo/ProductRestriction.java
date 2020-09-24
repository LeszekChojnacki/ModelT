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
import java.util.Iterator;

import org.apache.log4j.Logger;


/**
 * This restriction restricts vouchers to specified products
 *
 */
public class ProductRestriction extends GeneratedProductRestriction //NOSONAR
{
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(ProductRestriction.class.getName());

	@Override
	protected String[] getMessageAttributeValues()
	{
		return new String[]
		{ Localization.getLocalizedString("type.restriction.positive." + isPositiveAsPrimitive()), getProductNames() };
	}

	/**
	 * Returns the names of all products restricted by this restriction.
	 *
	 * @return a String representing a comma separated list of product names.
	 */
	protected String getProductNames()
	{
		final StringBuilder productNames = new StringBuilder();
		for (final Iterator iterator = getProducts().iterator(); iterator.hasNext();)
		{
			final Product product = (Product) iterator.next(); //NOSONAR
			productNames.append(product.getName());
			if (iterator.hasNext())
			{
				productNames.append(", ");
			}
		}
		return productNames.toString();
	}

	@Override
	public VoucherEntrySet getApplicableEntries(final AbstractOrder anOrder)
	{
		final VoucherEntrySet entries = new VoucherEntrySet();
		if (getProducts().isEmpty() && !isPositiveAsPrimitive())
		{
			entries.addAll(anOrder.getAllEntries()); //NOSONAR
		}
		else
		{
			for (final Iterator iterator = anOrder.getAllEntries().iterator(); iterator.hasNext();)  //NOSONAR
			{
				final AbstractOrderEntry entry = (AbstractOrderEntry) iterator.next();
				if (isFulfilledInternal(entry.getProduct()))
				{
					entries.add(new VoucherEntry(entry, entry.getQuantity().longValue(), entry.getUnit()));
				}
			}
		}
		return entries;
	}

	/**
	 * Gets the products (incl. VariantProducts) the given Voucher is valid for.
	 *
	 * @see de.hybris.platform.voucher.jalo.GeneratedProductRestriction#getProducts(de.hybris.platform.jalo.SessionContext)
	 */
	@Override
	public Collection getProducts(final SessionContext ctx)
	{
		final Collection products = super.getProducts(ctx);
		return products != null ? products : Collections.emptyList();
	}

	/**
	 * Returns <tt>true</tt> if the specified abstract order fulfills this restriction. More formally, returns
	 * <tt>true</tt> if the specified abstract order contains at least one product that fulfills this restriction.
	 *
	 * @param anOrder
	 *           the abstract order to check whether it fulfills this restriction.
	 * @return <tt>true</tt> if the specified abstract order fulfills this restriction, <tt>false</tt> else.
	 * @see Restriction#isFulfilledInternal(AbstractOrder)
	 */
	@Override
	protected boolean isFulfilledInternal(final AbstractOrder anOrder)
	{
		return !getApplicableEntries(anOrder).isEmpty();
	}

	/**
	 * Returns <tt>true</tt> if the specified product fulfills this restriction. More formally, returns <tt>true</tt> if
	 * the set of products defined by this restriction contains the specified product, such that
	 * <tt>(getProducts().contains(aProduct)==isPositive().booleanValue())</tt>.
	 *
	 * @param aProduct
	 *           the product to check whether it fulfills this restriction.
	 * @return <tt>true</tt> if the specified product fulfills this restriction, <tt>false</tt> else.
	 * @see Restriction#isFulfilledInternal(Product)
	 */
	@Override
	protected boolean isFulfilledInternal(final Product aProduct)  //NOSONAR
	{
		return this.containsProduct(aProduct) == super.isPositiveAsPrimitive();
	}

	/**
	 * Checks whether this product matches a chosen product of the Restriction entries. VariantProducts are also matched
	 * if the BaseProduct is restricted.
	 *
	 * @param product
	 * @return true if this product is a restricted one.
	 */
	private boolean containsProduct(final Product product)  //NOSONAR
	{
		final Collection products = super.getProducts();

		boolean result = products.contains(product);
		if (product instanceof VariantProduct)
		{
			result = result || products.contains(((VariantProduct) product).getBaseProduct());
		}
		return result;
	}

}
