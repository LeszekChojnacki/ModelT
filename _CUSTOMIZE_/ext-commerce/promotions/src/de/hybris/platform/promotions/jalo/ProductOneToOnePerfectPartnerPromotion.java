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
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;


/**
 * ProductOneToOnePerfectPartnerPromotion.
 * 
 * Buy product A together with product B for a total fixed price. For example: <i>Buy the XBox games console and this
 * specific game together for &euro;215.00</i>, <i>Buy this drill with this pair of safety goggles for &euro;100.00</i>.
 * The cart must contain one of each product to qualify.
 * 
 */
public class ProductOneToOnePerfectPartnerPromotion extends GeneratedProductOneToOnePerfectPartnerPromotion
{
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(ProductOneToOnePerfectPartnerPromotion.class);
	@SuppressWarnings("squid:S3008")
	private static final float TRIGGER_BUT_NO_PARTNER = 0.75F;

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
		if (!products.isEmpty())
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
		if (!products.isEmpty())
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
	public List<PromotionResult> evaluate(final SessionContext ctx, final PromotionEvaluationContext promoContext)
	{
		final List<PromotionResult> promotionResults = new ArrayList<PromotionResult>();
		final PromotionsManager.RestrictionSetResult restrictRes = findEligibleProductsInBasket(ctx, promoContext);

		if (restrictRes.isAllowedToContinue() && !restrictRes.getAllowedProducts().isEmpty())
		{
			final PromotionOrderView triggerItemView = promoContext.createView(ctx, this, restrictRes.getAllowedProducts());
			final List<Product> partnerProducts = new ArrayList<Product>(); // NOSONAR
			partnerProducts.add(getPartnerProduct(ctx));
			final PromotionOrderView partnerItemView = promoContext.createView(ctx, this, partnerProducts);
			final PromotionsManager promotionsManager = PromotionsManager.getInstance();

			while (triggerItemView.getTotalQuantity(ctx) > 0)
			{
				promoContext.startLoggingConsumed(this);

				final PromotionOrderEntryConsumed poecBase = triggerItemView.consume(ctx, 1).get(0);
				if (partnerItemView.getTotalQuantity(ctx) > 0)
				{
					final double baseProductRetailPrice = poecBase.getUnitPrice(ctx);

					// Fire
					final PromotionOrderEntryConsumed poec = partnerItemView.consume(ctx, 1).get(0);

					final double partnerProductRetailPrice = poec.getUnitPrice(ctx);
					final Double bundlePrice = this.getPriceForOrder(ctx, this.getBundlePrices(ctx), promoContext.getOrder(),
							ProductOneToOnePerfectPartnerPromotion.BUNDLEPRICES);

					final double bundleRetailValue = baseProductRetailPrice + partnerProductRetailPrice;
					final List<PromotionOrderEntryConsumed> consumedEntries = promoContext.finishLoggingAndGetConsumed(this, true);

					// Adjust the unit prices for all the consumed entries
					Helper.adjustUnitPrices(ctx, promoContext, consumedEntries, bundlePrice.doubleValue(), bundleRetailValue);

					final AbstractPromotionAction action = promotionsManager.createPromotionOrderAdjustTotalAction(ctx,
							bundlePrice.doubleValue() - bundleRetailValue);

					final PromotionResult result = promotionsManager.createPromotionResult(ctx, this, promoContext.getOrder(), 1.0F);
					result.setConsumedEntries(consumedEntries);
					result.addAction(ctx, action);

					promotionResults.add(result);
				}
				else
				{
					// Partially fire
					final PromotionResult result = promotionsManager.createPromotionResult(ctx, this, promoContext.getOrder(),
							TRIGGER_BUT_NO_PARTNER);
					result.setConsumedEntries(ctx, promoContext.finishLoggingAndGetConsumed(this, false));
					promotionResults.add(result);
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
			final Double bundlePrice = this.getPriceForOrder(ctx, this.getBundlePrices(ctx), promotionResult.getOrder(ctx),
					ProductOneToOnePerfectPartnerPromotion.BUNDLEPRICES);

			if (bundlePrice != null)
			{
				if (promotionResult.getFired(ctx))
				{
					final double totalDiscount = promotionResult.getTotalDiscount(ctx);

					// "Buy ProductName and PartnerProductName for {1} - You have saved {3}"
					final Object[] args =
					{ bundlePrice, Helper.formatCurrencyAmount(ctx, locale, orderCurrency, bundlePrice.doubleValue()),
							Double.valueOf(totalDiscount), Helper.formatCurrencyAmount(ctx, locale, orderCurrency, totalDiscount) };
					return formatMessage(this.getMessageFired(ctx), args, locale);
				}
				else if (promotionResult.getCouldFire(ctx))
				{
					// "Buy ProductName and PartnerProductName for {1} - Add PartnerProductName"
					final Object[] args =
					{ bundlePrice, Helper.formatCurrencyAmount(ctx, locale, orderCurrency, bundlePrice.doubleValue()) };
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

		builder.append(getPartnerProduct(ctx).getCode(ctx)).append('|');
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
		values.remove(ProductOneToOnePerfectPartnerPromotion.BUNDLEPRICES);

		// Clone price row
		values.put(ProductOneToOnePerfectPartnerPromotion.BUNDLEPRICES, deepClonePriceRows(ctx, getBundlePrices(ctx)));
	}

}
