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
import de.hybris.platform.jalo.product.Product;
import de.hybris.platform.promotions.result.PromotionEvaluationContext;
import de.hybris.platform.promotions.result.PromotionOrderView;
import de.hybris.platform.promotions.util.Helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;


/**
 * Buy a certain number of items for a fixed package price. For example: <i>Buy any 3 for &euro;20.00</i> or any other
 * combination of required quantity and price. The items must all be from the range of qualifying products.
 *
 */
public class ProductMultiBuyPromotion extends GeneratedProductMultiBuyPromotion // NOSONAR
{
	private static final Logger LOG = Logger.getLogger(ProductMultiBuyPromotion.class);

	/**
	 * Removes the item.
	 */
	@Override
	public void remove(final SessionContext ctx) throws ConsistencyCheckException
	{
		// Remove any linked price rows
		deletePromotionPriceRows(ctx, getBundlePrices(ctx));

		// then create the item
		super.remove(ctx);
	}

	@Override
	public List<PromotionResult> evaluate(final SessionContext ctx, final PromotionEvaluationContext promoContext) // NOSONAR
	{
		final List<PromotionResult> promotionResults = new ArrayList<>();
		final PromotionsManager.RestrictionSetResult restrictRes = findEligibleProductsInBasket(ctx, promoContext);
		final List<Product> products = restrictRes.getAllowedProducts(); // NOSONAR

		if (restrictRes.isAllowedToContinue() && !restrictRes.getAllowedProducts().isEmpty())
		{
			final Double promotionPriceValue = this.getPriceForOrder(ctx, this.getBundlePrices(ctx), promoContext.getOrder(),
					ProductMultiBuyPromotion.BUNDLEPRICES);
			if (promotionPriceValue != null)
			{
				final int triggerSize = this.getQualifyingCount(ctx).intValue();
				final PromotionOrderView pov = promoContext.createView(ctx, this, products);

				// Keep consuming items until exhausted
				while (pov.getTotalQuantity(ctx) >= triggerSize)
				{
					promoContext.startLoggingConsumed(this);
					pov.consumeFromHead(ctx, PromotionEvaluationContext.createPriceComparator(ctx), triggerSize);

					// Now work out the discount
					double thisPromoTotal = 0.0D;
					final List<PromotionOrderEntryConsumed> consumedEntries = promoContext.finishLoggingAndGetConsumed(this, true);
					if (consumedEntries != null && !consumedEntries.isEmpty()) //NOSONAR
					{
						for (final PromotionOrderEntryConsumed poec : consumedEntries)
						{
							thisPromoTotal += poec.getEntryPrice(ctx);
						}
					}

					final double adjustment = promotionPriceValue.doubleValue() - thisPromoTotal;
					if (LOG.isDebugEnabled()) //NOSONAR
					{
						LOG.debug("(" + getPK() + ") evaluate: totalValueOfConsumedEntries=[" + thisPromoTotal
								+ "] promotionPriceValue=[" + promotionPriceValue + "] adjustment=[" + adjustment + "]");
					}

					Helper.adjustUnitPrices(ctx, promoContext, consumedEntries, promotionPriceValue.doubleValue(), thisPromoTotal);

					final PromotionResult currResult = PromotionsManager.getInstance().createPromotionResult(ctx, this,
							promoContext.getOrder(), 1.0F);
					currResult.setConsumedEntries(ctx, consumedEntries);
					currResult.addAction(ctx, PromotionsManager.getInstance().createPromotionOrderAdjustTotalAction(ctx, adjustment));
					promotionResults.add(currResult);
				}

				// Check for partial firing
				final long remaining = pov.getTotalQuantity(ctx);
				if (remaining > 0)
				{
					promoContext.startLoggingConsumed(this);
					pov.consume(ctx, remaining);

					final float certainty = (float) remaining / (float) triggerSize; // NOSONAR
					final PromotionResult currResult = PromotionsManager.getInstance().createPromotionResult(ctx, this,
							promoContext.getOrder(), certainty);
					currResult.setConsumedEntries(ctx, promoContext.finishLoggingAndGetConsumed(this, false));
					promotionResults.add(currResult);
				}
			}
		}

		return promotionResults;
	}

	@Override
	public String getResultDescription(final SessionContext ctx, final PromotionResult promotionResult, final Locale locale)
	{
		final AbstractOrder order = promotionResult.getOrder(ctx);
		if (order != null)
		{
			final Currency orderCurrency = order.getCurrency(ctx);
			final int qualifyingCount = this.getQualifyingCount(ctx).intValue();

			final Double promotionPriceValue = this.getPriceForOrder(ctx, this.getBundlePrices(ctx), promotionResult.getOrder(ctx),
					ProductMultiBuyPromotion.BUNDLEPRICES);
			if (promotionPriceValue != null)
			{
				if (promotionResult.getFired(ctx))
				{
					final double totalDiscount = promotionResult.getTotalDiscount(ctx);

					// "Buy {0,number,integer} productName for {2} - You have saved {4}"
					final Object[] args =
					{ Integer.valueOf(qualifyingCount), promotionPriceValue,
							Helper.formatCurrencyAmount(ctx, locale, orderCurrency, promotionPriceValue.doubleValue()),
							Double.valueOf(totalDiscount), Helper.formatCurrencyAmount(ctx, locale, orderCurrency, totalDiscount) };
					return formatMessage(this.getMessageFired(ctx), args, locale);
				}
				else if (promotionResult.getCouldFire(ctx))
				{
					// "Buy {0,number,integer} productName for {2} - Add {3,number,integer} to qualify"
					final Object[] args =
					{ Integer.valueOf(qualifyingCount), promotionPriceValue,
							Helper.formatCurrencyAmount(ctx, locale, orderCurrency, promotionPriceValue.doubleValue()),
							Long.valueOf(qualifyingCount - promotionResult.getConsumedCount(ctx, true)) };
					return formatMessage(this.getMessageCouldHaveFired(ctx), args, locale);
				}
			}
		}

		// No data
		return "";
	}

	@Override
	protected void buildDataUniqueKey(final SessionContext ctx, final StringBuilder builder)
	{
		super.buildDataUniqueKey(ctx, builder);

		builder.append(getQualifyingCount(ctx)).append('|');
		buildDataUniqueKeyForPriceRows(ctx, builder, getBundlePrices(ctx));
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

		// Keep all existing attributes apart from BundlePrices, which we deep clone
		values.remove(ProductMultiBuyPromotion.BUNDLEPRICES);

		// Clone price row
		values.put(ProductMultiBuyPromotion.BUNDLEPRICES, deepClonePriceRows(ctx, getBundlePrices(ctx)));
	}

}
