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
package de.hybris.platform.stock.impl;

import static java.lang.String.format;

import de.hybris.platform.basecommerce.enums.InStockStatus;
import de.hybris.platform.basecommerce.enums.StockLevelStatus;
import de.hybris.platform.basecommerce.enums.StockLevelUpdateType;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.jalo.JaloSystemException;
import de.hybris.platform.ordersplitting.jalo.StockLevel;
import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.servicelayer.exceptions.SystemException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.stock.StockService;
import de.hybris.platform.stock.exception.InsufficientStockLevelException;
import de.hybris.platform.stock.exception.StockLevelNotFoundException;
import de.hybris.platform.stock.model.StockLevelHistoryEntryModel;
import de.hybris.platform.stock.strategy.ProductAvailabilityStrategy;
import de.hybris.platform.stock.strategy.StockLevelProductStrategy;
import de.hybris.platform.stock.strategy.StockLevelStatusStrategy;
import de.hybris.platform.tx.Transaction;
import de.hybris.platform.tx.TransactionBody;
import de.hybris.platform.util.Config;
import de.hybris.platform.util.Utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link StockService}.
 */
public class DefaultStockService implements StockService
{
	private static final Logger LOG = Logger.getLogger(DefaultStockService.class.getName());

	private static final String NO_STOCKLEVEL_FOR_PRODUCT_TEMPLATE = "no stock level for product [%s] found.";
	private ModelService modelService;
	private StockLevelDao stockLevelDao;
	private StockLevelStatusStrategy stockLevelStatusStrategy;
	private ProductAvailabilityStrategy productAvailabilityStrategy;
	private StockLevelProductStrategy stockLevelProductStrategy;

	@Override
	public StockLevelStatus getProductStatus(final ProductModel product, final WarehouseModel warehouse)
	{
		final StockLevelModel stockLevel = getStockLevelDao().findStockLevel(getStockLevelProductStrategy().convert(product),
				warehouse);
		return getStockLevelStatusStrategy().checkStatus(stockLevel);
	}

	@Override
	public StockLevelStatus getProductStatus(final ProductModel product, final Collection<WarehouseModel> warehouses)
	{
		final List<StockLevelModel> stockLevels = new ArrayList<>(
				getStockLevelDao().findStockLevels(getStockLevelProductStrategy().convert(product), warehouses));
		return getStockLevelStatusStrategy().checkStatus(stockLevels);
	}

	/**
	 * Creates a new stock level. The created {@link StockLevelModel} is saved.
	 *
	 * @param product
	 *           the product
	 * @param warehouse
	 *           warehouse of the product
	 * @param available
	 *           amount of available products
	 *
	 * @return the created {@link StockLevelModel}
	 */
	protected StockLevelModel createStockLevel(final ProductModel product, final WarehouseModel warehouse, final int available)
	{
		return createStockLevel(product, warehouse, available, 0, 0, InStockStatus.NOTSPECIFIED, 0, true);
	}

	/**
	 * Creates a new stock level. The created {@link StockLevelModel} is saved.
	 *
	 * @param product
	 *           the product
	 * @param warehouse
	 *           warehouse of the product
	 * @param available
	 *           amount of available products
	 * @param overSelling
	 *           amount of over-selling products
	 * @param reserved
	 *           amount of reserved products
	 * @param status
	 *           tag of in stock, out of stock, or not specified
	 * @param maxStockLevelHistoryCount
	 *           max count of {@link StockLevelHistoryEntryModel}, negative value for unlimited
	 * @param treatNegativeAsZero
	 *           true if negative stock level is treated as zero
	 *
	 * @return the created {@link StockLevelModel}
	 */
	protected StockLevelModel createStockLevel(final ProductModel product, final WarehouseModel warehouse, final int available, //NOSONAR
			final int overSelling, final int reserved, final InStockStatus status, final int maxStockLevelHistoryCount,
			final boolean treatNegativeAsZero)
	{
		if (available < 0)
		{
			throw new IllegalArgumentException("available amount cannot be negative.");
		}
		if (overSelling < 0)
		{
			throw new IllegalArgumentException("overSelling amount cannot be negative.");
		}

		StockLevelModel stockLevel = getStockLevelDao().findStockLevel(getStockLevelProductStrategy().convert(product), warehouse);
		if (stockLevel != null)
		{
			throw new JaloSystemException("product [" + product + "] in warehouse [" + warehouse.getName()
					+ "] already exists. the same product cannot be created in the same warehouse again.");
		}

		//create the new stock level
		stockLevel = getModelService().create(StockLevelModel.class);
		stockLevel.setProductCode(getStockLevelProductStrategy().convert(product));
		stockLevel.setWarehouse(warehouse);
		stockLevel.setAvailable(available);
		stockLevel.setOverSelling(overSelling);
		stockLevel.setReserved(reserved);
		stockLevel.setInStockStatus(status);
		stockLevel.setMaxStockLevelHistoryCount(maxStockLevelHistoryCount);
		stockLevel.setTreatNegativeAsZero(treatNegativeAsZero);
		//create the first history entry
		if (maxStockLevelHistoryCount != 0)
		{
			final List<StockLevelHistoryEntryModel> historyEntries = new ArrayList<>();
			final StockLevelHistoryEntryModel entry = this.createStockLevelHistoryEntry(stockLevel, available, 0,
					StockLevelUpdateType.WAREHOUSE, "new in stock");
			historyEntries.add(entry);
			stockLevel.setStockLevelHistoryEntries(historyEntries);
		}
		getModelService().save(stockLevel);
		return stockLevel;
	}

