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
package de.hybris.platform.orderhistory;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.orderhistory.model.OrderHistoryEntryModel;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.Collection;
import java.util.Date;


/**
 * This interface is provides methods for order history versioning and the creation of order snapshots.
 */
public interface OrderHistoryService
{
	/**
	 * Creates a exact copy of the given order except that the copy will be marked as copy by setting
	 * {@link OrderModel#VERSIONID} to <code>true</code>.
	 *
	 * @param currentVersion
	 * 		the original order to be copied copy
	 * @return a newly created order model (which is not persisted yet!)
	 */
	OrderModel createHistorySnapshot(OrderModel currentVersion);

	/**
	 * Actually persists a previously created order snapshot.
	 * Please note that it is necessary to use this method instead just calling {@link ModelService#save(Object)} to
	 * overcome Jalo layer business logic resetting some attributes. In future this may not be necessary.
	 *
	 * @param snapshot
	 * 		the order snapshot
	 * @throws IllegalArgumentException
	 * 		if the order is already persisted or is no snapshot at all
	 */
	void saveHistorySnapshot(OrderModel snapshot);

	/**
	 * Fetches all snapshots which have been created and assigned to the given order's history entries.
	 *
	 * @param currentVersion
	 * 		the original order
	 * @return all history snapshots
	 */
	Collection<OrderModel> getHistorySnapshots(OrderModel currentVersion);

	// YTODO how to fetch single entries - do we need some kind of unique key ?

	/**
	 * Fetches history entries of the ownerOrder from the given time range
	 *
	 * @param ownerOrder
	 * 		the owner order
	 * @param dateFrom
	 * 		upper limit of the time range
	 * @param dateTo
	 * 		lower limit of the time range
	 * @return Collection of history entries of the given <b>ownerOrder</b> that were captured in the given time range
	 */
	Collection<OrderHistoryEntryModel> getHistoryEntries(OrderModel ownerOrder, Date dateFrom, Date dateTo);

	/**
	 * Fetches the descriptions of history entries of the ownerOrder from the given time range
	 *
	 * @param ownerOrder
	 * 		the owner order
	 * @param dateFrom
	 * 		upper limit of the time range
	 * @param dateTo
	 * 		lower limit of the time range
	 * @return Collection of history entries of the given <b>ownerOrder</b> that were captured in the given time range
	 */
	Collection<String> getHistoryEntriesDescriptions(OrderModel ownerOrder, Date dateFrom, Date dateTo);

	/**
	 * Fetches history entries of the ownerOrder from the given employee
	 *
	 * @param ownerOrder
	 * 		the owner order
	 * @param employee
	 * 		instance of {@link EmployeeModel} to get the history of orders for
	 * @return collection of {@link OrderHistoryEntryModel}
	 */
	Collection<OrderHistoryEntryModel> getHistoryEntries(OrderModel ownerOrder, EmployeeModel employee);

	/**
	 * Fetches history entries of the orders placed by the <b>user</b> in the given time range
	 *
	 * @param user
	 * 		user that placed the orders
	 * @param dateFrom
	 * 		set <b>dateFrom</b> to null if you want to make the time range only half limited
	 * @param dateTo
	 * 		set <b>dateTo</b> to null if you want to make the time range only half limited
	 * @return collection of {@link OrderHistoryEntryModel}
	 */
	Collection<OrderHistoryEntryModel> getHistoryEntries(UserModel user, Date dateFrom, Date dateTo);


}
