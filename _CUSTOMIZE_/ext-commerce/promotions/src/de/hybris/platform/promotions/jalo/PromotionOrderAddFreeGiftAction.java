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
import de.hybris.platform.jalo.product.Unit;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;


/**
 * PromotionOrderAddFreeGiftAction. Action that adds a specified product to the order as a give away item. Applying this
 * action creates a new order entry for the free product with the quantity set to 1. This order entry is marked as a
 * give away. Undoing this action removes the free product from the order.
 *
 *
 */
public class PromotionOrderAddFreeGiftAction extends GeneratedPromotionOrderAddFreeGiftAction //NOSONAR
{
	private static final Logger LOG = Logger.getLogger(PromotionOrderAddFreeGiftAction.class.getName());
	private static final long QUANTITY = 1;


	@Override
	public boolean apply(final SessionContext ctx)
	{
		final AbstractOrder order = getPromotionResult(ctx).getOrder(ctx);

		// Create a new order entry
		final Product product = this.getFreeProduct(ctx); // NOSONAR
		final Unit unit = product.getUnit(ctx);

		if (LOG.isDebugEnabled())
		{
			LOG.debug("(" + getPK() + ") apply: Adding " + QUANTITY + " free gift to Cart with " + order.getAllEntries().size() // NOSONAR
					+ " order entries.");
		}
		final AbstractOrderEntry orderEntry = order.addNewEntry(product, 1, unit, false); // NOSONAR
		if (LOG.isDebugEnabled())
		{
			LOG.debug("(" + getPK() + ") apply: Adding " + QUANTITY + " free gift.  There are now " + order.getAllEntries().size() // NOSONAR
					+ " order entries.");
		}
		orderEntry.setGiveAway(ctx, true);

		if (LOG.isDebugEnabled())
		{
			LOG.debug("(" + getPK() + ") apply: Created a free gift order entry with " + orderEntry.getDiscountValues(ctx).size() // NOSONAR
					+ " discount values");
		}

		// Now show that the new order entry has been consumed by the Promotion
		final PromotionResult pr = this.getPromotionResult(ctx);

		// Create a new promotion order entry to hold the gift, adjusted unit price is 0
		final PromotionOrderEntryConsumed consumed = PromotionsManager.getInstance().createPromotionOrderEntryConsumed(ctx,
				this.getGuid(ctx), orderEntry, 1);
		consumed.setAdjustedUnitPrice(ctx, 0.0D);

		pr.addConsumedEntry(ctx, consumed);

		setMarkedApplied(ctx, true);

		// Return true, this action has added an order entry and therefore needs to calculate totals
		return true;
	}

	@Override
	public boolean undo(final SessionContext ctx) //NOSONAR
	{
		final AbstractOrder order = getPromotionResult(ctx).getOrder(ctx);

		if (LOG.isDebugEnabled())
		{
			LOG.debug("(" + getPK() + ") undo: Undoing add free gift from order with " + order.getAllEntries().size() // NOSONAR
					+ " order entries");
		}

		for (final AbstractOrderEntry aoe : (List<AbstractOrderEntry>) order.getAllEntries()) // NOSONAR
		{
			if (aoe.isGiveAway(ctx).booleanValue() && aoe.getProduct(ctx).equals(this.getFreeProduct(ctx))
					&& aoe.getQuantity(ctx).longValue() >= QUANTITY)
			{
				final long remainingQuantityAfterUndo = aoe.getQuantity(ctx).longValue() - QUANTITY;
				if (remainingQuantityAfterUndo < 1)
				{
					if (LOG.isDebugEnabled())//NOSONAR
					{
						LOG.debug("(" + getPK()
								+ ") undo: Line item has the same or less quantity than the offer.  Removing whole order entry.");
					}
					order.removeEntry(aoe); // NOSONAR
				}
				else
				{
					if (LOG.isDebugEnabled())//NOSONAR
					{
						LOG.debug("(" + getPK()
								+ ") undo: Line item has a greater quantity than the offer.  Removing the offer quantity and resetting giveaway flag.");
					}
					aoe.setQuantity(ctx, remainingQuantityAfterUndo);
					aoe.setGiveAway(ctx, false);
					//PRO-72, calculate the entry price since the product is no longer "free gift"
					try//NOSONAR
					{
						aoe.recalculate(); // NOSONAR
					}
					catch (final JaloPriceFactoryException jpe) //NOSONAR
					{
						LOG.error("unable to calculate the entry: " + jpe.getMessage());
					}
				}

				// Remove promotion order entry consumed
				final PromotionResult pr = this.getPromotionResult(ctx);
				final Collection<PromotionOrderEntryConsumed> consumedEntries = pr.getConsumedEntries(ctx);
				final Collection<PromotionOrderEntryConsumed> toRemoveConsumedEntries = new HashSet<>();
				for (final PromotionOrderEntryConsumed poec : consumedEntries)
				{
					if (poec.getCode(ctx).equals(this.getGuid(ctx)))//NOSONAR
					{
						toRemoveConsumedEntries.add(poec);
					}
				}

				for (final PromotionOrderEntryConsumed poec : toRemoveConsumedEntries)
				{
					pr.removeConsumedEntry(ctx, poec);
				}
				break;
			}
		}

		setMarkedApplied(ctx, false);

		if (LOG.isDebugEnabled())
		{
			LOG.debug("(" + getPK() + ") undo: Free gift removed from order which now has " + order.getAllEntries().size() // NOSONAR
					+ " order entries");
		}
		return true;
	}

	@Override
	public boolean isAppliedToOrder(final SessionContext ctx)
	{
		final AbstractOrder order = getPromotionResult(ctx).getOrder(ctx);

		for (final AbstractOrderEntry aoe : (List<AbstractOrderEntry>) order.getAllEntries()) // NOSONAR
		{
			if (aoe.isGiveAway(ctx).booleanValue() && aoe.getProduct(ctx).equals(this.getFreeProduct(ctx))
					&& aoe.getQuantity(ctx).longValue() >= QUANTITY)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public double getValue(final SessionContext ctx)
	{
		return 0.0D;
	}

	/**
	 * Called to deep clone attributes of this instance
	 * <p/>
	 * The values map contains all the attributes defined on this instance. The map will be used to initialize a new
	 * instance of the Action that is a clone of this instance. This method can remove, replace or add to the Map of
	 * attributes.
	 *
	 * @param ctx
	 *           The hybris context
	 * @param values
	 *           The map to write into
	 */
	@Override
	protected void deepCloneAttributes(final SessionContext ctx, final Map values)//NOSONAR
	{
		super.deepCloneAttributes(ctx, values);

		// leave all attributes in map
	}

}
