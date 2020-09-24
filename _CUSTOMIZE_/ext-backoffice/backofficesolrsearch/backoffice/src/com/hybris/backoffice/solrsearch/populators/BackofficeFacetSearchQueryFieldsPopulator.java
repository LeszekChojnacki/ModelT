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
package com.hybris.backoffice.solrsearch.populators;

import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.solrfacetsearch.constants.SolrfacetsearchConstants;
import de.hybris.platform.solrfacetsearch.search.impl.SearchQueryConverterData;
import de.hybris.platform.solrfacetsearch.search.impl.populators.FacetSearchQueryFieldsPopulator;

import org.apache.solr.client.solrj.SolrQuery;


public class BackofficeFacetSearchQueryFieldsPopulator extends FacetSearchQueryFieldsPopulator
{
	@Override
	public void populate(final SearchQueryConverterData source, final SolrQuery target) throws ConversionException
	{
		super.populate(source, target);
		if (source.getSearchQuery().getFacetSearchConfig().getSearchConfig().isRestrictFieldsInResponse())
		{
			target.addField(SolrfacetsearchConstants.PK_FIELD);
		}
	}
}
