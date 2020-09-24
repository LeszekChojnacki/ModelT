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
import de.hybris.platform.promotions.result.PromotionOrderEntry;
import de.hybris.platform.promotions.result.PromotionOrderView;
import de.hybris.platform.promotions.util.Helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;


/**
 * ProductPerfectPartnerBundlePromotion.
 *
 * Buy product A together with X products from the list B for a total fixed price. For example: <i>Buy the XBox games
 * console and 3 of the specified games together for &euro;215.00</i>, <i>Buy this drill with 2 of the safety
 * accessories for &euro;100.00</i>. The cart must contain the base product and the qualifying count of the partner
 * products to qualify.
 *
 *
 */
public class ProductPerfectPartnerBundlePromotion extends GeneratedProductPerfectPartnerBundlePromotion // NOSONAR
{
	private static final Logger LOGGER = Logger.getLogger(ProductPerfectPartnerBundlePromotion.class.getName());

	/**
	 * remove the item. you can delete this method if you don't want to intercept the removal of this item
	 */
	@Override
	public void remove(final SessionContext ctx) throws ConsistencyCheckException
	{
		// ## business code placed here will be executed before the item is removed

		// Remove any linked price rows
		deletePromotionPriceRows(ctx, getBundlePrices(ctx));

		// then create the item
		super.remove(ctx);

		// ## business code placed here will be executed after the item was removed
	}

	/**
	 * Get the base product for this perfect partner promotion.
	 *
	 * The base product is stored in the products collection.
	 *
	 * @param ctx
	 *           The hybris context
	 * @return The base product
	 */
	@Override
	public Product getBaseProduct(final SessionContext ctx) // NOSONAR
	{
		final Collection<Product> products = this.getProducts(ctx); // NOSONAR
		if (products != null && !products.isEmpty())
		{
			// Return first product
			return products.iterator().next();
		}

		return null;
	}

	/**
	 * Set the base product for this perfect partner promotion.
	 *
	 * The base product is stored in the products collection.
	 *
	 * @param ctx
	 *           The hybris context
	 * @param value
	 *           The product to set as the base product
	 */
	@Override
	public void setBaseProduct(final SessionContext ctx, final Product value) // NOSONAR
	{
		// First clear the collection of products
		final Collection<Product> products = this.getProducts(ctx); // NOSONAR
		if (products != null && !products.isEmpty())
		{
			for (final Product p : products) // NOSONAR
			{
				this.removeFromProducts(ctx, p);
			}
		}

		// Add base product to qualifying products collection
		this.addToProducts(ctx, value);
	}

