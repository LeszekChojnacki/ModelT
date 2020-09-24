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
package de.hybris.platform.returns.dao;

import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.returns.model.OrderEntryReturnRecordEntryModel;
import de.hybris.platform.returns.model.OrderReturnRecordEntryModel;
import de.hybris.platform.returns.model.OrderReturnRecordModel;
import de.hybris.platform.servicelayer.internal.dao.Dao;

import java.util.Collection;


/**
 * Dao object used in order return service
 */
public interface OrderReturnDao extends Dao
{
	/**
	 * Returns Order Return Registration Item. This Item holds relations to a particular return occurrences.
	 * 
	 * @param order
	 * @return OrderReturnRecordModel
	 */
	OrderReturnRecordModel getOrderReturnRecord(OrderModel order);

	/**
	 * Get order return record entries - each one refer to one order return request.
	 * 
	 * @param order
	 * @return collection of {@link OrderReturnRecordEntryModel}
	 */
	Collection<OrderReturnRecordEntryModel> getOrderReturnRecordEntries(OrderModel order);

	/**
	 * Returns particular order entry cancel record
	 * 
	 * @param orderEntry
	 *           - orderEntry
	 * @param returnEntry
	 *           - the whole Order return record
	 * @return OrderEntryReturnRecordEntryModel
	 */
	OrderEntryReturnRecordEntryModel getOrderEntryReturnRecord(OrderEntryModel orderEntry, OrderReturnRecordEntryModel returnEntry);
}
