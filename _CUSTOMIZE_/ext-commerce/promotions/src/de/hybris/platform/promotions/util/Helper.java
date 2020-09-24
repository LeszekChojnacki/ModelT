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
package de.hybris.platform.promotions.util;

import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.c2l.Currency;
import de.hybris.platform.jalo.order.AbstractOrder;
import de.hybris.platform.jalo.order.AbstractOrderEntry;
import de.hybris.platform.jalo.order.price.Discount;
import de.hybris.platform.jalo.product.Product;
import de.hybris.platform.promotions.jalo.PromotionOrderEntryConsumed;
import de.hybris.platform.promotions.jalo.PromotionsManager;
import de.hybris.platform.promotions.result.PromotionEvaluationContext;
import de.hybris.platform.util.DiscountValue;
import de.hybris.platform.util.TaxValue;
import de.hybris.platform.util.Utilities;
import de.hybris.platform.variants.jalo.VariantProduct;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;


/**
 * Static helper methods for the promotions extension.
 *
 *
 * @version v1
 */
public class Helper //NOSONAR
{
	private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Helper.class.getName());

	// ----------------------------------------------------------------------------
	// Items.xml initialization support methods
	// ----------------------------------------------------------------------------

	/**
	 * Create a Date for the first day of the specified year.
	 *
	 * @param year
	 *           the year
	 * @return a date that represents the first day of the year
	 */
	public static Date buildDateForYear(final int year)
	{
		//left with no refactoring due to PLA-7706 - causing many problems during initialization
		return (new java.util.GregorianCalendar(year, java.util.Calendar.JANUARY, 1)).getTime();
	}

	// ----------------------------------------------------------------------------
	// Currency support methods
	// ----------------------------------------------------------------------------

	/**
	 * Format an amount in a currency for a locale.
	 *
	 * @param ctx
	 *           the hybris context
	 * @param locale
	 *           the java locale that the amount should be rendered in
	 * @param currency
	 *           the hybris currency for the amount
	 * @param amount
	 *           the value
	 * @return a formatted string
	 */
	public static String formatCurrencyAmount(final SessionContext ctx, final Locale locale, Currency currency,
			final double amount)
	{
		if (ctx != null && locale != null)
		{
			if (currency == null)
			{
				// Default to the context currency
				currency = ctx.getCurrency(); //NOSONAR
			}

			// Lookup the number formatter for the locale
			final NumberFormat localisedNumberFormat = NumberFormat.getCurrencyInstance(locale);

			// Lookup the java currency object for the currency code (must be ISO 4217)
			final String currencyIsoCode = currency.getIsocode(ctx);
			final java.util.Currency javaCurrency = java.util.Currency.getInstance(currencyIsoCode);
			if (javaCurrency == null)
			{
				log.warn("formatCurrencyAmount failed to lookup java.util.Currency from [" + currencyIsoCode
						+ "] ensure this is an ISO 4217 code and is supported by the java runtime.");
			}
			else
			{
				localisedNumberFormat.setCurrency(javaCurrency);
			}

			adjustDigits((DecimalFormat) localisedNumberFormat, currency);
			adjustSymbol((DecimalFormat) localisedNumberFormat, currency);

			// Format the amount
			final String result = localisedNumberFormat.format(amount);

			// Print out debug to see what is happening on javelin's system
			if (log.isDebugEnabled())
			{
				log.debug("formatCurrencyAmount locale=[" + locale + "] currency=[" + currency + "] amount=[" + amount
						+ "] currencyIsoCode=[" + currencyIsoCode + "] javaCurrency=[" + javaCurrency + "] result=[" + result + "]");
			}

			return result;
		}
		return "";
	}

	/**
	 * Adjusts {@link java.text.DecimalFormat}'s fraction digits according to given {@link Currency}.
	 */
	protected static DecimalFormat adjustDigits(final DecimalFormat format, final Currency currency)
	{
		final int tempDigits = currency.getDigits() == null ? 0 : currency.getDigits().intValue();
		final int digits = Math.max(0, tempDigits);

		format.setMaximumFractionDigits(digits);
		format.setMinimumFractionDigits(digits);
		if (digits == 0)
		{
			format.setDecimalSeparatorAlwaysShown(false);
		}

		return format;
	}

	/**
	 * Adjusts {@link DecimalFormat}'s symbol according to given {@link Currency}.
	 */
	protected static DecimalFormat adjustSymbol(final DecimalFormat format, final Currency currency)
	{
		final String symbol = currency.getSymbol();
		if (symbol != null)
		{
			final DecimalFormatSymbols symbols = format.getDecimalFormatSymbols(); // does cloning
			final String iso = currency.getIsocode();
			boolean changed = false;
			if (!iso.equalsIgnoreCase(symbols.getInternationalCurrencySymbol()))
			{
				symbols.setInternationalCurrencySymbol(iso);
				changed = true;
			}
			if (!symbol.equals(symbols.getCurrencySymbol()))
			{
				symbols.setCurrencySymbol(symbol);
				changed = true;
			}
			if (changed)
			{
				format.setDecimalFormatSymbols(symbols);
			}
		}
		return format;
	}

	/**
	 * Get the smallest value that can be represented as a whole unit in the specified currency.
	 *
	 * @param ctx
	 *           the hybris context
	 * @param currency
	 *           the hybris currency
	 * @return A BigDecimal that represents the smallest unit in the currency
	 */
	protected static BigDecimal getSmallestCurrencyUnit(final SessionContext ctx, final Currency currency)
	{
		long factor = 1;
		final int currencyDigits = currency.getDigits(ctx).intValue();

		// My guess is that this is faster than Math.pow and BigDecimal.pow
		for (int i = 0; i < currencyDigits; i++)
		{
			factor = factor * 10;
		}

		return BigDecimal.ONE.setScale(currencyDigits, RoundingMode.HALF_EVEN)
				.divide(BigDecimal.valueOf(factor), RoundingMode.HALF_EVEN).setScale(currencyDigits, RoundingMode.HALF_EVEN);
	}

	/**
	 * Round the specified amount to the nearest whole unit in the specified currency.
	 *
	 * @param ctx
	 *           the hybris context
	 * @param currency
	 *           the hybris currency
	 * @param amount
	 *           the amount
	 * @return A BigDecimal that represents the amount rounded to the precision of the currency
	 */
	public static BigDecimal roundCurrencyValue(final SessionContext ctx, final Currency currency, final BigDecimal amount)
	{
		return amount.setScale(currency.getDigits(ctx).intValue(), RoundingMode.HALF_EVEN);
	}

	/**
	 * Round the specified amount to the nearest whole unit in the specified currency.
	 *
	 * @param ctx
	 *           the hybris context
	 * @param currency
	 *           the hybris currency
	 * @param amount
	 *           the amount
	 * @return A BigDecimal that represents the amount rounded to the precision of the currency
	 */
	public static BigDecimal roundCurrencyValue(final SessionContext ctx, final Currency currency, final double amount)
	{
		return BigDecimal.valueOf(amount).setScale(currency.getDigits(ctx).intValue(), RoundingMode.HALF_EVEN);
	}


	// ----------------------------------------------------------------------------
	// PromotionOrderEntryConsumed leveled unit price adjustment methods
	// ----------------------------------------------------------------------------

	/**
	 * Adjust the unit prices of the PromotionOrderEntryConsumed items specified to match the target total
	 * <p/>
	 * Applies unit price adjustments to the PromotionOrderEntryConsumed items to take the total value of the items from
	 * the originalTotal to the targetTotal specified. If necessary this method will split PromotionOrderEntryConsumed
	 * items to allow it to match the targetTotal value exactly.
	 *
	 * @param ctx
	 *           the hybris context
	 * @param promoContext
	 *           the promotion evaluation context
	 * @param consumedEntries
	 *           the list of consumed entries
	 * @param targetTotal
	 *           the target total value for the consumed entries
	 * @param originalTotal
	 *           the current total value for the consumed entries
	 */
	public static void adjustUnitPrices(final SessionContext ctx, final PromotionEvaluationContext promoContext, //NOSONAR
			final List<PromotionOrderEntryConsumed> consumedEntries, final double targetTotal, final double originalTotal)
	{
		if (log.isDebugEnabled())
		{
			log.debug("adjustUnitPrices consumedEntries.size=[" + consumedEntries.size() + "] targetTotal=[" + targetTotal
					+ "] originalTotal=[" + originalTotal + "]");
		}

		final Currency currency = promoContext.getOrder().getCurrency(ctx);

		//
		// Apply a common ratio discount to all unit prices in the order entries.
		// Because the unit prices must be valid units in the currency it will not
		// always be possible to reach the required total just by applying this
		// ratio. The unit prices must be rounded to a valid currency unit.
		//

		// Calculate discount ratio
		final double discountRatio = targetTotal / originalTotal;

		// Apply discount ratio to each entries adjusted unit price
		for (final PromotionOrderEntryConsumed poec : consumedEntries)
		{
			final double originalUnitPrice = poec.getUnitPrice(ctx);
			final BigDecimal adjustedUnitPrice = roundCurrencyValue(ctx, currency, originalUnitPrice * discountRatio);
			poec.setAdjustedUnitPrice(ctx, adjustedUnitPrice.doubleValue());
		}

		final BigDecimal targetTotalDecimal = roundCurrencyValue(ctx, currency, targetTotal);

		// Calculate the new total with the adjusted unit prices
		final BigDecimal newTotalDecimal = calculateOrderEntryAdjustedTotal(ctx, currency, consumedEntries);

		// Calculate remainder
		final BigDecimal remainderAmountDecimal = targetTotalDecimal.subtract(newTotalDecimal);

		//
		// Now we know how much value we have left to aportion after scaling the unit prices
		//

		if (remainderAmountDecimal.compareTo(BigDecimal.ZERO) != 0)
		{
			// We need to split the remainder into units that we can work with, of course this depends on the currency
			final BigDecimal smallestCurrencyUnit = getSmallestCurrencyUnit(ctx, currency);

			// Calculate how many of these smallestCurrencyUnit there are in the remainderAmountDecimal
			long unallocatedRemainderUnits = roundCurrencyValue(ctx, currency, remainderAmountDecimal.abs())
					.divideToIntegralValue(smallestCurrencyUnit).longValueExact();

			// We must preserve the sign of the adjustment
			BigDecimal smallestCurrencyUnitAdjustment = smallestCurrencyUnit;
			if (remainderAmountDecimal.compareTo(BigDecimal.ZERO) < 0)
			{
				smallestCurrencyUnitAdjustment = smallestCurrencyUnit.negate();
			}


			// This is really a version of the Knapsack problem, and is NP complete.
			// http://en.wikipedia.org/wiki/Knapsack_problem
			// It is slightly different because in finding an ideal fit we also have to consider the
			// factors of the remainder.

			// We will start with the Martello and Toth (1990) greedy approximation algorithm to solve the knapsack problem

			// Sort the order entries by product quantity descending
			final SortedSet<PromotionOrderEntryConsumed> orderEntriesSortedByQuantityDescending = new TreeSet<>(
					(final PromotionOrderEntryConsumed a, final PromotionOrderEntryConsumed b) -> b.getQuantity(ctx)
							.compareTo(a.getQuantity(ctx)));
			orderEntriesSortedByQuantityDescending.addAll(consumedEntries);

			// We would prefer to make smaller unit price adjustments than larger ones, therefore
			// we find the order entry with the largest quantity of units that is less than or equal
			// to the number of unallocatedRemainderUnits. We can then make a smallestCurrencyUnitAdjustment
			// to the order entry, and reduce unallocatedRemainderUnits by the order entry quantity.
			// We also handle multiples of the order entry quantity. And to make matters more interesting
			// we also check that the adjustment does not send the unit price below zero.
			for (final PromotionOrderEntryConsumed poec : orderEntriesSortedByQuantityDescending)
			{
				final long quantity = poec.getQuantity(ctx).longValue();
				if (quantity > 0 && quantity <= unallocatedRemainderUnits)
				{
					final double currentUnitPrice = poec.getAdjustedUnitPrice(ctx).doubleValue();
					if (currentUnitPrice > 0) //NOSONAR
					{
						// See how many times quantity goes into remaining units
						long multiple = unallocatedRemainderUnits / quantity;

						// Reduce the multiple if it would make the unit price negative
						for (; multiple > 1; multiple--)
						{
							if (smallestCurrencyUnitAdjustment.multiply(BigDecimal.valueOf(multiple))
									.add(BigDecimal.valueOf(currentUnitPrice)).doubleValue() >= 0)
							{
								break;
							}
						}

						// Adjust unit price
						final double adjustedUnitPrice = Helper.roundCurrencyValue(ctx, currency, smallestCurrencyUnitAdjustment
								.multiply(BigDecimal.valueOf(multiple)).add(BigDecimal.valueOf(currentUnitPrice))).doubleValue();
						poec.setAdjustedUnitPrice(ctx, adjustedUnitPrice);

						unallocatedRemainderUnits -= multiple * quantity;

						if (unallocatedRemainderUnits == 0)
						{
							// Done!
							break;
						}
					}
				}
			}

			if (unallocatedRemainderUnits > 0)
			{
				// Still have some remainder left to aportion, we are going to have so split entries
				// try to find an entry that is larger than the number of unallocated remainder units
				// and split it so that a smallestCurrencyUnitAdjustment can be made to one of the
				// two order entries.
				for (final PromotionOrderEntryConsumed poec : orderEntriesSortedByQuantityDescending)
				{
					final long quantity = poec.getQuantity(ctx).longValue();
					if (quantity > unallocatedRemainderUnits) //NOSONAR
					{
						final double currentUnitPrice = poec.getAdjustedUnitPrice(ctx).doubleValue();
						if (currentUnitPrice > 0)
						{
							// Split the line item into unallocatedRemainderUnits and quantity-unallocatedRemainderUnits
							// We cannot change the quantity of our line items because that will have an effect on the consumption logger
							// therefore we need to create 2 new lines are replace the existing line
							final PromotionOrderEntryConsumed splitPoec1 = PromotionsManager.getInstance()
									.createPromotionOrderEntryConsumed(ctx, poec.getCode(ctx), poec.getOrderEntry(ctx),
											quantity - unallocatedRemainderUnits);
							final PromotionOrderEntryConsumed splitPoec2 = PromotionsManager.getInstance()
									.createPromotionOrderEntryConsumed(ctx, poec.getCode(ctx), poec.getOrderEntry(ctx),
											unallocatedRemainderUnits);

							// Copy back the adjusted price on to the first line
							splitPoec1.setAdjustedUnitPrice(ctx, poec.getAdjustedUnitPrice(ctx));

							// Set the adjusted unit price on the split line
							final BigDecimal adjustedUnitPriceDecimal = smallestCurrencyUnitAdjustment
									.add(BigDecimal.valueOf(poec.getAdjustedUnitPrice(ctx).doubleValue()));
							splitPoec2.setAdjustedUnitPrice(ctx,
									Helper.roundCurrencyValue(ctx, currency, adjustedUnitPriceDecimal).doubleValue());

							// Remove the original line item that has now been split
							consumedEntries.remove(poec);

							// Add the split entry to the list
							consumedEntries.add(splitPoec1);
							consumedEntries.add(splitPoec2);
							break;
						}
					}
				}
			}

			// Calculate the total again and verify that the remainder is now zero
			final BigDecimal checkTotalDecimal = calculateOrderEntryAdjustedTotal(ctx, currency, consumedEntries);
			if (!checkTotalDecimal.equals(targetTotalDecimal))
			{
				log.error("adjustUnitPrices Failed in checkTotal. targetTotalDecimal=[" + targetTotalDecimal + "] checkTotalDecimal=["
						+ checkTotalDecimal + "] originalTotal=[" + originalTotal + "]");
			}
		}
	}

	/**
	 * Helper method to calculate the sum of the AdjustedEntryPrice for each of the PromotionOrderEntryConsumed items.
	 *
	 * @param ctx
	 *           the hybris context
	 * @param currency
	 *           the hybris currency
	 * @param entries
	 *           the list of PromotionOrderEntryConsumed items to sum
	 * @return A BigDecimal that represents the rounded sum of the adjusted entry prices
	 */
	protected static BigDecimal calculateOrderEntryAdjustedTotal(final SessionContext ctx, final Currency currency,
			final List<PromotionOrderEntryConsumed> entries)
	{
		BigDecimal total = BigDecimal.ZERO;

		for (final PromotionOrderEntryConsumed poec : entries)
		{
			total = total.add(BigDecimal.valueOf(poec.getAdjustedEntryPrice(ctx)));
		}

		return roundCurrencyValue(ctx, currency, total);
	}

	/**
	 * Return the base product for the product specified. Returns the current product if the product is not an instance
	 * of {@link de.hybris.platform.variants.jalo.VariantProduct} and the result of
	 * {@link de.hybris.platform.variants.jalo.VariantProduct#getBaseProduct()} if it is.
	 *
	 * @param ctx
	 *           The hybris context
	 * @param product
	 *           The product to get the base product for
	 * @return the base product for the product specified
	 */
	public static Product getBaseProduct(final SessionContext ctx, final Product product)  //NOSONAR
	{
		if (product instanceof CompositeProduct)
		{
			return ((CompositeProduct) product).getCompositeParentProduct(ctx);
		}
		if (product instanceof VariantProduct)
		{
			return ((VariantProduct) product).getBaseProduct(ctx);
		}

		return null;
	}

	/**
	 * This is a recursive variant of the method above.
	 */
	public static List<Product> getBaseProducts(final SessionContext ctx, final Product product)  //NOSONAR
	{
		final List<Product> result = new ArrayList<Product>();  //NOSONAR
		getBaseProducts(ctx, product, result);
		return result;
	}

	/**
	 * Internal recursive method to get all base products of a given product.
	 * <p>
	 *
	 * @param ctx
	 * @param product
	 * @param result
	 */
	protected static void getBaseProducts(final SessionContext ctx, final Product product, final List<Product> result)  //NOSONAR
	{
		if (product != null && result != null)
		{
			Product baseProduct = null;  //NOSONAR

			if (product instanceof CompositeProduct)
			{
				baseProduct = ((CompositeProduct) product).getCompositeParentProduct(ctx);
			}
			else if (product instanceof VariantProduct)
			{
				baseProduct = ((VariantProduct) product).getBaseProduct(ctx);
			}

			if (baseProduct != null && !baseProduct.equals(product) && !result.contains(baseProduct))
			{
				result.add(baseProduct);
				getBaseProducts(ctx, baseProduct, result);
			}
		}
	}

	/**
	 * Find a global discount value with specified Code
	 *
	 * @param ctx
	 *           the hybris context
	 * @param order
	 *           the order to lookup the discounts on
	 * @param discountValueCode
	 *           the code for the discount to find
	 * @return the discount value found
	 */
	public static DiscountValue findGlobalDiscountValue(final SessionContext ctx, final AbstractOrder order,
			final String discountValueCode)
	{
		final Collection<DiscountValue> discounts = order.getGlobalDiscountValues(ctx);  //NOSONAR
		for (final DiscountValue dv : discounts)
		{
			if (discountValueCode.equals(dv.getCode()))
			{
				return dv;
			}
		}
		return null;
	}

	/**
	 * Method to dump an order and its entries into a string.
	 *
	 * @param ctx
	 *           the hybris context
	 * @param order
	 *           the order to dump
	 * @return the string dump of the order
	 */
	public static String dumpOrder(final SessionContext ctx, final AbstractOrder order)
	{
		final StringBuilder builder = new StringBuilder();
		dumpOrder(ctx, order, builder);
		return builder.toString();
	}

	protected static void dumpOrder(final SessionContext ctx, final AbstractOrder order, final StringBuilder builder) //NOSONAR
	{
		builder.append("## DUMP ORDER -");
		if (order == null)
		{
			builder.append("order is NULL");
		}
		else if (!order.isAlive())
		{
			builder.append("order was removed or is not valid anymore.");
		}
		else
		{
			builder.append(" type: ").append(order.getClass().getSimpleName());
			builder.append(" code: ").append(order.getCode(ctx));
			builder.append(" calculated: ").append(order.isCalculated(ctx));
			builder.append("\r\n");

			final List<AbstractOrderEntry> entries = order.getEntries();
			if (entries != null && !entries.isEmpty())
			{
				builder.append("## OrderEntries:\r\n");
				for (final AbstractOrderEntry orderEntry : entries)
				{
					dumpOrderEntry(ctx, orderEntry, builder);
				}
			}

			final Collection<Discount> discounts = order.getDiscounts();
			if (discounts != null && !discounts.isEmpty())
			{
				builder.append("## Discounts:\r\n");
				for (final Discount discount : discounts)
				{
					builder.append("##   discount: ").append(discount.getCode()).append(" value: ").append(discount.getValue()) //NOSONAR
							.append("\r\n");
				}
			}

			final List<DiscountValue> globalDiscounts = order.getGlobalDiscountValues();  //NOSONAR
			if (globalDiscounts != null && !globalDiscounts.isEmpty())
			{
				builder.append("## Global Discounts:\r\n");
				for (final DiscountValue discount : globalDiscounts)
				{
					builder.append("##   discount: ").append(discount.getCode()).append(" value: ").append(discount.getValue())
							.append("\r\n");
				}
			}

			final Collection<TaxValue> taxValues = order.getTotalTaxValues(ctx);  //NOSONAR
			if (taxValues != null && !taxValues.isEmpty())
			{
				builder.append("## Tax Values:\r\n");
				for (final TaxValue taxValue : taxValues)
				{
					builder.append("##   taxValue: ").append(taxValue.getCode()).append(" value: ").append(taxValue.getValue())
							.append("\r\n");
				}
			}

			builder.append("## Totals -");
			builder.append(" subtotal: ").append(order.getSubtotal(ctx));
			builder.append(" totalDiscounts: ").append(order.getTotalDiscounts(ctx));
			builder.append(" deliveryCosts: ").append(order.getDeliveryCosts(ctx));  //NOSONAR
			builder.append(" totalTax: ").append(order.getTotalTax(ctx));
			builder.append(" total: ").append(order.getTotal(ctx));  //NOSONAR
			builder.append("\r\n");
		}
	}

	protected static void dumpOrderEntry(final SessionContext ctx, final AbstractOrderEntry orderEntry,
			final StringBuilder builder)
	{
		builder.append("##   [").append(orderEntry.getEntryNumber()).append("] ");
		builder.append(orderEntry.getProduct(ctx).getCode(ctx));
		builder.append(' ').append(orderEntry.getUnit(ctx).getName(ctx)).append(": ").append(orderEntry.getQuantity(ctx));
		builder.append(" baseprice: ").append(orderEntry.getBasePrice(ctx));
		builder.append(" totalprice: ").append(orderEntry.getTotalPrice(ctx));
		builder.append(" calculated: ").append(orderEntry.isCalculated(ctx));
		builder.append(" giveAway: ").append(orderEntry.isGiveAway(ctx));
		builder.append(" rejected: ").append(orderEntry.isRejected(ctx));
		builder.append("\r\n");

		final String info = orderEntry.getInfo(ctx);
		if (info != null && info.length() > 0)
		{
			builder.append("##       info: ").append(info).append("\r\n");
		}

		final List<DiscountValue> discounts = orderEntry.getDiscountValues(ctx);  //NOSONAR
		if (discounts != null && !discounts.isEmpty())
		{
			for (final DiscountValue discount : discounts)
			{
				builder.append("##       discount: ").append(discount.getCode()).append(" value: ").append(discount.getValue())
						.append("\r\n");
			}
		}

		final Collection<TaxValue> taxValues = orderEntry.getTaxValues(ctx);  //NOSONAR
		if (taxValues != null && !taxValues.isEmpty())
		{
			for (final TaxValue taxValue : taxValues)
			{
				builder.append("##       taxValue: ").append(taxValue.getCode()).append(" value: ").append(taxValue.getValue())
						.append("\r\n");
			}
		}
	}

	/**
	 * Join collection of objects together to form a delimited string
	 *
	 * @param items
	 *           the collection of items to join together
	 * @return a joined string
	 */
	public static String join(final Collection items)
	{
		return join(items, ", ");
	}

	/**
	 * Join collection of objects together to form a delimited string
	 *
	 * @param items
	 *           the collection of items to join together
	 * @param delimiter
	 *           the delimiter to place between the items
	 * @return a joined string
	 */
	public static String join(final Collection items, final String delimiter)
	{
		if (items == null)
		{
			return null;
		}

		if (items.isEmpty())
		{
			return "";
		}

		final StringBuilder builder = new StringBuilder();

		boolean first = true;
		for (final Object item : items)
		{
			if (first)
			{
				first = false;
			}
			else
			{
				builder.append(delimiter);
			}

			builder.append(item);
		}

		return builder.toString();
	}

	/**
	 * returns a <code>Date</code> of the current minute to assure that queries can be cached for up to 1 minute.
	 *
	 * @return the truncated Date
	 */
	public static Date getDateNowRoundedToMinute()
	{
		final Calendar now = Utilities.getDefaultCalendar();  //NOSONAR

		now.set(Calendar.MILLISECOND, 0);
		now.set(Calendar.SECOND, 0);
		return now.getTime();
	}

}
