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
import de.hybris.platform.jalo.order.AbstractOrderEntry;
import de.hybris.platform.jalo.product.Product;
import de.hybris.platform.promotions.jalo.AbstractPromotion;
import de.hybris.platform.promotions.jalo.PromotionOrderEntryConsumed;
import de.hybris.platform.promotions.jalo.PromotionsManager;
import de.hybris.platform.promotions.result.PromotionEvaluationContext.ConsumptionLogger;
import de.hybris.platform.promotions.util.CompositeProduct;
import de.hybris.platform.promotions.util.Helper;
import de.hybris.platform.variants.jalo.VariantProduct;

import java.util.List;


/**
 * An entry as viewed from the PromotionEvaluationContext. This entry always shows the current state of the promotions
 *
 *
 */
public class PromotionOrderEntry implements Comparable
{
	private final PromotionEvaluationContext.ConsumptionLogger logger;
	private final AbstractOrderEntry baseEntry;

	/**
	 * Constructor.
	 *
	 * @param orderEntry
	 *           The order entry
	 * @param logger
	 *           The logger
	 */
	public PromotionOrderEntry(final AbstractOrderEntry orderEntry, final PromotionEvaluationContext.ConsumptionLogger logger)
	{
		this.baseEntry = orderEntry;
		this.logger = logger;
	}

	/**
	 * Inform the context that a promotion wishes to consume the specified property of.
	 *
	 * @param ctx
	 *           The currency context to use
	 * @param promotion
	 *           The promotion wishing to consume some of an order entry
	 * @param quantity
	 *           The quantity of the order entry to consume
	 * @return The remaining quantity of the order entry
	 */
	public PromotionOrderEntryConsumed consume(final SessionContext ctx, final AbstractPromotion promotion, final long quantity) //NOSONAR
	{
		if (quantity == 0)
		{
			throw new PromotionException("Cannot consume zero products from an OrderEntry");
		}

		final long resultingQuantity = getQuantity(ctx) - quantity;
		if (resultingQuantity < 0)
		{
			throw new PromotionException(
					"Cannot remove " + quantity + " items.  There is not a sufficient quantity of this product remaining.");
		}

		final PromotionOrderEntryConsumed consumed = PromotionsManager.getInstance().createPromotionOrderEntryConsumed(ctx, "",
				this.baseEntry, quantity);
		logger.logOperation(consumed);
		return consumed;
	}

	/**
	 * Find the quantity of this orderEntry remaining after other promotions have run.
	 *
	 * @param ctx
	 *           The context to use
	 * @return The remaining quantity
	 */
	public long getQuantity(final SessionContext ctx)
	{
		long resultingQuantity = baseEntry.getQuantity(ctx).longValue();
		for (final PromotionOrderEntryConsumed poec : logger.getAllOperations())
		{
			if (poec.isRemovedFromOrder() && poec.getOrderEntry(ctx).equals(this.baseEntry))
			{
				resultingQuantity -= poec.getQuantity().longValue();
			}
		}
		return resultingQuantity;
	}

	/**
	 * Return the product for this order entry.
	 *
	 * Returns the actual product associated with the underlying order entry, this may be a {@link Product} or a
	 * {@link VariantProduct}.
	 *
	 * @param ctx
	 *           The hybris context
	 * @return The {@link Product} that this entry is for
	 *
	 * @see #getBaseProducts(de.hybris.platform.jalo.SessionContext)
	 */
	public Product getProduct(final SessionContext ctx) //NOSONAR
	{
		return baseEntry.getProduct(ctx);
	}

	/**
	 * Returns a collection of base products for this order entry.
	 *
	 * Returns the current product if the product is not an instance of {@link VariantProduct} or
	 * {@link CompositeProduct} otherwise the relevant base product is returned.
	 *
	 * @param ctx
	 *           The hybris context
	 * @return The base {@link Product} that this entry is for
	 */

	public List<Product> getBaseProducts(final SessionContext ctx) //NOSONAR
	{
		return Helper.getBaseProducts(ctx, baseEntry.getProduct(ctx));
	}

	/**
	 * Returns the unit price of the product in this order entry.
	 *
	 * @param ctx
	 *           The hybris context
	 * @return The unit price of the product
	 */
	public Double getBasePrice(final SessionContext ctx)
	{
		return this.baseEntry.getBasePrice(ctx);
	}

	/**
	 * Compare to another object.
	 *
	 * @param o
	 *           The object to compare to
	 * @return See {@link Comparable#compareTo}
	 */
	@Override
	public int compareTo(final Object o) //NOSONAR
	{
		return this.baseEntry.compareTo(o);
	}

	/**
	 * Get the base order entry.
	 *
	 * @return the base order entry
	 */
	public AbstractOrderEntry getBaseOrderEntry()
	{
		return baseEntry;
	}

	protected ConsumptionLogger getLogger()
	{
		return logger;
	}
}
