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

import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;


public class DefaultBackofficeFieldNamePostProcessor implements FieldNamePostProcessor
{

	protected static final String FROM_REGEXP_TEMPLATE = "\\%s$";
	protected static final String LANGUAGE_PREFIX = "_";
	protected static final String REPLACABLE_LOCALE_REGEXP_TEMPLATE = "([^:_]*):";
	protected static final String QUERY_VALUE_SEPARATOR = ":";
	private I18NService i18nService;
	private CommonI18NService commonI18NService;

	@Override
	public String process(final SearchQuery searchQuery, final Locale conditionLocale, final String fieldName)
	{
		if (conditionLocale != null)
		{
			final LanguageModel conditionLanguage = retrieveLanguageModel(conditionLocale);
			if (conditionLanguage != null)
			{
				final String searchQueryIsocode = searchQuery.getLanguage();
				final String conditionIsocode = conditionLanguage.getIsocode().toLowerCase(Locale.ENGLISH);

				final String replacedValue = LANGUAGE_PREFIX.concat(searchQueryIsocode);
				if (isProcessableFieldWithoutValue(conditionIsocode, searchQueryIsocode, fieldName, replacedValue))
				{
					final String to = LANGUAGE_PREFIX.concat(conditionIsocode);
					return fieldName.replaceFirst(String.format(FROM_REGEXP_TEMPLATE, replacedValue), to);
				} else if (isProcessableFieldWithValue(conditionIsocode, searchQueryIsocode, fieldName)) {
					final String replacement = conditionLanguage.getIsocode().concat(QUERY_VALUE_SEPARATOR);
					return fieldName.replaceFirst(REPLACABLE_LOCALE_REGEXP_TEMPLATE, replacement);
				}
			}
		}
		return fieldName;
	}

	private boolean isProcessableFieldWithoutValue(final String conditionIsocode, final String searchQueryIsocode, final String fieldName, final String replacedValue){
		return !StringUtils.equalsIgnoreCase(conditionIsocode, searchQueryIsocode)
				&& fieldName.endsWith(replacedValue);
	}

	private boolean isProcessableFieldWithValue(final String conditionIsocode, final String searchQueryIsocode, final String fieldName){
		final String from = LANGUAGE_PREFIX.concat(searchQueryIsocode);
		return !StringUtils.equalsIgnoreCase(conditionIsocode, searchQueryIsocode)
				&& !fieldName.endsWith(from);
	}

	protected LanguageModel retrieveLanguageModel(final Locale locale)
	{
		try
		{
			return commonI18NService.getLanguage(i18nService.getBestMatchingLocale(locale).toString());
		}
		catch (final UnknownIdentifierException e)
		{
			return commonI18NService.getLanguage(locale.toString());
		}
	}

	@Required
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}

	@Required
	public void setI18nService(final I18NService i18nService)
	{
		this.i18nService = i18nService;
	}
}