	@Override
	public List<PromotionResult> evaluate(final SessionContext ctx, final PromotionEvaluationContext promoContext) //NOSONAR
	{
		final List<PromotionResult> promotionResults = new ArrayList<>();

		final Integer qualifyingCount = this.getQualifyingCount(ctx);
		if (qualifyingCount != null && qualifyingCount.intValue() > 0)
		{
			final Double bundlePrice = this.getPriceForOrder(ctx, this.getBundlePrices(ctx), promoContext.getOrder(),
					ProductPerfectPartnerBundlePromotion.BUNDLEPRICES);
			if (bundlePrice != null)
			{
				final PromotionsManager.RestrictionSetResult rsr = this.findEligibleProductsInBasket(ctx, promoContext);
				if (rsr.isAllowedToContinue() && !rsr.getAllowedProducts().isEmpty())
				{
					final PromotionOrderView triggerItemView = promoContext.createView(ctx, this, rsr.getAllowedProducts());

					final List<Product> allPartnerProducts = (List<Product>) this.getPartnerProducts(ctx); // NOSONAR

					// Create the view on the partner products
					final PromotionOrderView partnerItemView = promoContext.createView(ctx, this, allPartnerProducts);

					while (triggerItemView.getTotalQuantity(ctx) > 0) //NOSONAR
					{
						promoContext.startLoggingConsumed(this);

						triggerItemView.consume(ctx, 1);
						if (partnerItemView.getTotalQuantity(ctx) > 0)
						{
							final List<PromotionOrderEntry> entriesSortedByPrice = partnerItemView.getAllEntriesByPrice(ctx);

							// Get the unique list of partner products in price order
							final ArrayList<Product> uniqueSortedPartnerProducts = new ArrayList(entriesSortedByPrice.size()); // NOSONAR
							for (final PromotionOrderEntry entry : entriesSortedByPrice)
							{
								final Product product = entry.getProduct(ctx); // NOSONAR
								if (!uniqueSortedPartnerProducts.contains(product))
								{
									uniqueSortedPartnerProducts.add(product);
								}
							}

							// Try to consume qualifyingCount distinct partners, consume cheapest first
							long foundCount = 0;
							for (final Product product : uniqueSortedPartnerProducts) // NOSONAR
							{
								final long availableQuantity = partnerItemView.getQuantity(ctx, product);
								if (availableQuantity > 0)
								{
									partnerItemView.consume(ctx, product, 1);
									foundCount++;

									// Stop when we have found enough products
									if (foundCount >= qualifyingCount.longValue())
									{
										break;
									}
								}
							}

							// Check to see if we found enough results
							if (foundCount == qualifyingCount.longValue())
							{
								// Firing, so work out the discount.  Go through each consumed item and add the price up
								final List<PromotionOrderEntryConsumed> consumedEntries = promoContext.finishLoggingAndGetConsumed(this,
										true);
								double bundleRetailValue = 0.0D;
								for (final PromotionOrderEntryConsumed poec : consumedEntries)
								{
									bundleRetailValue += (poec.getUnitPrice(ctx) * poec.getQuantityAsPrimitive(ctx));
								}

								Helper.adjustUnitPrices(ctx, promoContext, consumedEntries, bundlePrice.doubleValue(), bundleRetailValue);

								final PromotionOrderAdjustTotalAction poata = PromotionsManager.getInstance()
										.createPromotionOrderAdjustTotalAction(ctx, bundlePrice.doubleValue() - bundleRetailValue);
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
								// Partially fire - not found enough partner products
								final float certainty = foundCount / qualifyingCount.floatValue(); // NOSONAR
								final PromotionResult result = PromotionsManager.getInstance().createPromotionResult(ctx, this,
										promoContext.getOrder(), certainty);
								result.setConsumedEntries(ctx, promoContext.finishLoggingAndGetConsumed(this, false));
								promotionResults.add(result);
								break;
							}
						}
						else
						{
							// Partially fire - not found any partner products
							final float certainty = 0.5f;
							final PromotionResult result = PromotionsManager.getInstance().createPromotionResult(ctx, this,
									promoContext.getOrder(), certainty);
							result.setConsumedEntries(ctx, promoContext.finishLoggingAndGetConsumed(this, false));
							promotionResults.add(result);
							break;
						}
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
			final de.hybris.platform.jalo.c2l.Currency orderCurrency = order.getCurrency(ctx);

			final Double bundlePrices = this.getPriceForOrder(ctx, this.getBundlePrices(ctx), promotionResult.getOrder(ctx),
					ProductPerfectPartnerBundlePromotion.BUNDLEPRICES);
			if (bundlePrices != null)
			{
				if (promotionResult.getFired(ctx))
				{
					final double totalDiscount = promotionResult.getTotalDiscount(ctx);

					// "Buy ProductName together with X products from PartnerProductName, PartnerProductName, PartnerProductName for {1} - You have saved {3}"
					final Object[] args =
					{ bundlePrices, Helper.formatCurrencyAmount(ctx, locale, orderCurrency, bundlePrices.doubleValue()),
							Double.valueOf(totalDiscount), Helper.formatCurrencyAmount(ctx, locale, orderCurrency, totalDiscount) };
					return formatMessage(this.getMessageFired(ctx), args, locale);
				}
				else if (promotionResult.getCouldFire(ctx))
				{
					final Integer qualifyingCount = this.getQualifyingCount(ctx);
					if (qualifyingCount != null && qualifyingCount.intValue() > 0) //NOSONAR
					{
						// Subtract 1 for the base product
						final long consumedCount = promotionResult.getConsumedCount(ctx, true) - 1;
						final long neededCount = qualifyingCount.longValue() - consumedCount;

						if (LOGGER.isDebugEnabled())
						{
							LOGGER.debug("(" + getPK() + ") getResultDescription: consumedCount=[" + consumedCount + "] certainty=["
									+ promotionResult.getCertainty(ctx) + "] neededCount=[" + neededCount + "]");
						}

						// "Buy ProductName together with X products from PartnerProductName, PartnerProductName, PartnerProductName for {1} - Add another {2} products to your order to qualify!"
						final Object[] args =
						{ bundlePrices, Helper.formatCurrencyAmount(ctx, locale, orderCurrency, bundlePrices.doubleValue()),
								Long.valueOf(neededCount) };
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

		buildDataUniqueKeyForProducts(ctx, builder, getPartnerProducts(ctx));
		builder.append(getQualifyingCount(ctx)).append('|');
		buildDataUniqueKeyForPriceRows(ctx, builder, getBundlePrices(ctx));
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
		values.remove(ProductPerfectPartnerBundlePromotion.BUNDLEPRICES);

		// Clone price row
		values.put(ProductPerfectPartnerBundlePromotion.BUNDLEPRICES, deepClonePriceRows(ctx, getBundlePrices(ctx)));
	}

}
