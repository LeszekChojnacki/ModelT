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
package de.hybris.platform.basecommerce.site.dao.impl;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.basecommerce.site.dao.BaseSiteDao;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;


/**
 * Implementation of {@link BaseSiteDao} interface.
 */
public class BaseSiteDaoImpl extends AbstractItemDao implements BaseSiteDao
{
	private static final Logger LOG = Logger.getLogger(BaseSiteDaoImpl.class);

	@Override
	public List<BaseSiteModel> findAllBaseSites()
	{
		List<BaseSiteModel> result = null;
		final String query = "SELECT {" + BaseSiteModel.PK + "} FROM {" + BaseSiteModel._TYPECODE + "}";
		final SearchResult<BaseSiteModel> searchResult = search(new FlexibleSearchQuery(query));
		if (!searchResult.getResult().isEmpty())
		{
			result = searchResult.getResult();
		}
		else
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("No BaseSite item was found");
			}
			result = new ArrayList<BaseSiteModel>();
		}
		return result;
	}

	@Override
	public BaseSiteModel findBaseSiteByUID(final String siteUid)
	{
		BaseSiteModel result = null;
		final Map params = new HashMap();
		params.put(BaseSiteModel.UID, siteUid);
		final String query = "SELECT {" + BaseSiteModel.PK + "} FROM {" + BaseSiteModel._TYPECODE + "} WHERE {" + BaseSiteModel.UID
				+ "} = ?" + BaseSiteModel.UID;
		final SearchResult<BaseSiteModel> searchResult = search(query, params);
		if (!searchResult.getResult().isEmpty())
		{
			result = searchResult.getResult().get(0);
		}
		else
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("No BaseSite item was found with search uid: " + siteUid);
			}
		}
		return result;
	}

}
