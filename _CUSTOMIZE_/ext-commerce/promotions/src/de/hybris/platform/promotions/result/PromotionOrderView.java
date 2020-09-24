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
package de.hybris.platform.promotions.result;

import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.product.Product;
import de.hybris.platform.promotions.jalo.AbstractPromotion;
import de.hybris.platform.promotions.jalo.PromotionOrderEntryConsumed;
import de.hybris.platform.promotions.util.Comparators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * A filtered view of the OrderEntries for a given order.
 *
 *
 */
public class PromotionOrderView
{
	private final List<PromotionOrderEntry> orderEntries;
	private final AbstractPromotion promotion;

	protected PromotionOrderView(final AbstractPromotion promotion, final List<PromotionOrderEntry> orderEntries)
	{
		this.orderEntries = orderEntries;
		this.promotion = promotion;
	}

	/**
	 * Get the total number of products remaining in this view.
	 *
	 * @param ctx
	 *           the hybris context
	 * @return the number of products remaining
	 */
	public long getTotalQuantity(final SessionContext ctx)
	{
		long retval = 0;
		for (final PromotionOrderEntry oe : this.orderEntries)
		{
			retval += oe.getQuantity(ctx);
		}
		return retval;
	}

	/**
	 * Get the number of a single product remaining.
	 *
	 * @param ctx
	 *           the hybris context
	 * @param product
	 *           the product
	 * @return the quantity of the specified product remaining
	 */
	public long getQuantity(final SessionContext ctx, final Product product) //NOSONAR
	{
		long retval = 0;
		if (product != null)
		{
			for (final PromotionOrderEntry poe : this.orderEntries)
			{
				final List<Product> baseProducts = poe.getBaseProducts(ctx); //NOSONAR
				if (poe.getProduct(ctx).equals(product) || baseProducts.contains(product))
				{
					retval += poe.getQuantity(ctx);
				}
			}
		}
		return retval;
	}

	/**
	 * Consume a specific quantity of products from this view. The type of product is not relevant.
	 *
	 * @param ctx
	 *           the hybris context
	 * @param quantity
	 *           the quantity to consume
	 * @return a list of consumed order entries
	 */
	public List<PromotionOrderEntryConsumed> consume(final SessionContext ctx, final long quantity)
	{
		return doConsume(ctx, orderEntries, this.promotion, quantity);
	}

	/**
	 * Consume products from this view in the specified order.
	 *
	 * @param ctx
	 *           the hybris context
	 * @param comparator
	 *           the comparator
	 * @param quantity
	 *           the quantity to consume
	 * @return a list of consumed order entries
	 */
	public List<PromotionOrderEntryConsumed> consumeFromHead(final SessionContext ctx,
			final Comparator<PromotionOrderEntry> comparator, final long quantity)
	{
		final List<PromotionOrderEntry> orderedEntries = new ArrayList<>(orderEntries);
		if (comparator != null)
		{
			Collections.sort(orderedEntries, comparator);
		}
		return doConsume(ctx, orderedEntries, this.promotion, quantity);
	}

	/**
	 * Consume products from this view in the reverse of the specfied order.
	 *
	 * @param ctx
	 *           the hybris context
	 * @param comparator
	 *           the comparator
	 * @param quantity
	 *           the quantity to consume
	 * @return a list of consumed order entries
	 */
	public List<PromotionOrderEntryConsumed> consumeFromTail(final SessionContext ctx,
			final Comparator<PromotionOrderEntry> comparator, final long quantity)
	{
		final List<PromotionOrderEntry> orderedEntries = new ArrayList<>(orderEntries);
		if (comparator != null)
		{
			Collections.sort(orderedEntries, Collections.reverseOrder(comparator));
		}
		return doConsume(ctx, orderedEntries, this.promotion, quantity);
	}

	protected static List<PromotionOrderEntryConsumed> doConsume(final SessionContext ctx,
			final List<PromotionOrderEntry> workingEntries, final AbstractPromotion promotion, final long quantity)
	{
		final List<PromotionOrderEntryConsumed> consumed = new ArrayList<>();

		long remaining = quantity;
		for (final PromotionOrderEntry entry : workingEntries)
		{
			if (remaining <= 0)
			{
				break;
			}
			final long available = entry.getQuantity(ctx);
			if (available > 0)
			{
				final long consumeCount = (available < remaining) ? available : remaining;
				consumed.add(entry.consume(ctx, promotion, consumeCount));
				remaining -= consumeCount;
			}
		}

		if (remaining > 0)
		{
			throw new PromotionException("Attempt to consume more items than exist in this view of the order");
		}

		return consumed;
	}

