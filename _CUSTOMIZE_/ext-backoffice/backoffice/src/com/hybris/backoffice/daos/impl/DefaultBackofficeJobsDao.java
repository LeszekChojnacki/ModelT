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

import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.daos.BackofficeJobsDao;


public class DefaultBackofficeJobsDao implements BackofficeJobsDao
{

	private static final String CODES_PARAM = "codes";
	private static final String QUERY_STRING = String.format("SELECT {%s} FROM {%s} WHERE {%s} IN (?%s)", CronJobModel.PK,
			CronJobModel._TYPECODE, CronJobModel.CODE, CODES_PARAM);

	private FlexibleSearchService flexibleSearchService;


	@Override
	public List<CronJobModel> findAllJobs(final Collection<String> codes)
	{
		if (CollectionUtils.isNotEmpty(codes))
		{
			final FlexibleSearchQuery query = new FlexibleSearchQuery(QUERY_STRING);
			query.addQueryParameter(CODES_PARAM, codes);
			query.setResultClassList(Arrays.asList(CronJobModel.class));
			final SearchResult<CronJobModel> search = flexibleSearchService.search(query);
			return new ArrayList<>(search.getResult());
		}
		return Collections.emptyList();
	}

	protected FlexibleSearchService getFlexibleSearchService()
	{
		return flexibleSearchService;
	}

	@Required
	public void setFlexibleSearchService(final FlexibleSearchService flexibleSearchService)
	{
		this.flexibleSearchService = flexibleSearchService;
	}

}
