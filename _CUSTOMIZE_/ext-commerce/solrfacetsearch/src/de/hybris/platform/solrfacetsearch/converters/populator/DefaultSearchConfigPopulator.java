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
import de.hybris.platform.solrfacetsearch.config.SearchConfig;
import de.hybris.platform.solrfacetsearch.model.config.SolrSearchConfigModel;

import java.util.Collections;


public class DefaultSearchConfigPopulator implements Populator<SolrSearchConfigModel, SearchConfig>
{
	@Override
	public void populate(final SolrSearchConfigModel source, final SearchConfig target)
	{
		target.setDefaultSortOrder(
				source.getDefaultSortOrder() == null ? Collections.<String> emptyList() : source.getDefaultSortOrder());
		target.setPageSize(source.getPageSize() != null ? source.getPageSize().intValue() : 0);
		target.setAllFacetValuesInResponse(source.isAllFacetValuesInResponse());
		target.setRestrictFieldsInResponse(source.isRestrictFieldsInResponse());
		target.setEnableHighlighting(source.isEnableHighlighting());
		target.setLegacyMode(source.isLegacyMode());
	}
}
