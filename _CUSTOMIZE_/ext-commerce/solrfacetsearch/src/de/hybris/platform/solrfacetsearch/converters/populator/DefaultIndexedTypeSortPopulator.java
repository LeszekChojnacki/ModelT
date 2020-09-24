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
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.solrfacetsearch.config.IndexedTypeSort;
import de.hybris.platform.solrfacetsearch.config.IndexedTypeSortField;
import de.hybris.platform.solrfacetsearch.model.SolrSortFieldModel;
import de.hybris.platform.solrfacetsearch.model.SolrSortModel;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;


public class DefaultIndexedTypeSortPopulator implements Populator<SolrSortModel, IndexedTypeSort>
{
	private I18NService i18NService;

	private Converter<SolrSortFieldModel, IndexedTypeSortField> indexedTypeSortFieldConverter;

	@Override
	public void populate(final SolrSortModel source, final IndexedTypeSort target)
	{
		target.setSort(source);
		target.setCode(source.getCode());
		target.setName(source.getName());
		target.setLocalizedName(buildNameLocalizationMap(source));
		target.setApplyPromotedItems(source.isUseBoost());
		target.setFields(indexedTypeSortFieldConverter.convertAll(source.getFields()));
	}

	protected Map<String, String> buildNameLocalizationMap(final SolrSortModel source)
	{
		final Set<Locale> supportedLocales = i18NService.getSupportedLocales();

		return supportedLocales.stream().filter(locale -> StringUtils.isNotBlank(source.getName(locale)))
				.collect(Collectors.toMap(Locale::toString, locale -> source.getName(locale)));
	}

	public Converter<SolrSortFieldModel, IndexedTypeSortField> getIndexedTypeSortFieldConverter()
	{
		return indexedTypeSortFieldConverter;
	}

	@Required
	public void setIndexedTypeSortFieldConverter(
			final Converter<SolrSortFieldModel, IndexedTypeSortField> indexedTypeSortFieldConverter)
	{
		this.indexedTypeSortFieldConverter = indexedTypeSortFieldConverter;
	}

	public I18NService getI18NService()
	{
		return i18NService;
	}

	@Required
	public void setI18NService(final I18NService i18NService)
	{
		this.i18NService = i18NService;
	}
}
