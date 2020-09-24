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
import de.hybris.platform.basecommerce.enums.ConsignmentStatus;
import de.hybris.platform.ordersplitting.model.ConsignmentEntryModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.warehousing.daos.WarehousingConsignmentEntryQuantityDao;
import de.hybris.platform.warehousing.model.DeclineConsignmentEntryEventModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Provides the methods to retrieve the various quantities related to a consignment entry
 */
public class DefaultWarehousingConsignmentEntryQuantityDao extends AbstractItemDao
		implements WarehousingConsignmentEntryQuantityDao
{
	protected final static String shippedQuery = "SELECT SUM({consignmentEntry:" + ConsignmentEntryModel.QUANTITY + "}) FROM {"
			+ BasecommerceConstants.TC.CONSIGNMENTENTRY + " as consignmentEntry JOIN " + BasecommerceConstants.TC.CONSIGNMENT
			+ " as consignment ON {consignmentEntry:" + ConsignmentEntryModel.CONSIGNMENT + "}={consignment:" + ConsignmentModel.PK
			+ "}} WHERE {consignmentEntry." + ConsignmentEntryModel.PK + "}=?consignmentEntry AND ({consignment."
			+ ConsignmentModel.STATUS + "}=?shippedStatus OR {consignment." + ConsignmentModel.STATUS + "}=?pickedupStatus)";

	protected final static String declinedQuery = "SELECT SUM({dcee:" + DeclineConsignmentEntryEventModel.QUANTITY + "}) FROM {"
			+ DeclineConsignmentEntryEventModel._TYPECODE + " as dcee} WHERE {dcee:"
			+ DeclineConsignmentEntryEventModel.CONSIGNMENTENTRY + "}=?consignmentEntry";

	/**
	 * Retrieve the quantity shipped for a specific order entry
	 *
	 * @param consignmentEntry
	 *           the consignment entry for which we want to get the shipped quantity
	 * @return the shipped quantity
	 */
	@Override
	public Long getQuantityShipped(final ConsignmentEntryModel consignmentEntry)
	{
		final Map<String, Object> params = new HashMap<>();
		params.put("consignmentEntry", consignmentEntry);
		params.put("shippedStatus", ConsignmentStatus.SHIPPED);
		params.put("pickedupStatus", ConsignmentStatus.PICKUP_COMPLETE);

		final Long quantity = processRequestWithParams(shippedQuery, params);
		return quantity;
	}

	/**
	 * Retrieve the quantity allocated for a specific order entry
	 *
	 * @param consignmentEntry
	 *           the consignment entry for which we want to get the allocated quantity
	 * @return the allocated quantity
	 */
	@Override
	public Long getQuantityDeclined(final ConsignmentEntryModel consignmentEntry)
	{
		final Map<String, Object> params = new HashMap<>();
		params.put("consignmentEntry", consignmentEntry);
		return processRequestWithParams(declinedQuery, params);
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
		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(queryString);
		params.keySet().forEach(key -> fQuery.addQueryParameter(key, params.get(key)));

		final List<Class<Long>> resultClassList = new ArrayList<>();
		resultClassList.add(Long.class);
		fQuery.setResultClassList(resultClassList);

		final SearchResult<Long> result = getFlexibleSearchService().search(fQuery);
		return result.getResult().stream().filter(res -> res != null).findFirst().orElse(Long.valueOf(0L));
	}

}
