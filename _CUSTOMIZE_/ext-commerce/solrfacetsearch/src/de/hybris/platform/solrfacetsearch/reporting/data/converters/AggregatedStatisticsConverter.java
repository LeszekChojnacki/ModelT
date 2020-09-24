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
package de.hybris.platform.solrfacetsearch.reporting.data.converters;

import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.solrfacetsearch.daos.SolrFacetSearchConfigDao;
import de.hybris.platform.solrfacetsearch.model.config.SolrFacetSearchConfigModel;
import de.hybris.platform.solrfacetsearch.model.reporting.SolrQueryAggregatedStatsModel;
import de.hybris.platform.solrfacetsearch.reporting.data.AggregatedSearchQueryInfo;


/**
 * Converts POJO object (AggregatedKeywordSearchResult) to model (SolrQueryAggregatedStatsModel)
 */
public class AggregatedStatisticsConverter implements Converter<AggregatedSearchQueryInfo, SolrQueryAggregatedStatsModel>
{
	private ModelService modelService;
	private SolrFacetSearchConfigDao solrFacetSearchConfigDao;
	private CommonI18NService i18nService;

	@Override
	public SolrQueryAggregatedStatsModel convert(final AggregatedSearchQueryInfo result)
	{
		final SolrQueryAggregatedStatsModel model = modelService.create(SolrQueryAggregatedStatsModel.class);
		final SolrFacetSearchConfigModel indexConfig = solrFacetSearchConfigDao.findFacetSearchConfigByName(result.getIndexName());
		final LanguageModel lang = i18nService.getLanguage(result.getLanguage());
		model.setIndexConfig(indexConfig);
		model.setLanguage(lang);
		model.setTime(result.getDate());
		model.setCount(result.getCount());
		model.setAvgNumberOfResults(result.getAverageNumberOfResults());
		model.setQuery(result.getQuery());
		return model;
	}

	@Override
	public SolrQueryAggregatedStatsModel convert(final AggregatedSearchQueryInfo source,
			final SolrQueryAggregatedStatsModel prototype)
	{
		throw new UnsupportedOperationException();
	}

	public void setSolrFacetSearchConfigDao(final SolrFacetSearchConfigDao solrFacetSearchConfigDao)
	{
		this.solrFacetSearchConfigDao = solrFacetSearchConfigDao;
	}

	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	public void setI18nService(final CommonI18NService i18nService)
	{
		this.i18nService = i18nService;
	}

}
