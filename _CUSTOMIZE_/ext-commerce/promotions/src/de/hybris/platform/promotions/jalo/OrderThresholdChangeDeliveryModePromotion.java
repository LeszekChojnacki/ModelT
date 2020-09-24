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
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * OrderThresholdChangeDeliveryModePromotion.
 *
 * Get a different shipping method when you spend at least a certain value. For example: <i>Get free next day delivery
 * when you spend &euro;150.00</i>. Change the shipping mode when the order subtotal is at least the threshold value.
 *
 *
 */
public class OrderThresholdChangeDeliveryModePromotion extends GeneratedOrderThresholdChangeDeliveryModePromotion //NOSONAR
{

	/**
	 * remove the item. you can delete this method if you don't want to intercept the removal of this item
	 */
	@Override
	public void remove(final SessionContext ctx) throws ConsistencyCheckException
	{
		// ## business code placed here will be executed before the item is removed

		// Remove any linked price rows
		deletePromotionPriceRows(ctx, getThresholdTotals(ctx));

		// then create the item
		super.remove(ctx);

		// ## business code placed here will be executed after the item was removed
	}

	@Override
	public List<PromotionResult> evaluate(final SessionContext ctx, final PromotionEvaluationContext promoContext)
	{
		final List<PromotionResult> results = new ArrayList<>();

		if (checkRestrictions(ctx, promoContext))
		{
			// Allowed to continue
			final AbstractOrder order = promoContext.getOrder();
			final double orderSubtotalAfterDiscounts = getOrderSubtotalAfterDiscounts(ctx, order);

			// Check the order threshold value
			final Double thresholdPriceValue = this.getPriceForOrder(ctx, this.getThresholdTotals(ctx), promoContext.getOrder(),
					OrderThresholdPerfectPartnerPromotion.THRESHOLDTOTALS);
			if (thresholdPriceValue != null)
			{
				if (orderSubtotalAfterDiscounts >= thresholdPriceValue.doubleValue())
				{
					final PromotionResult result = PromotionsManager.getInstance().createPromotionResult(ctx, this,
							promoContext.getOrder(), 1.0F);
					result.addAction(ctx, PromotionsManager.getInstance().createPromotionOrderChangeDeliveryModeAction(ctx,
							this.getDeliveryMode(ctx)));
					results.add(result);
				}
				else
				{
					final float certainty =
							(float) orderSubtotalAfterDiscounts / (float) thresholdPriceValue.doubleValue(); // NOSONAR
					final PromotionResult result = PromotionsManager.getInstance().createPromotionResult(ctx, this,
							promoContext.getOrder(), certainty);
					results.add(result);
				}
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

			final Double threshold = this.getPriceForOrder(ctx, this.getThresholdTotals(ctx), order,
					OrderThresholdChangeDeliveryModePromotion.THRESHOLDTOTALS);
			if (threshold != null)
			{
				if (promotionResult.getFired(ctx))
				{
					// "You spent over {1} to qualify for enhanced shipping"
					final Object[] args =
					{ threshold, Helper.formatCurrencyAmount(ctx, locale, order.getCurrency(ctx), threshold.doubleValue()) };
					return formatMessage(this.getMessageFired(ctx), args, locale);
				}
				else if (promotionResult.getCouldFire(ctx))
				{
					final double orderSubtotalAfterDiscounts = getOrderSubtotalAfterDiscounts(ctx, order);
					final double difference = threshold.doubleValue() - orderSubtotalAfterDiscounts;

					// "Spend {1} to get enhanced shipping - Spend another {3} qualify"
					final Object[] args =
					{ threshold, Helper.formatCurrencyAmount(ctx, locale, orderCurrency, threshold.doubleValue()),
							Double.valueOf(difference), Helper.formatCurrencyAmount(ctx, locale, orderCurrency, difference) };
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

		builder.append(getDeliveryMode(ctx).getCode(ctx)).append('|');
		buildDataUniqueKeyForPriceRows(ctx, builder, getThresholdTotals(ctx));
	}

	/**
	 * Called to deep clone attributes of this instance
	 * <p/>
	 * The values map contains all the attributes defined on this instance. The map will be used to initialse a new
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

		values.remove(OrderThresholdFreeGiftPromotion.THRESHOLDTOTALS);

		// Clone price row
		values.put(OrderThresholdFreeGiftPromotion.THRESHOLDTOTALS, deepClonePriceRows(ctx, getThresholdTotals(ctx)));
	}

}
