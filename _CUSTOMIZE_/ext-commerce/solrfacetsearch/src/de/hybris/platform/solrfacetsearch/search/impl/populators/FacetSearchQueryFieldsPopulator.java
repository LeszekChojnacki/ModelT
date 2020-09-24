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
package de.hybris.platform.solrfacetsearch.search.impl.populators;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.solrfacetsearch.constants.SolrfacetsearchConstants;
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider;
import de.hybris.platform.solrfacetsearch.search.FieldNameTranslator;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.solrfacetsearch.search.impl.SearchQueryConverterData;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.beans.factory.annotation.Required;


public class FacetSearchQueryFieldsPopulator implements Populator<SearchQueryConverterData, SolrQuery>
{
	private FieldNameTranslator fieldNameTranslator;

	public FieldNameTranslator getFieldNameTranslator()
	{
		return fieldNameTranslator;
	}

	@Required
	public void setFieldNameTranslator(final FieldNameTranslator fieldNameTranslator)
	{
		this.fieldNameTranslator = fieldNameTranslator;
	}

	@Override
	public void populate(final SearchQueryConverterData source, final SolrQuery target)
	{
		final SearchQuery searchQuery = source.getSearchQuery();
		final List<String> fields = searchQuery.getFields();

		if (CollectionUtils.isNotEmpty(fields))
		{
			// id and pk fields should always be included in the response
			target.addField(SolrfacetsearchConstants.SCORE_FIELD);
			target.addField(SolrfacetsearchConstants.ID_FIELD);
			target.addField(SolrfacetsearchConstants.PK_FIELD);

			for (final String field : fields)
			{
				target.addField(fieldNameTranslator.translate(searchQuery, field, FieldNameProvider.FieldType.INDEX));
			}
		} else {
			target.addField(SolrfacetsearchConstants.SCORE_FIELD);
			target.addField(SolrfacetsearchConstants.ALL_FIELDS);
		}

	}
}
