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
package de.hybris.platform.ordersplitting.impl;

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.ordersplitting.WarehouseService;
import de.hybris.platform.ordersplitting.daos.WarehouseDao;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.servicelayer.util.ServicesUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Required;


/**
 *
 * Default Implementation of {@link WarehouseService}
 */
public class DefaultWarehouseService implements WarehouseService
{
	private WarehouseDao warehouseDao;

	@Override
	public List<WarehouseModel> getWarehouses(final Collection<? extends AbstractOrderEntryModel> orderEntries)
	{
		ServicesUtil.validateParameterNotNullStandardMessage("orderEntries", orderEntries);
		List<WarehouseModel> result = null;
		for (final AbstractOrderEntryModel entry : orderEntries)
		{
			if (result == null)
			{
				result = new ArrayList<WarehouseModel>(warehouseDao.getWarehouses(entry.getProduct().getCode()));
			}
			else
			{
				result.retainAll(warehouseDao.getWarehouses(entry.getProduct().getCode()));
			}
			if (result.isEmpty())
			{
				return getDefWarehouse();
			}
		}

		return result;
	}

	@Override
	public List<WarehouseModel> getWarehousesWithProductsInStock(final AbstractOrderEntryModel orderEntry)
	{
		ServicesUtil.validateParameterNotNullStandardMessage("orderEntry", orderEntry);
		final List<WarehouseModel> result = warehouseDao.getWarehousesWithProductsInStock(orderEntry.getProduct().getCode(),
				orderEntry.getQuantity().longValue(), orderEntry.getChosenVendor());

		if (result.isEmpty())
		{
			return getDefWarehouse();
		}
		return result;
	}

	@Override
	public List<WarehouseModel> getDefWarehouse()
	{
		return warehouseDao.getDefWarehouse();
	}


	@Override
	public WarehouseModel getWarehouseForCode(final String code)
	{
		ServicesUtil.validateParameterNotNullStandardMessage("code", code);

		final List<WarehouseModel> res = warehouseDao.getWarehouseForCode(code);

		ServicesUtil.validateIfSingleResult(res, WarehouseModel.class, "code", code);

		return res.get(0);
	}

	@Required
	public void setWarehouseDao(final WarehouseDao warehouseDao)
	{
		this.warehouseDao = warehouseDao;
	}
}
