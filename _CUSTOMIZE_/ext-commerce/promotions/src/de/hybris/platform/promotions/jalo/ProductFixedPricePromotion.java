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


import de.hybris.platform.jalo.ConsistencyCheckException;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.c2l.Currency;
import de.hybris.platform.jalo.order.AbstractOrder;
import de.hybris.platform.promotions.result.PromotionEvaluationContext;
import de.hybris.platform.promotions.result.PromotionOrderEntry;
import de.hybris.platform.promotions.result.PromotionOrderView;
import de.hybris.platform.promotions.util.Helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;


/**
 * ProductFixedPricePromotion.
 *
 * Buy any selected product for a fixed price. For example: <i>Buy any Beatles CD for &euro;10.00</i>. Each cart product
 * in the set of qualifying products will be sold for the specified price.
 *
 */
public class ProductFixedPricePromotion extends GeneratedProductFixedPricePromotion // NOSONAR
{
	private static final Logger LOG = Logger.getLogger(ProductFixedPricePromotion.class);

	/**
	 * Removes the item.
	 */
	@Override
	public void remove(final SessionContext ctx) throws ConsistencyCheckException
	{
		// Remove any linked price rows
		deletePromotionPriceRows(ctx, getProductFixedUnitPrice(ctx));

		// then create the item
		super.remove(ctx);
	}

	private boolean hasPromotionPriceRowForCurrency(final AbstractOrder order,
			final Collection<PromotionPriceRow> promotionPriceRows)
	{
		final String name = this.getComposedType().getName() + " (" + this.getCode() + ": " + this.getTitle() + ")";
		if (promotionPriceRows.isEmpty())
		{
			LOG.warn(name + " has no PromotionPriceRow. Skipping evaluation");
			return false;
		}
		final Currency currency = order.getCurrency();
		for (final PromotionPriceRow ppr : promotionPriceRows)
		{
			if (currency.equals(ppr.getCurrency()))
			{
				return true;
			}
		}
		LOG.warn(name + " has no PromotionPriceRow for currency " + currency.getName() + ". Skipping evaluation");
		return false;
	}

	@Override
	public List<PromotionResult> evaluate(final SessionContext ctx, final PromotionEvaluationContext promoContext)
	{
		final List<PromotionResult> results = new ArrayList<>();

		// Find all valid products in the cart
		final PromotionsManager.RestrictionSetResult rsr = this.findEligibleProductsInBasket(ctx, promoContext);
		final Collection<PromotionPriceRow> promotionPriceRows = this.getProductFixedUnitPrice(ctx);
		final AbstractOrder order = promoContext.getOrder();
		final boolean hasValidPromotionPriceRow = hasPromotionPriceRowForCurrency(order, promotionPriceRows);

		if (hasValidPromotionPriceRow && rsr.isAllowedToContinue() && !rsr.getAllowedProducts().isEmpty())
		{
			final PromotionOrderView view = promoContext.createView(ctx, this, rsr.getAllowedProducts());
			// Every product matched on this promotion ends up being a fixed price
			while (view.getTotalQuantity(ctx) > 0)
			{
				promoContext.startLoggingConsumed(this);

				// Get the next order entry
				final PromotionOrderEntry entry = view.peek(ctx);
				final long quantityToDiscount = entry.getQuantity(ctx);
				final long quantityOfOrderEntry = entry.getBaseOrderEntry().getQuantity(ctx).longValue();

				final Double fixedUnitPrice = this.getPriceForOrder(ctx, promotionPriceRows, order,
						ProductFixedPricePromotion.PRODUCTFIXEDUNITPRICE);
				if (fixedUnitPrice != null)
				{
					// Set the adjusted unit price
					for (final PromotionOrderEntryConsumed poec : view.consume(ctx, quantityToDiscount)) // NOSONAR
					{
						poec.setAdjustedUnitPrice(ctx, fixedUnitPrice);
					}

					// The adjustment to the order entry
					final double adjustment = quantityToDiscount
							* (fixedUnitPrice.doubleValue() - entry.getBasePrice(ctx).doubleValue());

					final PromotionResult result = PromotionsManager.getInstance().createPromotionResult(ctx, this,
							promoContext.getOrder(), 1.0F);

					result.setConsumedEntries(ctx, promoContext.finishLoggingAndGetConsumed(this, true));
					final PromotionOrderEntryAdjustAction poeac = PromotionsManager.getInstance()
							.createPromotionOrderEntryAdjustAction(ctx, entry.getBaseOrderEntry(), quantityOfOrderEntry, adjustment);
					result.addAction(ctx, poeac);

					results.add(result);
				}
				else
				{
					promoContext.abandonLogging(this);
				}
			}

			// There is no partially fired state for this promotion, it either has products to discount or it doesn't.
			return results;
		}

		return results;
	}

	@Override
	public String getResultDescription(final SessionContext ctx, final PromotionResult promotionResult, final Locale locale)
	{
		final AbstractOrder order = promotionResult.getOrder(ctx);
		if (order != null && promotionResult.getFired(ctx))
		{
			final Double fixedUnitPrice = this.getPriceForOrder(ctx, this.getProductFixedUnitPrice(ctx), order,
					ProductFixedPricePromotion.PRODUCTFIXEDUNITPRICE);
			if (fixedUnitPrice != null)
			{
				final double totalDiscount = promotionResult.getTotalDiscount(ctx);
				final de.hybris.platform.jalo.c2l.Currency orderCurrency = order.getCurrency(ctx);

				// "Fixed price of {1} - You have saved {3}"
				final Object[] args =
				{ fixedUnitPrice, Helper.formatCurrencyAmount(ctx, locale, orderCurrency, fixedUnitPrice.doubleValue()),
						Double.valueOf(totalDiscount), Helper.formatCurrencyAmount(ctx, locale, orderCurrency, totalDiscount) };
				return formatMessage(this.getMessageFired(ctx), args, locale);
			}

			// This promotion does not have a could fire state. There are either qualifying items in the cart or not, so there is no ambiguity.
		}

		return "";
	}

	@Override
	protected void buildDataUniqueKey(final SessionContext ctx, final StringBuilder builder)
	{
		super.buildDataUniqueKey(ctx, builder);
		buildDataUniqueKeyForPriceRows(ctx, builder, getProductFixedUnitPrice(ctx));
	}

	/**
	 * Called to deep clone attributes of this instance
	 *
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
	protected void deepCloneAttributes(final SessionContext ctx, final Map values)
	{
		super.deepCloneAttributes(ctx, values);

		// Keep all existing attributes apart from productFixedUnitPrice, which we deep clone
		values.remove(ProductFixedPricePromotion.PRODUCTFIXEDUNITPRICE);

		// Clone price row
		values.put(ProductFixedPricePromotion.PRODUCTFIXEDUNITPRICE, deepClonePriceRows(ctx, getProductFixedUnitPrice(ctx)));
	}

}
