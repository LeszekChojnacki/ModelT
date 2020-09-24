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
package de.hybris.platform.returns.impl.executors;

import de.hybris.platform.returns.ReturnActionAdapter;
import de.hybris.platform.returns.ReturnActionRequest;
import de.hybris.platform.returns.ReturnActionRequestExecutor;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import de.hybris.platform.basecommerce.enums.ReturnStatus;
import de.hybris.platform.ordercancel.OrderCancelWarehouseAdapter;
import de.hybris.platform.returns.OrderReturnException;
import de.hybris.platform.returns.model.ReturnRequestModel;
import de.hybris.platform.servicelayer.model.ModelService;
import org.springframework.util.Assert;


/**
 * This executor uses {@link OrderCancelWarehouseAdapter} to forward cancel requests to the Warehouse for further
 * processing. From this point order cancel processing is suspended until a response from a Warehouse is received. This
 * response contains details if the order cancel request has been performed completely, partially, or not at all.
 */
public class DefaultReturnActionRequestExecutor implements ReturnActionRequestExecutor
{
	private static final Logger LOG = Logger.getLogger(DefaultReturnActionRequestExecutor.class.getName());

	private ModelService modelService;
	private ReturnActionAdapter returnActionAdapter;

	@Override
	public void processApprovingRequest(ReturnRequestModel returnRequest) throws OrderReturnException
	{
		returnRequest.setStatus(ReturnStatus.APPROVING);
		modelService.save(returnRequest);
		getReturnActionAdapter().requestReturnApproval(returnRequest);
		LOG.info("Return request: " + returnRequest.getCode() + " is being approved");
	}

	@Override
	public void processReceivingRequest(ReturnRequestModel returnRequest) throws OrderReturnException
	{
		returnRequest.setStatus(ReturnStatus.RECEIVING);
		modelService.save(returnRequest);
		getReturnActionAdapter().requestReturnReception(returnRequest);
		LOG.info("Return request: " + returnRequest.getCode() + " is being received");
	}

	@Override
	public void processCancellingRequest(ReturnRequestModel returnRequest) throws OrderReturnException
	{
		returnRequest.setStatus(ReturnStatus.CANCELLING);
		modelService.save(returnRequest);
		getReturnActionAdapter().requestReturnCancellation(returnRequest);
		LOG.info("Return request: " + returnRequest.getCode() + " is being cancelled");
	}

	@Override
	public void processManualPaymentReversalForReturnRequest(final ReturnActionRequest returnActionRequest) throws OrderReturnException
	{
		Assert.notNull(returnActionRequest, "ReturnActionRequest cannot be null");

		final ReturnRequestModel returnRequest = returnActionRequest.getReturnRequest();
		returnRequest.setStatus(ReturnStatus.REVERSING_PAYMENT);
		modelService.save(returnRequest);
		getReturnActionAdapter().requestManualPaymentReversalForReturnRequest(returnRequest);
		LOG.info("Manually reversing the payment for Return request: " + returnRequest.getCode());
	}

	@Override
	public void processManualTaxReversalForReturnRequest(final ReturnActionRequest returnActionRequest) throws OrderReturnException
	{
		Assert.notNull(returnActionRequest, "ReturnActionRequest cannot be null");

		final ReturnRequestModel returnRequest = returnActionRequest.getReturnRequest();
		returnRequest.setStatus(ReturnStatus.REVERSING_TAX);
		modelService.save(returnRequest);
		getReturnActionAdapter().requestManualTaxReversalForReturnRequest(returnRequest);
		LOG.info("Manually reversing the tax for Return request: " + returnRequest.getCode());
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	protected ReturnActionAdapter getReturnActionAdapter()
	{
		return returnActionAdapter;
	}

	@Required
	public void setReturnActionAdapter(ReturnActionAdapter returnActionAdapter)
	{
		this.returnActionAdapter = returnActionAdapter;
	}
}
