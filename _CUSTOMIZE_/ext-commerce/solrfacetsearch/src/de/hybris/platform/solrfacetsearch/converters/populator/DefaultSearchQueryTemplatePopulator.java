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

import de.hybris.platform.converters.Converters;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.SearchQueryProperty;
import de.hybris.platform.solrfacetsearch.config.SearchQuerySort;
import de.hybris.platform.solrfacetsearch.config.SearchQueryTemplate;
import de.hybris.platform.solrfacetsearch.model.SolrSearchQueryPropertyModel;
import de.hybris.platform.solrfacetsearch.model.SolrSearchQuerySortModel;
import de.hybris.platform.solrfacetsearch.model.SolrSearchQueryTemplateModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexedPropertyModel;

import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Required;


public class DefaultSearchQueryTemplatePopulator implements Populator<SolrSearchQueryTemplateModel, SearchQueryTemplate>
{

	private Converter<SolrIndexedPropertyModel, IndexedProperty> indexedPropertyConverter;
	private Converter<SolrSearchQueryPropertyModel, SearchQueryProperty> solrSearchQueryPropertyConverter;
	private Converter<SolrSearchQuerySortModel, SearchQuerySort> solrSearchQuerySortConverter;

	@Override
	public void populate(final SolrSearchQueryTemplateModel source, final SearchQueryTemplate target)
	{
		target.setName(source.getName());
		target.setShowFacets(source.isShowFacets());
		target.setRestrictFieldsInResponse(source.isRestrictFieldsInResponse());
		target.setEnableHighlighting(source.isEnableHighlighting());
		target.setGroup(source.isGroup());
		target.setGroupLimit(source.getGroupLimit());
		target.setGroupFacets(source.isGroupFacets());
		target.setPageSize(source.getPageSize());
		target.setFtsQueryBuilder(source.getFtsQueryBuilder());
		target.setFtsQueryBuilderParameters(source.getFtsQueryBuilderParameters());

		if (source.getGroupProperty() != null)
		{
			target.setGroupProperty(indexedPropertyConverter.convert(source.getGroupProperty()));
		}

		target.setSearchQueryProperties(Converters.convertAll(source.getSearchQueryProperties(), solrSearchQueryPropertyConverter)
				.stream().collect(Collectors.toMap(SearchQueryProperty::getIndexedProperty, Function.identity())));

		target.setSearchQuerySorts(Converters.convertAll(source.getSearchQuerySorts(), solrSearchQuerySortConverter));
	}

	@Required
	public void setIndexedPropertyConverter(final Converter<SolrIndexedPropertyModel, IndexedProperty> indexedPropertyConverter)
	{
		this.indexedPropertyConverter = indexedPropertyConverter;
	}

	@Required
	public void setSolrSearchQueryPropertyConverter(
			final Converter<SolrSearchQueryPropertyModel, SearchQueryProperty> solrSearchQueryPropertyConverter)
	{
		this.solrSearchQueryPropertyConverter = solrSearchQueryPropertyConverter;
	}

	@Required
	public void setSolrSearchQuerySortConverter(
			final Converter<SolrSearchQuerySortModel, SearchQuerySort> solrSearchQuerySortConverter)
	{
		this.solrSearchQuerySortConverter = solrSearchQuerySortConverter;
	}
}
