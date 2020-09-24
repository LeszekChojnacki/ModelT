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
import de.hybris.platform.promotions.util.Pair;
import de.hybris.platform.util.DiscountValue;

import java.util.Collection;


/**
 * PromotionOrderEntryAdjustAction. Action to create a fixed price adjustment to a specific quantity of products. The
 * adjustment is applied against the unit price of the OrderEntry. For this reason if the OrderEntry quantity is
 * different from the number to adjust we need to evenly distribute the discount so that the cart is correctly
 * calculated. The only other way to deal with this would be to split the OrderEntry into two, one with the already used
 * items and one with the discounted items, but this would mean invalidating the promotion context as it wraps all of
 * the OrderEntry objects Applying this action creates an order entry discount value for the first order entry with
 * sufficient quantity of products. Undoing this action removes the order entry discount value created.
 * 
 * If there is no single order entry with at least {@link #getOrderEntryQuantity} quantity of product
 * {@link #getOrderEntryProduct} then this action will fail to apply. This can happen if the product quantity is split
 * across multiple order entries.
 * 
 * 
 */
public class PromotionOrderEntryAdjustAction extends GeneratedPromotionOrderEntryAdjustAction
{
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger(PromotionOrderEntryAdjustAction.class.getName());

	@Override
	public boolean apply(final SessionContext ctx)
	{
		final AbstractOrder order = getPromotionResult(ctx).getOrder(ctx);

		if (log.isDebugEnabled())
		{
			log.debug("(" + getPK() + ") apply: Applying OrderEntry adjustment action for order [" + order.getPK() + "]");
		}
		boolean needsCalc = false;
		final Integer orderEntryNumber = getOrderEntryNumber(ctx);
		final AbstractOrderEntry orderEntry = findOrderEntry(order, ctx, orderEntryNumber);

		if (orderEntry != null)
		{
			// The orderEntryAdjustment is the adjustment that we want to make to the quantity of products.
			final double orderEntryAdjustment = this.getAmount(ctx).doubleValue();

			// We need to make an adjustment to the base price for the items in the order entry
			final double unitAdjustment = orderEntryAdjustment / orderEntry.getQuantity(ctx).longValue();

			final String code = this.getGuid(ctx);
			final DiscountValue dv = new DiscountValue(code, -1.0F * unitAdjustment, true, order.getCurrency(ctx).getIsoCode(ctx)); // NOSONAR
			insertFirstOrderEntryDiscountValue(ctx, orderEntry, dv);
			if (log.isDebugEnabled())
			{
				log.debug("(" + getPK() + ") apply: Creating an adjustment of " + this.getAmount(ctx) + " to order entry '"
						+ orderEntry.getPK() + "'.  Order entry now has " + orderEntry.getDiscountValues(ctx).size() + " adjustments"); // NOSONAR
			}
			needsCalc = true;
		}
		else
		{
			log.error("(" + getPK() + ") apply: Could not find an order entry to adjust with product '"
					+ this.getOrderEntryProduct(ctx) + "' and quantity '" + this.getOrderEntryQuantity(ctx) + "'");
		}

		setMarkedApplied(ctx, true);

		return needsCalc;
	}

	protected AbstractOrderEntry findOrderEntry(final AbstractOrder order, final SessionContext ctx, final Integer orderEntryNumber)
	{
		AbstractOrderEntry result = null;
		//PRO-75, null for old versions that don't have the orderEntryNumber, and the behavior should not be changed
		if (orderEntryNumber == null)
		{
			for (final AbstractOrderEntry oe : (Iterable<AbstractOrderEntry>) order.getAllEntries()) // NOSONAR
			{
				if (oe.getProduct(ctx).equals(this.getOrderEntryProduct(ctx))
						&& oe.getQuantity(ctx).longValue() >= this.getOrderEntryQuantityAsPrimitive(ctx))
				{
					result = oe;
					break;
				}
			}
		}
		else
		{
			result = order.getEntry(orderEntryNumber.intValue()); // NOSONAR
		}

		return result;
	}

	@Override
	public boolean undo(final SessionContext ctx)
	{
		final AbstractOrder order = getPromotionResult(ctx).getOrder(ctx);

		if (log.isDebugEnabled())
		{
			log.debug("(" + getPK() + ") undo: Undoing order entry adjustment for order [" + order.getPK() + "]");
		}

		boolean calculateTotals = false;

		final OrderEntryAndDiscountValue orderEntryAndDiscountValue = findOrderEntryDiscountValue(ctx, order, this.getGuid(ctx));
		if (orderEntryAndDiscountValue != null)
		{
			orderEntryAndDiscountValue.getKey().removeDiscountValue(ctx, orderEntryAndDiscountValue.getValue()); // NOSONAR
			calculateTotals = true;
		}

		setMarkedApplied(ctx, false);

		return calculateTotals;
	}

	@Override
	public boolean isAppliedToOrder(final SessionContext ctx)
	{
		final AbstractOrder order = getPromotionResult(ctx).getOrder(ctx);

		if (log.isDebugEnabled())
		{
			log.debug("(" + getPK() + ") isAppliedToOrder: Checking if this action is applied to order [" + order.getPK() + "]");
		}

		return findOrderEntryDiscountValue(ctx, order, this.getGuid(ctx)) != null;
	}

	@Override
	public double getValue(final SessionContext ctx)
	{
		return -1.0D * this.getAmount(ctx).doubleValue();
	}

	protected static final class OrderEntryAndDiscountValue extends Pair<AbstractOrderEntry, DiscountValue>
	{
		public OrderEntryAndDiscountValue(final AbstractOrderEntry key, final DiscountValue value)
		{
			super(key, value);
		}
	}

	protected static OrderEntryAndDiscountValue findOrderEntryDiscountValue(final SessionContext ctx, final AbstractOrder order,
			final String discountValueCode)
	{
		final Collection<AbstractOrderEntry> entries = order.getAllEntries(); // NOSONAR
		for (final AbstractOrderEntry entry : entries)
		{
			final DiscountValue discountValue = findOrderEntryDiscountValue(ctx, entry, discountValueCode);
			if (discountValue != null)
			{
				return new OrderEntryAndDiscountValue(entry, discountValue);
			}
		}
		return null;
	}

	protected static DiscountValue findOrderEntryDiscountValue(final SessionContext ctx, final AbstractOrderEntry orderEntry,
			final String discountValueCode)
	{
		final Collection<DiscountValue> discounts = orderEntry.getDiscountValues(ctx); // NOSONAR
		for (final DiscountValue dv : discounts)
		{
			if (discountValueCode.equals(dv.getCode()))
			{
				return dv;
			}
		}
		return null;
	}

}
