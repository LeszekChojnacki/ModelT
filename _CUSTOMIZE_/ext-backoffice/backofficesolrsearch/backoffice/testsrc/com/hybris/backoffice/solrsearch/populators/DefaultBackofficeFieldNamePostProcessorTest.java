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

import static com.hybris.backoffice.solrsearch.populators.DefaultBackofficeFieldNamePostProcessor.QUERY_VALUE_SEPARATOR;
import static com.hybris.backoffice.solrsearch.populators.DefaultBackofficeFieldNamePostProcessor.REPLACABLE_LOCALE_REGEXP_TEMPLATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;


@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultBackofficeFieldNamePostProcessorTest
{

	protected static final String ISOCODE_DE = "de";
	protected static final String ISOCODE_EN = "en";
	protected static final String ISOCODE_ES_CO = "es_CO";

	protected static final String VALUE = "*dededede*";

	protected static final String ENGLISH_FIELD_NAME_WITHOUT_VALUE = "name_text_en";
	protected static final String ENGLISH_FIELD_NAME_WITH_VALUE = ENGLISH_FIELD_NAME_WITHOUT_VALUE.concat(QUERY_VALUE_SEPARATOR)
			.concat(VALUE);

	protected static final String GERMAN_FIELD_NAME_WITHOUT_VALUE = "name_text_de";
	protected static final String GERMAN_FIELD_NAME_WITH_VALUE = GERMAN_FIELD_NAME_WITHOUT_VALUE.concat(QUERY_VALUE_SEPARATOR)
			.concat(VALUE);

	protected static final String SPANISH_FIELD_NAME_WITHOUT_VALUE = "name_text_es_CO";

	@Spy
	private DefaultBackofficeFieldNamePostProcessor namePostProcessor;
	@Mock
	private SearchQuery searchQuery;
	@Mock
	private I18NService i18nService;
	@Mock
	private CommonI18NService commonI18NService;

	@Mock
	private LanguageModel conditionLanguage;

	@Before
	public void setup()
	{
		namePostProcessor.setCommonI18NService(commonI18NService);
		namePostProcessor.setI18nService(i18nService);
		when(i18nService.getBestMatchingLocale(any(Locale.class))).thenReturn(Locale.GERMAN);
	}

	@Test
	public void shouldReplaceOnlyIsoCode()
	{
		final String replacement = ISOCODE_DE.concat(QUERY_VALUE_SEPARATOR);
		final String result = ENGLISH_FIELD_NAME_WITH_VALUE.replaceFirst(REPLACABLE_LOCALE_REGEXP_TEMPLATE, replacement);
		assertThat(result).isEqualTo(GERMAN_FIELD_NAME_WITH_VALUE);
	}

	@Test
	public void shouldProcessFieldLocaleWithValue()
	{
		// given
		when(namePostProcessor.retrieveLanguageModel(any(Locale.class))).thenReturn(conditionLanguage);
		when(searchQuery.getLanguage()).thenReturn(ISOCODE_EN);
		when(conditionLanguage.getIsocode()).thenReturn(ISOCODE_DE);

		// when
		final String result = namePostProcessor.process(searchQuery, Locale.GERMAN, ENGLISH_FIELD_NAME_WITH_VALUE);

		// then
		assertThat(result).isEqualTo(GERMAN_FIELD_NAME_WITH_VALUE);
	}

	@Test
	public void shouldProcessFieldLocaleWithoutValue()
	{
		// given
		when(namePostProcessor.retrieveLanguageModel(any(Locale.class))).thenReturn(conditionLanguage);
		when(conditionLanguage.getIsocode()).thenReturn(ISOCODE_DE);
		when(searchQuery.getLanguage()).thenReturn(ISOCODE_EN);

		// when
		final String result = namePostProcessor.process(searchQuery, Locale.ENGLISH, ENGLISH_FIELD_NAME_WITHOUT_VALUE);

		// then
		assertThat(result).isEqualTo(GERMAN_FIELD_NAME_WITHOUT_VALUE);
	}

	@Test
	public void shouldProcessFieldLocaleWithRegion()
	{
		// given
		when(namePostProcessor.retrieveLanguageModel(any(Locale.class))).thenReturn(conditionLanguage);
		when(searchQuery.getLanguage()).thenReturn(ISOCODE_EN);
		when(conditionLanguage.getIsocode()).thenReturn(ISOCODE_ES_CO);

		// when
		final String result = namePostProcessor.process(searchQuery, Locale.GERMAN, SPANISH_FIELD_NAME_WITHOUT_VALUE);

		// then
		assertThat(result).isEqualTo(SPANISH_FIELD_NAME_WITHOUT_VALUE);
	}

}
