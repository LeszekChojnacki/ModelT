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
package de.hybris.platform.ordersplitting.daos.impl;

import de.hybris.platform.ordersplitting.daos.WarehouseDao;
import de.hybris.platform.ordersplitting.jalo.StockLevel;
import de.hybris.platform.ordersplitting.jalo.Warehouse;
import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.ordersplitting.model.VendorModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 */
public class DefaultWarehouseDao extends DefaultGenericDao<WarehouseModel> implements WarehouseDao
{
	public DefaultWarehouseDao()
	{
		super(WarehouseModel._TYPECODE);
	}

	@Override
	public List<WarehouseModel> getWarehouseForCode(final String code)
	{
		return find(Collections.singletonMap(WarehouseModel.CODE, code));
	}

	@Override
	public List<WarehouseModel> getDefWarehouse()
	{
		return find(Collections.singletonMap(WarehouseModel.DEFAULT, Boolean.TRUE));
	}


	@Override
	public List<WarehouseModel> getWarehouses(final String productCode)
	{
		final StringBuilder query = new StringBuilder("select distinct {w:").append(Warehouse.PK).append('}');
		query.append("   from {").append(StockLevelModel._TYPECODE).append(" as s}, ");
		query.append("        {").append(WarehouseModel._TYPECODE).append(" as w} ");
		query.append("   where {s:").append(StockLevel.WAREHOUSE).append("} = {w:").append(Warehouse.PK).append('}');
		query.append("     and {s:").append(StockLevelModel.PRODUCTCODE).append("} = (?productCode)");

		final SearchResult<WarehouseModel> queryResult = getFlexibleSearchService().search(query.toString(),
				Collections.singletonMap("productCode", productCode));

		return queryResult.getResult();
	}

	@Override
	public List<WarehouseModel> getWarehousesWithProductsInStock(final String productCode, final long quantity,
			final VendorModel vendor)
	{
		final Map<String, Object> params = new HashMap<>();
		final StringBuilder query = new StringBuilder("select distinct {w:").append(Warehouse.PK).append('}');
		query.append("   from {").append(StockLevelModel._TYPECODE).append(" as s}, ");
		query.append("        {").append(WarehouseModel._TYPECODE).append(" as w} ");
		query.append("   where {s:").append(StockLevel.WAREHOUSE).append("} = {w:").append(Warehouse.PK).append('}');
		query.append("     and {s:").append(StockLevel.AVAILABLE).append("} >= ?quantity");
		query.append("     and {s:").append(StockLevelModel.PRODUCTCODE).append('}').append(" = ?code");

		if (vendor != null && vendor.getPk() != null)
		{
			query.append("     and {w:").append(Warehouse.VENDOR).append("} = ?vendor");
			params.put("vendor", vendor.getPk());
		}
		params.put("quantity", Long.valueOf(quantity));
		params.put("code", productCode);

		final SearchResult<WarehouseModel> result = getFlexibleSearchService().search(query.toString(), params);

		return result.getResult();
	}

}
