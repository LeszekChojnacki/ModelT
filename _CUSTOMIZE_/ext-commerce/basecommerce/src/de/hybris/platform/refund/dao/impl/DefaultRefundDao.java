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
package de.hybris.platform.refund.dao.impl;

import de.hybris.platform.jalo.Item;
import de.hybris.platform.refund.dao.RefundDao;
import de.hybris.platform.refund.impl.DefaultRefundService;
import de.hybris.platform.returns.model.RefundEntryModel;
import de.hybris.platform.returns.model.ReturnRequestModel;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Dao object used in in {@link DefaultRefundService}
 *
 */
public class DefaultRefundDao extends AbstractItemDao implements RefundDao
{
	/**
	 * Returns the refunds for the specified request
	 *
	 * @param request
	 *           the request
	 * @return the refunds
	 */
	@Override
	public List<RefundEntryModel> getRefunds(final ReturnRequestModel request)
	{
		final Map<String, Object> params = new HashMap();
		params.put("request", request);

		final String query = "SELECT {" + Item.PK + "} FROM { " + RefundEntryModel._TYPECODE + "} WHERE { "
				+ RefundEntryModel.RETURNREQUEST + "}=?request ORDER BY {" + Item.PK + "} ASC";
		return getFlexibleSearchService().<RefundEntryModel> search(query, params).getResult();
	}

}
