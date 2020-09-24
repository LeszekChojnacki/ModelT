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
import de.hybris.platform.solrfacetsearch.config.FacetType;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.ValueRangeSet;
import de.hybris.platform.solrfacetsearch.config.WildcardType;
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexedPropertyModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrValueRangeSetModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Populates {@link IndexedProperty} instance based on the {@link SolrIndexedPropertyModel}.
 */
public class DefaultIndexedPropertyPopulator implements Populator<SolrIndexedPropertyModel, IndexedProperty>
{
	private Converter<SolrValueRangeSetModel, ValueRangeSet> valueRangeSetConverter;

	@Override
	public void populate(final SolrIndexedPropertyModel source, final IndexedProperty target)
	{
		target.setAutoSuggest(BooleanUtils.toBoolean(source.getUseForAutocomplete()));
		target.setCurrency(source.isCurrency());
		target.setExportId(StringUtils.isBlank(source.getExportId()) ? source.getName() : source.getExportId());
		target.setFacet(source.isFacet());
		target.setFacetDisplayNameProvider(source.getFacetDisplayNameProvider());
		target.setFacetSortProvider(source.getCustomFacetSortProvider());
		target.setTopValuesProvider(source.getTopValuesProvider());
		target.setFacetType(source.getFacetType() == null ? FacetType.REFINE
				: FacetType.valueOf(source.getFacetType().getCode().toUpperCase(Locale.ROOT)));
		target.setFieldValueProvider(source.getFieldValueProvider());
		target.setLocalized(source.isLocalized());
		target.setMultiValue(source.isMultiValue());
		target.setName(source.getName());
		target.setDisplayName(source.getDisplayName());
		target.setPriority(source.getPriority());
		target.setIncludeInResponse(source.isIncludeInResponse());
		target.setSortableType(source.getSortableType() == null ? null : source.getSortableType().getCode());
		target.setSpellCheck(BooleanUtils.toBoolean(source.getUseForSpellchecking()));
		target.setHighlight(BooleanUtils.toBoolean(source.getUseForHighlighting()));
		target.setType(source.getType().getCode());
		target.setValueProviderParameter(source.getValueProviderParameter());
		target.setValueProviderParameters(
				Collections.unmodifiableMap(new HashMap<String, String>(source.getValueProviderParameters())));
		target.setValueRangeSets(convertRangeSet(source));
		target.setFtsQuery(source.isFtsQuery());
		target.setFtsQueryMinTermLength(source.getFtsQueryMinTermLength());
		target.setFtsQueryBoost(source.getFtsQueryBoost());
		target.setFtsFuzzyQuery(source.isFtsFuzzyQuery());
		target.setFtsFuzzyQueryMinTermLength(source.getFtsFuzzyQueryMinTermLength());
		target.setFtsFuzzyQueryFuzziness(source.getFtsFuzzyQueryFuzziness());
		target.setFtsFuzzyQueryBoost(source.getFtsFuzzyQueryBoost());
		target.setFtsWildcardQuery(source.isFtsWildcardQuery());
		target.setFtsWildcardQueryMinTermLength(source.getFtsWildcardQueryMinTermLength());
		target.setFtsWildcardQueryType(
				source.getFtsWildcardQueryType() == null ? null : WildcardType.valueOf(source.getFtsWildcardQueryType().getCode()));
		target.setFtsWildcardQueryBoost(source.getFtsWildcardQueryBoost());
		target.setFtsPhraseQuery(source.isFtsPhraseQuery());
		target.setFtsPhraseQuerySlop(source.getFtsPhraseQuerySlop());
		target.setFtsPhraseQueryBoost(source.getFtsPhraseQueryBoost());
	}

	protected Map<String, ValueRangeSet> convertRangeSet(final SolrIndexedPropertyModel source)
	{
		List<ValueRangeSet> convertAll = Converters.convertAll(source.getRangeSets(), valueRangeSetConverter);
		if (source.getRangeSet() != null)
		{
			convertAll = new ArrayList<>(convertAll);
			convertAll.add(valueRangeSetConverter.convert(source.getRangeSet()));
		}
		final Map<String, ValueRangeSet> valueRangeSets = new HashMap<>();
		for (final ValueRangeSet valueRangeSet : convertAll)
		{
			valueRangeSets.put(valueRangeSet.getQualifier(), valueRangeSet);
		}
		return valueRangeSets;
	}

	@Required
	public void setValueRangeSetConverter(final Converter<SolrValueRangeSetModel, ValueRangeSet> valueRangeSetConverter)
	{
		this.valueRangeSetConverter = valueRangeSetConverter;
	}

}
