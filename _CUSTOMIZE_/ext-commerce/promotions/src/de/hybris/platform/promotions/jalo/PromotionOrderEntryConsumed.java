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

import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.product.Product;
import de.hybris.platform.jalo.product.Unit;

import org.apache.log4j.Logger;


/**
 * PromotionOrderEntryConsumed. The PromotionOrderEntryConsumed represents a quantity of products consumed or
 * potentially consumed by a promotion. This is a view over an {@link de.hybris.platform.jalo.order.AbstractOrderEntry}
 * where the quantity may be less than or equal to the quantity of the underlying order entry. The unit price is taken
 * from the underlying order entry and the entry price is calculated from this and the quantity specified. There is also
 * an adjusted unit price and a calculated adjusted entry price. The adjusted unit price is set by the promotion
 * represent the change in price if the promotion's actions are applied.
 *
 *
 */
public class PromotionOrderEntryConsumed extends GeneratedPromotionOrderEntryConsumed //NOSONAR
{
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(PromotionOrderEntryConsumed.class.getName());

	// Default to true so that new items are always consumed
	private transient boolean removedFromOrder = true;

	/**
	 * The original price for this order entry.
	 *
	 * @return The price
	 */
	public final double getEntryPrice()
	{
		return getEntryPrice(JaloSession.getCurrentSession().getSessionContext());
	}

	/**
	 * The original price for this order entry.
	 *
	 * @param ctx
	 *           The hybris context
	 * @return The price
	 */
	public double getEntryPrice(final SessionContext ctx)
	{
		return getQuantity(ctx).doubleValue() * getUnitPrice(ctx);
	}

	/**
	 * The original unit price for this order entry.
	 *
	 * @return The price
	 */
	public final double getUnitPrice()
	{
		return getUnitPrice(JaloSession.getCurrentSession().getSessionContext());
	}

	/**
	 * The original unit price for this order entry.
	 *
	 * @param ctx
	 *           The hybris context
	 * @return The price
	 */
	public double getUnitPrice(final SessionContext ctx)
	{
		return getOrderEntry(ctx).getBasePrice(ctx).doubleValue();
	}

	/**
	 * Flag to indicate if this promotion order entry is consumed. This property is internal and transient. It should not
	 * be used from customer code.
	 *
	 * @return flag
	 */
	public boolean isRemovedFromOrder()
	{
		return removedFromOrder;
	}

	/**
	 * Flag to indicate if this promotion order entry is consumed. This property is internal and transient. It should not
	 * be used from customer code.
	 *
	 * @param removedFromOrder
	 *           flag
	 */
	public void setRemovedFromOrder(final boolean removedFromOrder)
	{
		this.removedFromOrder = removedFromOrder;
	}

	/**
	 * Get the product for this order entry.
	 *
	 * @return The product
	 */
	public final Product getProduct() // NOSONAR
	{
		return getProduct(JaloSession.getCurrentSession().getSessionContext());
	}

	/**
	 * Get the product for this order entry.
	 *
	 * @param ctx
	 *           The hybris context
	 * @return The product
	 */
	public Product getProduct(final SessionContext ctx) // NOSONAR
	{
		return getOrderEntry(ctx).getProduct(ctx);
	}

	/**
	 * Get the units for this order entry.
	 *
	 * @return The units
	 */
	public final Unit getUnit()
	{
		return getUnit(JaloSession.getCurrentSession().getSessionContext());
	}

	/**
	 * Get the units for this order entry.
	 *
	 * @param ctx
	 *           The hybris context
	 * @return The units
	 */
	public Unit getUnit(final SessionContext ctx)
	{
		return getOrderEntry(ctx).getUnit(ctx);
	}

	/**
	 * Get the adjusted price for this order entry. This method uses the adjusted unit price to calculate the price for
	 * the entry.
	 *
	 * @return The adjusted price
	 */
	public final double getAdjustedEntryPrice()
	{
		return getAdjustedEntryPrice(JaloSession.getCurrentSession().getSessionContext());
	}

	/**
	 * Get the adjusted price for this order entry. This method uses the adjusted unit price to calculate the price for
	 * the entry.
	 *
	 * @param ctx
	 *           The hybris context
	 * @return The adjusted price
	 */
	public double getAdjustedEntryPrice(final SessionContext ctx)
	{
		return getQuantity(ctx).longValue() * getAdjustedUnitPrice(ctx).doubleValue();
	}
}
