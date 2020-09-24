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
package de.hybris.platform.solrfacetsearch.converters.populator;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.SearchConfig;
import de.hybris.platform.solrfacetsearch.config.SolrConfig;
import de.hybris.platform.solrfacetsearch.model.config.SolrFacetSearchConfigModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrSearchConfigModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrServerConfigModel;


public class DefaultFacetSearchConfigPopulator implements Populator<SolrFacetSearchConfigModel, FacetSearchConfig>
{
	private Converter<SolrSearchConfigModel, SearchConfig> solrSearchConfigConverter;
	private Converter<SolrServerConfigModel, SolrConfig> solrServerConfigConverter;
	private Converter<SolrFacetSearchConfigModel, IndexConfig> indexConfigConverter;

	@Override
	public void populate(final SolrFacetSearchConfigModel source, final FacetSearchConfig target)
	{
		try
		{
			target.setDescription(source.getDescription());
			target.setIndexConfig(getIndexConfigFromItems(source));
			target.setName(source.getName());
			target.setSearchConfig(getSearchConfig(source.getSolrSearchConfig()));
			target.setSolrConfig(getSolrConfigFromItems(source.getSolrServerConfig()));
		}
		catch (final Exception ex)
		{
			throw new ConversionException("Cannot convert facet search config", ex);
		}
	}

	protected SearchConfig getSearchConfig(final SolrSearchConfigModel searchConfigModel)
	{
		return solrSearchConfigConverter.convert(searchConfigModel);
	}

	protected SolrConfig getSolrConfigFromItems(final SolrServerConfigModel itemConfig)
	{
		return solrServerConfigConverter.convert(itemConfig);
	}

	protected IndexConfig getIndexConfigFromItems(final SolrFacetSearchConfigModel configModel)
	{
		return indexConfigConverter.convert(configModel);
	}

	/**
	 * @param solrSearchConfigConverter
	 *           the solrSearchConfigConverter to set
	 */
	public void setSolrSearchConfigConverter(final Converter<SolrSearchConfigModel, SearchConfig> solrSearchConfigConverter)
	{
		this.solrSearchConfigConverter = solrSearchConfigConverter;
	}

	/**
	 * @param solrServerConfigConverter
	 *           the solrServerConfigConverter to set
	 */
	public void setSolrServerConfigConverter(final Converter<SolrServerConfigModel, SolrConfig> solrServerConfigConverter)
	{
		this.solrServerConfigConverter = solrServerConfigConverter;
	}

	/**
	 * @param indexConfigConverter
	 *           the indexConfigConverter to set
	 */
	public void setIndexConfigConverter(final Converter<SolrFacetSearchConfigModel, IndexConfig> indexConfigConverter)
	{
		this.indexConfigConverter = indexConfigConverter;
	}
}