	private StockLevelHistoryEntryModel createStockLevelHistoryEntry(final StockLevelModel stockLevel,
			final StockLevelUpdateType updateType, final int reserved, final String comment)
	{
		if (stockLevel.getMaxStockLevelHistoryCount() != 0)
		{
			final StockLevelHistoryEntryModel historyEntry = getModelService().create(StockLevelHistoryEntryModel.class);
			historyEntry.setStockLevel(stockLevel);
			historyEntry.setActual(stockLevel.getAvailable());
			historyEntry.setReserved(reserved);
			historyEntry.setUpdateType(updateType);
			if (comment != null)
			{
				historyEntry.setComment(comment);
			}
			historyEntry.setUpdateDate(new Date());
			getModelService().save(historyEntry);
			return historyEntry;
		}
		return null;
	}

	/**
	 * Creates a new stock level history entry. The created {@link StockLevelHistoryEntryModel} is saved.
	 *
	 * @param stockLevel
	 *           the stock level whose history entry should be created
	 * @param actual
	 *           the actual amount
	 * @param reserved
	 *           the reserved amount
	 * @param updateType
	 *           the update type
	 * @param comment
	 *           the comment
	 *
	 * @return the created {@link StockLevelHistoryEntryModel}
	 */
	protected StockLevelHistoryEntryModel createStockLevelHistoryEntry(final StockLevelModel stockLevel, final int actual,
			final int reserved, final StockLevelUpdateType updateType, final String comment)
	{
		if (stockLevel == null)
		{
			throw new IllegalArgumentException("stock level cannot be null.");
		}
		if (actual < 0)
		{
			throw new IllegalArgumentException("actual amount cannot be negative.");
		}
		if (stockLevel.getMaxStockLevelHistoryCount() != 0)
		{
			final StockLevelHistoryEntryModel historyEntry = getModelService().create(StockLevelHistoryEntryModel.class);
			historyEntry.setStockLevel(stockLevel);
			historyEntry.setActual(actual);
			historyEntry.setReserved(reserved);
			historyEntry.setUpdateType(updateType);
			if (comment != null)
			{
				historyEntry.setComment(comment);
			}
			historyEntry.setUpdateDate(new Date());
			getModelService().save(historyEntry);
			return historyEntry;
		}
		return null;
	}

	@Override
	public int getStockLevelAmount(final ProductModel product, final WarehouseModel warehouse)
	{
		final StockLevelModel stockLevel = this.checkAndGetStockLevel(product, warehouse);
		return stockLevel.getAvailable() - stockLevel.getReserved();
	}

	@Override
	public int getTotalStockLevelAmount(final ProductModel product)
	{
		final List<StockLevelModel> stockLevels = new ArrayList<>(
				getStockLevelDao().findAllStockLevels(getStockLevelProductStrategy().convert(product)));
		if (stockLevels.isEmpty())
		{
			throw new StockLevelNotFoundException(format(NO_STOCKLEVEL_FOR_PRODUCT_TEMPLATE, product.toString()));
		}
		return calculateTotalActualAmount(stockLevels);
	}

	@Override
	public int getTotalStockLevelAmount(final ProductModel product, final Collection<WarehouseModel> warehouses)
	{
		final List<StockLevelModel> stockLevels = new ArrayList<>(
				getStockLevelDao().findStockLevels(getStockLevelProductStrategy().convert(product), warehouses));
		if (stockLevels.isEmpty())
		{
			throw new StockLevelNotFoundException(format(NO_STOCKLEVEL_FOR_PRODUCT_TEMPLATE, product.toString()));
		}
		return calculateTotalActualAmount(stockLevels);
	}

