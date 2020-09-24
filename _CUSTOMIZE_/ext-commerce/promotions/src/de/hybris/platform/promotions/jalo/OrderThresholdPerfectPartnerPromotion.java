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
import de.hybris.platform.promotions.result.PromotionOrderEntry;
import de.hybris.platform.promotions.result.PromotionOrderView;
import de.hybris.platform.promotions.util.Helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;


/**
 * Order Threshold Perfect Partner Promotion.
 *
 * Get this product for a set price when you spend at least the specified value. For example: <i>Get a gravy boat for
 * &euro;14.00 when you spend over &euro;150.00</i>. Get the partner product at a fixed price when your order subtotal
 * is at least the threshold value.
 *
 *
 * @version v1.0
 */
public class OrderThresholdPerfectPartnerPromotion extends GeneratedOrderThresholdPerfectPartnerPromotion // NOSONAR
{
	private static final Logger LOG = Logger.getLogger(OrderThresholdPerfectPartnerPromotion.class.getName());

	/**
	 * remove the item. you can delete this method if you don't want to intercept the removal of this item
	 */
	@Override
	public void remove(final SessionContext ctx) throws ConsistencyCheckException
	{
		// ## business code placed here will be executed before the item is removed

		// Remove any linked price rows
		deletePromotionPriceRows(ctx, getThresholdTotals(ctx));
		deletePromotionPriceRows(ctx, getProductPrices(ctx));

		// then create the item
		super.remove(ctx);

		// ## business code placed here will be executed after the item was removed
	}

