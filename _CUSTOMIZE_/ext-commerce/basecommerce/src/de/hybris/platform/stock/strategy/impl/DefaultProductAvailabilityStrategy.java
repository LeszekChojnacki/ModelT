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
package de.hybris.platform.stock.strategy.impl;

import de.hybris.platform.basecommerce.messages.ResourceBundleProvider;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.stock.impl.StockLevelDao;
import de.hybris.platform.stock.strategy.BestMatchStrategy;
import de.hybris.platform.stock.strategy.ProductAvailabilityStrategy;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 *
 */
public class DefaultProductAvailabilityStrategy implements ProductAvailabilityStrategy
{
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(DefaultProductAvailabilityStrategy.class.getName());

	private I18NService i18nService;
	private ResourceBundleProvider bundleProvider;
	private BestMatchStrategy bestMatchStrategy;
	private StockLevelDao stockLevelDao;

	public interface RESOURCEBUNDLE //NOSONAR
	{
		public static final String AVALABILITY_TEMPLATE = "de.hybris.platform.validation.services.impl.DefaultProductAvailabilityStrategy.availability";
		public static final String TOTAL_TEMPLATE = "de.hybris.platform.validation.services.impl.DefaultProductAvailabilityStrategy.total";

		// Template:
		// Warehouse: {warehouse} Product: {product} Availability: {availability} Date: {date} //NOSONAR
		// Total: {total}  																							// NOSONAR
		public static final String DATE = "date";
		public static final String WAREHOUSE = "warehouse";
		public static final String AVALABILITY = "availability";
		public static final String PRODUCT = "product";
		public static final String TOTAL = "total";
	}

	/**
	 * Converted the mapped quantities in a textual representation.
	 *
	 * The default templates will generate output like ...
	 *
	 * <pre>
	 *  Warehouse: {warehouse} Product: {product} Availability: {availability} Date: {date}
	 *  ...
	 *  Warehouse: {warehouse} Product: {product} Availability: {availability} Date: {date}
	 *  Total: {total}
	 * </pre>
	 *
	 * @param quantities
	 *           the mapped quantities
	 * @param productCode
	 *           the product for which the quantities belongs to
	 * @param date
	 *           the date of the availability (unused yet)
	 * @param language
	 *           the language used for the localization process
	 */
	@Override
	public String parse(final Map<WarehouseModel, Integer> quantities, final String productCode, final Date date,
			final LanguageModel language)
	{
		final StringBuilder parsedResult = new StringBuilder();
		int total = 0;

		// step. 1
		// ... adding line(s) ' Warehouse: {warehouse} Product: {product} Availability: {availability} Date: {date}'
		for (final Iterator<Entry<WarehouseModel, Integer>> it = quantities.entrySet().iterator(); it.hasNext();)
		{
			final Map.Entry<WarehouseModel, Integer> entry = it.next();

			final Map<String, Object> attributes = new HashMap<>();

			attributes.put(RESOURCEBUNDLE.DATE, date);
			attributes.put(RESOURCEBUNDLE.PRODUCT, productCode);
			attributes.put(RESOURCEBUNDLE.WAREHOUSE, entry.getKey().getCode());
			attributes.put(RESOURCEBUNDLE.AVALABILITY, entry.getValue());
			total += (entry.getValue()).intValue();

			parsedResult.append(getLocalizedString(RESOURCEBUNDLE.AVALABILITY_TEMPLATE,
					language != null ? getLocale(language.getIsocode()) : i18nService.getCurrentLocale(), attributes));
			parsedResult.append('\n');
		}

		// step. 2
		// ... adding line 'Total: {total}'
		final Map<String, Object> attributes = new HashMap<>();
		attributes.put(RESOURCEBUNDLE.TOTAL, Integer.valueOf(total));
		parsedResult.append(getLocalizedString(RESOURCEBUNDLE.TOTAL_TEMPLATE,
				language != null ? getLocale(language.getIsocode()) : i18nService.getCurrentLocale(), attributes));

		return parsedResult.toString();
	}

	/**
	 * Gets the locale based on two or one part ISO code.
	 *
	 * @param isoCode
	 *           the iso code
	 *
	 * @return the locale
	 */
	private Locale getLocale(final String isoCode)
	{
		final String[] splitCode = isoCode.split("_");
		if (splitCode.length == 2)
		{
			return new Locale(splitCode[0], splitCode[1]);

		}
		return new Locale(isoCode);
	}

