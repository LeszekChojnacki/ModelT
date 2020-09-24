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
package de.hybris.platform.warehousing.stock.daos.impl;

import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.warehousing.stock.daos.WarehouseStockLevelDao;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;


/**
 * Default Data Access to retrieve a specific {@link StockLevelModel}.
 */
public class DefaultWarehouseStockLevelDao extends AbstractItemDao implements WarehouseStockLevelDao
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultWarehouseStockLevelDao.class);

	@Override
	public List<StockLevelModel> getStockLevels(final String productCode, final String warehouseCode, final String binCode,
			final Date releaseDate)
	{
		validateParameterNotNullStandardMessage("productCode", productCode);
		validateParameterNotNullStandardMessage("warehouseCode", warehouseCode);

		final Map<String, Object> params = new HashMap<String, Object>();

		params.put(StockLevelModel.PRODUCTCODE, productCode);
		params.put(WarehouseModel.CODE, warehouseCode);

		final StringBuilder query = new StringBuilder("SELECT {s:").append(StockLevelModel.PK).append("} FROM { ")
				.append(StockLevelModel._TYPECODE).append(" AS s ").append("JOIN ").append(WarehouseModel._TYPECODE)
				.append(" AS w ON ").append("{s:").append(StockLevelModel.WAREHOUSE).append("}={w:").append(WarehouseModel.PK)
				.append('}').append("} WHERE {s:").append(StockLevelModel.PRODUCTCODE).append("} = ?")
				.append(StockLevelModel.PRODUCTCODE).append(" AND {w:").append(WarehouseModel.CODE).append("} = ?")
				.append(WarehouseModel.CODE);

		if (binCode != null)
		{
			params.put(StockLevelModel.BIN, binCode);
			query.append(" AND {s:").append(StockLevelModel.BIN).append("} = ?").append(StockLevelModel.BIN);
		}

		if (releaseDate != null)
		{
			final Calendar calendar = Calendar.getInstance();
			calendar.setTime(releaseDate);
			params.put(StockLevelModel.RELEASEDATE, releaseDate);
			query.append(" AND DAY({s:").append(StockLevelModel.RELEASEDATE).append("}) = DAY(CAST(?")
					.append(StockLevelModel.RELEASEDATE).append(" AS DATE))");
			query.append(" AND MONTH({s:").append(StockLevelModel.RELEASEDATE).append("}) = MONTH(CAST(?")
					.append(StockLevelModel.RELEASEDATE).append(" AS DATE))");
			query.append(" AND YEAR({s:").append(StockLevelModel.RELEASEDATE).append("}) = YEAR(CAST(?")
					.append(StockLevelModel.RELEASEDATE).append(" AS DATE))");
		}
		else
		{
			params.put(StockLevelModel.RELEASEDATE, new Date());
			query.append(" AND ({s:").append(StockLevelModel.RELEASEDATE).append("} <= CAST(?").append(StockLevelModel.RELEASEDATE)
					.append(" AS DATE) OR {s:").append(StockLevelModel.RELEASEDATE).append("} IS NULL)");
		}

		if (LOG.isDebugEnabled())
		{
			LOG.debug("Searching for stockLevel for product code [{}] and warehouse code [{}]", productCode, warehouseCode);
		}

		final SearchResult<StockLevelModel> results = getFlexibleSearchService().search(query.toString(), params);

		if (LOG.isDebugEnabled())
		{
			LOG.debug("Results: {}", (results == null ? "null" : String.valueOf(results.getCount())));
		}

		return (results == null || CollectionUtils.isEmpty(results.getResult())) ? Collections.emptyList() : results.getResult();
	}

	@Override
	public List<StockLevelModel> getFutureStockLevels(final String productCode, final String warehouseCode, final String binCode)
	{
		validateParameterNotNullStandardMessage("productCode", productCode);
		validateParameterNotNullStandardMessage("warehouseCode", warehouseCode);

		final Map<String, Object> params = new HashMap<String, Object>();

		params.put(StockLevelModel.PRODUCTCODE, productCode);
		params.put(WarehouseModel.CODE, warehouseCode);
		params.put(StockLevelModel.RELEASEDATE, new Date());

		final StringBuilder query = new StringBuilder("SELECT {s:").append(StockLevelModel.PK).append("} FROM { ")
				.append(StockLevelModel._TYPECODE).append(" AS s ").append("JOIN ").append(WarehouseModel._TYPECODE)
				.append(" AS w ON ").append("{s:").append(StockLevelModel.WAREHOUSE).append("}={w:").append(WarehouseModel.PK)
				.append('}').append("} WHERE {s:").append(StockLevelModel.PRODUCTCODE).append("} = ?")
				.append(StockLevelModel.PRODUCTCODE).append(" AND {w:").append(WarehouseModel.CODE).append("} = ?")
				.append(WarehouseModel.CODE).append(" AND {s:").append(StockLevelModel.RELEASEDATE).append("} > CAST(?")
				.append(StockLevelModel.RELEASEDATE).append(" AS DATE)");

		if (binCode != null)
		{
			params.put(StockLevelModel.BIN, binCode);
			query.append(" AND {s:").append(StockLevelModel.BIN).append("} = ?").append(StockLevelModel.BIN);
		}

		if (LOG.isDebugEnabled())
		{
			LOG.debug("Searching for future stockLevel for product code [{}] and warehouse code [{}]", productCode, warehouseCode);
		}

		final SearchResult<StockLevelModel> results = getFlexibleSearchService().search(query.toString(), params);

		if (LOG.isDebugEnabled())
		{
			LOG.debug("Results: {}", (results == null ? "null" : String.valueOf(results.getCount())));
		}

		return (results == null || CollectionUtils.isEmpty(results.getResult())) ? Collections.emptyList() : results.getResult();
	}
}
