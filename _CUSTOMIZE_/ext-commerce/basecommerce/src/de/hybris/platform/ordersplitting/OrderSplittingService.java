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
package de.hybris.platform.ordersplitting;

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;

import java.util.List;


/**
 * The OrderSplittingService provides methods for splitting order entries for consignments.
 */
public interface OrderSplittingService
{

	/**
	 * Split order entries for consignments.
	 *
	 * @param order
	 * 		order to split
	 * @param orderEntryList
	 * 		the order entry list
	 * @return the list of consignments
	 * @throws ConsignmentCreationException
	 * 		if the consignment was not created correctly
	 */
	List<ConsignmentModel> splitOrderForConsignment(final AbstractOrderModel order, List<AbstractOrderEntryModel> orderEntryList)
			throws ConsignmentCreationException;

	/**
	 * Split order entries for consignments w/o persisting changes
	 *
	 * @param order
	 * 		order to split
	 * @param orderEntryList
	 * 		list of order entries
	 * @return list of consignments
	 * @throws ConsignmentCreationException
	 * 		if the consignment was not created correctly
	 */
	List<ConsignmentModel> splitOrderForConsignmentNotPersist(final AbstractOrderModel order,
			List<AbstractOrderEntryModel> orderEntryList) throws ConsignmentCreationException;
}
