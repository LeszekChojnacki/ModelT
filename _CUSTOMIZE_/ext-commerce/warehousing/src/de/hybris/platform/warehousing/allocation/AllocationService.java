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
package de.hybris.platform.warehousing.allocation;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.ordersplitting.model.ConsignmentEntryModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.warehousing.data.allocation.DeclineEntries;
import de.hybris.platform.warehousing.data.sourcing.SourcingResult;
import de.hybris.platform.warehousing.data.sourcing.SourcingResults;
import de.hybris.platform.warehousing.model.DeclineConsignmentEntryEventModel;

import java.util.Collection;


/**
 * Service layer to create consignments.
 */
public interface AllocationService
{
	/**
	 * Create consignments for the sourcing results gathered from the sourcing strategies executions.<br/>
	 * All consignments and order will be persisted after creations and updates.
	 *
	 * @param order
	 * 		- the order to create consignments for
	 * @param code
	 * 		- the consignment code; never <tt>null</tt>
	 * @param results
	 * 		- the sourcing results collected from the sourcing strategies; never <tt>null</tt>
	 * @return a collection of {@link ConsignmentModel}; never <tt>null</tt>
	 */
	Collection<ConsignmentModel> createConsignments(AbstractOrderModel order, String code, SourcingResults results);

	/**
	 * Create a consignment for the sourcing result.<br/>
	 * The consignment and the order will be persisted after creation and updates.
	 *
	 * @param order
	 * 		- the order to create consignments for
	 * @param code
	 * 		- the consignment code; never <tt>null</tt>
	 * @param result
	 * 		- the sourcing results collected from the sourcing strategies; never <tt>null</tt>
	 * @return a {@link ConsignmentModel}; never <tt>null</tt>
	 */
	ConsignmentModel createConsignment(AbstractOrderModel order, String code, SourcingResult result);

	/**
	 * Manually reallocate the consignment entries provided to a new point of service. Creates new consignments for the
	 * items that were reallocated. This will also create {@link DeclineConsignmentEntryEventModel} items to represent
	 * the reallocations that can be retrieved by {@link ConsignmentEntryModel#getDeclineEntryEvents()}.</br></br>
	 * <p>
	 * Furthermore, the service can only reallocate an item if it is pending as in
	 * {@link ConsignmentEntryModel#getQuantityPending()}.
	 *
	 * @param declinedEntries
	 * 		- the collection of decline entries to be reallocated
	 * @return the collection of newly created consignments
	 */
	Collection<ConsignmentModel> manualReallocate(DeclineEntries declinedEntries);

	/**
	 * Creates {@link DeclineConsignmentEntryEventModel} items to represent
	 * the reallocations that can be retrieved by {@link ConsignmentEntryModel#getDeclineEntryEvents()}.</br></br>
	 * <p>
	 * Furthermore, the service can only reallocate an item if it is pending as in
	 * {@link ConsignmentEntryModel#getQuantityPending()}.
	 *
	 * @param declinedEntries
	 * 		- the collection of decline entries to be reallocated
	 */
	void autoReallocate(DeclineEntries declinedEntries);

}