	/**
	 * Converted the mapped availability in a textual representation.
	 *
	 * The default templates will generate output like ...
	 *
	 * <pre>
	 *  Warehouse: {warehouse} Product: {product} Availability: {availability} Date: {date}
	 *  ...
	 *  Warehouse: {warehouse} Product: {product} Availability: {availability} Date: {date}
	 *  Total: {total}
	 * </pre>
	 *
	 * @param quantities
	 *           the mapped availability dates
	 */
	@Override
	public String parse(final Map<WarehouseModel, Date> quantities, final String productCode, final int quantity,
			final LanguageModel language)
	{
		final StringBuilder parsedResult = new StringBuilder();
		int total = 0;

		// step. 1
		// ... adding line(s) ' Warehouse: {warehouse} Product: {product} Availability: {availability} Date: {date}'

		for (final Iterator<Entry<WarehouseModel, Date>> it = quantities.entrySet().iterator(); it.hasNext();)
		{
			final Map.Entry<WarehouseModel, Date> entry = it.next();

			final Map<String, Object> attributes = new HashMap<>();

			attributes.put(RESOURCEBUNDLE.AVALABILITY, Integer.valueOf(quantity));
			attributes.put(RESOURCEBUNDLE.PRODUCT, productCode);
			attributes.put(RESOURCEBUNDLE.WAREHOUSE, entry.getKey().getCode());
			attributes.put(RESOURCEBUNDLE.DATE, entry.getValue());
			total += quantity;

			parsedResult.append(getLocalizedString(RESOURCEBUNDLE.AVALABILITY_TEMPLATE,
					language != null ? getLocale(language.getIsocode()) : i18nService.getCurrentLocale(), attributes));
			parsedResult.append('\n');
		}

		// step. 2
		// ... adding line 'Total: {total}'
		final Map<String, Object> attributes = new HashMap<>();
		attributes.put(RESOURCEBUNDLE.TOTAL, Integer.valueOf(total));
		parsedResult.append(getLocalizedString(RESOURCEBUNDLE.TOTAL_TEMPLATE,
				language != null ? getLocale(language.getIsocode()) : i18nService.getCurrentLocale(), attributes));

		return parsedResult.toString();
	}

	/**
	 * Gets the product quantity for the specified product, warehouses and date.
	 *
	 * @param warehouses
	 *           the warehouses
	 * @param productCode
	 *           code of the product
	 * @param date
	 *           the date the specified quantity has to be available at least. ... will NOT be evaluated by this
	 *           implementation !!!
	 * @return Returns mapped available quantity of product in all specified warehouses.
	 */
	@Override
	public Map<WarehouseModel, Integer> getAvailability(final String productCode, final List<WarehouseModel> warehouses,
			final Date date)
	{
		final Map<WarehouseModel, Integer> results = new HashMap<>();

		for (final WarehouseModel warehouse : warehouses)
		{
			final Integer quantity = stockLevelDao.getAvailableQuantity(warehouse, productCode);
			results.put(warehouse, quantity);
		}
		return Collections.unmodifiableMap(results);
	}

	/**
	 * Returns product availability, passing product, and quantity as parameters.
	 *
	 * @param productCode
	 *           code of the product
	 * @param warehouses
	 *           the warehouses
	 * @param preOrderQuantity
	 *           the asked min. preOrderQuantity
	 * @return Returns the date, when the questioned quantity will be available
	 */
	@Override
	public Map<WarehouseModel, Date> getAvailability(final String productCode, final List<WarehouseModel> warehouses,
			final int preOrderQuantity)

	{
		final Map<WarehouseModel, Date> results = new HashMap<>();

		final Collection<StockLevelModel> stockLevels = stockLevelDao.findStockLevels(productCode, warehouses, preOrderQuantity);

		for (final StockLevelModel stockLevel : stockLevels)
		{
			results.put(stockLevel.getWarehouse(), stockLevel.getNextDeliveryTime());
		}
		return Collections.unmodifiableMap(results);
	}

	private String getLocalizedString(final String key, final Locale locale, final Map<String, Object> placeholders)
	{
		// get localized value for message
		final ResourceBundle bundle = bundleProvider.getResourceBundle(locale);

		String result = bundle.getString(key);
		if (placeholders != null)
		{
			// replace placeholders
			result = replacePlaceholders(result, placeholders);
		}
		return result;
	}

	@Override
	public WarehouseModel getBestMatchOfQuantity(final Map<WarehouseModel, Integer> map)
	{
		return bestMatchStrategy.getBestMatchOfQuantity(map);
	}

	@Override
	public WarehouseModel getBestMatchOfAvailability(final Map<WarehouseModel, Date> map)
	{
		return bestMatchStrategy.getBestMatchOfAvailability(map);
	}

	/**
	 * Replaces placeholders of type {placeholdername}.
	 */
	private String replacePlaceholders(final String value, final Map<String, Object> placeholders)
	{
		String result = value;
		// replace placeholders
		for (final Map.Entry<String, Object> entry : placeholders.entrySet())
		{
			final String varKey = entry.getKey();
			final Object varValue = entry.getValue() == null ? "" : entry.getValue();

			result = result.replaceAll("\\{" + varKey + "\\}", varValue.toString());
		}
		return result;
	}

	@Required
	public void setI18nService(final I18NService i18nService)
	{
		this.i18nService = i18nService;
	}

	@Required
	public void setBundleProvider(final ResourceBundleProvider bundleProvider)
	{
		this.bundleProvider = bundleProvider;
	}

	/**
	 * @param bestMatchStrategy
	 *           the bestMatchStrategy to set
	 */
	@Required
	public void setBestMatchStrategy(final BestMatchStrategy bestMatchStrategy)
	{
		this.bestMatchStrategy = bestMatchStrategy;
	}

	@Required
	public void setStockLevelDao(final StockLevelDao stockLevelDao)
	{
		this.stockLevelDao = stockLevelDao;
	}
}
