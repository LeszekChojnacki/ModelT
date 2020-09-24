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
package de.hybris.platform.returns.dao;

import de.hybris.platform.returns.impl.DefaultReturnService;
import de.hybris.platform.returns.model.ReplacementOrderModel;
import de.hybris.platform.returns.model.ReturnRequestModel;
import de.hybris.platform.servicelayer.internal.dao.Dao;


/**
 * Dao object used in {@link DefaultReturnService}
 * 
 */
public interface ReplacementOrderDao extends Dao
{

	/**
	 * Returns the {@link ReplacementOrderModel} by the specified 'RMA value'
	 * 
	 * @param rma
	 *           value
	 * @return replacement order
	 */
	ReplacementOrderModel getReplacementOrder(String rma);

	/**
	 * Creates a {@link ReplacementOrderModel}
	 * 
	 * @param request
	 *           the return request to which the order will be assigned
	 * @return the Replacement Order'
	 */
	ReplacementOrderModel createReplacementOrder(final ReturnRequestModel request);
}
