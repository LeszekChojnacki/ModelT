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
package de.hybris.platform.solrfacetsearch.attribute;

import de.hybris.platform.servicelayer.model.attribute.DynamicAttributeHandler;
import de.hybris.platform.solrfacetsearch.model.config.SolrServerConfigModel;

import java.util.Date;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;


public class DefaultSolrServerConfigVersionProvider implements DynamicAttributeHandler<String, SolrServerConfigModel>
{
	protected static final String SEPARATOR = "_";

	@Override
	public String get(final SolrServerConfigModel model)
	{
		return generateVersion(model);
	}

	@Override
	public void set(final SolrServerConfigModel model, final String value)
	{
		throw new UnsupportedOperationException("The attribute is readonly");
	}

	public String generateVersion(final SolrServerConfigModel solrServerConfig)
	{
		final StringBuilder version = new StringBuilder();
		version.append(convertDate(solrServerConfig.getModifiedtime()));

		if (CollectionUtils.isNotEmpty(solrServerConfig.getSolrEndpointUrls()))
		{
			version.append(SEPARATOR);
			version.append(solrServerConfig.getSolrEndpointUrls().stream().map(endpoint -> convertDate(endpoint.getModifiedtime()))
					.sorted().collect(Collectors.joining(SEPARATOR)));
		}

		return version.toString();
	}

	protected String convertDate(final Date date)
	{
		return String.valueOf(date.getTime());
	}
}
