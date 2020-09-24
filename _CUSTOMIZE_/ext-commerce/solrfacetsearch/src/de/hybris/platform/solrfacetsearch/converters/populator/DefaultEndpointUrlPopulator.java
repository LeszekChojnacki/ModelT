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
import de.hybris.platform.solrfacetsearch.config.EndpointURL;
import de.hybris.platform.solrfacetsearch.model.config.SolrEndpointUrlModel;


/**
 * Populator for generated {@link EndpointURL} POJO class
 */
public class DefaultEndpointUrlPopulator implements Populator<SolrEndpointUrlModel, EndpointURL>
{
	@Override
	public void populate(final SolrEndpointUrlModel source, final EndpointURL target)
	{
		target.setUrl(source.getUrl());
		target.setMaster(source.isMaster());
		target.setModifiedTime(source.getModifiedtime());
	}
}
