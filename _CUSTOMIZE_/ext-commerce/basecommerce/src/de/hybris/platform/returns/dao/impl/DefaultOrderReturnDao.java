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
package de.hybris.platform.returns.dao.impl;

import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.ordercancel.exceptions.OrderCancelDaoException;
import de.hybris.platform.ordercancel.model.OrderEntryCancelRecordEntryModel;
import de.hybris.platform.returns.dao.OrderReturnDao;
import de.hybris.platform.returns.model.OrderEntryReturnRecordEntryModel;
import de.hybris.platform.returns.model.OrderReturnRecordEntryModel;
import de.hybris.platform.returns.model.OrderReturnRecordModel;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.Collection;
import java.util.Collections;


/**
 *
 */
public class DefaultOrderReturnDao extends AbstractItemDao implements OrderReturnDao
{
	@Override
	public OrderEntryReturnRecordEntryModel getOrderEntryReturnRecord(final OrderEntryModel orderEntry,
			final OrderReturnRecordEntryModel returnEntry)
	{
		final FlexibleSearchQuery query = new FlexibleSearchQuery(//
				"SELECT {PK} FROM {" + OrderReturnRecordEntryModel._TYPECODE + " as oe} " //
						+ "WHERE {oe." + OrderEntryReturnRecordEntryModel.MODIFICATIONRECORDENTRY + "}=?record " //
						+ "AND {oe." + OrderEntryCancelRecordEntryModel.ORDERENTRY + "}=?orderEntry");
		query.addQueryParameter("record", returnEntry.getPk());
		query.addQueryParameter("orderEntry", orderEntry.getPk());

		final SearchResult<OrderEntryReturnRecordEntryModel> result = search(query);
		return result.getResult().isEmpty() ? null : result.getResult().get(0);
	}

	@Override
	public OrderReturnRecordModel getOrderReturnRecord(final OrderModel order)
	{
		if (order == null)
		{
			throw new IllegalArgumentException("Order cannot be null");
		}
		final FlexibleSearchQuery query = new FlexibleSearchQuery(//
				"SELECT {PK} " //
						+ "FROM {" + OrderReturnRecordModel._TYPECODE + "} " //
						+ "WHERE {" + OrderReturnRecordModel.ORDER + "}=?order ");
		query.addQueryParameter("order", order.getPk());
		final SearchResult<OrderReturnRecordModel> result = search(query);
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
			throw new OrderCancelDaoException(order.getCode(), "Only one return record allowed");
		}
	}

	@Override
	public Collection<OrderReturnRecordEntryModel> getOrderReturnRecordEntries(final OrderModel order)
	{
		if (order == null)
		{
			throw new IllegalArgumentException("Order cannot be null");
		}
		final FlexibleSearchQuery query = new FlexibleSearchQuery(//
				"SELECT {entries.PK} " //
						+ "FROM {" + OrderReturnRecordModel._TYPECODE + " as record JOIN " + OrderReturnRecordEntryModel._TYPECODE
						+ " as entries ON {record.PK} = {entries." + OrderReturnRecordEntryModel.MODIFICATIONRECORD + "}} " //
						+ "WHERE {record." + OrderReturnRecordModel.ORDER + "}=?order ");
		query.addQueryParameter("order", order.getPk());
		final SearchResult<OrderReturnRecordEntryModel> result = search(query);
		return result.getResult().isEmpty() ? Collections.emptyList() : result.getResult();
	}
}
