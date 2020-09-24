/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 *
 */
package de.hybris.platform.warehousing.stock.services.impl;

import de.hybris.platform.basecommerce.enums.InStockStatus;
import de.hybris.platform.commerceservices.stock.strategies.CommerceAvailabilityCalculationStrategy;
import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.stock.StockService;
import de.hybris.platform.stock.impl.StockLevelDao;
import de.hybris.platform.warehousing.atp.strategy.impl.WarehousingAvailabilityCalculationStrategy;
import de.hybris.platform.warehousing.enums.AsnStatus;
import de.hybris.platform.warehousing.stock.daos.WarehouseStockLevelDao;
import de.hybris.platform.warehousing.stock.services.WarehouseStockService;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;


/**
 * The default implementation will get the stock level for the given product-warehouse pair using the
 * {@link StockService}, then submit that stock level to the {@link CommerceAvailabilityCalculationStrategy}.
 *
 * @see StockService
 * @see CommerceAvailabilityCalculationStrategy
 */
public class DefaultWarehouseStockService implements WarehouseStockService
{
	private StockService stockService;
	private StockLevelDao stockLevelDao;
	private WarehousingAvailabilityCalculationStrategy commerceStockLevelCalculationStrategy;
	private ModelService modelService;
	private WarehouseStockLevelDao warehouseStockLevelDao;

	@Override
	public Long getStockLevelForProductCodeAndWarehouse(final String productCode, final WarehouseModel warehouse)
	{
		Preconditions.checkArgument(productCode != null, "productCode cannot be null.");
		Preconditions.checkArgument(warehouse != null, "Warehouse cannot be null.");
		final Collection<StockLevelModel> stockLevels = getStockLevelDao()
				.findStockLevels(productCode, Collections.singleton(warehouse));

		if (CollectionUtils.isNotEmpty(stockLevels))
		{
			return getCommerceStockLevelCalculationStrategy().calculateAvailability(stockLevels);
		}
		return Long.valueOf(0);
	}

	@Override
	public StockLevelModel createStockLevel(final String productCode, final WarehouseModel warehouse,
			final int initialQuantityOnHand, final InStockStatus status, final Date releaseDate, final String bin)
	{
		final StockLevelModel stockLevel = getModelService().create(StockLevelModel.class);
		stockLevel.setProductCode(productCode);
		stockLevel.setWarehouse(warehouse);
		stockLevel.setAvailable(initialQuantityOnHand);
		stockLevel.setInStockStatus(status);
		stockLevel.setReleaseDate(releaseDate);
		stockLevel.setBin(bin);
		getModelService().save(stockLevel);
		return stockLevel;
	}

	@Override
	public StockLevelModel getUniqueStockLevel(final String productCode, final String warehouseCode, final String binCode,
			final Date releaseDate)
	{
		List<StockLevelModel> stockLevelResults = getWarehouseStockLevelDao()
				.getStockLevels(productCode, warehouseCode, binCode, releaseDate);

		if (releaseDate == null && stockLevelResults.isEmpty())
		{
			stockLevelResults = getWarehouseStockLevelDao().getFutureStockLevels(productCode, warehouseCode, binCode);
			stockLevelResults = stockLevelResults.stream()
					.filter(sl -> sl.getAsnEntry() != null && AsnStatus.RECEIVED.equals(sl.getAsnEntry().getAsn().getStatus()))
					.collect(Collectors.toList());
		}

		Assert.notNull(stockLevelResults,
				String.format("No StockLevel can be found for product code [%s] and warehouse [%s].", productCode, warehouseCode));
		Assert.notEmpty(stockLevelResults,
				String.format("No StockLevel can be found for product code [%s] and warehouse [%s].", productCode, warehouseCode));
		Assert.isTrue(!(stockLevelResults.size() > 1), String.format(
				"More than one StockLevels have been found for product code [%s] and warehouse [%s]. You might want to be more specific and provide bin code and/or release date",
				productCode, warehouseCode));
		return stockLevelResults.iterator().next();
	}

	protected StockService getStockService()
	{
		return stockService;
	}

	@Required
	public void setStockService(final StockService stockService)
	{
		this.stockService = stockService;
	}

	protected WarehousingAvailabilityCalculationStrategy getCommerceStockLevelCalculationStrategy()
	{
		return commerceStockLevelCalculationStrategy;
	}

	@Required
	public void setCommerceStockLevelCalculationStrategy(
			final WarehousingAvailabilityCalculationStrategy commerceStockLevelCalculationStrategy)
	{
		this.commerceStockLevelCalculationStrategy = commerceStockLevelCalculationStrategy;
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

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	protected WarehouseStockLevelDao getWarehouseStockLevelDao()
	{
		return warehouseStockLevelDao;
	}

	@Required
	public void setWarehouseStockLevelDao(WarehouseStockLevelDao warehouseStockLevelDao)
	{
		this.warehouseStockLevelDao = warehouseStockLevelDao;
	}
}