	private int calculateTotalActualAmount(final List<StockLevelModel> stockLevels)
	{
		int totalActualAmount = 0;
		for (final StockLevelModel stockLevel : stockLevels)
		{
			//actualAmount can be negative
			final int actualAmount = stockLevel.getAvailable() - stockLevel.getReserved();
			if (actualAmount > 0 || !stockLevel.isTreatNegativeAsZero())
			{
				totalActualAmount += actualAmount;
			}
		}
		return totalActualAmount;
	}

	@Override
	public void setInStockStatus(final ProductModel product, final Collection<WarehouseModel> warehouses,
			final InStockStatus status)
	{
		final Collection<StockLevelModel> stockLevels = new ArrayList<>(
				getStockLevelDao().findStockLevels(getStockLevelProductStrategy().convert(product), warehouses));
		if (!stockLevels.isEmpty())
		{
			for (final StockLevelModel level : stockLevels)
			{
				level.setInStockStatus(status);
			}
			getModelService().saveAll(stockLevels);
		}
	}

	@Override
	public InStockStatus getInStockStatus(final ProductModel product, final WarehouseModel warehouse)
	{
		final StockLevelModel stockLevel = this.checkAndGetStockLevel(product, warehouse);
		return stockLevel.getInStockStatus();
	}

	
	private void clearCacheForItem(final StockLevelModel stockLevel)
	{
		Utilities.invalidateCache(stockLevel.getPk());
		getModelService().refresh(stockLevel);
	}

	@Override
	public void reserve(final ProductModel product, final WarehouseModel warehouse, final int amount, final String comment)
			throws InsufficientStockLevelException
	{
		if (amount <= 0)
		{
			throw new IllegalArgumentException("amount must be greater than zero.");
		}

		final StockLevelModel currentStockLevel = checkAndGetStockLevel(product, warehouse);

		final Integer reserved = getStockLevelDao().reserve(currentStockLevel, amount);
		if (reserved == null)
		{
			throw new InsufficientStockLevelException(
					"insufficient available amount for stock level [" + currentStockLevel.getPk() + "]");
		}
		else
		{
			clearCacheForItem(currentStockLevel);
			createStockLevelHistoryEntry(currentStockLevel, StockLevelUpdateType.CUSTOMER_RESERVE, reserved.intValue(), comment);
		}
	}

	@Override
	public void release(final ProductModel product, final WarehouseModel warehouse, final int amount, final String comment)
	{
		if (amount <= 0)
		{
			throw new IllegalArgumentException("amount must be greater than zero.");
		}
		final StockLevelModel currentStockLevel = this.checkAndGetStockLevel(product, warehouse);
		final Integer reserved = getStockLevelDao().release(currentStockLevel, amount);
		if (reserved == null)
		{
			throw new SystemException("release failed for stock level [" + currentStockLevel.getPk() + "]!");
		}
		else
		{
			clearCacheForItem(currentStockLevel);
			createStockLevelHistoryEntry(currentStockLevel, StockLevelUpdateType.CUSTOMER_RELEASE, reserved.intValue(), comment);
		}
	}

	@Override
	public void updateActualStockLevel(final ProductModel product, final WarehouseModel warehouse, final int actualAmount,
			final String comment)
	{
		final StockLevelModel stockLevel;
		try
		{
			stockLevel = this.checkAndGetStockLevel(product, warehouse);
		}
		catch (final StockLevelNotFoundException e) // NOSONAR
		{
			this.createStockLevel(product, warehouse, actualAmount);
			return;
		}

		try
		{
			final int amount;
			if (actualAmount < 0)
			{
				amount = 0;
				LOG.warn("actual amount is negative, changing amount to 0");
			}
			else
			{
				amount = actualAmount;
			}
			getStockLevelDao().updateActualAmount(stockLevel, amount);
			clearCacheForItem(stockLevel);
			createStockLevelHistoryEntry(stockLevel, StockLevelUpdateType.WAREHOUSE, 0, comment);
		}
		catch (final Exception e)
		{
			LOG.error("update not successful: " + e.getMessage());
			throw new SystemException(e);
		}
	}

