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

import static java.lang.Integer.valueOf;

import de.hybris.platform.basecommerce.enums.InStockStatus;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.servicelayer.exceptions.SystemException;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.type.TypeService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;


/**
 * Default implementation of the {@link StockLevelDao}.
 */
public class DefaultStockLevelDao extends AbstractItemDao implements StockLevelDao
{

	private static final Logger LOG = Logger.getLogger(DefaultStockLevelDao.class);

	private TypeService typeService;

	private TransactionTemplate transactionTemplate;
	private JdbcTemplate jdbcTemplate;
	private StockLevelColumns stockLevelColumns;

	@Override
	public StockLevelModel findStockLevel(final String productCode, final WarehouseModel warehouse)
	{
		checkProductCode(productCode);
		checkWarehouse(warehouse);
		final String query = "SELECT {" + StockLevelModel.PK + "} FROM {" + StockLevelModel._TYPECODE + "} WHERE {"
				+ StockLevelModel.PRODUCTCODE + "} = ?" + StockLevelModel.PRODUCTCODE + " AND {" + StockLevelModel.WAREHOUSE + "} = ?"
				+ StockLevelModel.WAREHOUSE;
		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query);
		fQuery.addQueryParameter(StockLevelModel.PRODUCTCODE, productCode);
		fQuery.addQueryParameter(StockLevelModel.WAREHOUSE, warehouse);
		final SearchResult<StockLevelModel> result = getFlexibleSearchService().search(fQuery);
		final List<StockLevelModel> stockLevels = result.getResult();