	@Override
	public List<PromotionResult> evaluate(final SessionContext ctx, final PromotionEvaluationContext promoContext) //NOSONAR
	{
		final List<PromotionResult> promotionResults = new ArrayList<>();

		if (checkRestrictions(ctx, promoContext))
		{
			// Allowed to continue
			final AbstractOrder order = promoContext.getOrder();
			final double orderSubtotalAfterDiscounts = getOrderSubtotalAfterDiscounts(ctx, order);

			if (LOG.isDebugEnabled())
			{
				LOG.debug("(" + getPK() + ") evaluate: orderSubtotalAfterDiscounts=[" + orderSubtotalAfterDiscounts
						+ "] orderSubtotal=[" + order.getSubtotal(ctx) + "] orderTotal=[" + order.getTotal(ctx) + "] totalDiscounts=[" // NOSONAR
						+ order.getTotalDiscounts(ctx) + "] totalTax=[" + order.getTotalTax(ctx) + "]"); // NOSONAR
			}

			// Check the order threshold value
			final Double thresholdPriceValue = this.getPriceForOrder(ctx, this.getThresholdTotals(ctx), promoContext.getOrder(),
					OrderThresholdPerfectPartnerPromotion.THRESHOLDTOTALS);
			if (thresholdPriceValue != null)
			{
				// Only allow the actual product
				final List<Product> allowedProducts = new ArrayList<>(1); // NOSONAR
				allowedProducts.add(this.getDiscountProduct(ctx));

				final PromotionOrderView pov = promoContext.createView(ctx, this, allowedProducts);

				if (pov.getTotalQuantity(ctx) > 0)
				{
					// Order contains a discounted product

					// Get the discount product price
					final Double discountPriceValue = this.getPriceForOrder(ctx, this.getProductPrices(ctx), promoContext.getOrder(),
							OrderThresholdPerfectPartnerPromotion.PRODUCTPRICES);
					if (discountPriceValue != null) //NOSONAR
					{
						final Comparator<PromotionOrderEntry> priceComparator = PromotionEvaluationContext.createPriceComparator(ctx);

						// Peek at the cheapest product to get its unit price
						final double productPrice = pov.peekFromHead(ctx, priceComparator).getBasePrice(ctx).doubleValue();

						// Adjust the threshold to take into account the reduced value of the discounted product
						double adjustedThreshold = thresholdPriceValue.doubleValue() + productPrice;
						if (isIncludeDiscountedPriceInThreshold(ctx).booleanValue())
						{
							adjustedThreshold -= discountPriceValue.doubleValue();
						}

						if (LOG.isDebugEnabled())
						{
							LOG.debug("(" + getPK() + ") evaluate: Order contains product. orderSubtotalAfterDiscounts=["
									+ orderSubtotalAfterDiscounts + "] thresholdPrice=[" + thresholdPriceValue + "] productPrice=[" // NOSONAR
									+ productPrice + "] discountPrice=[" + discountPriceValue + "] adjustedThreshold=[" + adjustedThreshold
									+ "]");
						}

						if (orderSubtotalAfterDiscounts >= adjustedThreshold)
						{
							// Order has reached threshold, can fire
							if (LOG.isDebugEnabled())
							{
								LOG.debug("(" + getPK() + ") evaluate: Order contains product and has reached threshold");
							}

							promoContext.startLoggingConsumed(this);

							// Consume the partner product
							final PromotionOrderEntryConsumed poec = pov.consumeFromHead(ctx, priceComparator, 1).get(0);

							// Adjust the unit price of the partner product
							poec.setAdjustedUnitPrice(ctx, discountPriceValue);

							// Now work out the discount
							final double adjustment = discountPriceValue.doubleValue() - productPrice;
							if (LOG.isDebugEnabled())
							{
								LOG.debug("(" + getPK() + ") evaluate: discountPrice=[" + discountPriceValue + "] productPrice=["
										+ productPrice + "] adjustment=[" + adjustment + "]");
							}

							final PromotionResult currResult = PromotionsManager.getInstance().createPromotionResult(ctx, this,
									promoContext.getOrder(), 1.0F);
							currResult.setConsumedEntries(ctx, promoContext.finishLoggingAndGetConsumed(this, true));
							currResult.addAction(ctx,
									PromotionsManager.getInstance().createPromotionOrderAdjustTotalAction(ctx, adjustment));
							promotionResults.add(currResult);
						}
						else
						{
							// Order has not reached threshold
							if (LOG.isDebugEnabled())
							{
								LOG.debug("(" + getPK() + ") evaluate: Order contains product. But has not reached threshold");
							}
							promoContext.startLoggingConsumed(this);

							pov.consumeFromHead(ctx, priceComparator, 1);

							float certainty = 0.5f;
							if (adjustedThreshold > 0 && orderSubtotalAfterDiscounts > 0)
							{
								// Limit to range 0.5+ to 1.0
								certainty = (float) ((orderSubtotalAfterDiscounts / adjustedThreshold) * 0.5d) + 0.5f; // NOSONAR
							}

							final PromotionResult currResult = PromotionsManager.getInstance().createPromotionResult(ctx, this,
									promoContext.getOrder(), certainty);
							currResult.setConsumedEntries(ctx, promoContext.finishLoggingAndGetConsumed(this, false));
							promotionResults.add(currResult);
						}
					}
				}
				else
				{
					// Order does not contain a discounted product
					if (orderSubtotalAfterDiscounts >= thresholdPriceValue.doubleValue()) //NOSONAR
					{
						// Order has reached threshold
						if (LOG.isDebugEnabled())
						{
							LOG.debug("(" + getPK()
									+ ") evaluate: Order does not contain product, but has reached threshold value. orderSubtotalAfterDiscounts=["
									+ orderSubtotalAfterDiscounts + "] thresholdPrice=[" + thresholdPriceValue + "]");
						}

						final PromotionResult currResult = PromotionsManager.getInstance().createPromotionResult(ctx, this,
								promoContext.getOrder(), 0.6f);
						promotionResults.add(currResult);
					}
					else
					{
						// Order has not yet reached threshold
						if (LOG.isDebugEnabled())
						{
							LOG.debug("(" + getPK()
									+ ") evaluate: Order does not contain product, not reached threshold value. orderSubtotalAfterDiscounts=["
									+ orderSubtotalAfterDiscounts + "] thresholdPrice=[" + thresholdPriceValue + "]");
						}

						float certainty = 0f;
						if (thresholdPriceValue.doubleValue() > 0 && orderSubtotalAfterDiscounts > 0)
						{
							// Limit to range 0.0 to 0.5
							certainty = (float) ((orderSubtotalAfterDiscounts / thresholdPriceValue.doubleValue()) * 0.5d);
						}

						final PromotionResult currResult = PromotionsManager.getInstance().createPromotionResult(ctx, this,
								promoContext.getOrder(), certainty);
						promotionResults.add(currResult);
					}
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
			final Currency orderCurrency = order.getCurrency(ctx);

			// Check the order threshold value
			final Double thresholdPriceValue = this.getPriceForOrder(ctx, this.getThresholdTotals(ctx), order,
					OrderThresholdPerfectPartnerPromotion.THRESHOLDTOTALS);
			if (thresholdPriceValue != null)
			{
				// Discount price for product
				final Double discountPriceValue = this.getPriceForOrder(ctx, this.getProductPrices(ctx), order,
						OrderThresholdPerfectPartnerPromotion.PRODUCTPRICES);
				if (discountPriceValue != null)
				{
					// Check if the promotion has fired
					if (promotionResult.getFired(ctx)) //NOSONAR
					{
						final double totalSaving = promotionResult.getTotalDiscount(ctx);

						// "Spend {1} get ProductName for {3} - You have saved {5}"
						final Object[] args =
						{ thresholdPriceValue,
								Helper.formatCurrencyAmount(ctx, locale, orderCurrency, thresholdPriceValue.doubleValue()),
								discountPriceValue,
								Helper.formatCurrencyAmount(ctx, locale, orderCurrency, discountPriceValue.doubleValue()),
								Double.valueOf(totalSaving), Helper.formatCurrencyAmount(ctx, locale, orderCurrency, totalSaving) };
						return formatMessage(this.getMessageFired(ctx), args, locale);
					}
					else
					{
						final double orderSubtotalAfterDiscounts = getOrderSubtotalAfterDiscounts(ctx, order);

						// Check if the product is in the order
						final Collection<PromotionOrderEntryConsumed> consumedEntries = promotionResult.getConsumedEntries(ctx);
						if (consumedEntries != null && !consumedEntries.isEmpty())
						{
							// Sum the total value of the discounted products (should only be 1)
							double productPrice = 0d;
							for (final PromotionOrderEntryConsumed entry : consumedEntries)
							{
								productPrice += entry.getEntryPrice(ctx);
							}

							// Adjust the threshold to take into account the reduced value of the discounted product
							double adjustedThreshold = thresholdPriceValue.doubleValue() + productPrice;
							if (isIncludeDiscountedPriceInThreshold(ctx).booleanValue())
							{
								adjustedThreshold -= discountPriceValue.doubleValue();
							}

							final double amountMoreRequiredToQualify = adjustedThreshold - orderSubtotalAfterDiscounts;

							// Found matching product in cart - not met threshold
							// "Spend {1} get ProductName for {3} - Spend {5} more to qualify!"
							final Object[] args =
							{ thresholdPriceValue,
									Helper.formatCurrencyAmount(ctx, locale, orderCurrency, thresholdPriceValue.doubleValue()),
									discountPriceValue,
									Helper.formatCurrencyAmount(ctx, locale, orderCurrency, discountPriceValue.doubleValue()),
									Double.valueOf(amountMoreRequiredToQualify),
									Helper.formatCurrencyAmount(ctx, locale, orderCurrency, amountMoreRequiredToQualify) };
							return formatMessage(this.getMessageProductNoThreshold(ctx), args, locale);
						}
						else
						{
							// No matching product in cart
							final float certainty = promotionResult.getCertainty(ctx).floatValue();

							if (certainty <= 0.5f)
							{
								// Order has not yet reached threshold & does not contain product

								// Work out how much more the user has to spend to reach the total
								final double amountMoreRequiredToQualify = thresholdPriceValue.doubleValue()
										- orderSubtotalAfterDiscounts;

								// "Spend {1} get ProductName for {3} - Spend {5} more and add productName to your order to qualify!"
								final Object[] args =
								{ thresholdPriceValue,
										Helper.formatCurrencyAmount(ctx, locale, orderCurrency, thresholdPriceValue.doubleValue()),
										discountPriceValue,
										Helper.formatCurrencyAmount(ctx, locale, orderCurrency, discountPriceValue.doubleValue()),
										Double.valueOf(amountMoreRequiredToQualify),
										Helper.formatCurrencyAmount(ctx, locale, orderCurrency, amountMoreRequiredToQualify) };
								return formatMessage(this.getMessageCouldHaveFired(ctx), args, locale);
							}
							else
							{
								// Order has reached the threshold, but does not contain product
								// "Spend {1} get ProductName for {2} - Add productName to your order to qualify!"
								final Object[] args =
								{ thresholdPriceValue,
										Helper.formatCurrencyAmount(ctx, locale, orderCurrency, thresholdPriceValue.doubleValue()),
										discountPriceValue,
										Helper.formatCurrencyAmount(ctx, locale, orderCurrency, discountPriceValue.doubleValue()) };
								return formatMessage(this.getMessageThresholdNoProduct(ctx), args, locale);
							}
						}
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

		buildDataUniqueKeyForPriceRows(ctx, builder, getThresholdTotals(ctx));
		builder.append(getDiscountProduct(ctx).getCode(ctx)).append('|');
		buildDataUniqueKeyForPriceRows(ctx, builder, getProductPrices(ctx));
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

		values.remove(OrderThresholdPerfectPartnerPromotion.THRESHOLDTOTALS);
		values.remove(OrderThresholdPerfectPartnerPromotion.PRODUCTPRICES);

		// Clone price row
		values.put(OrderThresholdPerfectPartnerPromotion.THRESHOLDTOTALS, deepClonePriceRows(ctx, getThresholdTotals(ctx)));
		values.put(OrderThresholdPerfectPartnerPromotion.PRODUCTPRICES, deepClonePriceRows(ctx, getProductPrices(ctx)));
	}

}
