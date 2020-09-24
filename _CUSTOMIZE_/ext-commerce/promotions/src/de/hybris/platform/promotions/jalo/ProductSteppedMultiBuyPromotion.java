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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;


/**
 * ProductSteppedMultiBuyPromotion.
 *
 * Buy a certain number of items for a fixed package price, buy a larger quantity of those items to qualify for further
 * price tiers. For example: <i>Buy any 3 for &euro;30.00, buy 4 for &euro;35.00 or buy 6 for &euro;50.00</i>. The items
 * must all be from the range of qualifying products.
 *
 *
 */
public class ProductSteppedMultiBuyPromotion extends GeneratedProductSteppedMultiBuyPromotion // NOSONAR
{
	private static final Logger LOG = Logger.getLogger(ProductSteppedMultiBuyPromotion.class.getName());

	/**
	 * remove the item. you can delete this method if you don't want to intercept the removal of this item
	 */
	@Override
	public void remove(final SessionContext ctx) throws ConsistencyCheckException
	{
		// ## business code placed here will be executed before the item is removed

		// Remove any linked price rows
		final Collection<PromotionQuantityAndPricesRow> rows = getQualifyingCountsAndBundlePrices(ctx);
		if (rows != null && !rows.isEmpty())
		{
			for (final PromotionQuantityAndPricesRow row : rows)
			{
				row.remove(ctx);
			}
		}

		// then create the item
		super.remove(ctx);

		// ## business code placed here will be executed after the item was removed
	}

	@Override
	public List<PromotionResult> evaluate(final SessionContext ctx, final PromotionEvaluationContext promoContext) //NOSONAR
	{
		final List<PromotionResult> promotionResults = new ArrayList<>();

		final PromotionsManager.RestrictionSetResult restrictRes = findEligibleProductsInBasket(ctx, promoContext);
		final List<Product> products = restrictRes.getAllowedProducts(); // NOSONAR
		if (restrictRes.isAllowedToContinue() && !restrictRes.getAllowedProducts().isEmpty())
		{
			final SortedSet<QuantityPrice> steps = getSteps(ctx, promoContext.getOrder(),
					this.getQualifyingCountsAndBundlePrices(ctx));
			if (steps != null && !steps.isEmpty())
			{
				final PromotionOrderView pov = promoContext.createView(ctx, this, products);

				// We need to remember which step we triggered last
				QuantityPrice lastTriggeredStep = null;

				// Keep consuming items until exhausted
				while (true)
				{
					// Work out which step is firing
					final QuantityPrice triggeredStep = findStep(steps, pov.getTotalQuantity(ctx));
					if (triggeredStep == null) //NOSONAR
					{
						break;
					}

					// Remember this as the last triggered step
					lastTriggeredStep = triggeredStep;

					promoContext.startLoggingConsumed(this);

					pov.consumeFromHead(ctx, PromotionEvaluationContext.createPriceComparator(ctx), triggeredStep.getQuantity());

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

					final double adjustment = triggeredStep.getPrice() - thisPromoTotal;
					if (LOG.isDebugEnabled()) //NOSONAR
					{
						LOG.debug("(" + getPK() + ") evaluate: triggeredStep quantity=[" + triggeredStep.getQuantity()
								+ "] totalValueOfConsumedEntries=[" + thisPromoTotal + "] promotionPriceValue=["
								+ triggeredStep.getPrice() + "] adjustment=[" + adjustment + "]");
					}

					Helper.adjustUnitPrices(ctx, promoContext, consumedEntries, triggeredStep.getPrice(), thisPromoTotal);

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
					final QuantityPrice nextStep = findNextStep(steps, lastTriggeredStep);
					if (LOG.isDebugEnabled()) //NOSONAR
					{
						LOG.debug("(" + getPK() + ") evaluate: nextStep for protential promotion quantity=[" + nextStep.getQuantity()
								+ "]");
					}

					if (remaining >= nextStep.getQuantity()) //NOSONAR
					{
						LOG.error("(" + getPK() + ") evaluate: nextStep for protential promotion, remaining=[" + remaining
								+ "] is greater than or equal to nextStep.quantity=[" + nextStep.getQuantity() + "]");
					}

					promoContext.startLoggingConsumed(this);
					pov.consume(ctx, remaining);

					float certainty;

					if (lastTriggeredStep != null && lastTriggeredStep != steps.first()) //NOSONAR
					{
						// We are part way towards a bigger step, our certainty needs to be weighted to show that it is easy to
						// jump up to the next step.
						certainty = (float) (remaining + lastTriggeredStep.getQuantity()) // NOSONAR
								/ (float) (nextStep.getQuantity() + lastTriggeredStep.getQuantity());
					}
					else
					{
						// We are at the bottom step, or have wrapped around to the bottom step, so it is the simple case
						certainty = (float) remaining / (float) nextStep.getQuantity(); // NOSONAR
					}

					final PromotionResult currResult = PromotionsManager.getInstance().createPromotionResult(ctx, this,
							promoContext.getOrder(), certainty);
					currResult.setConsumedEntries(ctx, promoContext.finishLoggingAndGetConsumed(this, false));
					currResult.setCustom(ctx, String.valueOf(nextStep.getQuantity()));
					promotionResults.add(currResult);
				}
			}
		}

		return promotionResults;
	}

