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
import de.hybris.platform.jalo.c2l.Currency;
import de.hybris.platform.jalo.order.AbstractOrder;
import de.hybris.platform.promotions.result.PromotionEvaluationContext;
import de.hybris.platform.promotions.result.PromotionOrderEntry;
import de.hybris.platform.promotions.result.PromotionOrderView;
import de.hybris.platform.promotions.util.Helper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;


/**
 * ProductPercentageDiscountPromotion.
 *
 * Get a percentage off of the price of any of these products. For example: <i>15% off of all garden furniture</i>,
 * <i>10% off of all chart CDs</i>. Each cart product in the set of qualifying products will have its price reduced by
 * the specified percentage.
 *
 */
public class ProductPercentageDiscountPromotion extends GeneratedProductPercentageDiscountPromotion //NOSONAR
{
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(ProductPercentageDiscountPromotion.class);

	@Override
	public List<PromotionResult> evaluate(final SessionContext ctx, final PromotionEvaluationContext promoContext) //NOSONAR
	{
		final List<PromotionResult> results = new ArrayList<>();

		// Find all valid products in the cart
		final PromotionsManager.RestrictionSetResult rsr = this.findEligibleProductsInBasket(ctx, promoContext);

		if (rsr.isAllowedToContinue() && !rsr.getAllowedProducts().isEmpty())
		{
			final PromotionOrderView view = promoContext.createView(ctx, this, rsr.getAllowedProducts());
			final PromotionsManager promotionsManager = PromotionsManager.getInstance();

			// Every product matched on this promotion ends up being a fixed price
			while (view.getTotalQuantity(ctx) > 0)
			{
				promoContext.startLoggingConsumed(this);

				// Get the next order entry
				final PromotionOrderEntry entry = view.peek(ctx);
				final BigDecimal quantityToDiscount = BigDecimal.valueOf(entry.getQuantity(ctx));
				final BigDecimal quantityOfOrderEntry = BigDecimal.valueOf(entry.getBaseOrderEntry().getQuantity(ctx).longValue());

				final BigDecimal percentageDiscount = new BigDecimal(this.getPercentageDiscount(ctx).toString())
						.divide(new BigDecimal("100.0"));

				// The adjustment to the order entry
				final BigDecimal originalUnitPrice = new BigDecimal(entry.getBasePrice(ctx).toString());
				final BigDecimal originalEntryPrice = originalUnitPrice.multiply(quantityToDiscount);

				final Currency currency = promoContext.getOrder().getCurrency(ctx);

				// Calculate the new entry price and round it to a valid amount
				final BigDecimal adjustedEntryPrice = Helper.roundCurrencyValue(ctx, currency,
						originalEntryPrice.subtract(originalEntryPrice.multiply(percentageDiscount)));

				// Calculate the unit price and round it
				final BigDecimal adjustedUnitPrice = Helper.roundCurrencyValue(ctx, currency,
						adjustedEntryPrice.equals(BigDecimal.ZERO) ? BigDecimal.ZERO
								: adjustedEntryPrice.divide(quantityToDiscount, RoundingMode.HALF_EVEN));

				// Work out the fiddle amount that cannot be shared amongst the quantityToDiscount
				final BigDecimal fiddleAmount = adjustedEntryPrice.subtract(adjustedUnitPrice.multiply(quantityToDiscount));

				// Is the fiddleAmount 0 (i.e. can we adjust the unit price for all items)
				if (fiddleAmount.compareTo(BigDecimal.ZERO) == 0)
				{
					for (final PromotionOrderEntryConsumed poec : view.consume(ctx, quantityToDiscount.longValue())) //NOSONAR
					{
						poec.setAdjustedUnitPrice(ctx, adjustedUnitPrice.doubleValue());
					}
				}
				else
				{
					// We have to fiddle the unit price of the last item

					// Apply normal adjusted price to all but 1 products
					for (final PromotionOrderEntryConsumed poec : view.consume(ctx, quantityToDiscount.longValue() - 1)) //NOSONAR
					{
						poec.setAdjustedUnitPrice(ctx, adjustedUnitPrice.doubleValue());
					}

					// Adjust the last product with the fiddleAmount
					for (final PromotionOrderEntryConsumed poec : view.consume(ctx, 1)) //NOSONAR
					{
						poec.setAdjustedUnitPrice(ctx,
								Helper.roundCurrencyValue(ctx, currency, adjustedUnitPrice.add(fiddleAmount)).doubleValue());
					}
				}

				final PromotionResult result = promotionsManager.createPromotionResult(ctx, this, promoContext.getOrder(), 1.0F);
				result.setConsumedEntries(ctx, promoContext.finishLoggingAndGetConsumed(this, true));
				final BigDecimal adjustment = Helper.roundCurrencyValue(ctx, currency,
						adjustedEntryPrice.subtract(originalEntryPrice));
				final PromotionOrderEntryAdjustAction poeac = promotionsManager.createPromotionOrderEntryAdjustAction(ctx,
						entry.getBaseOrderEntry(), quantityOfOrderEntry.longValue(), adjustment.doubleValue());
				result.addAction(ctx, poeac);

				results.add(result);
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
		if (order != null)
		{
			final Currency orderCurrency = order.getCurrency(ctx);

			if (promotionResult.getFired(ctx))
			{
				final Double totalDiscount = Double.valueOf(promotionResult.getTotalDiscount(ctx));
				final Double percentageDiscount = this.getPercentageDiscount(ctx);

				// "{0} percentage discount - You have saved {2}"
				final Object[] args =
				{ percentageDiscount, totalDiscount,
						Helper.formatCurrencyAmount(ctx, locale, orderCurrency, totalDiscount.doubleValue()) };
				return formatMessage(this.getMessageFired(ctx), args, locale);
			}
			// This promotion does not have a could fire state. There are either qualifying items in the cart or not, so there is no abiguity.
		}

		return "";
	}

	@Override
	protected void buildDataUniqueKey(final SessionContext ctx, final StringBuilder builder)
	{
		super.buildDataUniqueKey(ctx, builder);
		builder.append(getPercentageDiscount(ctx)).append('|');
	}

}
