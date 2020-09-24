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
package de.hybris.platform.refund.dao;

import de.hybris.platform.refund.impl.DefaultRefundService;
import de.hybris.platform.returns.model.RefundEntryModel;
import de.hybris.platform.returns.model.ReturnRequestModel;

import java.util.List;


/**
 * Dao object used in in {@link DefaultRefundService}
 * 
 */
public interface RefundDao
{
	/**
	 * Returns the refunds for the specified request
	 * 
	 * @param request
	 *           the request
	 * @return the refunds
	 */
	List<RefundEntryModel> getRefunds(final ReturnRequestModel request);
}
