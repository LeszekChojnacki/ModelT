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
package de.hybris.platform.promotions.util.impex;

import de.hybris.platform.impex.jalo.header.AbstractDescriptor;
import de.hybris.platform.impex.jalo.header.StandardColumnDescriptor;
import de.hybris.platform.impex.jalo.translators.CollectionValueTranslator;
import de.hybris.platform.impex.jalo.translators.SingleValueTranslator;
import de.hybris.platform.jalo.Item;
import de.hybris.platform.jalo.JaloInvalidParameterException;
import de.hybris.platform.jalo.JaloSystemException;
import de.hybris.platform.jalo.c2l.C2LManager;
import de.hybris.platform.jalo.c2l.Currency;
import de.hybris.platform.jalo.type.AttributeDescriptor;
import de.hybris.platform.jalo.type.CollectionType;
import de.hybris.platform.jalo.type.ComposedType;
import de.hybris.platform.jalo.type.Type;
import de.hybris.platform.jalo.type.TypeManager;
import de.hybris.platform.promotions.jalo.OrderThresholdPerfectPartnerPromotion;
import de.hybris.platform.promotions.jalo.PromotionPriceRow;
import de.hybris.platform.promotions.jalo.PromotionsManager;
import de.hybris.platform.promotions.util.Tuple3;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * ImpEx translator that parses price rows into a collection
 *
 * PriceRowList:=PriceRow[,PriceRowList] PriceRow:=Price Currency
 *
 * For example:
 *
 * 4.99 GBP,8.25 EUR
 */
public class PromotionPriceRowsTranslator extends CollectionValueTranslator
{
	public PromotionPriceRowsTranslator()
	{
		super(getCollectionType(), new PromotionPriceRowsTranslator.PriceRowTranslator());
	}

	protected static CollectionType getCollectionType()
	{
		final ComposedType ct = TypeManager.getInstance().getComposedType(OrderThresholdPerfectPartnerPromotion.class);  //NOSONAR
		final AttributeDescriptor ad = ct.getAttributeDescriptor(OrderThresholdPerfectPartnerPromotion.THRESHOLDTOTALS);
		final Type t = ad.getAttributeType();

		return (CollectionType) t;
	}

	/**
	 * Support prices with commas in them
	 */
	@Override
	protected List splitAndUnescape(final String valueExpr)
	{
		final List<String> tokens = super.splitAndUnescape(valueExpr);
		if (tokens == null || tokens.size() < 2)
		{
			return tokens;
		}

		final List<String> result = new ArrayList<>(tokens.size());
		result.add(tokens.get(0));

		int i = 1;
		for (final int s = tokens.size(); i < s; i++)
		{
			final String prev = tokens.get(i - 1);
			final String current = tokens.get(i);

			if (Character.isDigit(prev.charAt(prev.length() - 1)))
			{
				final int lastReaultPosition = result.size() - 1;
				final String lastResultTokenText = result.get(lastReaultPosition);
				result.set(lastReaultPosition, lastResultTokenText + getCollectionValueDelimiter() + current);
			}
			else
			{
				result.add(current);
			}
		}

		return result;
	}

	protected static class PriceRowTranslator extends SingleValueTranslator
	{
		private static final Logger LOG = LoggerFactory.getLogger(PriceRowTranslator.class);
		private final Map<String, Currency> currenciesISOs = new HashMap<>();
		private final Map<String, Currency> currenciesSymbols = new HashMap<>();
		private NumberFormat numberFormat;



		@Override
		public void init(final StandardColumnDescriptor standardColumnDescriptor)
		{
			super.init(standardColumnDescriptor);

			// Get the locale
			final java.util.Locale loc = getColumnDescriptor().getHeader().getReader().getLocale();

			numberFormat = NumberFormat.getInstance(loc);
			if (getNumberFormatString(standardColumnDescriptor) != null)
			{
				if (numberFormat instanceof DecimalFormat)
				{
					((DecimalFormat) numberFormat).applyPattern(getNumberFormatString(standardColumnDescriptor));
				}
				else
				{
					LOG.error("no DecimalFormat in PriceRowTranslator.init()");
				}
			}

			// Build a cache of all currencies
			for (final Currency c : C2LManager.getInstance().getAllCurrencies())  //NOSONAR
			{
				currenciesISOs.put(c.getIsoCode().toLowerCase(), c);  //NOSONAR
				if (c.getSymbol() != null)
				{
					currenciesSymbols.put(c.getSymbol().toLowerCase(), c);
				}
			}

		}
		@SuppressWarnings("squid:S2325")
		private String getNumberFormatString(final AbstractDescriptor columnDescriptor)
		{
			if (columnDescriptor != null)
			{
				final String format = columnDescriptor.getDescriptorData().getModifier("numberformat");
				if (format != null && format.length() >= 0)
				{
					return format;
				}
			}
			return null;
		}

		private Tuple3<Currency, String, Integer> parseCurrency(final String valueExpr)
		{
			for (final String iso : currenciesISOs.keySet()) //NOSONAR
			{
				final int index = valueExpr.toLowerCase().indexOf(iso);
				if (index != -1)
				{
					return new Tuple3(currenciesISOs.get(iso), iso, Integer.valueOf(index));
				}
			}

			for (final String symbol : currenciesSymbols.keySet()) //NOSONAR
			{
				final int index = valueExpr.toLowerCase().indexOf(symbol);
				if (index != -1)
				{
					return new Tuple3(currenciesSymbols.get(symbol), symbol, Integer.valueOf(index));
				}
			}

			return null;
		}

		@Override
		protected Object convertToJalo(final String valueExpr, final Item forItem)
		{
			final Tuple3<Currency, String, Integer> currencyInfo = parseCurrency(valueExpr);
			if (currencyInfo == null)
			{
				throw new JaloInvalidParameterException("Unable to find the currency definition in price row [" + valueExpr + "]",
						999);
			}

			// Pull out the price string and parse it
			final String priceValueString = valueExpr.substring(0, currencyInfo.getThird().intValue()).trim();
			double price = 0.0D;
			try
			{
				price = numberFormat.parse(priceValueString).doubleValue();
			}
			catch (final ParseException parseEx)
			{
				throw new JaloSystemException(parseEx);
			}

			return PromotionsManager.getInstance().createPromotionPriceRow(currencyInfo.getFirst(), price);
		}

		@Override
		protected String convertToString(final Object object)
		{
			final PromotionPriceRow priceRow = (PromotionPriceRow) object;
			final StringBuilder builder = new StringBuilder();

			builder.append(priceRow.getPriceAsPrimitive());
			builder.append(' ');
			builder.append(priceRow.getCurrency().getIsoCode());  //NOSONAR

			return builder.toString();
		}
	}
}
