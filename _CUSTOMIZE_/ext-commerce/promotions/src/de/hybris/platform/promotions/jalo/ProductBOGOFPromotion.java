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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;


/**
 * Buy x get y free promotion.
 *
 * Buy a certain number of items, get specified number of lowest valued items for free. For example: <i>Buy 1 get 1
 * free</i> (also known as <i>Buy 2 for the price of 1</i>), Buy 3 for the price of 2</i> or any combination of paid for
 * and free products. The items must all be from the range of qualifying products.
 *
 */
public class ProductBOGOFPromotion extends GeneratedProductBOGOFPromotion //NOSONAR
{

	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(ProductBOGOFPromotion.class);

	@Override
	public List<PromotionResult> evaluate(final SessionContext ctx, final PromotionEvaluationContext promoContext)
	{
		final List<PromotionResult> results = new ArrayList<>();

		// Find the eligible products, and apply any restrictions
		final PromotionsManager.RestrictionSetResult restrictResult = findEligibleProductsInBasket(ctx, promoContext);

		// If the restrictions did not reject this promotion, and there are still products allowed after the restrictions
		if (restrictResult.isAllowedToContinue() && !restrictResult.getAllowedProducts().isEmpty())
		{
			final int qualifyingCount = this.getQualifyingCount(ctx).intValue();
			final int freeCount = this.getFreeCount(ctx).intValue();
			final PromotionsManager promotionsManager = PromotionsManager.getInstance();

			// Create a view of the order containing only the allowed products
			final PromotionOrderView orderView = promoContext.createView(ctx, this, restrictResult.getAllowedProducts());

			// Repeat whilst there are enough products remaining to allow the promotion to fire
			while (orderView.getTotalQuantity(ctx) >= qualifyingCount)
			{
				// Begin logging of promotions consuming order entries
				promoContext.startLoggingConsumed(this);

				// Get a price comparator
				final Comparator<PromotionOrderEntry> comparator = PromotionEvaluationContext.createPriceComparator(ctx);

				// Consume high priced items as these are the ones that will be paid for
				orderView.consumeFromTail(ctx, comparator, (qualifyingCount - freeCount)); // NOSONAR

				// Consume the free items from the cheap end of the list, as these result in the lowest discount
				final List<PromotionOrderEntryConsumed> freeItems = orderView.consumeFromHead(ctx, comparator, freeCount);

				// Create the actions to take for this promotion to fire.  In this case an entry level discount is created
				// for each of the free items.
				final List<AbstractPromotionAction> actions = new ArrayList<>();
				for (final PromotionOrderEntryConsumed poec : freeItems)
				{
					// Set the adjusted unit price to zero, these are free items
					poec.setAdjustedUnitPrice(ctx, 0D);

					final double adjustment = poec.getEntryPrice(ctx) * -1.0D;

					// This action creates an order entry discount.
					actions.add(promotionsManager.createPromotionOrderEntryAdjustAction(ctx, poec.getOrderEntry(ctx), adjustment));
					// Create a global adjustment to reduce value by product entry price
					//actions.add(PromotionsManager.getInstance().createPromotionOrderAdjustTotalAction(ctx, adjustment)); //NOSONAR
				}

				// Put together a the result for this iteration.
				final PromotionResult result = promotionsManager.createPromotionResult(ctx, this, promoContext.getOrder(), 1.0F);

				// Get a list of all the order entries that were consumed during this run
				final List<PromotionOrderEntryConsumed> consumed = promoContext.finishLoggingAndGetConsumed(this, true);
				result.setConsumedEntries(ctx, consumed);

				// Add the actions that this promotion has produced
				result.setActions(ctx, actions);

				// Add the result object to the list of results
				results.add(result);
			}

			// At this point the promotion cannot fire any more, so evaluate what the chances are of it firing again
			// Check to see if there are still some qualifying products in the basket
			final long remainingCount = orderView.getTotalQuantity(ctx);
			if (orderView.getTotalQuantity(ctx) > 0)
			{
				// Start logging the products we could take
				promoContext.startLoggingConsumed(this);

				// Consume the products passing false the removeFromOrder.  This means that we noted which products
				// *could* cause us to fire, but are not actually removing them from the context.
				orderView.consume(ctx, remainingCount);

				// The certainty for this is calculated as a percentage based on the qualifying items available versus
				// the number needed to make this promotion fire.
				final float certainty = (float) remainingCount / (float) qualifyingCount; // NOSONAR

				// Create the promotion result
				final PromotionResult result = promotionsManager.createPromotionResult(ctx, this, promoContext.getOrder(), certainty);

				// Fill in the entries we could have consumed
				result.setConsumedEntries(promoContext.finishLoggingAndGetConsumed(this, false));

				// Add the result to the list of results
				results.add(result);
			}
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

			final Integer qualifyingCount = this.getQualifyingCount(ctx);
			final Integer freeCount = this.getFreeCount(ctx);

			if (promotionResult.getFired(ctx))
			{
				final double totalDiscount = promotionResult.getTotalDiscount(ctx);

				// "These items qualify for our buy {0,number,integer} get {1,number,integer} free offer - You have saved {3}"
				final Object[] args =
				{ qualifyingCount, freeCount, Double.valueOf(totalDiscount),
						Helper.formatCurrencyAmount(ctx, locale, orderCurrency, totalDiscount) };
				return formatMessage(this.getMessageFired(ctx), args, locale);
			}
			else if (promotionResult.getCouldFire(ctx))
			{
				// "Buy {0,choice,1#one more item|1<another {0,number,integer} items} to qualify for our buy {1,number,integer} get {2,number,integer} free offer"
				final Object[] args =
				{ Long.valueOf(this.getQualifyingCount(ctx).longValue() - promotionResult.getConsumedCount(ctx, true)),
						qualifyingCount, freeCount };
				return formatMessage(this.getMessageCouldHaveFired(ctx), args, locale);
			}
		}
		return "";
	}

	@Override
	protected void buildDataUniqueKey(final SessionContext ctx, final StringBuilder builder)
	{
		super.buildDataUniqueKey(ctx, builder);

		builder.append(getQualifyingCount(ctx)).append('|');
		builder.append(getFreeCount(ctx)).append('|');
	}

}
