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
public class DefaultSolrIndexingClientConfigCredentialsPopulator implements Populator<SolrServerConfigModel, SolrClientConfig>
{
	@Override
	public void populate(final SolrServerConfigModel source, final SolrClientConfig target)
	{
		target.setUsername(ConfigurationUtils.getString(source, SolrServerConfigModel.INDEXINGUSERNAME,
				SolrfacetsearchConstants.INDEXING_CLIENT_USERNAME_TEMPLATE, source.getName()));

		target.setPassword(ConfigurationUtils.getString(source, SolrServerConfigModel.INDEXINGPASSWORD,
				SolrfacetsearchConstants.INDEXING_CLIENT_PASSWORD_TEMPLATE, source.getName()));
	}
}
