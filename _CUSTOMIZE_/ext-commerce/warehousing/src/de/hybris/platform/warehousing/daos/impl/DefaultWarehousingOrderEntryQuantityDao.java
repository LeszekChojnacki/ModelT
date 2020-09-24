/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 *
 */
package de.hybris.platform.warehousing.daos.impl;

import de.hybris.platform.basecommerce.constants.BasecommerceConstants;
import de.hybris.platform.basecommerce.enums.ReturnStatus;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.ordercancel.model.OrderEntryCancelRecordEntryModel;
import de.hybris.platform.returns.model.ReturnEntryModel;
import de.hybris.platform.returns.model.ReturnRequestModel;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.warehousing.daos.WarehousingOrderEntryQuantityDao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Provides the methods to retrieve the various quantities related to an order entry
 */
public class DefaultWarehousingOrderEntryQuantityDao extends AbstractItemDao implements WarehousingOrderEntryQuantityDao
{

	protected final static String cancelQuery = "SELECT SUM({ocr:" + OrderEntryCancelRecordEntryModel.CANCELLEDQUANTITY
			+ "}) FROM {" + BasecommerceConstants.TC.ORDERENTRYCANCELRECORDENTRY + " as ocr} WHERE {ocr:"
			+ OrderEntryCancelRecordEntryModel.ORDERENTRY + "}=?orderEntry";

	protected final static String returnedQuery = "SELECT SUM({returnEntry:" + ReturnEntryModel.RECEIVEDQUANTITY + "}) FROM {"
			+ BasecommerceConstants.TC.RETURNENTRY + " as returnEntry JOIN " + BasecommerceConstants.TC.RETURNREQUEST
			+ " as returnRequest ON {returnEntry:" + ReturnEntryModel.RETURNREQUEST + "}={returnRequest:" + ReturnRequestModel.PK
			+ "}} WHERE {returnEntry:" + ReturnEntryModel.ORDERENTRY + "}=?orderEntry AND ({returnRequest:"
			+ ReturnRequestModel.STATUS + "}=?receivedStatus OR {returnRequest:" + ReturnRequestModel.STATUS + "}=?completedStatus)";

	/**
	 * Retrieve the cancelled quantity for a specific order entry
	 *
	 * @param orderEntry
	 *           the order entry for which we want to get the cancelled quantity
	 * @return the cancelled quantity
	 */
	@Override
	public Long getCancelledQuantity(final OrderEntryModel orderEntry)
	{
		final Map<String, Object> params = new HashMap<>();
		params.put("orderEntry", orderEntry);
		return processRequestWithParams(cancelQuery, params);
	}

	/**
	 * Retrieve the quantity returned for a specific order entry
	 *
	 * @param orderEntry
	 *           the order entry for which we want to get the returned quantity
	 * @return the returned quantity
	 */
	@Override
	public Long getQuantityReturned(final OrderEntryModel orderEntry)
	{
		final Map<String, Object> params = new HashMap<>();
		params.put("orderEntry", orderEntry);
		params.put("receivedStatus", ReturnStatus.RECEIVED);
		params.put("completedStatus", ReturnStatus.COMPLETED);

		return processRequestWithParams(returnedQuery, params);
	}

	/**
	 * Process the flexible search given in parameter and applies the list of parameters associated
	 *
	 * @param queryString
	 *           the flexible search to process
	 * @param params
	 *           the list of params requested by the associated query
	 * @return the quantity asked
	 */
	@Override
	public Long processRequestWithParams(final String queryString, final Map<String, Object> params)
	{
		if(params.keySet().stream().anyMatch(key -> params.get(key) instanceof ItemModel && ((ItemModel) params.get(key)).getPk() == null)){
			return 0L;
		}

		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(queryString);
		params.keySet().forEach(key -> fQuery.addQueryParameter(key, params.get(key)));

		final List<Class<Long>> resultClassList = new ArrayList<>();
		resultClassList.add(Long.class);
		fQuery.setResultClassList(resultClassList);

		final SearchResult<Long> result = getFlexibleSearchService().search(fQuery);
		return result.getResult().stream().filter(res -> res != null).findFirst().orElse(Long.valueOf(0L));
	}

}
