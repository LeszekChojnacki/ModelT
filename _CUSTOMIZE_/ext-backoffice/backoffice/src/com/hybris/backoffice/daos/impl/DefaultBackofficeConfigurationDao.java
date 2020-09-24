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
package com.hybris.backoffice.daos.impl;

import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.daos.BackofficeConfigurationDao;


/**
 * Default implementation of {@link BackofficeConfigurationDao}
 */
public class DefaultBackofficeConfigurationDao implements BackofficeConfigurationDao
{

	private FlexibleSearchService flexibleSearchService;
	private static final String QUERY_MEDIAS_FOR_CODE = "SELECT {PK} FROM {" + MediaModel._TYPECODE + "} WHERE {" + MediaModel.CODE + "} IN (?codes)";

	@Override
	public List<MediaModel> findMedias(final String code)
	{
		final FlexibleSearchQuery fsq = new FlexibleSearchQuery(QUERY_MEDIAS_FOR_CODE);
		fsq.addQueryParameter("codes", code);
		fsq.setResultClassList(Collections.singletonList(MediaModel.class));
		final SearchResult<MediaModel> resultSet = flexibleSearchService.search(fsq);
		return resultSet.getResult();
	}

	public FlexibleSearchService getFlexibleSearchService() {
		return flexibleSearchService;
	}

	@Required
	public void setFlexibleSearchService(final FlexibleSearchService flexibleSearchService) {
		this.flexibleSearchService = flexibleSearchService;
	}
}
