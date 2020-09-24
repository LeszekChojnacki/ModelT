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
package de.hybris.platform.ordercancel.dao;

import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.ordercancel.exceptions.OrderCancelDaoException;
import de.hybris.platform.ordercancel.model.OrderCancelConfigModel;
import de.hybris.platform.ordercancel.model.OrderCancelRecordEntryModel;
import de.hybris.platform.ordercancel.model.OrderCancelRecordModel;
import de.hybris.platform.ordercancel.model.OrderEntryCancelRecordEntryModel;
import de.hybris.platform.servicelayer.internal.dao.Dao;

import java.util.Collection;


/**
 * Dao object used in order cancellation service
 */
public interface OrderCancelDao extends Dao
{
	/**
	 * Returns Order Cancel Registration Item. This Item holds relations to a particular cancellation occurrences.
	 *
	 * @param order
	 * @return OrderCancelRecordModel
	 * @throws OrderCancelDaoException
	 */
	OrderCancelRecordModel getOrderCancelRecord(OrderModel order);

	/**
	 * Return order cancel record entries - each one refer to one order cancel request.
	 *
	 * @param order
	 * @return collection of {@link OrderCancelRecordEntryModel}
	 * @throws OrderCancelDaoException
	 */
	Collection<OrderCancelRecordEntryModel> getOrderCancelRecordEntries(OrderModel order);

	/**
	 * Get Order Cancel records issued by the given employee
	 *
	 * @param employee
	 * @return collection of {@link OrderCancelRecordEntryModel}
	 * @throws OrderCancelDaoException
	 */
	Collection<OrderCancelRecordEntryModel> getOrderCancelRecordEntries(EmployeeModel employee);

	/**
	 * Returns OrderCancelConfiguration item
	 *
	 * @return {@link OrderCancelConfigModel}
	 * @throws OrderCancelDaoException
	 */
	OrderCancelConfigModel getOrderCancelConfiguration();

	/**
	 * Returns particular order entry cancel record
	 *
	 * @param orderEntry
	 *           - orderEntry
	 * @param cancelEntry
	 *           - the whole Order cancel record
	 * @return OrderEntryCancelRecordEntryModel
	 */
	OrderEntryCancelRecordEntryModel getOrderEntryCancelRecord(OrderEntryModel orderEntry,
			OrderCancelRecordEntryModel cancelEntry);
}