	/**
	 * Gets the product quantity for the specified product, warehouses and date.
	 *
	 * @param warehouses
	 *           the warehouses
	 * @param product
	 *           the product
	 * @param date
	 *           the date the specified quantity has to be available at least.
	 * @return Returns the mapped quantity
	 */
	@Override
	public Map<WarehouseModel, Integer> getAvailability(final List<WarehouseModel> warehouses, final ProductModel product,
			final Date date)
	{
		return getProductAvailabilityStrategy().getAvailability(getStockLevelProductStrategy().convert(product), warehouses, date);
	}

	/**
	 * Returns product availability, passing product, and quantity as parameters
	 *
	 * @param product
	 *           the product
	 * @param warehouses
	 *           the warehouses
	 * @param quantity
	 *           the asked quantity
	 * @return Returns the mapped availability date
	 */
	@Override
	public Map<WarehouseModel, Date> getAvailability(final List<WarehouseModel> warehouses, final ProductModel product,
			final int quantity)
	{
		return getProductAvailabilityStrategy().getAvailability(getStockLevelProductStrategy().convert(product), warehouses,
				quantity);
	}

	@Override
	public String getAvailability(final ProductModel product, final WarehouseModel warehouse, final Date date,
			final LanguageModel language)
	{
		final List<WarehouseModel> warehouses = new ArrayList<>();
		warehouses.add(warehouse);
		return getAvailability(product, warehouses, date, language);
	}

	/**
	 * Gets the available quantity of the product for the specified warehouses by invoking injected strategy.
	 *
	 * <b>Configuration</b>:
	 *
	 * <pre>
	 *  <bean id="defaultStockLevelService" class="..."
	 * 				parent="abstractBusinessService" scope="tenant">
	 * 		<property name="productAvailabilityStrategy" ref="productAvailabilityStrategy" />
	 *  </bean>
	 * </pre>
	 *
	 * @param product
	 *           the product
	 * @param warehouses
	 *           the warehouses
	 * @param date
	 *           the date the specified quantity has to be available at least (could be null).
	 * @param language
	 *           language for which the text should be localized
	 * @return Returns a configured availability text message
	 */
	@Override
	public String getAvailability(final ProductModel product, final List<WarehouseModel> warehouses, final Date date,
			final LanguageModel language)
	{
		final Map<WarehouseModel, Integer> mappedAvalabilities = getProductAvailabilityStrategy()
				.getAvailability(getStockLevelProductStrategy().convert(product), warehouses, date);
		return getProductAvailabilityStrategy().parse(mappedAvalabilities, getStockLevelProductStrategy().convert(product), date,
				language);
	}

	@Override
	public String getAvailability(final ProductModel product, final WarehouseModel warehouse, final int quantity,
			final LanguageModel language)
	{
		final List<WarehouseModel> warehouses = new ArrayList<>();
		warehouses.add(warehouse);
		return getAvailability(product, warehouses, quantity, language);
	}

	/**
	 * Gets availability date by invoking strategy for calculating product availability, passing product, quantity and
	 * warehouses as parameters by invoking injected strategy.
	 *
	 * <b>Configuration</b>:
	 *
	 * <pre>
	 *  <bean id="defaultStockLevelService" class="..."
	 * 				parent="abstractBusinessService" scope="tenant">
	 * 		<property name="productAvailabilityStrategy" ref="productAvailabilityStrategy" />
	 *  </bean>
	 * </pre>
	 *
	 * @param product
	 *           the product
	 * @param warehouses
	 *           the warehouses
	 * @param quantity
	 *           the asked quantity
	 * @return Returns a configured availability text message
	 */
	@Override
	public String getAvailability(final ProductModel product, final List<WarehouseModel> warehouses, final int quantity,
			final LanguageModel language)
	{
		final Map<WarehouseModel, Date> mappedAvalabilities = getProductAvailabilityStrategy()
				.getAvailability(getStockLevelProductStrategy().convert(product), warehouses, quantity);
		return getProductAvailabilityStrategy().parse(mappedAvalabilities, getStockLevelProductStrategy().convert(product),
				quantity, language);
	}

	/**
	 * Returns the warehouse which offers the "best" product "quantity" by invoking injected strategy.<br>
	 *
	 * <b>Configuration:</b>
	 *
	 * <pre>
	 * <bean id="defaultStockLevelService" class="..." parent="abstractBusinessService" scope="tenant">
	 * 	<property name="productAvailabilityStrategy" ref="productAvailabilityStrategy" />
	 * </bean>
	 *
	 * <bean id="defaultProductAvailabilityStrategy" class="..." scope="tenant">
	 * 	<property name="bestMatchStrategy" ref="bestMatchStrategy"/>
	 * </bean>
	 *
	 * <bean id="defaultBestMatchStrategy" class="..." scope="tenant">
	 * </bean>
	 * </pre>
	 *
	 * @param map
	 *           the mapped quantities of a certain product
	 * @return WarehouseModel best match
	 */
	@Override
	public WarehouseModel getBestMatchOfQuantity(final Map<WarehouseModel, Integer> map)
	{
		return getProductAvailabilityStrategy().getBestMatchOfQuantity(map);
	}

