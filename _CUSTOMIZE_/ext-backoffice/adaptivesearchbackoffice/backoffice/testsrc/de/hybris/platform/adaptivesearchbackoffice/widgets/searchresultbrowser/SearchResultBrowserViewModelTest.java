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
package de.hybris.platform.adaptivesearchbackoffice.widgets.searchresultbrowser;


import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.SEARCH_RESULT_SOCKET;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.adaptivesearch.model.AbstractAsConfigurableSearchConfigurationModel;
import de.hybris.platform.adaptivesearch.services.AsConfigurationService;
import de.hybris.platform.adaptivesearchbackoffice.data.NavigationContextData;
import de.hybris.platform.adaptivesearchbackoffice.data.SearchContextData;
import de.hybris.platform.adaptivesearchbackoffice.data.SearchResultData;
import de.hybris.platform.adaptivesearchbackoffice.facades.AsSearchConfigurationFacade;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;

import java.util.Currency;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.hybris.cockpitng.testing.AbstractWidgetUnitTest;
import com.hybris.cockpitng.testing.annotation.DeclaredBindingParam;
import com.hybris.cockpitng.testing.annotation.DeclaredCommand;
import com.hybris.cockpitng.testing.annotation.DeclaredCommands;
import com.hybris.cockpitng.testing.annotation.DeclaredInput;
import com.hybris.cockpitng.testing.annotation.NullSafeWidget;


@NullSafeWidget(true)
@DeclaredInput(value = SEARCH_RESULT_SOCKET, socketType = SearchResultData.class)
@DeclaredCommands(
{ @DeclaredCommand(value = "changePage", params =
		{ @DeclaredBindingParam(qualifier = "activePage", type = int.class),
				@DeclaredBindingParam(qualifier = "pageSize", type = int.class) }),
		@DeclaredCommand(value = "changeSort", params =
		{ @DeclaredBindingParam(qualifier = "sort", type = String.class) }), @DeclaredCommand(value = "dropPromotedItem", params =
		{ @DeclaredBindingParam(qualifier = "draggedResult", type = DocumentModel.class),
				@DeclaredBindingParam(qualifier = "targetResult", type = DocumentModel.class) }) })
@UnitTest
public class SearchResultBrowserViewModelTest extends AbstractWidgetUnitTest<SearchResultBrowserViewModel>
{
	@Mock
	private CommonI18NService commonI18NService;

	@Mock
	private AsConfigurationService asConfigurationService;

	@Mock
	private AsSearchConfigurationFacade asSearchConfigurationFacade;

	@Mock
	private LanguageModel language;

	@Mock
	private CurrencyModel currency;

	@Mock
	private AbstractAsConfigurableSearchConfigurationModel searchConfiguration;

	private final SearchResultBrowserViewModel searchResultBrowserViewModel = new SearchResultBrowserViewModel();

	@Override
	protected SearchResultBrowserViewModel getWidgetController()
	{
		return searchResultBrowserViewModel;
	}

	@Before
	public void setUp()
	{
		searchResultBrowserViewModel.commonI18NService = commonI18NService;
		searchResultBrowserViewModel.asConfigurationService = asConfigurationService;
		searchResultBrowserViewModel.asSearchConfigurationFacade = asSearchConfigurationFacade;

		final NavigationContextData navigationContext = new NavigationContextData();
		final SearchContextData searchContext = new SearchContextData();

		final SearchResultData searchResult = new SearchResultData();
		searchResult.setNavigationContext(navigationContext);
		searchResult.setSearchContext(searchContext);

		searchResultBrowserViewModel.setSearchResult(searchResult);

		when(asSearchConfigurationFacade.getOrCreateSearchConfiguration(navigationContext, searchContext))
				.thenReturn(searchConfiguration);
	}

	@Test
	public void formatCurrencyNullValue()
	{
		// when
		final String formattedValue = searchResultBrowserViewModel.formatCurrency(null);

		// then
		assertEquals(StringUtils.EMPTY, formattedValue);
	}

	@Test
	public void formatCurrency0Value()
	{
		// given
		final Double value = Double.valueOf(0d);

		final Locale locale = Locale.US;
		final String languageCode = locale.toString();
		final String currencyCode = Currency.getInstance(locale).toString();

		final SearchContextData searchContext = searchResultBrowserViewModel.getSearchContext();
		searchContext.setLanguage(languageCode);
		searchContext.setCurrency(currencyCode);

		when(language.getIsocode()).thenReturn(languageCode);
		when(currency.getIsocode()).thenReturn(currencyCode);
		when(currency.getDigits()).thenReturn(Integer.valueOf(2));

		when(commonI18NService.getLocaleForIsoCode(languageCode)).thenReturn(locale);
		when(commonI18NService.getLanguage(languageCode)).thenReturn(language);
		when(commonI18NService.getCurrency(currencyCode)).thenReturn(currency);

		// when
		final String formattedValue = searchResultBrowserViewModel.formatCurrency(value);

		// then
		assertEquals("$0.00", formattedValue);
	}

	@Test
	public void formatCurrency()
	{
		// given
		final Double value = Double.valueOf(1.23d);

		final Locale locale = Locale.US;
		final String languageCode = locale.toString();
		final String currencyCode = Currency.getInstance(locale).toString();

		final SearchContextData searchContext = searchResultBrowserViewModel.getSearchContext();
		searchContext.setLanguage(languageCode);
		searchContext.setCurrency(currencyCode);

		when(language.getIsocode()).thenReturn(languageCode);
		when(currency.getIsocode()).thenReturn(currencyCode);
		when(currency.getDigits()).thenReturn(Integer.valueOf(2));

		when(commonI18NService.getLocaleForIsoCode(languageCode)).thenReturn(locale);
		when(commonI18NService.getLanguage(languageCode)).thenReturn(language);
		when(commonI18NService.getCurrency(currencyCode)).thenReturn(currency);

		// when
		final String formattedValue = searchResultBrowserViewModel.formatCurrency(value);

		// then
		assertEquals("$1.23", formattedValue);
	}
}
