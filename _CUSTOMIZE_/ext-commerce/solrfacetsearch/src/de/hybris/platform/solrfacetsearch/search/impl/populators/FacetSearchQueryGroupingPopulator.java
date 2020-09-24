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
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider.FieldType;
import de.hybris.platform.solrfacetsearch.search.FieldNameTranslator;
import de.hybris.platform.solrfacetsearch.search.GroupCommandField;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.solrfacetsearch.search.impl.SearchQueryConverterData;

import org.apache.commons.collections.CollectionUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.beans.factory.annotation.Required;


public class FacetSearchQueryGroupingPopulator implements Populator<SearchQueryConverterData, SolrQuery>
{
	public static final String GROUP_PARAM = "group";
	public static final String GROUP_LIMIT_PARAM = "group.limit";
	public static final String GROUP_FIELD_PARAM = "group.field";
	public static final String GROUP_NGROUPS_PARAM = "group.ngroups";
	public static final String GROUP_FACET_PARAM = "group.facet";

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
		if (CollectionUtils.isNotEmpty(searchQuery.getGroupCommands()))
		{
			target.add(GROUP_PARAM, Boolean.TRUE.toString());
			target.add(GROUP_FACET_PARAM, Boolean.toString(searchQuery.isGroupFacets()));
			target.add(GROUP_NGROUPS_PARAM, Boolean.TRUE.toString());

			for (final GroupCommandField groupCommand : searchQuery.getGroupCommands())
			{
				final String groupField = fieldNameTranslator.translate(searchQuery, groupCommand.getField(), FieldType.INDEX);
				target.add(GROUP_FIELD_PARAM, groupField);

				if (groupCommand.getGroupLimit() != null)
				{
					target.add(GROUP_LIMIT_PARAM, groupCommand.getGroupLimit().toString());
				}
			}
		}
	}
}