	/**
	 * Consume products from this view.
	 *
	 * @param ctx
	 *           the hybris context
	 * @param product
	 *           the product type
	 * @param quantity
	 *           the quantity
	 * @return a list of consumed order entries
	 */
	public List<PromotionOrderEntryConsumed> consume(final SessionContext ctx, final Product product, final long quantity) //NOSONAR
	{
		final List<PromotionOrderEntryConsumed> consumed = new ArrayList<>();

		long remaining = quantity;
		for (final PromotionOrderEntry entry : orderEntries)
		{
			if (remaining <= 0)
			{
				break;
			}
			final List<Product> baseProducts = entry.getBaseProducts(ctx); //NOSONAR
			if (entry.getProduct(ctx).equals(product) || baseProducts.contains(product))
			{
				final long available = entry.getQuantity(ctx);
				if (available > 0)
				{
					final long consumeCount = (available < remaining) ? available : remaining;
					consumed.add(entry.consume(ctx, promotion, consumeCount));
					remaining -= consumeCount;
				}
			}
		}

		if (remaining > 0)
		{
			throw new PromotionException("Attempt to consume more items than exist in this view of the order");
		}

		return consumed;
	}

	/**
	 * Get all the entries in this view.
	 *
	 * @param ctx
	 *           the hybris context
	 * @return the order entries
	 */
	public List<PromotionOrderEntry> getAllEntries(final SessionContext ctx) //NOSONAR
	{
		return Collections.unmodifiableList(orderEntries);
	}

	/**
	 * Get all the entries in this view sorted by price.
	 *
	 * @param ctx
	 *           the hybris context
	 * @return the order entries
	 */
	public List<PromotionOrderEntry> getAllEntriesByPrice(final SessionContext ctx) //NOSONAR
	{
		final ArrayList<PromotionOrderEntry> sortedEntries = new ArrayList<>(orderEntries);
		Collections.sort(sortedEntries, Comparators.promotionOrderEntryByPriceComparator);
		return sortedEntries;
	}

	/**
	 * Look at the next promotion order entry from this view in the order specified.
	 *
	 * @param ctx
	 *           the hybris context
	 * @param comparator
	 *           the comparator
	 * @return the next order entry
	 */
	public PromotionOrderEntry peekFromHead(final SessionContext ctx, final Comparator<PromotionOrderEntry> comparator)
	{
		final List<PromotionOrderEntry> orderedEntries = new ArrayList<>(orderEntries);
		if (comparator != null)
		{
			Collections.sort(orderedEntries, comparator);
		}
		return doPeek(ctx, orderedEntries);
	}

	/**
	 * Look at the next promotion order entry from this view.
	 *
	 * @param ctx
	 *           the hybris context
	 * @return the next order entry
	 */
	public PromotionOrderEntry peek(final SessionContext ctx)
	{
		return doPeek(ctx, orderEntries);
	}

	/**
	 * Look at the next promotion order entry from this view in the reverse of the order specified.
	 *
	 * @param ctx
	 *           the hybris context
	 * @param comparator
	 *           the comparator
	 * @return the next order entry
	 */
	public PromotionOrderEntry peekFromTail(final SessionContext ctx, final Comparator<PromotionOrderEntry> comparator)
	{
		final List<PromotionOrderEntry> orderedEntries = new ArrayList<>(orderEntries);
		if (comparator != null)
		{
			Collections.sort(orderedEntries, Collections.reverseOrder(comparator));
		}
		return doPeek(ctx, orderedEntries);
	}

	protected static PromotionOrderEntry doPeek(final SessionContext ctx, final List<PromotionOrderEntry> workingEntries)
	{
		for (final PromotionOrderEntry entry : workingEntries)
		{
			final long available = entry.getQuantity(ctx);
			if (available > 0)
			{
				return entry;
			}
		}

		return null;
	}

	protected List<PromotionOrderEntry> getOrderEntries()
	{
		return orderEntries;
	}

	protected AbstractPromotion getPromotion()
	{
		return promotion;
	}
}
