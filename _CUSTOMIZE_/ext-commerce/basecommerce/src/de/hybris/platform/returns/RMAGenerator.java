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
 * "Return Merchandise Authorization" aka "Return Material Authorization" (RMA) generator
 * 
 */
public interface RMAGenerator
{
	/**
	 * RMA generation based on the assigned {@link ReturnRequestModel}
	 * 
	 * @param request
	 *           the current ReturnRequest for which the RMA will be generated
	 * @return rma
	 */
	String generateRMA(ReturnRequestModel request);
}
