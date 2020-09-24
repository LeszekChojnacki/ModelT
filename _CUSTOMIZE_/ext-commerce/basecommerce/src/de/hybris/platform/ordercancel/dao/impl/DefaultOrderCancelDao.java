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
package de.hybris.platform.ordercancel.dao.impl;

import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.ordercancel.dao.OrderCancelDao;
import de.hybris.platform.ordercancel.exceptions.AmbiguousOrderCancelConfigurationException;
import de.hybris.platform.ordercancel.exceptions.OrderCancelDaoException;
import de.hybris.platform.ordercancel.model.OrderCancelConfigModel;
import de.hybris.platform.ordercancel.model.OrderCancelRecordEntryModel;
import de.hybris.platform.ordercancel.model.OrderCancelRecordModel;
import de.hybris.platform.ordercancel.model.OrderEntryCancelRecordEntryModel;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.Collection;
import java.util.Collections;


/**
 *
 */
public class DefaultOrderCancelDao extends AbstractItemDao implements OrderCancelDao
{
	private static final String ONE_CONFIG_ALLOWED = "Only one Order Cancel Configuration is Allowed";

	@Override
	public OrderCancelRecordModel getOrderCancelRecord(final OrderModel order)
	{
		if (order == null)
		{
			throw new IllegalArgumentException("Order cannot be null");
		}
		final FlexibleSearchQuery query = new FlexibleSearchQuery(//
				"SELECT {PK} " //
						+ "FROM {" + OrderCancelRecordModel._TYPECODE + "} " //
						+ "WHERE {" + OrderCancelRecordModel.ORDER + "}=?order ");
		query.addQueryParameter("order", order.getPk());
		final SearchResult<OrderCancelRecordModel> result = search(query);
		if (result.getResult().isEmpty())
		{
			return null;
		}
		else if (result.getResult().size() == 1)
		{
			return result.getResult().get(0);
		}
		else
		{
			throw new OrderCancelDaoException(order.getCode(), "Only one cancel record allowed");
		}
	}

	@Override
	public Collection<OrderCancelRecordEntryModel> getOrderCancelRecordEntries(final OrderModel order)
	{
		if (order == null)
		{
			throw new IllegalArgumentException("Order cannot be null");
		}
		final FlexibleSearchQuery query = new FlexibleSearchQuery(//
				"SELECT {entries.PK} " //
						+ "FROM {" + OrderCancelRecordModel._TYPECODE + " as record JOIN " + OrderCancelRecordEntryModel._TYPECODE
						+ " as entries ON {record.PK} = {entries." + OrderCancelRecordEntryModel.MODIFICATIONRECORD + "}} " //
						+ "WHERE {record." + OrderCancelRecordModel.ORDER + "}=?order ");
		query.addQueryParameter("order", order.getPk());
		final SearchResult<OrderCancelRecordEntryModel> result = search(query);
		return result.getResult().isEmpty() ? Collections.emptyList() : result.getResult();
	}


	@Override
	public Collection<OrderCancelRecordEntryModel> getOrderCancelRecordEntries(final EmployeeModel employee)
	{
		return null; // NOSONAR
	}

	@Override
	public OrderCancelConfigModel getOrderCancelConfiguration()
	{
		final FlexibleSearchQuery query = new FlexibleSearchQuery(//
				"SELECT {PK} FROM {" + OrderCancelConfigModel._TYPECODE + "}");
		final SearchResult<OrderCancelConfigModel> result = search(query);
		if (result.getTotalCount() > 1)
		{
			throw new AmbiguousOrderCancelConfigurationException(ONE_CONFIG_ALLOWED);
		}
		return result.getTotalCount() == 0 ? null : result.getResult().get(0);
	}


	@Override
	public OrderEntryCancelRecordEntryModel getOrderEntryCancelRecord(final OrderEntryModel orderEntry,
			final OrderCancelRecordEntryModel cancelEntry)
	{
		final FlexibleSearchQuery query = new FlexibleSearchQuery(//
				"SELECT {PK} FROM {" + OrderEntryCancelRecordEntryModel._TYPECODE + " as oe} " //
						+ "WHERE {oe." + OrderEntryCancelRecordEntryModel.MODIFICATIONRECORDENTRY + "}=?record " //
						+ "AND {oe." + OrderEntryCancelRecordEntryModel.ORDERENTRY + "}=?orderEntry");
		query.addQueryParameter("record", cancelEntry.getPk());
		query.addQueryParameter("orderEntry", orderEntry.getPk());
		final SearchResult<OrderEntryCancelRecordEntryModel> result = search(query);
		return result.getResult().isEmpty() ? null : result.getResult().get(0);
	}

}
