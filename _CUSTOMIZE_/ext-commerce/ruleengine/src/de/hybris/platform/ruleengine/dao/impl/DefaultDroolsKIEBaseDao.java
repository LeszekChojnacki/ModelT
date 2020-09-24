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
package de.hybris.platform.ruleengine.dao.impl;

import de.hybris.platform.ruleengine.dao.DroolsKIEBaseDao;
import de.hybris.platform.ruleengine.model.DroolsKIEBaseModel;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.List;


/**
 * Provides default dao implementation for {@code DroolsKIEBaseModel}
 *
 */
public class DefaultDroolsKIEBaseDao extends AbstractItemDao implements DroolsKIEBaseDao
{
	private static final String FIND_ALL_KIEBASES = "select {" + DroolsKIEBaseModel.PK + "} from {" + DroolsKIEBaseModel._TYPECODE
			+ "}";

	@Override
	public List<DroolsKIEBaseModel> findAllKIEBases()
	{
		final FlexibleSearchQuery query = new FlexibleSearchQuery(FIND_ALL_KIEBASES);
		final SearchResult<DroolsKIEBaseModel> searchResult = getFlexibleSearchService().search(query);
		return searchResult.getResult();
	}

}
