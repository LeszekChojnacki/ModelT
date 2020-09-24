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
package de.hybris.platform.promotionengineservices.order.dao.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.order.daos.impl.DefaultOrderDao;
import de.hybris.platform.promotionengineservices.order.dao.ExtendedOrderDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;

import java.util.HashMap;
import java.util.Map;


/**
 * The class extends {@link DefaultOrderDao} and is a default implementation of {@link ExtendedOrderDao} interface.
 * 
 * @deprecated Since 6.7. Use {@link de.hybris.platform.ruleengineservices.order.dao.ExtendedOrderDao} instead.
 */
@Deprecated
public class DefaultExtendedOrderDao extends DefaultOrderDao implements ExtendedOrderDao
{
	protected static final String FIND_ORDERS_BY_CODE_QUERY = "SELECT {" + AbstractOrderModel.PK + "}, {"
			+ AbstractOrderModel.CREATIONTIME + "}, {" + AbstractOrderModel.CODE + "} FROM {" + AbstractOrderModel._TYPECODE
			+ "} WHERE {" + AbstractOrderModel.CODE + "} = ?code";

	@Override
	public AbstractOrderModel findOrderByCode(final String code)
	{
		validateParameterNotNull(code, "Code must not be null");
		final Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("code", code);
		return getFlexibleSearchService().searchUnique(new FlexibleSearchQuery(FIND_ORDERS_BY_CODE_QUERY, queryParams));
	}

}
