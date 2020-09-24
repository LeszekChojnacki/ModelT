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
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.solrfacetsearch.config.IndexOperation;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.config.IndexedTypeFlexibleSearchQuery;
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexedTypeModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexerQueryModel;

import java.util.EnumMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Required;


public class DefaultIndexedTypeFlexibleSearchQueriesPopulator implements Populator<SolrIndexedTypeModel, IndexedType>
{
	private Converter<SolrIndexerQueryModel, IndexedTypeFlexibleSearchQuery> indexedTypeFlexibleSearchQueryConverter;

	@Override
	public void populate(final SolrIndexedTypeModel source, final IndexedType target)
	{
		final Map<IndexOperation, IndexedTypeFlexibleSearchQuery> queries = new EnumMap(IndexOperation.class);

		for (final SolrIndexerQueryModel queryModel : source.getSolrIndexerQueries())
		{
			final IndexedTypeFlexibleSearchQuery queryData = indexedTypeFlexibleSearchQueryConverter.convert(queryModel);
			queries.put(IndexOperation.valueOf(queryModel.getType().toString()), queryData);
		}

		target.setFlexibleSearchQueries(queries);
	}

	@Required
	public void setIndexedTypeFlexibleSearchQueryConverter(
			final Converter<SolrIndexerQueryModel, IndexedTypeFlexibleSearchQuery> indexedTypeFlexibleSearchQueryConverter)
	{
		this.indexedTypeFlexibleSearchQueryConverter = indexedTypeFlexibleSearchQueryConverter;
	}
}
