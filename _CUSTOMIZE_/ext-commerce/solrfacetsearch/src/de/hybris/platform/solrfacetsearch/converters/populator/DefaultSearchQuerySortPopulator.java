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
import de.hybris.platform.solrfacetsearch.config.SearchQuerySort;
import de.hybris.platform.solrfacetsearch.model.SolrSearchQuerySortModel;


/**
 * Populates {@link SearchQuerySort} from the model {@link SolrSearchQuerySortModel}
 */
public class DefaultSearchQuerySortPopulator implements Populator<SolrSearchQuerySortModel, SearchQuerySort>
{

	@Override
	public void populate(final SolrSearchQuerySortModel source, final SearchQuerySort target)
	{
		target.setField(source.getField());
		target.setAscending(source.isAscending());
	}
}
