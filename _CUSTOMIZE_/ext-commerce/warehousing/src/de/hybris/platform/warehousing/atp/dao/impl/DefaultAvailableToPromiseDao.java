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
package de.hybris.platform.warehousing.atp.dao.impl;

import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.warehousing.atp.dao.AvailableToPromiseDao;
import de.hybris.platform.warehousing.model.AllocationEventModel;
import de.hybris.platform.warehousing.model.CancellationEventModel;
import de.hybris.platform.warehousing.model.IncreaseEventModel;
import de.hybris.platform.warehousing.model.InventoryEventModel;
import de.hybris.platform.warehousing.model.ShrinkageEventModel;
import de.hybris.platform.warehousing.model.WastageEventModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * Default implementation of the {@link AvailableToPromiseDao}
 */
public class DefaultAvailableToPromiseDao extends AbstractItemDao implements AvailableToPromiseDao
{
	protected static final String STOCK_LEVELS = "stockLevels";
	protected static final String STOCK_LEVELS_RETURNED = "stockLevelsReturned";
	protected static final String STOCK_LEVELS_EXTERNAL = "stockLevelsExternal";

	@Override
	public Long getAvailabilityForStockLevels(final Map<String, Object> params)
	{
		if (((List) params.get(STOCK_LEVELS)).isEmpty())
		{
			return 0L;
		}
		final String stockLevelQryString = ("SELECT SUM({" + StockLevelModel.AVAILABLE + "}) FROM {" + StockLevelModel._TYPECODE
				+ "} WHERE {" + StockLevelModel.PK + "} IN (?" + STOCK_LEVELS + ")");

		return returnAggregateQuantity(stockLevelQryString, params, STOCK_LEVELS);
	}

	@Override
	public Long getAllocationQuantityForStockLevels(final Map<String, Object> params)
	{
		if (((List) params.get(STOCK_LEVELS)).isEmpty())
		{
			return 0L;
		}
		final String inventoryEvtQryString = ("SELECT SUM({" + InventoryEventModel.QUANTITY + "}) FROM {"
				+ AllocationEventModel._TYPECODE + " as ae} WHERE {ae." + InventoryEventModel.STOCKLEVEL + "} IN (?" + STOCK_LEVELS
				+ ")");

		return returnAggregateQuantity(inventoryEvtQryString, params, STOCK_LEVELS);
	}

	@Override
	public Long getCancellationQuantityForStockLevels(final Map<String, Object> params)
	{
		if (((List) params.get(STOCK_LEVELS)).isEmpty())
		{
			return 0L;
		}
		final String inventoryEvtQryString = ("SELECT SUM({" + InventoryEventModel.QUANTITY + "}) FROM {"
				+ CancellationEventModel._TYPECODE + " as ae} WHERE {ae." + InventoryEventModel.STOCKLEVEL + "} IN (?" + STOCK_LEVELS
				+ ")");

		return returnAggregateQuantity(inventoryEvtQryString, params, STOCK_LEVELS);
	}

	@Override
	public Long getReservedQuantityForStockLevels(final Map<String, Object> params)
	{
		if (((List) params.get(STOCK_LEVELS)).isEmpty())
		{
			return 0L;
		}
		final String stockLevelQryString = ("SELECT SUM({" + StockLevelModel.RESERVED + "}) FROM {" + StockLevelModel._TYPECODE
				+ "} WHERE {" + StockLevelModel.PK + "} IN (?" + STOCK_LEVELS + ")");

		return returnAggregateQuantity(stockLevelQryString, params, STOCK_LEVELS);
	}

	@Override
	public Long getShrinkageQuantityForStockLevels(final Map<String, Object> params)
	{
		if (((List) params.get(STOCK_LEVELS)).isEmpty())
		{
			return 0L;
		}
		final String inventoryEvtQryString = ("SELECT SUM({" + ShrinkageEventModel.QUANTITY + "}) FROM {"
				+ ShrinkageEventModel._TYPECODE + " as ae} WHERE {ae." + ShrinkageEventModel.STOCKLEVEL + "} IN (?" + STOCK_LEVELS
				+ ")");

		return returnAggregateQuantity(inventoryEvtQryString, params, STOCK_LEVELS);
	}

	@Override
	public Long getWastageQuantityForStockLevels(final Map<String, Object> params)
	{
		if (((List) params.get(STOCK_LEVELS)).isEmpty())
		{
			return 0L;
		}
		final String inventoryEvtQryString = ("SELECT SUM({" + WastageEventModel.QUANTITY + "}) FROM {"
				+ WastageEventModel._TYPECODE + " as ae} WHERE {ae." + WastageEventModel.STOCKLEVEL + "} IN (?" + STOCK_LEVELS + ")");

		return returnAggregateQuantity(inventoryEvtQryString, params, STOCK_LEVELS);
	}

	@Override
	public Long getIncreaseQuantityForStockLevels(final Map<String, Object> params)
	{
		if (((List) params.get(STOCK_LEVELS)).isEmpty())
		{
			return 0L;
		}
		final String inventoryEvtQryString = ("SELECT SUM({" + IncreaseEventModel.QUANTITY + "}) FROM {"
				+ IncreaseEventModel._TYPECODE + " as ae} WHERE {ae." + IncreaseEventModel.STOCKLEVEL + "} IN (?" + STOCK_LEVELS
				+ ")");

		return returnAggregateQuantity(inventoryEvtQryString, params, STOCK_LEVELS);
	}

	/**
	 * Runs an aggregation query that returns an instance of {@link Long}. <tt>Never null.</tt>
	 *
	 * @param queryString
	 * 		Query to run
	 * @param params
	 * 		Parameters to apply to the query
	 * @return The aggregation result
	 */
	protected Long returnAggregateQuantity(final String queryString, final Map<String, Object> params, final String key)
	{
		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(queryString);
		fQuery.addQueryParameter(key, params.get(key));

		final List<Class<Long>> resultClassList = new ArrayList<>();
		resultClassList.add(Long.class);
		fQuery.setResultClassList(resultClassList);

		final SearchResult<Long> result = getFlexibleSearchService().search(fQuery);
		return result.getResult().stream().filter(Objects::nonNull).findFirst().orElse(0L);
	}
}