	/**
	 * Returns the warehouse which offers the "best" product "availability" by invoking injected strategy.<br>
	 *
	 * <b>Configuration:</b>
	 *
	 * <pre>
	 * <bean id="defaultStockLevelService" class="..." parent="abstractBusinessService" scope="tenant">
	 * 	<property name="productAvailabilityStrategy" ref="productAvailabilityStrategy" />
	 * </bean>
	 *
	 * <bean id="defaultProductAvailabilityStrategy" class="..." scope="tenant">
	 * 	<property name="bestMatchStrategy" ref="bestMatchStrategy"/>
	 * </bean>
	 *
	 * <bean id="defaultBestMatchStrategy" class="..." scope="tenant">
	 * </bean>
	 * </pre>
	 *
	 * @param map
	 *           the mapped available dates of a certain product
	 * @return WarehouseModel best match
	 */
	@Override
	public WarehouseModel getBestMatchOfAvailability(final Map<WarehouseModel, Date> map)
	{
		return getProductAvailabilityStrategy().getBestMatchOfAvailability(map);
	}

	@Override
	public StockLevelModel getStockLevel(final ProductModel product, final WarehouseModel warehouse)
	{
		return getStockLevelDao().findStockLevel(getStockLevelProductStrategy().convert(product), warehouse);
	}

	@Override
	public Collection<StockLevelModel> getAllStockLevels(final ProductModel product)
	{
		return getStockLevelDao().findAllStockLevels(getStockLevelProductStrategy().convert(product));
	}

	@Override
	public Collection<StockLevelModel> getStockLevels(final ProductModel product, final Collection<WarehouseModel> warehouses)
	{
		return getStockLevelDao().findStockLevels(getStockLevelProductStrategy().convert(product), warehouses);
	}

	/**
	 * Checks if the stock level of the specified product in the specified warehouse exists, and returns the found
	 * {@link StockLevelModel}.
	 *
	 * @param product
	 *           the product
	 * @param warehouse
	 *           the warehouse where product is 'stored'
	 * @return found {@link StockLevelModel}
	 * @throws StockLevelNotFoundException
	 *            if no such {@link StockLevelModel} can be found
	 */
	private StockLevelModel checkAndGetStockLevel(final ProductModel product, final WarehouseModel warehouse)
	{
		final StockLevelModel stockLevel = getStockLevelDao().findStockLevel(getStockLevelProductStrategy().convert(product),
				warehouse);
		if (stockLevel == null)
		{
			throw new StockLevelNotFoundException(
					format(NO_STOCKLEVEL_FOR_PRODUCT_TEMPLATE, product.toString()) + " in warehouse [" + warehouse.getName());
		}
		return stockLevel;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	protected StockLevelDao getStockLevelDao()
	{
		return stockLevelDao;
	}

	@Required
	public void setStockLevelDao(final StockLevelDao stockLevelDao)
	{
		this.stockLevelDao = stockLevelDao;
	}

	protected StockLevelStatusStrategy getStockLevelStatusStrategy()
	{
		return stockLevelStatusStrategy;
	}

	@Required
	public void setStockLevelStatusStrategy(final StockLevelStatusStrategy stockLevelStatusStrategy)
	{
		this.stockLevelStatusStrategy = stockLevelStatusStrategy;
	}

	protected ProductAvailabilityStrategy getProductAvailabilityStrategy()
	{
		return productAvailabilityStrategy;
	}

	/**
	 * The injected strategy will be used for calculating the availability of the specified product.
	 *
	 * @param productAvailabilityStrategy
	 *           the productAvailabilityStrategy to set
	 */
	@Required
	public void setProductAvailabilityStrategy(final ProductAvailabilityStrategy productAvailabilityStrategy)
	{
		this.productAvailabilityStrategy = productAvailabilityStrategy;
	}

	protected StockLevelProductStrategy getStockLevelProductStrategy()
	{
		return stockLevelProductStrategy;
	}

	@Required
	public void setStockLevelProductStrategy(final StockLevelProductStrategy stockLevelProductStrategy)
	{
		this.stockLevelProductStrategy = stockLevelProductStrategy;
	}
}
