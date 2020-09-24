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
package de.hybris.platform.warehousing.cancellation;

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.ordercancel.OrderCancelResponse;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;

import java.util.Map;


/**
 * Service to cancel items from consignments.
 */
public interface ConsignmentCancellationService
{
	/**
	 * Cancel consignments linked to an {@link OrderCancelResponse}. It creates cancellation events accordingly and will
	 * trigger another sourcing if necessary
	 * It cancels all the consignments for the given product
	 *
	 * @param orderCancelResponse
	 *           the order cancel response
	 */
	void processConsignmentCancellation(OrderCancelResponse orderCancelResponse);

	/**
	 * Cancel a consignment. This will cancel the entire consignment and not only the quantity requested for
	 * cancellation. If there is a leftover, then another sourcing evaluation will be performed in order to define the
	 * best location for the update quantities ordered.
	 *
	 * @param consignment
	 *           the consignment to cancel
	 * @param orderCancelResponse
	 *           the order cancel response
	 *@return Map<AbstractOrderEntryModel, Long>
	 */
	Map<AbstractOrderEntryModel, Long> cancelConsignment(final ConsignmentModel consignment,
			final OrderCancelResponse orderCancelResponse);
}