	@Override
	public String getResultDescription(final SessionContext ctx, final PromotionResult promotionResult, final Locale locale) //NOSONAR
	{
		final AbstractOrder order = promotionResult.getOrder(ctx);
		if (order != null)
		{
			final de.hybris.platform.jalo.c2l.Currency orderCurrency = order.getCurrency(ctx);

			final SortedSet<QuantityPrice> steps = getSteps(ctx, promotionResult.getOrder(),
					this.getQualifyingCountsAndBundlePrices(ctx));
			if (steps != null && !steps.isEmpty())
			{
				if (promotionResult.getFired(ctx))
				{
					double promotionPriceValue = 0D;
					final long consumedCount = promotionResult.getConsumedCount(ctx, false);

					// Find the price for the consumed quantity
					for (final QuantityPrice step : steps) //NOSONAR
					{
						if (step.getQuantity() == consumedCount)
						{
							promotionPriceValue = step.getPrice();
						}
					}

					final double totalDiscount = promotionResult.getTotalDiscount(ctx);

					// "Buy {0,number,integer} productName for {2} - You have saved {4}"
					final ArrayList args = new ArrayList();
					args.add(Long.valueOf(consumedCount));
					args.add(Double.valueOf(promotionPriceValue));
					args.add(Helper.formatCurrencyAmount(ctx, locale, orderCurrency, promotionPriceValue));
					args.add(Double.valueOf(totalDiscount));
					args.add(Helper.formatCurrencyAmount(ctx, locale, orderCurrency, totalDiscount));

					// Get the list of steps in ascending order
					final ArrayList<QuantityPrice> ascendingSteps = new ArrayList<>(steps);
					Collections.reverse(ascendingSteps);

					// Add all the steps to the message args
					for (final QuantityPrice step : ascendingSteps) //NOSONAR
					{
						args.add(Long.valueOf(step.getQuantity()));
						args.add(Double.valueOf(step.getPrice()));
						args.add(Helper.formatCurrencyAmount(ctx, locale, orderCurrency, step.getPrice()));
					}

					return formatMessage(this.getMessageFired(ctx), args.toArray(), locale);
				}
				else if (promotionResult.getCouldFire(ctx))
				{
					// Get the next step from the custom data
					final String customData = promotionResult.getCustom(ctx);
					if (customData != null && customData.length() > 0) //NOSONAR
					{
						final long nextStepQunatity = Long.parseLong(customData);
						double nextStepPrice = 0D;
						QuantityPrice nextStep = null;

						for (final QuantityPrice step : steps)
						{
							if (step.getQuantity() == nextStepQunatity)
							{
								nextStep = step;
								nextStepPrice = step.getPrice();
							}
						}

						QuantityPrice currentStep = null;
						if (nextStep != null)
						{
							final SortedSet<QuantityPrice> lowerSteps = steps.tailSet(nextStep);
							if (lowerSteps.size() > 1)
							{
								final Iterator<QuantityPrice> iter = lowerSteps.iterator();

								// Skip first as it is the 'nextStep'
								iter.next();

								currentStep = iter.next();
							}
						}

						long currentStepQuantity = 0;
						if (currentStep != null)
						{
							currentStepQuantity = currentStep.getQuantity();
						}

						// "Buy {0,number,integer} productName for {2} - Add {3,number,integer} to qualify"
						final ArrayList args = new ArrayList();
						args.add(Long.valueOf(nextStepQunatity));
						args.add(Double.valueOf(nextStepPrice));
						args.add(Helper.formatCurrencyAmount(ctx, locale, orderCurrency, nextStepPrice));
						args.add(Long.valueOf(nextStepQunatity - currentStepQuantity - promotionResult.getConsumedCount(ctx, true)));

						// Get the list of steps in ascending order
						final ArrayList<QuantityPrice> ascendingSteps = new ArrayList<>(steps);
						Collections.reverse(ascendingSteps);

						// Add all the steps to the message args
						for (final QuantityPrice step : ascendingSteps)
						{
							args.add(Long.valueOf(step.getQuantity()));
							args.add(Double.valueOf(step.getPrice()));
							args.add(Helper.formatCurrencyAmount(ctx, locale, orderCurrency, step.getPrice()));
						}

						return formatMessage(this.getMessageCouldHaveFired(ctx), args.toArray(), locale);
					}
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

		final Collection<PromotionQuantityAndPricesRow> rows = getQualifyingCountsAndBundlePrices(ctx);
		if (rows != null && !rows.isEmpty())
		{
			builder.append(rows.size()).append('|');
			for (final PromotionQuantityAndPricesRow row : rows)
			{
				builder.append(row.getQuantity(ctx)).append('|');
				buildDataUniqueKeyForPriceRows(ctx, builder, row.getPrices(ctx));
			}
		}
		else
		{
			builder.append('0').append('|');
		}
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

		// Keep all existing attributes apart from BundlePrices, which we deep clone
		values.remove(ProductSteppedMultiBuyPromotion.QUALIFYINGCOUNTSANDBUNDLEPRICES);

		// Clone price row
		values.put(ProductSteppedMultiBuyPromotion.QUALIFYINGCOUNTSANDBUNDLEPRICES,
				deepCloneQuantityAndPricesRows(ctx, getQualifyingCountsAndBundlePrices(ctx)));
	}

	protected static Collection<PromotionQuantityAndPricesRow> deepCloneQuantityAndPricesRows(final SessionContext ctx,
			final Collection<PromotionQuantityAndPricesRow> rows)
	{
		final Collection<PromotionQuantityAndPricesRow> dupRows = new ArrayList<>();

		if (rows != null && !rows.isEmpty())
		{
			for (final PromotionQuantityAndPricesRow row : rows)
			{
				dupRows.add(PromotionsManager.getInstance().createPromotionQuantityAndPricesRow(ctx, row.getQuantity(ctx).longValue(),
						deepClonePriceRows(ctx, row.getPrices(ctx))));
			}
		}

		return dupRows;
	}

	protected static class QuantityPrice
	{
		private final long quantity;
		private final double price;

		QuantityPrice(final long quantity, final double price)
		{
			this.quantity = quantity;
			this.price = price;
		}

		public long getQuantity()
		{
			return quantity;
		}

		public double getPrice()
		{
			return price;
		}
	}

	protected SortedSet<QuantityPrice> getSteps(final SessionContext ctx, final AbstractOrder order,
			final Collection<PromotionQuantityAndPricesRow> rows)
	{
		// Create a set that sorts by qualifying count descending
		final SortedSet<QuantityPrice> qualifyingCountAndPrices = new TreeSet<>((final QuantityPrice a,
				final QuantityPrice b) -> (Long.valueOf(b.getQuantity())).compareTo(Long.valueOf(a.getQuantity())));

		// Get the step quantities and prices, sort the steps in descending order
		if (rows != null && !rows.isEmpty())
		{
			for (final PromotionQuantityAndPricesRow row : rows)
			{
				// Check that the quantity is more than 0
				final long quantity = row.getQuantity(ctx).longValue();
				if (quantity > 0)
				{
					// Check that the price is set for the order's currency
					final Double promotionPriceValue = this.getPriceForOrder(ctx, row.getPrices(ctx), order,
							ProductSteppedMultiBuyPromotion.QUALIFYINGCOUNTSANDBUNDLEPRICES);
					if (promotionPriceValue != null) //NOSONAR
					{
						qualifyingCountAndPrices.add(new QuantityPrice(quantity, promotionPriceValue.doubleValue()));
					}
				}
			}
		}

		return qualifyingCountAndPrices;
	}

	protected static QuantityPrice findStep(final SortedSet<QuantityPrice> steps, final long count)
	{
		for (final QuantityPrice step : steps)
		{
			if (step.getQuantity() <= count)
			{
				return step;
			}
		}
		return null;
	}

	protected static QuantityPrice findNextStep(final SortedSet<QuantityPrice> steps, final QuantityPrice lastTriggeredStep)
	{
		QuantityPrice nextStep;
		if (lastTriggeredStep == null)
		{
			// Have not triggered, therefore the next step is the lowest step
			nextStep = steps.last();
		}
		else
		{
			final SortedSet<QuantityPrice> higherQuantitySteps = steps.headSet(lastTriggeredStep);
			if (higherQuantitySteps != null && !higherQuantitySteps.isEmpty())
			{
				// The last one will have the lowest quantity from this set
				nextStep = higherQuantitySteps.last();
			}
			else
			{
				// There are no higher steps, wrap around back to the lowest step
				nextStep = steps.last();
			}
		}

		return nextStep;
	}

}
