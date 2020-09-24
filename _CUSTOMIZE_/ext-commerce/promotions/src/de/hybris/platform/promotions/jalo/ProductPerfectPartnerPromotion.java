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
 * ProductPerfectPartnerPromotion.
 *
 * Buy a product from set A together with a product from set B, get B for a fixed price. For example: <i>Buy a games
 * console and one of these selected games, get the game for &euro;10.00</i>, <i>Buy any drill and a pair of safety
 * goggles, get the safety goggles at the special price of &euro;5.00</i>. The cart must contain one product from each
 * set of qualifying products to qualify.
 *
 *
 */
public class ProductPerfectPartnerPromotion extends GeneratedProductPerfectPartnerPromotion //NOSONAR
{
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(ProductPerfectPartnerPromotion.class.getName());
	private static final float TRIGGER_BUT_NO_PARTNER = 0.75F;

	/**
	 * remove the item. you can delete this method if you don't want to intercept the removal of this item
	 */
	@Override
	public void remove(final SessionContext ctx) throws ConsistencyCheckException
	{
		// ## business code placed here will be executed before the item is removed

		// Remove any linked price rows
		deletePromotionPriceRows(ctx, getPartnerPrices(ctx));

		// then create the item
		super.remove(ctx);

		// ## business code placed here will be executed after the item was removed
	}

	@Override
	public List<PromotionResult> evaluate(final SessionContext ctx, final PromotionEvaluationContext promoContext)
	{
		final List<PromotionResult> promotionResults = new ArrayList<>();

		final Double partnerProductPrice = this.getPriceForOrder(ctx, this.getPartnerPrices(ctx), promoContext.getOrder(),
				ProductPerfectPartnerPromotion.PARTNERPRICES);
		if (partnerProductPrice != null)
		{
			final PromotionsManager.RestrictionSetResult rsr = this.findEligibleProductsInBasket(ctx, promoContext);
			if (rsr.isAllowedToContinue() && !rsr.getAllowedProducts().isEmpty())
			{
				final PromotionOrderView triggerItemView = promoContext.createView(ctx, this, rsr.getAllowedProducts());
				final PromotionOrderView partnerItemView = promoContext.createView(ctx, this,
						(List<Product>) this.getPartnerProducts(ctx));// NOSONAR

				while (triggerItemView.getTotalQuantity(ctx) > 0)
				{
					promoContext.startLoggingConsumed(this);

					triggerItemView.consume(ctx, 1);
					if (partnerItemView.getTotalQuantity(ctx) > 0) //NOSONAR
					{
						// Fire
						final PromotionOrderEntryConsumed poec = partnerItemView.consume(ctx, 1).get(0);

						final double partnerProductRetailPrice = poec.getUnitPrice(ctx);

						// The adjustment to the order entry
						final double adjustment = partnerProductPrice.doubleValue() - partnerProductRetailPrice;

						// Adjust the unit price to reflect the partner price
						poec.setAdjustedUnitPrice(partnerProductPrice);

						final AbstractPromotionAction action = PromotionsManager.getInstance()
								.createPromotionOrderEntryAdjustAction(ctx, poec.getOrderEntry(ctx), adjustment);

						final PromotionResult result = PromotionsManager.getInstance().createPromotionResult(ctx, this,
								promoContext.getOrder(), 1.0F);
						result.setConsumedEntries(promoContext.finishLoggingAndGetConsumed(this, true));
						result.addAction(ctx, action);

						promotionResults.add(result);
					}
					else
					{
						// Partially fire
						final PromotionResult result = PromotionsManager.getInstance().createPromotionResult(ctx, this,
								promoContext.getOrder(), TRIGGER_BUT_NO_PARTNER);
						result.setConsumedEntries(ctx, promoContext.finishLoggingAndGetConsumed(this, false));
						promotionResults.add(result);
						break;
					}
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
			final de.hybris.platform.jalo.c2l.Currency orderCurrency = order.getCurrency(ctx);

			final Double offerPrice = this.getPriceForOrder(ctx, this.getPartnerPrices(ctx), promotionResult.getOrder(ctx),
					ProductPerfectPartnerPromotion.PARTNERPRICES);
			if (offerPrice != null)
			{
				if (promotionResult.getFired(ctx))
				{
					final double totalDiscount = promotionResult.getTotalDiscount(ctx);

					// "Buy ProductName, get PartnerProductName for {1} - You have saved {3}"
					final Object[] args =
					{ offerPrice, Helper.formatCurrencyAmount(ctx, locale, orderCurrency, offerPrice.doubleValue()),
							Double.valueOf(totalDiscount), Helper.formatCurrencyAmount(ctx, locale, orderCurrency, totalDiscount) };
					return formatMessage(this.getMessageFired(ctx), args, locale);
				}
				else if (promotionResult.getCouldFire(ctx))
				{
					// "Buy ProductName, get PartnerProductName for {1}"
					final Object[] args =
					{ offerPrice, Helper.formatCurrencyAmount(ctx, locale, orderCurrency, offerPrice.doubleValue()) };
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

		buildDataUniqueKeyForProducts(ctx, builder, getPartnerProducts(ctx));
		buildDataUniqueKeyForPriceRows(ctx, builder, getPartnerPrices(ctx));
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
	protected void deepCloneAttributes(final SessionContext ctx, final Map values)
	{
		super.deepCloneAttributes(ctx, values);

		// Keep all existing attributes apart from partnerPrices, which we deep clone
		values.remove(ProductPerfectPartnerPromotion.PARTNERPRICES);

		// Clone price row
		values.put(ProductPerfectPartnerPromotion.PARTNERPRICES, deepClonePriceRows(ctx, getPartnerPrices(ctx)));
	}

}
