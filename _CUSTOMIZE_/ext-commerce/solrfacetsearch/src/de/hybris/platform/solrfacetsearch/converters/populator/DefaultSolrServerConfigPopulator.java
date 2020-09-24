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

import de.hybris.platform.converters.Converters;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.solrfacetsearch.common.ConfigurationUtils;
import de.hybris.platform.solrfacetsearch.config.EndpointURL;
import de.hybris.platform.solrfacetsearch.config.QueryMethod;
import de.hybris.platform.solrfacetsearch.config.SolrClientConfig;
import de.hybris.platform.solrfacetsearch.config.SolrConfig;
import de.hybris.platform.solrfacetsearch.config.SolrServerMode;
import de.hybris.platform.solrfacetsearch.constants.SolrfacetsearchConstants;
import de.hybris.platform.solrfacetsearch.enums.SolrServerModes;
import de.hybris.platform.solrfacetsearch.model.config.SolrEndpointUrlModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrServerConfigModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Required;



/**
 * Populates {@link SolrConfig} data using {@link SolrServerConfigModel} instance.
 */
public class DefaultSolrServerConfigPopulator implements Populator<SolrServerConfigModel, SolrConfig>
{
	private Converter<SolrServerConfigModel, SolrClientConfig> solrClientConfigConverter;
	private Converter<SolrServerConfigModel, SolrClientConfig> solrIndexingClientConfigConverter;
	private Converter<SolrEndpointUrlModel, EndpointURL> endpointUrlConverter;

	@Override
	public void populate(final SolrServerConfigModel source, final SolrConfig target)
	{
		target.setName(source.getName());
		target.setMode(populateConfigServerMode(source));
		target.setEndpointURLs(populateEndpointUrls(source));
		target.setClientConfig(populateClientConfig(source));
		target.setIndexingClientConfig(populateIndexingClientConfig(source));

		//Try to populate these configurations from properties, if present
		target.setUseMasterNodeExclusivelyForIndexing(populateConfigUseMasterNodeExclusivelyForIndexing(source));
		target.setNumShards(populateConfigNumShards(source));
		target.setReplicationFactor(populateReplicationFactor(source));

		target.setQueryMethod(
				source.getSolrQueryMethod() == null ? null : QueryMethod.valueOf(source.getSolrQueryMethod().toString()));

		target.setModifiedTime(source.getModifiedtime());
		target.setVersion(source.getVersion());
	}

	protected boolean populateConfigUseMasterNodeExclusivelyForIndexing(final SolrServerConfigModel source)
	{
		final Object value = ConfigurationUtils.getObject(source, SolrServerConfigModel.USEMASTERNODEEXCLUSIVELYFORINDEXING,
				SolrfacetsearchConstants.SERVER_USE_MASTER_NODE_EXCLUSIVELY_FOR_INDEXING, source.getName());

		if (value instanceof Boolean)
		{
			return (Boolean) value;
		}
		else if (value instanceof String)
		{
			return Boolean.valueOf((String) value);
		}

		return false;
	}

	protected Integer populateConfigNumShards(final SolrServerConfigModel source)
	{
		final Object value = ConfigurationUtils.getObject(source, SolrServerConfigModel.NUMSHARDS,
				SolrfacetsearchConstants.SERVER_NUM_SHARDS, source.getName());

		if (value instanceof Integer)
		{
			return (Integer) value;
		}
		else if (value instanceof String)
		{
			return Integer.valueOf((String) value);
		}

		return null;
	}

	protected Integer populateReplicationFactor(final SolrServerConfigModel source)
	{
		final Object value = ConfigurationUtils.getObject(source, SolrServerConfigModel.REPLICATIONFACTOR,
				SolrfacetsearchConstants.SERVER_REPLICATION_FACTOR, source.getName());

		if (value instanceof Integer)
		{
			return (Integer) value;
		}
		else if (value instanceof String)
		{
			return Integer.valueOf((String) value);
		}

		return null;

	}

	protected SolrServerMode populateConfigServerMode(final SolrServerConfigModel source)
	{
		final Object value = ConfigurationUtils.getObject(source, SolrServerConfigModel.MODE,
				SolrfacetsearchConstants.SERVER_MODE_TEMPLATE, source.getName());

		if (value instanceof SolrServerModes)
		{
			return SolrServerMode.valueOf(((SolrServerModes) value).name());
		}
		else if (value instanceof String)
		{
			return SolrServerMode.valueOf(((String) value).toUpperCase(Locale.ROOT));
		}
		else
		{
			return null;
		}
	}

	protected List<EndpointURL> populateEndpointUrls(final SolrServerConfigModel source)
	{
		final Object value = ConfigurationUtils.getObject(source, SolrServerConfigModel.SOLRENDPOINTURLS,
				SolrfacetsearchConstants.SERVER_URLS_TEMPLATE, source.getName());

		if (value instanceof String)
		{
			final Date modifiedTime = new Date();
			final List<EndpointURL> urls = new ArrayList<>();

			final String[] urlsFromProperty = ((String) value).trim().split("[\\s,]+");
			if (urlsFromProperty.length == 0)
			{
				return Collections.emptyList();
			}

			urls.add(createEndpoint(urlsFromProperty[0], true, modifiedTime));

			for (int i = 1; i < urlsFromProperty.length; i++)
			{
				urls.add(createEndpoint(urlsFromProperty[i], false, modifiedTime));
			}

			return urls;
		}
		else
		{
			return Converters.convertAll((List<SolrEndpointUrlModel>) value, endpointUrlConverter);
		}
	}

	protected EndpointURL createEndpoint(final String url, final boolean isMaster, final Date modifiedTime)
	{
		final EndpointURL result = new EndpointURL();

		result.setMaster(isMaster);
		result.setUrl(url);
		result.setModifiedTime(modifiedTime);

		return result;
	}

	protected SolrClientConfig populateClientConfig(final SolrServerConfigModel source)
	{
		return solrClientConfigConverter.convert(source);
	}

	protected SolrClientConfig populateIndexingClientConfig(final SolrServerConfigModel source)
	{
		return solrIndexingClientConfigConverter.convert(source);
	}

	public Converter<SolrServerConfigModel, SolrClientConfig> getSolrClientConfigConverter()
	{
		return solrClientConfigConverter;
	}

	@Required
	public void setSolrClientConfigConverter(final Converter<SolrServerConfigModel, SolrClientConfig> solrClientConfigConverter)
	{
		this.solrClientConfigConverter = solrClientConfigConverter;
	}

	public Converter<SolrServerConfigModel, SolrClientConfig> getSolrIndexingClientConfigConverter()
	{
		return solrIndexingClientConfigConverter;
	}

	@Required
	public void setSolrIndexingClientConfigConverter(
			final Converter<SolrServerConfigModel, SolrClientConfig> solrIndexingClientConfigConverter)
	{
		this.solrIndexingClientConfigConverter = solrIndexingClientConfigConverter;
	}

	@Required
	public void setEndpointUrlConverter(final Converter<SolrEndpointUrlModel, EndpointURL> endpointUrlConverter)
	{
		this.endpointUrlConverter = endpointUrlConverter;
	}
}
