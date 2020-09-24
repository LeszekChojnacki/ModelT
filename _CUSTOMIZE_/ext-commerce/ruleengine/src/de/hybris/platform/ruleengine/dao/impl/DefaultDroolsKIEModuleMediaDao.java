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

import static com.google.common.collect.ImmutableMap.of;

import de.hybris.platform.ruleengine.dao.DroolsKIEModuleMediaDao;
import de.hybris.platform.ruleengine.model.DroolsKIEModuleMediaModel;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.Map;
import java.util.Optional;


/**
 * Provides default dao implementation of {@code DroolsKIEModuleMediaDao}
 *
 */
public class DefaultDroolsKIEModuleMediaDao extends AbstractItemDao implements DroolsKIEModuleMediaDao
{
	private static final String FIND_KIEMODULE_MEDIA = "select {" + DroolsKIEModuleMediaModel.PK + "} " 
			+ "from {" + DroolsKIEModuleMediaModel._TYPECODE + "} " 
			+ "where {" + DroolsKIEModuleMediaModel.KIEMODULENAME + "} = ?kmname "
			+ "and {" + DroolsKIEModuleMediaModel.RELEASEID + "} = ?releaseId";

	@Override
	public Optional<DroolsKIEModuleMediaModel> findKIEModuleMedia(final String kieModuleName, final String releaseId)
	{
		final Map<String, Object> queryParams = of("kmname", kieModuleName, "releaseId", releaseId);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(FIND_KIEMODULE_MEDIA, queryParams);
		final SearchResult<DroolsKIEModuleMediaModel> searchResult = getFlexibleSearchService().search(query);
		return searchResult.getResult().stream().findFirst();
	}
}
