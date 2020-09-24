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
package de.hybris.platform.returns;

import de.hybris.platform.returns.model.ReturnRequestModel;


/**
 * Adapter to request any action concerning a specific return request. Could be approving, cancelling or receiving
 * action.
 */
public interface ReturnActionAdapter
{
	/**
	 * Requests approval operation on a ReturnRequest.
	 */
	void requestReturnApproval(ReturnRequestModel returnRequest);

	/**
	 * Claims reception operation on a ReturnRequest.
	 */
	void requestReturnReception(ReturnRequestModel returnRequest);

	/**
	 * Requests cancellation operation on a ReturnRequest.
	 */
	void requestReturnCancellation(ReturnRequestModel returnRequest);

	/**
	 * Requests manual payment reversal operation on a ReturnRequest.
	 */
	void requestManualPaymentReversalForReturnRequest(ReturnRequestModel returnRequest);

	/**
	 * Requests manual tax reversal operation on a ReturnRequest.
	 */
	void requestManualTaxReversalForReturnRequest(ReturnRequestModel returnRequest);

}
