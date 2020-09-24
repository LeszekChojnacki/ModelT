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
package de.hybris.platform.warehousing.inventoryevent.dao;

import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.ordersplitting.model.ConsignmentEntryModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.warehousing.data.allocation.DeclineEntry;
import de.hybris.platform.warehousing.model.AllocationEventModel;
import de.hybris.platform.warehousing.model.InventoryEventModel;

import java.util.Collection;


/**
 * The inventory events Dao
 */
public interface InventoryEventDao
{

	/**
	 * Retrieves a list of {@link AllocationEventModel} related to a {@link ConsignmentEntryModel}
	 *
	 * @param consignmentEntry
	 * 		the {@link ConsignmentEntryModel} for which to retrieve the event
	 * @return the list of {@link AllocationEventModel} linked to the entry.
	 */
	Collection<AllocationEventModel> getAllocationEventsForConsignmentEntry(ConsignmentEntryModel consignmentEntry);

	/**
	 * Retrieves a collection of {@link AllocationEventModel} for an {@link OrderEntryModel}
	 *
	 * @param orderEntry
	 * 		the order entry for which to retrieve the events
	 * @return the collection of allocation events for the order entry
	 */
	Collection<AllocationEventModel> getAllocationEventsForOrderEntry(OrderEntryModel orderEntry);

	/**
	 * Retrieves a collection of {@link de.hybris.platform.warehousing.model.InventoryEventModel} for the {@link StockLevelModel}
	 *
	 * @param stockLevel
	 * 		the {@link StockLevelModel} for which the events need to be extracted
	 * @param eventClassType
	 * 		the class representing the type of inventory event
	 * @return the collection of inventory events for the given {@link StockLevelModel}
	 */
	<T extends InventoryEventModel> Collection<T> getInventoryEventsForStockLevel(StockLevelModel stockLevel,
			Class<T> eventClassType);

	/**
	 * Retrieves a collection of {@link AllocationEventModel} for the given {@link ConsignmentModel}
	 *
	 * @param consignment
	 * 		the {@link ConsignmentModel} for which all allocation events will be returned
	 * @return the collection of {@link AllocationEventModel}
	 */
	Collection<AllocationEventModel> getAllocationEventsForConsignment(ConsignmentModel consignment);

}
