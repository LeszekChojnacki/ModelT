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
package de.hybris.platform.solrfacetsearch.reporting.impl;

import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.solrfacetsearch.model.reporting.SolrQueryAggregatedStatsModel;
import de.hybris.platform.solrfacetsearch.reporting.AggregatedQueryStatsService;
import de.hybris.platform.solrfacetsearch.reporting.data.AggregatedSearchQueryInfo;

import java.util.ArrayList;
import java.util.List;


/**
 * Default implementation of {@link AggregatedQueryStatsService}.
 *
 * Saves aggregated query statistics into hybris database
 */
public class DefaultAggregatedQueryStatsService implements AggregatedQueryStatsService
{
	private Converter<AggregatedSearchQueryInfo, SolrQueryAggregatedStatsModel> converter;
	private ModelService modelService;

	@Override
	public void save(final List<AggregatedSearchQueryInfo> aggregatedStatistics)
	{
		final List<SolrQueryAggregatedStatsModel> aggregatedStatisticsModels = convertPojosToModels(aggregatedStatistics);
		modelService.saveAll(aggregatedStatisticsModels);
	}

	protected List<SolrQueryAggregatedStatsModel> convertPojosToModels(final List<AggregatedSearchQueryInfo> allStatistics)
	{
		final List<SolrQueryAggregatedStatsModel> res = new ArrayList<>();
		for (final AggregatedSearchQueryInfo result : allStatistics)
		{
			final SolrQueryAggregatedStatsModel model = converter.convert(result);
			res.add(model);
		}
		return res;
	}

	public void setConverter(final Converter<AggregatedSearchQueryInfo, SolrQueryAggregatedStatsModel> converter)
	{
		this.converter = converter;
	}

	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}
}
