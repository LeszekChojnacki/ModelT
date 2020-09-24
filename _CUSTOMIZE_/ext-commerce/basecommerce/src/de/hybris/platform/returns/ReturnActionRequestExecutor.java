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
 * This is the interface for executors of any return requests action (approving, cancelling or receiving). An executor performs all actions necessary to
 * initialize approving/cancelling/receiving operation. Typically it is putting it in the appropriate state and forwarding the request to the
 * process engine for further processing.
 */
public interface ReturnActionRequestExecutor
{
	/**
	 * Process an approving request for a given return request
	 * 
	 * @param returnRequest
	 *           the return request to be approved
	 * @throws OrderReturnException
	 */
	void processApprovingRequest(ReturnRequestModel returnRequest) throws OrderReturnException;

	/**
	 * Process a receiving request for a given return request
	 *
	 * @param returnRequest
	 *           the return request to be received
	 * @throws OrderReturnException
	 */
	void processReceivingRequest(ReturnRequestModel returnRequest) throws OrderReturnException;

	/**
	 * Process a cancelling request for a given return request
	 *
	 * @param returnRequest
	 *           the return request to be cancelled
	 * @throws OrderReturnException
	 */
	void processCancellingRequest(ReturnRequestModel returnRequest) throws OrderReturnException;

	/**
	 * Process a manual payment reversal request for a given returnRequest
	 *
	 * @param returnActionRequest
	 *           the returnActionRequest containing returnRequest for which payment to be manually reversed
	 * @throws OrderReturnException
	 */
	void processManualPaymentReversalForReturnRequest(ReturnActionRequest returnActionRequest) throws OrderReturnException;

	/**
	 * Process a manual tax reversal request for a given returnRequest
	 *
	 * @param returnActionRequest
	 *           the returnActionRequest containing returnRequest for which tax to be manually reversed
	 * @throws OrderReturnException
	 */
	void processManualTaxReversalForReturnRequest(ReturnActionRequest returnActionRequest) throws OrderReturnException;
}
