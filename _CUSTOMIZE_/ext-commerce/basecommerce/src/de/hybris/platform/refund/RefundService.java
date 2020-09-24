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
package de.hybris.platform.refund;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.returns.OrderReturnRecordsHandlerException;
import de.hybris.platform.returns.model.RefundEntryModel;
import de.hybris.platform.returns.model.ReturnRequestModel;

import java.util.List;


/**
 * Service for handling Refund's
 */
public interface RefundService
{
	/**
	 * Create a "refund order" , which will be a clone of the original one. The new instance should be used for applying
	 * the refund. (see: RefundService#calculate)
	 *
	 * @param original
	 * 		the original order
	 * @return the refund order
	 */
	OrderModel createRefundOrderPreview(OrderModel original);

	/**
	 * Based on the assigned refund entries the order will be recalculated.
	 *
	 * @param refunds
	 * 		the refunds for which the amount will be calculated
	 * @param order
	 * 		the refund order for which the refund will be calculated
	 */
	void apply(final List<RefundEntryModel> refunds, final OrderModel order);

	/**
	 * Based on the assigned refund entries of the 'previewOrder'
	 * {@link RefundService#createRefundOrderPreview(OrderModel)} , the 'finalOrder' will be recalculated and
	 * modification entries will be written.
	 *
	 * @param previewOrder
	 * 		the order which holds the "refund entries"
	 * @param request
	 * 		the request which holds the order for which the refunds will be calculated.
	 * @throws OrderReturnRecordsHandlerException
	 * 		in the case of any service error
	 */
	public void apply(final OrderModel previewOrder, final ReturnRequestModel request) throws OrderReturnRecordsHandlerException;

	/**
	 * Returns the refunds for the specified order
	 *
	 * @param request
	 * 		the request
	 * @return the refunds
	 */
	List<RefundEntryModel> getRefunds(final ReturnRequestModel request);

}
