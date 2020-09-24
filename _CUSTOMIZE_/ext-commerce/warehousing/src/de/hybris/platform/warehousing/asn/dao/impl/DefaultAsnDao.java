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
package de.hybris.platform.warehousing.asn.dao.impl;

import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.warehousing.asn.dao.AsnDao;
import de.hybris.platform.warehousing.model.AdvancedShippingNoticeModel;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;
import static org.springframework.util.Assert.isTrue;


/**
 * Default implementation of {@link AsnDao}
 */
public class DefaultAsnDao extends AbstractItemDao implements AsnDao
{
	protected static final String ASNENTRIES_QUERY_PARAM = "asnEntries";
	protected static final String STOCKLEVEL_FOR_ASNENTRY_QUERY =
			"Select {" + StockLevelModel.PK + "} FROM {" + StockLevelModel._TYPECODE + "} WHERE {" + StockLevelModel.ASNENTRY
					+ "} IN (?" + ASNENTRIES_QUERY_PARAM + ")";
	protected static final String ASN_FOR_INTERNALID =
			"Select {" + AdvancedShippingNoticeModel.PK + "} FROM {" + AdvancedShippingNoticeModel._TYPECODE + "} WHERE {"
					+ AdvancedShippingNoticeModel.INTERNALID + "} = ?" + AdvancedShippingNoticeModel.INTERNALID;

	@Override
	public List<StockLevelModel> getStockLevelsForAsn(final AdvancedShippingNoticeModel asn)
	{
		validateParameterNotNullStandardMessage("asn", asn);
		isTrue(CollectionUtils.isNotEmpty(asn.getAsnEntries()), "No ASN Entries found to find the stock level");

		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(STOCKLEVEL_FOR_ASNENTRY_QUERY);
		fQuery.addQueryParameter(ASNENTRIES_QUERY_PARAM, asn.getAsnEntries());

		final SearchResult result = this.getFlexibleSearchService().search(fQuery);

		return result.getResult();
	}

	@Override
	public AdvancedShippingNoticeModel getAsnForInternalId(final String internalId)
	{
		validateParameterNotNullStandardMessage("internalId", internalId);

		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(ASN_FOR_INTERNALID);
		fQuery.addQueryParameter(AdvancedShippingNoticeModel.INTERNALID, internalId);

		final SearchResult asnResult = this.getFlexibleSearchService().search(fQuery);
		final List<AdvancedShippingNoticeModel> asns = asnResult.getResult();
		if (asns.isEmpty())
		{
			throw new UnknownIdentifierException("AdvancedShippingNotice with internal id: [" + internalId + "] not found!");
		}
		else if (asns.size() > 1)
		{
			throw new AmbiguousIdentifierException(
					"AdvancedShippingNotice with internal id: [" + internalId + "] is not unique, " + asns.size()
							+ " AdvancedShippingNotices found!");
		}
		return asns.get(0);
	}
}
