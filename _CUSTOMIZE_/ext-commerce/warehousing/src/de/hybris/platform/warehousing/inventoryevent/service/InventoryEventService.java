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
package de.hybris.platform.warehousing.inventoryevent.service;

import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.ordersplitting.model.ConsignmentEntryModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.warehousing.data.allocation.DeclineEntry;
import de.hybris.platform.warehousing.model.AllocationEventModel;
import de.hybris.platform.warehousing.model.CancellationEventModel;
import de.hybris.platform.warehousing.model.IncreaseEventModel;
import de.hybris.platform.warehousing.model.InventoryEventModel;
import de.hybris.platform.warehousing.model.ShrinkageEventModel;
import de.hybris.platform.warehousing.model.WastageEventModel;

import java.util.Collection;
import java.util.Map;


/**
 * The service related to the inventory events. The inventory events are used for ATP calculation and included
 * allocation and cancellation events.
 */
public interface InventoryEventService
{
	/**
	 * Persists the {@link AllocationEventModel} to account for the changes in stock quantities.
	 *
	 * @param consignment
	 * 		the {@link ConsignmentModel} where stock was allocated; never <tt>null</tt>
	 * @return a collection of {@link AllocationEventModel}; never <tt>null</tt>
	 */
	Collection<AllocationEventModel> createAllocationEvents(ConsignmentModel consignment);

	/**
	 * Create multiple {@link AllocationEventModel} that are required to fulfill the {@link ConsignmentEntryModel}
	 *
	 * @param consignmentEntry
	 * 		the {@link ConsignmentEntryModel}
	 * @return a list of persisted {@link AllocationEventModel}
	 */
	Collection<AllocationEventModel> createAllocationEventsForConsignmentEntry(ConsignmentEntryModel consignmentEntry);

	/**
	 * Create a shrinkage event.
	 *
	 * @param shrinkageEventModel
	 * 		the event containing information about the shrinkage
	 * @return the shrinkage event; never <tt>null</tt>
	 */
	ShrinkageEventModel createShrinkageEvent(ShrinkageEventModel shrinkageEventModel);

	/**
	 * Create a wastage event.
	 *
	 * @param wastageEventModel
	 * 		the event containing information about the wastage
	 * @return the wastage event; never <tt>null</tt>
	 */
	WastageEventModel createWastageEvent(WastageEventModel wastageEventModel);

	/**
	 * Create multiple {@link CancellationEventModel}
	 *
	 * @param cancellationEventModel
	 * 		the event containing information about the cancelled consignment entry
	 * @return the collection of persisted {@link CancellationEventModel}
	 */
	Collection<CancellationEventModel> createCancellationEvents(CancellationEventModel cancellationEventModel);

	/**
	 * Retrieves a collection of {@link AllocationEventModel} for a {@link ConsignmentEntryModel}
	 *
	 * @param consignmentEntry
	 * 		the {@link ConsignmentEntryModel} for which to retrieve the event
	 * @return the collection of {@link AllocationEventModel} associated to the consignment entry
	 */
	Collection<AllocationEventModel> getAllocationEventsForConsignmentEntry(ConsignmentEntryModel consignmentEntry);

	/**
	 * Retrieves a collection of {@link AllocationEventModel} for an {@link OrderEntryModel}
	 *
	 * @param orderEntry
	 * 		the {@link OrderEntryModel} for which to retrieve the events
	 * @return the collection of {@link AllocationEventModel} for the order entry
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
	 * Create an inventory increase event
	 *
	 * @param increaseEventModel
	 * 		the {@link IncreaseEventModel} containing information about the increase event model
	 * @return the increase event model
	 */
	IncreaseEventModel createIncreaseEvent(IncreaseEventModel increaseEventModel);

	/**
	 * Delete or update the allocation event which has been created for a specific entry to decline
	 *
	 * @param declineEntry
	 * 		the {@link DeclineEntry} to decline/reallocate
	 * @param quantityToDecline
	 * 		the quantity to decline/reallocate
	 */
	void reallocateAllocationEvent(DeclineEntry declineEntry, Long quantityToDecline);

	/**
	 * Extracts the {@link StockLevelModel} of {@link AllocationEventModel} and sorts them by quantity.
	 * Tries to map quantity to a stock level for adjustement
	 *
	 * @param allocationEvents
	 * 		collection of allocation events
	 * @param quantityToDecline
	 * 		total quantity to be adjusted
	 * @return a map of {@link AllocationEventModel} with the quantity to adjust for this stock level
	 */
	Map<AllocationEventModel, Long> getAllocationEventsForReallocation(Collection<AllocationEventModel> allocationEvents,
			Long quantityToDecline);

	/**
	 * Retrieves a collection of {@link AllocationEventModel} for the given {@link ConsignmentModel}
	 *
	 * @param consignment
	 * 		the {@link ConsignmentModel} for which all allocation events will be returned
	 * @return the collection of {@link AllocationEventModel}
	 */
	Collection<AllocationEventModel> getAllocationEventsForConsignment(ConsignmentModel consignment);
}
