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

import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.order.AbstractOrderEntry;
import de.hybris.platform.jalo.product.Product;
import de.hybris.platform.jalo.product.Unit;
import de.hybris.platform.promotions.jalo.PromotionOrderEntryConsumed;
import de.hybris.platform.promotions.jalo.PromotionResult;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;


/**
 * WrappedOrderEntry. A decorated presentation of OrderEntry which attaches extra information about whether the entry
 * was part of any promotions.
 */
public class WrappedOrderEntry
{
	private final List<PromotionResult> promotionResults = new LinkedList<>();
	private final AbstractOrderEntry baseEntry;
	private long quantity;

	protected WrappedOrderEntry(final SessionContext ctx, final AbstractOrderEntry orderEntry)
	{
		this.baseEntry = orderEntry;
		this.quantity = baseEntry.getQuantity(ctx).longValue();
	}

	@SuppressWarnings("unused")
	protected WrappedOrderEntry(final SessionContext ctx, final AbstractOrderEntry orderEntry, final long quantity) //NOSONAR
	{
		this.baseEntry = orderEntry;
		this.quantity = quantity;
	}

	protected WrappedOrderEntry(final SessionContext ctx, final AbstractOrderEntry orderEntry, final long quantity,
			final Collection<PromotionResult> promotionResults)
	{
		this.baseEntry = orderEntry;
		this.quantity = quantity;
		addPromotionResults(ctx, promotionResults); // NOSONAR
	}

	/**
	 * The quantity of the order entry.
	 *
	 * @return The quantity
	 */
	public long getQuantity()
	{
		return quantity;
	}

	/**
	 * The quantity of the order entry.
	 *
	 * @param ctx
	 *           The hybris session context
	 * @return The quantity
	 */
	public long getQuantity(final SessionContext ctx) //NOSONAR
	{
		return quantity;
	}

	/**
	 * The product in this order entry.
	 *
	 * @return The product in this order entry
	 */
	public Product getProduct() //NOSONAR
	{
		return getProduct(JaloSession.getCurrentSession().getSessionContext());
	}

	/**
	 * The product in this order entry.
	 *
	 * @param ctx
	 *           The hybris session context
	 * @return The product in this order entry
	 */
	public Product getProduct(final SessionContext ctx) //NOSONAR
	{
		return baseEntry.getProduct(ctx);
	}

	/**
	 * The product in this order entry.
	 *
	 * @return The product in this order entry
	 */
	public AbstractOrderEntry getBaseOrderEntry()
	{
		return baseEntry;
	}

	/**
	 * The list of {@link PromotionResult} objects that applied to this order entry.
	 *
	 * @return A list of {@link PromotionResult} objects
	 */
	public List<PromotionResult> getPromotionResults()
	{
		return promotionResults;
	}

	/**
	 * Add a promotion result to this wrapped order entry.
	 *
	 * @param ctx
	 *           The hybris session context
	 * @param promotionResult
	 *           The promotion result to add
	 */
	protected void addPromotionResult(final SessionContext ctx, final PromotionResult promotionResult)
	{
		// Assume list is already in order and use binary search to locate the insertion point.
		// See javadoc for Collections.binarySearch() for details.
		int insertIndex = Collections.binarySearch(promotionResults, promotionResult, new PromotionResultComparator(ctx));
		insertIndex = (insertIndex < 0) ? -insertIndex - 1 : insertIndex;
		promotionResults.add(insertIndex, promotionResult);
	}

	/**
	 * Add a multiple promotion results to this wrapped order entry.
	 *
	 * @param ctx
	 *           The hybris session context
	 * @param promotionResults
	 *           The promotion results to add
	 */
	protected void addPromotionResults(final SessionContext ctx, final Collection<PromotionResult> promotionResults)
	{
		for (final PromotionResult result : promotionResults)
		{
			addPromotionResult(ctx, result);
		}
	}

	/**
	 * Protected method to allow order entries to be adjusted by consuming quantities.
	 *
	 * @param quantity
	 *           The quantity to consume
	 */
	protected void consume(final long quantity)
	{
		if (quantity > this.quantity)
		{
			throw new IllegalArgumentException("Cannot consume more than quantity");
		}
		this.quantity -= quantity;
	}

	/**
	 * Get the total price for this order entry (unit price * quantity).
	 *
	 * @return The total price
	 */
	public double getEntryPrice()
	{
		return getEntryPrice(JaloSession.getCurrentSession().getSessionContext());
	}

	/**
	 * Get the total price for this order entry (unit price * quantity).
	 *
	 * @param ctx
	 *           The hybris session context
	 * @return The total price
	 */
	public double getEntryPrice(final SessionContext ctx)
	{
		return quantity * getUnitPrice(ctx);
	}

	/**
	 * Get the price for one unit of the represented product.
	 *
	 * @return The unit price
	 */
	public double getUnitPrice()
	{
		return getUnitPrice(JaloSession.getCurrentSession().getSessionContext());
	}

	/**
	 * Returns the price for one unit of the represented product.
	 *
	 * @param ctx
	 *           The hybris session context
	 * @return The unit price
	 */
	public double getUnitPrice(final SessionContext ctx)
	{
		return baseEntry.getTotalPrice(ctx).doubleValue() / baseEntry.getQuantity(ctx).longValue();
	}

	/**
	 * Get the unit for this entry.
	 *
	 * @return The unit
	 */
	public Unit getUnit()
	{
		return getUnit(JaloSession.getCurrentSession().getSessionContext());
	}

	/**
	 * Get the unit for this entry.
	 *
	 * @param ctx
	 *           The hybris session context
	 * @return The unit
	 */
	public Unit getUnit(final SessionContext ctx)
	{
		return baseEntry.getUnit(ctx);
	}

	protected boolean consumePromotionOrderEntryConsumed(final SessionContext ctx, final PromotionOrderEntryConsumed poec)
	{
		if (baseEntry.equals(poec.getOrderEntry(ctx))
				//	Consuming the quantity only makes sense for the legacy promotion types. Promotion types defined in promotionengineservices should not be checked here
				&& isLegacyPromotion(poec))
		{
			consume(poec.getQuantity(ctx).longValue());
			return true;
		}
		return false;
	}

	/**
	 * Returns false if the Promotion of the Promotion Result is/extends one from promotionengineservices. True
	 * otherwise.
	 */
	protected boolean isLegacyPromotion(final PromotionOrderEntryConsumed poec)
	{
		if (poec == null || poec.getPromotionResult() == null || poec.getPromotionResult().getPromotion() == null)
		{
			return true;
		}
		Class clazz = poec.getPromotionResult().getPromotion().getClass();
		while (clazz != null)
		{
			if (clazz.getName().contains("promotionengineservices"))
			{
				return false;
			}
			clazz = clazz.getSuperclass();
		}
		return true;
	}

	/**
	 * Comparator for {@link PromotionResult} that compares by certainty and then by priority.
	 */
	protected static class PromotionResultComparator implements Comparator<PromotionResult>
	{
		private final SessionContext ctx;

		public PromotionResultComparator(final SessionContext ctx)
		{
			this.ctx = ctx;
		}

		@Override
		public int compare(final PromotionResult a, final PromotionResult b)
		{
			// Sort by certainty, descending
			int result = b.getCertainty(ctx).compareTo(a.getCertainty(ctx));
			if (result == 0)
			{
				// Sort by priority, descending
				result = b.getPromotion(ctx).getPriority(ctx).compareTo(a.getPromotion(ctx).getPriority(ctx));
			}
			return result;
		}
	}

}
