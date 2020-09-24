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
import de.hybris.platform.promotions.util.Helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;


/**
 * OrderThresholdDiscountPromotion.
 *
 * Get a fixed discount when you spend at least a certain value. For example: <i>Save &euro;5.00 when you spend over
 * &euro;150.00</i>. Get a discount when your order subtotal is at least the threshold value.
 *
 *
 */
public class OrderThresholdDiscountPromotion extends GeneratedOrderThresholdDiscountPromotion //NOSONAR
{
	private static final Logger LOG = Logger.getLogger(OrderThresholdDiscountPromotion.class.getName());

	/**
	 * remove the item.
	 */
	@Override
	public void remove(final SessionContext ctx) throws ConsistencyCheckException
	{
		// ## business code placed here will be executed before the item is removed

		// Remove any linked price rows
		deletePromotionPriceRows(ctx, getThresholdTotals(ctx));

		// Remove any linked price rows
		deletePromotionPriceRows(ctx, getDiscountPrices(ctx));

		// then create the item
		super.remove(ctx);

		// ## business code placed here will be executed after the item was removed
	}

	@Override
	public List<PromotionResult> evaluate(final SessionContext ctx, final PromotionEvaluationContext promoContext) // NOSONAR
	{
		final List<PromotionResult> promotionResults = new ArrayList<>();

		if (checkRestrictions(ctx, promoContext))
		{
			final Double threshold = this.getPriceForOrder(ctx, this.getThresholdTotals(ctx), promoContext.getOrder(),
					OrderThresholdDiscountPromotion.THRESHOLDTOTALS);
			if (threshold != null)
			{
				// Get the discount price
				final Double discountPriceValue = this.getPriceForOrder(ctx, this.getDiscountPrices(ctx), promoContext.getOrder(),
						OrderThresholdDiscountPromotion.DISCOUNTPRICES);
				if (discountPriceValue != null)
				{
					final AbstractOrder order = promoContext.getOrder();
					final double orderSubtotalAfterDiscounts = getOrderSubtotalAfterDiscounts(ctx, order);

					// If we pass the threshold then fire, and add an action to put a discount in the basket
					if (orderSubtotalAfterDiscounts >= threshold.doubleValue()) //NOSONAR
					{
						if (LOG.isDebugEnabled())
						{
							LOG.debug("(" + getPK() + ") evaluate: Subtotal " + orderSubtotalAfterDiscounts + ">" + threshold
									+ ".  Creating a discount action for value:" + discountPriceValue + ".");
						}
						final PromotionResult result = PromotionsManager.getInstance().createPromotionResult(ctx, this,
								promoContext.getOrder(), 1.0F);

						//make sure that the discount value is not greater than the sub total value of the abstract order
						double realDiscountPriceValue = discountPriceValue.doubleValue();
						if (realDiscountPriceValue > orderSubtotalAfterDiscounts)
						{
							realDiscountPriceValue = orderSubtotalAfterDiscounts;
						}
						result.addAction(ctx,
								PromotionsManager.getInstance().createPromotionOrderAdjustTotalAction(ctx, -realDiscountPriceValue));

						promotionResults.add(result);
					}
					// Otherwise calculate the certainty by seeing how close the order is to meeting the threshold
					else
					{
						if (LOG.isDebugEnabled())
						{
							LOG.debug("(" + getPK() + ") evaluate: Subtotal " + orderSubtotalAfterDiscounts + "<" + threshold
									+ ".  Skipping discount action.");
						}
						final float certainty = (float) (orderSubtotalAfterDiscounts / threshold.doubleValue());
						final PromotionResult result = PromotionsManager.getInstance().createPromotionResult(ctx, this,
								promoContext.getOrder(), certainty);
						promotionResults.add(result);
					}
				}
			}
		}

		return promotionResults;
	}

