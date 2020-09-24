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
import de.hybris.platform.jalo.order.AbstractOrderEntry;
import de.hybris.platform.jalo.order.price.JaloPriceFactoryException;
import de.hybris.platform.jalo.product.Product;
import de.hybris.platform.promotions.jalo.PromotionsManager.RestrictionSetResult;
import de.hybris.platform.promotions.result.PromotionEvaluationContext;

import java.util.ArrayList;
import java.util.List;


/**
 * Order Promotion. Base class for order level promotions.
 */
public abstract class OrderPromotion extends GeneratedOrderPromotion
{
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(OrderPromotion.class.getName());

	/**
	 * Gets the order subtotal minus any order discounts.
	 *
	 * @param ctx
	 *           the hybris context
	 * @param order
	 *           the order to get the subtotal from
	 * @return the subtotal minus any discounts
	 */
	protected static final double getOrderSubtotalAfterDiscounts(final SessionContext ctx, final AbstractOrder order)
	{
		double orderSubtotalWithoutDiscounts = 0D;
		if (ctx != null && order != null)
		{
			// Try to calculate totals, but don't recalculate
			try
			{
				order.calculateTotals(false);// NOSONAR
			}
			catch (final JaloPriceFactoryException ex)
			{
				log.error("orderSubtotalAfterDiscounts - failed to calculateTotals on order [" + order + "]", ex);
			}

			orderSubtotalWithoutDiscounts = order.getSubtotal(ctx).doubleValue() - order.getTotalDiscounts(ctx).doubleValue();
		}
		return orderSubtotalWithoutDiscounts;
	}

	/**
	 * Check the restrictions on this promotion. Returns true if the restrictions allow this promotion to evaluate.
	 *
	 * @param ctx
	 *           the hybris context
	 * @param promoContext
	 *           the promotion context
	 * @return true if the restrictions allow this promotion to evaluate
	 */
	protected boolean checkRestrictions(final SessionContext ctx, final PromotionEvaluationContext promoContext)
	{
		// Run restrictions if appropriate
		if (!promoContext.getObserveRestrictions())
		{
			return true;
		}
		else
		{
			final AbstractOrder order = promoContext.getOrder();
			final List<Product> products = new ArrayList<>(); // NOSONAR
			for (final AbstractOrderEntry entry : order.getEntries())
			{
				products.add(entry.getProduct());
			}
			final RestrictionSetResult evaluateRestrictions = PromotionsManager.getInstance().evaluateRestrictions(ctx, products,
					order, this, promoContext.getDate());
			return evaluateRestrictions.isAllowedToContinue();
		}
	}

}
