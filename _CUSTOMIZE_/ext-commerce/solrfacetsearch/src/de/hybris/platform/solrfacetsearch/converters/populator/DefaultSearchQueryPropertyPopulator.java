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
import de.hybris.platform.solrfacetsearch.config.FacetType;
import de.hybris.platform.solrfacetsearch.config.SearchQueryProperty;
import de.hybris.platform.solrfacetsearch.config.WildcardType;
import de.hybris.platform.solrfacetsearch.model.SolrSearchQueryPropertyModel;

import java.util.Locale;

import org.apache.commons.lang.BooleanUtils;


public class DefaultSearchQueryPropertyPopulator implements Populator<SolrSearchQueryPropertyModel, SearchQueryProperty>
{
	@Override
	public void populate(final SolrSearchQueryPropertyModel source, final SearchQueryProperty target)
	{
		target.setIndexedProperty(source.getIndexedProperty().getName());
		target.setFacet(source.isFacet());
		target.setFacetType(source.getFacetType() == null ? FacetType.REFINE
				: FacetType.valueOf(source.getFacetType().getCode().toUpperCase(Locale.ROOT)));
		target.setPriority(source.getPriority());
		target.setIncludeInResponse(source.isIncludeInResponse());
		target.setHighlight(BooleanUtils.toBoolean(source.getUseForHighlighting()));
		target.setFacetDisplayNameProvider(source.getFacetDisplayNameProvider());
		target.setFacetSortProvider(source.getFacetSortProvider());
		target.setFacetTopValuesProvider(source.getFacetTopValuesProvider());
		target.setFtsQuery(source.isFtsQuery());
		target.setFtsQueryMinTermLength(source.getFtsQueryMinTermLength());
		target.setFtsQueryBoost(source.getFtsQueryBoost());
		target.setFtsFuzzyQuery(source.isFtsFuzzyQuery());
		target.setFtsFuzzyQueryMinTermLength(source.getFtsFuzzyQueryMinTermLength());
		target.setFtsFuzzyQueryFuzziness(source.getFtsFuzzyQueryFuzziness());
		target.setFtsFuzzyQueryBoost(source.getFtsFuzzyQueryBoost());
		target.setFtsWildcardQuery(source.isFtsWildcardQuery());
		target.setFtsWildcardQueryMinTermLength(source.getFtsWildcardQueryMinTermLength());
		target.setFtsWildcardQueryType(source.getFtsWildcardQueryType() == null ? null
				: WildcardType.valueOf(source.getFtsWildcardQueryType().getCode().toUpperCase(Locale.ROOT)));
		target.setFtsWildcardQueryBoost(source.getFtsWildcardQueryBoost());
		target.setFtsPhraseQuery(source.isFtsPhraseQuery());
		target.setFtsPhraseQuerySlop(source.getFtsPhraseQuerySlop());
		target.setFtsPhraseQueryBoost(source.getFtsPhraseQueryBoost());
	}
}
