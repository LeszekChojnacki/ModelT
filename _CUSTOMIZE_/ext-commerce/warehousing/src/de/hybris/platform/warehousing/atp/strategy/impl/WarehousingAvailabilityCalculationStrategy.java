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
package de.hybris.platform.warehousing.atp.strategy.impl;

import de.hybris.platform.basecommerce.enums.InStockStatus;
import de.hybris.platform.commerceservices.stock.strategies.CommerceAvailabilityCalculationStrategy;
import de.hybris.platform.commerceservices.stock.strategies.impl.DefaultCommerceAvailabilityCalculationStrategy;
import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;
import de.hybris.platform.storelocator.model.PointOfServiceModel;
import de.hybris.platform.warehousing.atp.formula.services.AtpFormulaService;
import de.hybris.platform.warehousing.enums.AsnStatus;
import de.hybris.platform.warehousing.model.AtpFormulaModel;
import de.hybris.platform.warehousing.returns.service.RestockConfigService;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;


/**
 * Warehousing implementation of {@link CommerceAvailabilityCalculationStrategy}
 */
public class WarehousingAvailabilityCalculationStrategy extends DefaultCommerceAvailabilityCalculationStrategy
{
	private AtpFormulaService atpFormulaService;
	private RestockConfigService restockConfigService;
	private BaseStoreService baseStoreService;

	private static final String STOCK_LEVELS = "stockLevels";
	private static final Logger LOGGER = LoggerFactory.getLogger(WarehousingAvailabilityCalculationStrategy.class);

	@Override
	public Long calculateAvailability(final Collection<StockLevelModel> stockLevels)
	{
		Long availability = Long.valueOf(0);

		if (!stockLevels.isEmpty())
		{
			if (stockLevels.stream().anyMatch(stockLevel -> InStockStatus.FORCEINSTOCK.equals(stockLevel.getInStockStatus())))
			{
				return null;
			}
			final AtpFormulaModel atpFormula = getDefaultAtpFormula(stockLevels);
			if (atpFormula != null)
			{
				final Map<String, Object> params = filterStocks(stockLevels, atpFormula);
				availability = getAtpFormulaService().getAtpValueFromFormula(atpFormula, params);
			}
			else
			{
				LOGGER.debug("No AtpFormula found, The availability is set to 0 by default");
			}
		}

		return availability;
	}

	/**
	 * Gets the default ATP formula based on the current basestore or from the first stock level  of the given list
	 *
	 * @param stockLevels
	 * 		the list of stock levels from where to retrieve the default base store in case the current basestore is unknown
	 * @return the default ATP formula
	 */
	protected AtpFormulaModel getDefaultAtpFormula(final Collection<StockLevelModel> stockLevels)
	{
		validateParameterNotNullStandardMessage("stocklevels", stockLevels);
		BaseStoreModel currentBaseStore = getBaseStoreService().getCurrentBaseStore();

		if (currentBaseStore == null && !stockLevels.isEmpty())
		{
			final WarehouseModel warehouse = stockLevels.iterator().next().getWarehouse();
			final Collection<BaseStoreModel> basestores = warehouse.getBaseStores();
			final Collection<PointOfServiceModel> posList = warehouse.getPointsOfService();

			if (!CollectionUtils.isEmpty(basestores))
			{
				currentBaseStore = basestores.iterator().next();
			}
			else if (!CollectionUtils.isEmpty(posList))
			{
				currentBaseStore = posList.iterator().next().getBaseStore();
			}
		}

		return currentBaseStore != null ? currentBaseStore.getDefaultAtpFormula() : null;
	}

	/**
	 * Filters the {@link StockLevelModel}(s) based on given {@link AtpFormulaModel}
	 *
	 * @param stockLevels
	 * 		{@link StockLevelModel} to be filtered
	 * @param atpFormula
	 * 		{@link AtpFormulaModel} the formula to filter the stocks
	 * @return A map that contains filtered stocklevels
	 */
	protected Map<String, Object> filterStocks(final Collection<StockLevelModel> stockLevels, final AtpFormulaModel atpFormula)
	{
		final Collection<StockLevelModel> stockLevelsFiltered = filterStockLevels(stockLevels);
		if (atpFormula.getExternal() == null || !atpFormula.getExternal())
		{
			stockLevelsFiltered.removeAll(filterStockLevelsExternal(stockLevelsFiltered));
		}
		if (atpFormula.getReturned() == null || !atpFormula.getReturned())
		{
			stockLevelsFiltered.removeAll(filterStockLevelsReturned(stockLevelsFiltered));
		}

		final Map<String, Object> params = new HashMap<>();
		params.put(STOCK_LEVELS, stockLevelsFiltered);

		return params;
	}

	/**
	 * Removes the {@link StockLevelModel}(s) that are either forced out of stock
	 * Or if they belong to cancelled {@link de.hybris.platform.warehousing.model.AdvancedShippingNoticeModel}
	 *
	 * @param stockLevels
	 * 		the stock levels to filter
	 * @return the filtered stock levels
	 */
	protected Collection<StockLevelModel> filterStockLevels(final Collection<StockLevelModel> stockLevels)
	{
		validateParameterNotNullStandardMessage("stocklevels", stockLevels);
		return stockLevels.stream().filter(
				level -> !InStockStatus.FORCEOUTOFSTOCK.equals(level.getInStockStatus()) && (level.getAsnEntry() == null
						|| !AsnStatus.CANCELLED.equals(level.getAsnEntry().getAsn().getStatus()))).collect(Collectors.toList());
	}

	/**
	 * Filters the stock level that are returned bins
	 *
	 * @param stockLevels
	 * 		the {@link StockLevelModel} to filter
	 * @return the filtered stock levels (i.e levels that are returned bins)
	 */
	protected Collection<StockLevelModel> filterStockLevelsReturned(final Collection<StockLevelModel> stockLevels)
	{
		validateParameterNotNullStandardMessage("stocklevels", stockLevels);
		final String returnedBinCode = getRestockConfigService().getReturnedBinCode();
		return (stockLevels.isEmpty()) ?
				java.util.Collections.emptyList() :
				stockLevels.stream().filter(level -> returnedBinCode.equals(level.getBin())).collect(Collectors.toList());
	}


	/**
	 * Filters the stock level that are external
	 *
	 * @param stockLevels
	 * 		the {@link StockLevelModel} to filter
	 * @return the external stocklevels
	 */
	protected Collection<StockLevelModel> filterStockLevelsExternal(final Collection<StockLevelModel> stockLevels)
	{
		validateParameterNotNullStandardMessage("stocklevels", stockLevels);
		return (stockLevels.isEmpty()) ?
				java.util.Collections.emptyList() :
				stockLevels.stream().filter(level -> level.getWarehouse().isExternal()).collect(Collectors.toList());
	}

	protected AtpFormulaService getAtpFormulaService()
	{
		return atpFormulaService;
	}

	@Required
	public void setAtpFormulaService(final AtpFormulaService atpFormulaService)
	{
		this.atpFormulaService = atpFormulaService;
	}


	protected RestockConfigService getRestockConfigService()
	{
		return restockConfigService;
	}

	@Required
	public void setRestockConfigService(RestockConfigService restockConfigService)
	{
		this.restockConfigService = restockConfigService;
	}

	protected BaseStoreService getBaseStoreService()
	{
		return baseStoreService;
	}

	@Required
	public void setBaseStoreService(BaseStoreService baseStoreService)
	{
		this.baseStoreService = baseStoreService;
	}
}