		if (stockLevels.isEmpty())
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("no stock level for product [" + productCode + "] in warehouse [" + warehouse.getName() + "] found.");
			}
			return null;
		}
		else if (stockLevels.size() == 1)
		{
			return stockLevels.get(0);
		}
		else
		{
			LOG.error("more than one stock level with product code [" + productCode + "] and warehouse [" + warehouse.getName()
					+ "] found, and the first one is returned.");
			return stockLevels.get(0);
		}
	}

	@Override
	public Collection<StockLevelModel> findAllStockLevels(final String productCode)
	{
		checkProductCode(productCode);
		final String query = "SELECT {" + StockLevelModel.PK + "} FROM {" + StockLevelModel._TYPECODE + "} WHERE {"
				+ StockLevelModel.PRODUCTCODE + "} = ?" + StockLevelModel.PRODUCTCODE;
		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query);
		fQuery.addQueryParameter(StockLevelModel.PRODUCTCODE, productCode);
		final SearchResult<StockLevelModel> result = getFlexibleSearchService().search(fQuery);
		return result.getResult();
	}

	@Override
	public Collection<StockLevelModel> findStockLevels(final String productCode, final Collection<WarehouseModel> warehouses)
	{
		return findStockLevelsImpl(productCode, warehouses, null);
	}

	@Override
	public Collection<StockLevelModel> findStockLevels(final String productCode, final Collection<WarehouseModel> warehouses,
			final int preOrderQuantity)
	{
		return findStockLevelsImpl(productCode, warehouses, Integer.valueOf(preOrderQuantity));
	}

	private Collection<StockLevelModel> findStockLevelsImpl(final String productCode, final Collection<WarehouseModel> warehouses,
			final Integer preOrderQuantity)
	{
		checkProductCode(productCode);
		final List<WarehouseModel> filteredWarehouses = filterWarehouses(warehouses);
		if (filteredWarehouses.isEmpty())
		{
			return Collections.emptyList();
		}
		final String warehousesParam = "WAREHOUSES_PARAM";
		final StringBuilder query = new StringBuilder();

		query.append("SELECT {").append(StockLevelModel.PK).append("} FROM {").append(StockLevelModel._TYPECODE).append("} WHERE {")
				.append(StockLevelModel.PRODUCTCODE).append("} = ?").append(StockLevelModel.PRODUCTCODE);

		if (preOrderQuantity != null)
		{
			query.append(" AND {").append(StockLevelModel.MAXPREORDER).append("} >= ?").append(StockLevelModel.MAXPREORDER);
		}

		query.append(" AND {").append(StockLevelModel.WAREHOUSE).append("} IN (?").append(warehousesParam).append(")");

		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query);
		fQuery.addQueryParameter(StockLevelModel.PRODUCTCODE, productCode);
		if (preOrderQuantity != null)
		{
			fQuery.addQueryParameter(StockLevelModel.MAXPREORDER, preOrderQuantity);
		}
		fQuery.addQueryParameter(warehousesParam, filteredWarehouses);
		final SearchResult<StockLevelModel> result = getFlexibleSearchService().search(fQuery);
		return result.getResult();
	}

	@Override
	public Integer getAvailableQuantity(final WarehouseModel warehouse, final String productCode)
	{
		final Map<String, Object> params = new HashMap<>();
		final StringBuilder query = new StringBuilder("select {s.").append(StockLevelModel.AVAILABLE).append('}');
		query.append("  from {").append(StockLevelModel._TYPECODE).append(" as s}");
		query.append("  where {s.").append(StockLevelModel.WAREHOUSE).append("} = ?").append(StockLevelModel.WAREHOUSE);
		query.append("  and {s.").append(StockLevelModel.PRODUCTCODE).append("} = ?").append(StockLevelModel.PRODUCTCODE);
		params.put(StockLevelModel.WAREHOUSE, warehouse);
		params.put(StockLevelModel.PRODUCTCODE, productCode);

		final FlexibleSearchQuery searchQuery = new FlexibleSearchQuery(query.toString(), params);
		final List resultClassList = new ArrayList();
		resultClassList.add(Integer.class);
		searchQuery.setResultClassList(resultClassList);
		final SearchResult<Integer> result = getFlexibleSearchService().search(searchQuery);

		if (result.getResult().isEmpty())
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("NO StockLevel instance found for product '" + productCode + "' and warehouse '" + warehouse + "'!.");
			}
			return Integer.valueOf(0);
		}
		else if (result.getResult().size() > 1)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("More than one StockLevel instance found for product '" + productCode + "' and warehouse '" + warehouse
						+ "'!.");
			}
			return Integer.valueOf(0);
		}

		final Object res = result.getResult().iterator().next();

		return (Integer) res;
	}

	@SuppressWarnings("squid:S1188")
	@Override
	public Integer reserve(final StockLevelModel stockLevel, final int amount)
	{
		return transactionTemplate.execute(new TransactionCallback<Integer>()
		{
			@Override
			public Integer doInTransaction(final TransactionStatus status)
			{
				try
				{
					final InStockStatus inStockStatus = stockLevel.getInStockStatus();
					final String reserveQuery = assembleReserveStockLevelQuery(inStockStatus);
					final Integer theAmount = Integer.valueOf(amount);
					final Long pk = Long.valueOf(stockLevel.getPk().getLongValue());
					int rows;
					if (InStockStatus.FORCEINSTOCK.equals(inStockStatus))
					{
						rows = jdbcTemplate.update(reserveQuery, theAmount, pk);
					}
					else
					{
						rows = jdbcTemplate.update(reserveQuery, theAmount, pk, theAmount);
					}

					return peekStockLevelReserved(rows, stockLevel.getPk(), "more rows found for the reservation: [");
				}
				catch (final DataAccessException dae)
				{
					throw new SystemException(dae);
				}
			}
		});
	}

	@Override
	public Integer release(final StockLevelModel stockLevel, final int amount)
	{
		return transactionTemplate.execute(new TransactionCallback<Integer>()
		{
			@Override
			public Integer doInTransaction(final TransactionStatus status)
			{
				try
				{
					final int rows = runJdbcQuery(assembleReleaseStockLevelQuery(), amount, stockLevel);

					return peekStockLevelReserved(rows, stockLevel.getPk(), "more rows found for the release: [");
				}
				catch (final DataAccessException dae)
				{
					throw new SystemException(dae);
				}
			}
		});
	}

	private Integer peekStockLevelReserved(final int rows, final PK pk, final String message)
	{
		final Long pkLong = Long.valueOf(pk.getLongValue());
		Integer currentReserved = valueOf(-1);
		if (rows == 1)
		{
			//get current reserved value via another JDBC query
			final String requestAmountQuery = assembleRequestStockLevelQuery();
			currentReserved = jdbcTemplate.queryForObject(requestAmountQuery, Integer.class, pkLong);
		}
		else if (rows > 1)
		{
			throw new IllegalStateException(message + rows + "] rows for stock level [" + pkLong + "]");
		}
		return rows == 1 ? currentReserved : null;
	}

	@Override
	public void updateActualAmount(final StockLevelModel stockLevel, final int actualAmount)
	{
		transactionTemplate.execute(new TransactionCallbackWithoutResult()
		{
			@Override
			protected void doInTransactionWithoutResult(final TransactionStatus arg0)
			{
				try
				{
					final int rows = runJdbcQuery(assembleUpdateStockLevelQuery(), actualAmount, stockLevel);
					if (rows > 1)
					{
						throw new IllegalStateException("more stock level rows found for the update: [" + stockLevel.getPk() + "]");
					}
				}
				catch (final DataAccessException dae)
				{
					throw new SystemException(dae);
				}
			}
		});
	}

	private int runJdbcQuery(final String query, final int amount, final StockLevelModel stockLevel)
	{
		final Integer theAmount = Integer.valueOf(amount);
		final Long pk = Long.valueOf(stockLevel.getPk().getLongValue());
		int rows;
		rows = jdbcTemplate.update(query, theAmount, pk);
		return rows;
	}

	private class StockLevelColumns
	{
		private final String tableName;
		private final String pkCol;
		private final String reservedCol;
		private final String availableCol;
		private final String oversellingCol;

		private StockLevelColumns(final TypeService typeService)
		{
			final ComposedTypeModel stockLevelType = typeService.getComposedTypeForClass(StockLevelModel.class);
			tableName = stockLevelType.getTable();
			pkCol = typeService.getAttributeDescriptor(stockLevelType, StockLevelModel.PK).getDatabaseColumn();
			reservedCol = typeService.getAttributeDescriptor(stockLevelType, StockLevelModel.RESERVED).getDatabaseColumn();
			availableCol = typeService.getAttributeDescriptor(stockLevelType, StockLevelModel.AVAILABLE).getDatabaseColumn();
			oversellingCol = typeService.getAttributeDescriptor(stockLevelType, StockLevelModel.OVERSELLING).getDatabaseColumn();
		}

	}

	/**
	 * Assembles the request query for reserved amount.
	 */
	private String assembleRequestStockLevelQuery()
	{
		prepareStockLevelColumns();
		final StringBuilder query = new StringBuilder("SELECT ").append(stockLevelColumns.reservedCol);
		query.append(" FROM ").append(stockLevelColumns.tableName).append(" WHERE ").append(stockLevelColumns.pkCol).append("=?");
		return query.toString();
	}

	/**
	 * Assembles the request query for update the current available and reset the reserved to zero.
	 */
	private String assembleUpdateStockLevelQuery()
	{
		prepareStockLevelColumns();
		final StringBuilder query = new StringBuilder("UPDATE ").append(stockLevelColumns.tableName);
		query.append(" SET ").append(stockLevelColumns.reservedCol).append(" = 0, ").append(stockLevelColumns.availableCol)
				.append(" =?");
		query.append(" WHERE ").append(stockLevelColumns.pkCol).append("=?");
		return query.toString();
	}

	/**
	 * Assembles the request query for release amount.
	 */
	private String assembleReleaseStockLevelQuery()
	{
		prepareStockLevelColumns();
		final StringBuilder query = new StringBuilder("UPDATE ").append(stockLevelColumns.tableName);
		query.append(" SET ").append(stockLevelColumns.reservedCol).append(" = ").append(stockLevelColumns.reservedCol)
				.append(" - ? ");
		query.append(" WHERE ").append(stockLevelColumns.pkCol).append("=?");
		return query.toString();
	}

	/**
	 * Assembles the UPDATE reservation query.
	 */
	private String assembleReserveStockLevelQuery(final InStockStatus inStockStatus)
	{
		prepareStockLevelColumns();
		final StringBuilder query = new StringBuilder("UPDATE ").append(stockLevelColumns.tableName);
		query.append(" SET ").append(stockLevelColumns.reservedCol).append(" = ").append(stockLevelColumns.reservedCol)
				.append(" + ? ");
		query.append(" WHERE ").append(stockLevelColumns.pkCol).append("=?");

		if (!InStockStatus.FORCEINSTOCK.equals(inStockStatus))
		{
			query.append(" AND ").append(stockLevelColumns.availableCol).append(" + ").append(stockLevelColumns.oversellingCol)
					.append(" - ").append(stockLevelColumns.reservedCol).append(" >= ? ");
		}

		return query.toString();
	}

	private void prepareStockLevelColumns()
	{
		if (this.stockLevelColumns == null)
		{
			this.stockLevelColumns = new StockLevelColumns(typeService);
		}
	}

	private void checkProductCode(final String productCode)
	{
		if (productCode == null)
		{
			throw new IllegalArgumentException("product code cannot be null.");
		}
	}

	private void checkWarehouse(final WarehouseModel warehouse)
	{
		if (warehouse == null)
		{
			throw new IllegalArgumentException("warehouse cannot be null.");
		}
	}

	/**
	 * Filters warehouse list to remove null's and duplicate elements.
	 */
	private List<WarehouseModel> filterWarehouses(final Collection<WarehouseModel> warehouses)
	{
		if (warehouses == null)
		{
			throw new IllegalArgumentException("warehouses cannot be null.");
		}
		final Set<WarehouseModel> result = new HashSet<>();
		for (final WarehouseModel house : warehouses)
		{
			if (house != null)
			{
				result.add(house);
			}
		}
		return new ArrayList<>(result);
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}

	@Required
	public void setTransactionTemplate(final TransactionTemplate transactionTemplate)
	{
		this.transactionTemplate = transactionTemplate;
	}

	@Required
	public void setJdbcTemplate(final JdbcTemplate jdbcTemplate)
	{
		this.jdbcTemplate = jdbcTemplate;
	}

}
