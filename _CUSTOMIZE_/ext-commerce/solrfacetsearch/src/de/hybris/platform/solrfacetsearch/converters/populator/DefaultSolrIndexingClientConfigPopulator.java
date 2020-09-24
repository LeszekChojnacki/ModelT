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
import de.hybris.platform.solrfacetsearch.common.ConfigurationUtils;
import de.hybris.platform.solrfacetsearch.config.SolrClientConfig;
import de.hybris.platform.solrfacetsearch.constants.SolrfacetsearchConstants;
import de.hybris.platform.solrfacetsearch.model.config.SolrServerConfigModel;


/**
 * Populates {@link SolrClientConfig} data using {@link SolrServerConfigModel} instance.
 */
public class DefaultSolrIndexingClientConfigPopulator implements Populator<SolrServerConfigModel, SolrClientConfig>
{
	@Override
	public void populate(final SolrServerConfigModel source, final SolrClientConfig target)
	{
		target.setAliveCheckInterval(ConfigurationUtils.getInteger(source, SolrServerConfigModel.INDEXINGALIVECHECKINTERVAL,
				SolrfacetsearchConstants.INDEXING_CLIENT_ALIVE_CHECK_INTERVAL_TEMPLATE, source.getName()));

		target.setConnectionTimeout(ConfigurationUtils.getInteger(source, SolrServerConfigModel.INDEXINGCONNECTIONTIMEOUT,
				SolrfacetsearchConstants.INDEXING_CLIENT_CONNECTION_TIMEOUT_TEMPLATE, source.getName()));

		target.setSocketTimeout(ConfigurationUtils.getInteger(source, SolrServerConfigModel.INDEXINGSOCKETTIMEOUT,
				SolrfacetsearchConstants.INDEXING_CLIENT_SOCKET_TIMEOUT_TEMPLATE, source.getName()));

		target.setMaxConnections(ConfigurationUtils.getInteger(source, SolrServerConfigModel.INDEXINGMAXTOTALCONNECTIONS,
				SolrfacetsearchConstants.INDEXING_CLIENT_MAX_CONNECTIONS_TEMPLATE, source.getName()));

		target.setMaxConnectionsPerHost(
				ConfigurationUtils.getInteger(source, SolrServerConfigModel.INDEXINGMAXTOTALCONNECTIONSPERHOSTCONFIG,
						SolrfacetsearchConstants.INDEXING_CLIENT_MAX_CONNECTIONS_PER_HOST_TEMPLATE, source.getName()));

		// this is no longer supported and will be removed
		target.setTcpNoDelay(source.isTcpNoDelay());
	}
}
