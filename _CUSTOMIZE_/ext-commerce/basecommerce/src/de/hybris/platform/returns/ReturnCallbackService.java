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


/**
 * This interface is used by adapter to provide feedback information about execution of actions on a return request.
 */
public interface ReturnCallbackService
{
	/**
	 * Callback method used by the adapter to pass approval operation result. Adapter uses this method to provide
	 * feedback information how was the ReturnRequest approved (completely, partially, not at all).
	 *
	 * @param approvalResponse
	 * 		- instance of  {@link ReturnActionResponse}
	 * @throws OrderReturnException
	 * 		in case of error
	 */
	void onReturnApprovalResponse(ReturnActionResponse approvalResponse) throws OrderReturnException;

	/**
	 * Callback method used by the adapter to pass cancellation operation result. Adapter uses this method to provide
	 * feedback information how was the ReturnRequest cancelled (completely, partially, not at all).
	 *
	 * @param cancelResponse
	 * 		- instance of {@link ReturnActionResponse}
	 * @throws OrderReturnException
	 * 		in case of error
	 */
	void onReturnCancelResponse(ReturnActionResponse cancelResponse) throws OrderReturnException;

	/**
	 * Callback method used by the adapter to pass reception operation result. Adapter uses this method to provide
	 * feedback information how was the ReturnRequest received (completely, partially, not at all).
	 *
	 * @param receptionResponse
	 * 		- instance of {@link ReturnActionResponse}
	 * @throws OrderReturnException
	 * 		in case of error
	 */
	void onReturnReceptionResponse(ReturnActionResponse receptionResponse) throws OrderReturnException;
}
