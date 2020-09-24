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
package de.hybris.platform.returns.impl;

import de.hybris.platform.returns.ReturnActionAdapter;
import de.hybris.platform.returns.model.ReturnRequestModel;
import org.apache.log4j.Logger;

/**
 * Default implementation of the return action adapter
 */
public class DefaultReturnActionAdapter implements ReturnActionAdapter
{
	private static final Logger LOG = Logger.getLogger(DefaultReturnActionAdapter.class.getName());

	@Override
	public void requestReturnApproval(ReturnRequestModel returnRequest)
	{
		LOG.info("Return approval requested. Default implementation is empty, please provide your own implementation");
	}

	@Override
	public void requestReturnReception(ReturnRequestModel returnRequest)
	{
		LOG.info("Return reception requested. Default implementation is empty, please provide your own implementation");
	}

	@Override
	public void requestReturnCancellation(ReturnRequestModel returnRequest)
	{
		LOG.info("Return cancellation requested. Default implementation is empty, please provide your own implementation");
	}

	@Override
	public void requestManualPaymentReversalForReturnRequest(final ReturnRequestModel returnRequest)
	{
		LOG.info("Return manual payment reversal requested. Default implementation is empty, please provide your own implementation");
	}

	@Override
	public void requestManualTaxReversalForReturnRequest(final ReturnRequestModel returnRequest)
	{
		LOG.info("Return manual tax reversal requested. Default implementation is empty, please provide your own implementation");
	}
}
