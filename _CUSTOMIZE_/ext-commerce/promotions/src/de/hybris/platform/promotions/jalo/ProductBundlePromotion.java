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
 * ProductBundlePromotion.
 *
 * Buy one of each of the items specified for a fixed price. For example: <i>Buy product A, B and C together for
 * &euro;35.00</i> or any selection of specific products for a fixed price. The cart must contain one of every product
 * in the qualifying products for this promotion to fire.
 *
 */
public class ProductBundlePromotion extends GeneratedProductBundlePromotion //NOSONAR
{
	private static final Logger LOG = Logger.getLogger(ProductBundlePromotion.class);

	/**
	 * remove the item. you can delete this method if you don't want to intercept the removal of this item
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
	public List<PromotionResult> evaluate(final SessionContext ctx, final PromotionEvaluationContext promoContext) //NOSONAR
	{
		final List<PromotionResult> promotionResults = new ArrayList<>();
		final PromotionsManager.RestrictionSetResult rsr = this.findAllProducts(ctx, promoContext);

		if (rsr.isAllowedToContinue() && !rsr.getAllowedProducts().isEmpty())
		{
			final List<Product> allBundleProducts = rsr.getAllowedProducts(); // NOSONAR
			final PromotionOrderView view = promoContext.createView(ctx, this, allBundleProducts);
			final long neededToFireCount = allBundleProducts.size();

			while (true)
			{
				promoContext.startLoggingConsumed(this);

				// Count the number of available bundle products in the basket.  Take one of each.
				long foundCount = 0;
				for (final Product product : allBundleProducts) // NOSONAR
				{
					final long availableQuantity = view.getQuantity(ctx, product);
					if (availableQuantity > 0) // NOSONAR
					{
						view.consume(ctx, product, 1);
						foundCount++;
					}
				}

				if (foundCount == neededToFireCount)
				{
					// Firing, so work out the discount.  Go through each consumed item and add the price up
					final List<PromotionOrderEntryConsumed> consumedEntries = promoContext.finishLoggingAndGetConsumed(this, true);
					double bundleRetailValue = 0.0D;
					for (final PromotionOrderEntryConsumed poec : consumedEntries) //NOSONAR
					{
						bundleRetailValue += poec.getUnitPrice(ctx);
					}

					final Double offerValue = this.getPriceForOrder(ctx, this.getBundlePrices(ctx), promoContext.getOrder(),
							ProductBundlePromotion.BUNDLEPRICES);
					if (offerValue != null) //NOSONAR
					{
						Helper.adjustUnitPrices(ctx, promoContext, consumedEntries, offerValue.doubleValue(), bundleRetailValue);

						final PromotionOrderAdjustTotalAction poata = PromotionsManager.getInstance()
								.createPromotionOrderAdjustTotalAction(ctx, offerValue.doubleValue() - bundleRetailValue);
						final List<AbstractPromotionAction> actions = new ArrayList<>();
						actions.add(poata);

						final PromotionResult result = PromotionsManager.getInstance().createPromotionResult(ctx, this,
								promoContext.getOrder(), 1.0F);
						result.setConsumedEntries(ctx, consumedEntries);
						result.setActions(actions);

						promotionResults.add(result);
					}
					else
					{
						promoContext.abandonLogging(this);
					}
				}
				else
				{
					if (foundCount > 0) // NOSONAR
					{
						final float certainty = (float) foundCount / (float) neededToFireCount; // NOSONAR
						final PromotionResult result = PromotionsManager.getInstance().createPromotionResult(ctx, this,
								promoContext.getOrder(), certainty);
						result.setConsumedEntries(ctx, promoContext.finishLoggingAndGetConsumed(this, false));
						promotionResults.add(result);
					}
					else
					{
						promoContext.abandonLogging(this);
					}
					break;
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

			final Double offerValue = this.getPriceForOrder(ctx, this.getBundlePrices(ctx), promotionResult.getOrder(ctx),
					ProductBundlePromotion.BUNDLEPRICES);
			if (offerValue != null)
			{
				if (promotionResult.getFired(ctx))
				{
					final double totalDiscount = promotionResult.getTotalDiscount(ctx);

					// "Buy this bundle for {1} - You have saved {3}"
					final Object[] args =
					{ offerValue, Helper.formatCurrencyAmount(ctx, locale, orderCurrency, offerValue.doubleValue()),
							Double.valueOf(totalDiscount), Helper.formatCurrencyAmount(ctx, locale, orderCurrency, totalDiscount) };
					return formatMessage(this.getMessageFired(ctx), args, locale);
				}
				else if (promotionResult.getCouldFire(ctx))
				{
					// Avoid having to run a query again to see the size of the result set.  We can infer this from the certainty and the consumed count
					final long consumedCount = promotionResult.getConsumedCount(ctx, true);
					final long neededCount =
							Math.round(consumedCount / promotionResult.getCertainty(ctx).floatValue()); // NOSONAR

					if (LOG.isDebugEnabled()) // NOSONAR
					{
						LOG.debug("(" + getPK() + ") getResultDescription: consumedCount=[" + consumedCount + "] certainty=["
								+ promotionResult.getCertainty(ctx) + "] neededCount=[" + neededCount + "]");
					}

					// "Buy {0,choice,1#one more item|1<another {0,number,integer} items} from this bundle to get these products for {2}"
					final Object[] args =
					{ Long.valueOf(neededCount - consumedCount), offerValue,
							Helper.formatCurrencyAmount(ctx, locale, orderCurrency, offerValue.doubleValue()) };
					return formatMessage(this.getMessageCouldHaveFired(ctx), args, locale);
				}
			}
		}
		return "";
	}

	@Override
	protected void buildDataUniqueKey(final SessionContext ctx, final StringBuilder builder)
	{
		super.buildDataUniqueKey(ctx, builder);
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
		values.remove(ProductBundlePromotion.BUNDLEPRICES);

		// Clone price row
		values.put(ProductBundlePromotion.BUNDLEPRICES, deepClonePriceRows(ctx, getBundlePrices(ctx)));
	}

}