	@Override
	public String getResultDescription(final SessionContext ctx, final PromotionResult result, final Locale locale)
	{
		final AbstractOrder order = result.getOrder(ctx);
		if (order != null)
		{
			final Currency orderCurrency = order.getCurrency(ctx);

			final Double threshold = this.getPriceForOrder(ctx, this.getThresholdTotals(ctx), order,
					OrderThresholdDiscountPromotion.THRESHOLDTOTALS);
			if (threshold != null)
			{
				// Discount price for product
				final Double discountPriceValue = this.getPriceForOrder(ctx, this.getDiscountPrices(ctx), order,
						OrderThresholdDiscountPromotion.DISCOUNTPRICES);
				if (discountPriceValue != null)
				{
					if (result.getFired(ctx)) //NOSONAR
					{
						// "You saved {3} for spending over {1}"
						final Object[] args =
						{ threshold, Helper.formatCurrencyAmount(ctx, locale, orderCurrency, threshold.doubleValue()),
								discountPriceValue,
								Helper.formatCurrencyAmount(ctx, locale, orderCurrency, discountPriceValue.doubleValue()) };
						return formatMessage(this.getMessageFired(ctx), args, locale);
					}
					else if (result.getCouldFire(ctx))
					{
						final double orderSubtotalAfterDiscounts = getOrderSubtotalAfterDiscounts(ctx, order);
						final double amountRequired = threshold.doubleValue() - orderSubtotalAfterDiscounts;

						// "Spend {1} to get a discount of {3} - Spend another {5} to qualify"
						final Object[] args =
						{ threshold, Helper.formatCurrencyAmount(ctx, locale, orderCurrency, threshold.doubleValue()),
								discountPriceValue,
								Helper.formatCurrencyAmount(ctx, locale, orderCurrency, discountPriceValue.doubleValue()),
								Double.valueOf(amountRequired), Helper.formatCurrencyAmount(ctx, locale, orderCurrency, amountRequired) };
						return formatMessage(this.getMessageCouldHaveFired(ctx), args, locale);
					}
				}
			}
		}

		return "";
	}

	@Override
	protected void buildDataUniqueKey(final SessionContext ctx, final StringBuilder builder)
	{
		super.buildDataUniqueKey(ctx, builder);

		buildDataUniqueKeyForPriceRows(ctx, builder, getThresholdTotals(ctx));
		buildDataUniqueKeyForPriceRows(ctx, builder, getDiscountPrices(ctx));
	}

	/**
	 * Called to deep clone attributes of this instance. The values map contains all the attributes defined on this
	 * instance. The map will be used to initialize a new instance of the Action that is a clone of this instance. This
	 * method can remove, replace or add to the Map of attributes.
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

		values.remove(OrderThresholdDiscountPromotion.THRESHOLDTOTALS);
		values.remove(OrderThresholdDiscountPromotion.DISCOUNTPRICES);

		// Clone price row
		values.put(OrderThresholdDiscountPromotion.THRESHOLDTOTALS, deepClonePriceRows(ctx, getThresholdTotals(ctx)));
		values.put(OrderThresholdDiscountPromotion.DISCOUNTPRICES, deepClonePriceRows(ctx, getDiscountPrices(ctx)));
	}

	/**
	 * Override buildPromotionResultDataUnigueKey. This implementation ignores the consumed products because they are
	 * added by applying the action.
	 *
	 * @param ctx
	 *           The hybris context
	 * @param promotionResult
	 *           the promotion result
	 * @param builder
	 *           the builder to create the unique key in
	 */
	@Override
	protected void buildPromotionResultDataUnigueKey(final SessionContext ctx, final PromotionResult promotionResult,
			final StringBuilder builder)
	{
		builder.append(promotionResult.getCertainty(ctx)).append('|');
		builder.append(promotionResult.getCustom(ctx)).append('|');

		final Collection<AbstractPromotionAction> actions = promotionResult.getActions(ctx);
		if (actions != null && !actions.isEmpty())
		{
			for (final AbstractPromotionAction action : actions)
			{
				builder.append(action.getClass().getSimpleName()).append('|');
			}
		}
	}

}
