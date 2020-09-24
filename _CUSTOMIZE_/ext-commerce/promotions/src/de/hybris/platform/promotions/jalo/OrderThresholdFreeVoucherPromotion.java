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
 * OrderThresholdFreeVoucherPromotion.
 *
 * Get a voucher free when you spend at least a certain value. For example: <i>Get a free voucher when you spend over
 * &euro;150.00</i>. Get a voucher when your order subtotal is at least the threshold value.
 * <p />
 * Notes for implementers:<br />
 * The OrderThresholdFreeVoucherPromotion does not actually apply the voucher to the cart. When this promotion fires and
 * returns a PromotionResult, that result can be applied and undone. This has no side-effects, but the applied state of
 * the result is stored. It is up to the site implementer to check for applied promotion results that are for
 * OrderThresholdFreeVoucherPromotions and retrieve the {@link #getFreeVoucher()} from the promotion and to give this to
 * the user.
 *
 *
 */
public class OrderThresholdFreeVoucherPromotion extends GeneratedOrderThresholdFreeVoucherPromotion //NOSONAR
{
	private static final Logger LOG = Logger.getLogger(OrderThresholdFreeVoucherPromotion.class.getName());

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
		final List<PromotionResult> promotionResults = new ArrayList<>();

		if (checkRestrictions(ctx, promoContext))
		{
			final Double threshold = this.getPriceForOrder(ctx, this.getThresholdTotals(ctx), promoContext.getOrder(),
					OrderThresholdFreeGiftPromotion.THRESHOLDTOTALS);
			if (threshold != null)
			{
				final AbstractOrder order = promoContext.getOrder();
				final double orderSubtotalAfterDiscounts = getOrderSubtotalAfterDiscounts(ctx, order);

				// If we pass the threshold then fire
				if (orderSubtotalAfterDiscounts >= threshold.doubleValue())
				{
					if (LOG.isDebugEnabled()) //NOSONAR
					{
						LOG.debug("(" + getPK() + ") evaluate: Subtotal " + orderSubtotalAfterDiscounts + ">" + threshold.doubleValue()
								+ ".  Creating a null action.");
					}
					final PromotionResult result = PromotionsManager.getInstance().createPromotionResult(ctx, this,
							promoContext.getOrder(), 1.0F);

					result.addAction(ctx, PromotionsManager.getInstance().createPromotionNullAction(ctx));

					promotionResults.add(result);
				}
				// Otherwise calculate the certainty by seeing how close the order is to meeting the threshold
				else
				{
					if (LOG.isDebugEnabled()) //NOSONAR
					{
						LOG.debug("(" + getPK() + ") evaluate: Subtotal " + orderSubtotalAfterDiscounts + "<" + threshold
								+ ".  Skipping null action.");
					}
					final float certainty = (float) (orderSubtotalAfterDiscounts / threshold.doubleValue());
					final PromotionResult result = PromotionsManager.getInstance().createPromotionResult(ctx, this,
							promoContext.getOrder(), certainty);
					promotionResults.add(result);
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
					OrderThresholdFreeGiftPromotion.THRESHOLDTOTALS);
			if (threshold != null)
			{
				if (result.getFired(ctx))
				{
					// "You got a free Voucher for spending over {1}"
					final Object[] args =
					{ threshold, Helper.formatCurrencyAmount(ctx, locale, orderCurrency, threshold.doubleValue()) };
					return formatMessage(this.getMessageFired(ctx), args, locale);
				}
				else if (result.getCouldFire(ctx))
				{
					final double orderSubtotalAfterDiscounts = getOrderSubtotalAfterDiscounts(ctx, order);
					final double amountRequired = threshold.doubleValue() - orderSubtotalAfterDiscounts;

					// "Spend {1} to get a free Voucher - Spend another {3} to qualify"
					final Object[] args =
					{ threshold, Helper.formatCurrencyAmount(ctx, locale, orderCurrency, threshold.doubleValue()),
							Double.valueOf(amountRequired), Helper.formatCurrencyAmount(ctx, locale, orderCurrency, amountRequired) };
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

		builder.append(getFreeVoucher(ctx).getName(ctx)).append('|');
		buildDataUniqueKeyForPriceRows(ctx, builder, getThresholdTotals(ctx));
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

		values.remove(OrderThresholdFreeGiftPromotion.THRESHOLDTOTALS);

		// Clone price row
		values.put(OrderThresholdFreeGiftPromotion.THRESHOLDTOTALS, deepClonePriceRows(ctx, getThresholdTotals(ctx)));
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
		builder.append(getFreeVoucher(ctx).getName(ctx)).append('|');

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
