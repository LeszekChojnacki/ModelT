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
package de.hybris.platform.adaptivesearchbackoffice.widgets.searchcontext;


import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.NAVIGATION_CONTEXT_SOCKET;
import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.SEARCH_CONTEXT_SOCKET;

import de.hybris.platform.adaptivesearch.strategies.AsSearchProvider;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProviderFactory;
import de.hybris.platform.adaptivesearchbackoffice.data.NavigationContextData;
import de.hybris.platform.adaptivesearchbackoffice.data.SearchContextData;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ListModelList;

import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.annotations.ViewEvent;
import com.hybris.cockpitng.i18n.CockpitLocaleService;
import com.hybris.cockpitng.labels.LabelService;
import com.hybris.cockpitng.util.DefaultWidgetController;


/**
 * Controller for the search context widget.
 */
public class SearchContextController extends DefaultWidgetController
{
	protected static final String LANGUAGE_SELECTOR_ID = "languageSelector";
	protected static final String CURRENCY_SELECTOR_ID = "currencySelector";

	protected static final String ON_VALUE_CHANGED = "onValueChanged";

	protected static final String NAVIGATION_CONTEXT_KEY = "navigationContext";
	protected static final String SEARCH_CONTEXT_KEY = "searchContext";

	@WireVariable
	protected transient SessionService sessionService;

	protected transient I18NService i18nService;

	@WireVariable
	protected transient AsSearchProviderFactory asSearchProviderFactory;

	@WireVariable
	protected transient CockpitLocaleService cockpitLocaleService;

	@WireVariable
	protected transient LabelService labelService;

	protected Combobox languageSelector;
	protected Combobox currencySelector;

	private final ListModelList<LanguageModel> languagesModel = new ListModelList<>();
	private final ListModelList<CurrencyModel> currenciesModel = new ListModelList<>();

	public ListModelList<LanguageModel> getLanguagesModel()
	{
		return languagesModel;
	}

	public ListModelList<CurrencyModel> getCurrenciesModel()
	{
		return currenciesModel;
	}

	public NavigationContextData getNavigationContext()
	{
		return getModel().getValue(NAVIGATION_CONTEXT_KEY, NavigationContextData.class);
	}

	public void setNavigationContext(final NavigationContextData navigationContext)
	{
		getModel().put(NAVIGATION_CONTEXT_KEY, navigationContext);
	}

	public SearchContextData getSearchContext()
	{
		return getModel().getValue(SEARCH_CONTEXT_KEY, SearchContextData.class);
	}

	public void setSearchContext(final SearchContextData searchContext)
	{
		getModel().put(SEARCH_CONTEXT_KEY, searchContext);
	}

	@SocketEvent(socketId = NAVIGATION_CONTEXT_SOCKET)
	public void updateSearchContext(final NavigationContextData navigationContextData)
	{
		setNavigationContext(navigationContextData);
		final SearchContextData searchContext = getSearchContext();
		updateSelectors(searchContext);
		sendSearchContext(searchContext);
	}

	@Override
	public void initialize(final Component component)
	{
		initializeSelectors();

		component.addEventListener(Events.ON_CREATE, event -> {
			final SearchContextData searchContext = getSearchContext();
			updateSelectors(searchContext);
			sendSearchContext(searchContext);
		});
	}

	protected void initializeSelectors()
	{
		final SearchContextData searchContext = new SearchContextData();
		searchContext.setCurrency(null);
		searchContext.setLanguage(null);

		setSearchContext(searchContext);

		languageSelector.setModel(languagesModel);
		currencySelector.setModel(currenciesModel);
	}

	protected void updateSelectors(final SearchContextData searchContext)
	{
		sessionService.executeInLocalView(new SessionExecutionBody()
		{
			@Override
			public void executeWithoutResult()
			{
				i18nService.setLocalizationFallbackEnabled(true);

				updateLanguages(searchContext);
				updateCurrencies(searchContext);
			}
		});
	}

	protected void sendSearchContext(final SearchContextData searchContext)
	{
		if (searchContext != null)
		{
			// creates a clone to prevent modifications outside the widget
			final SearchContextData clonedSearchContext = new SearchContextData();
			clonedSearchContext.setLanguage(searchContext.getLanguage());
			clonedSearchContext.setCurrency(searchContext.getCurrency());

			sendOutput(SEARCH_CONTEXT_SOCKET, clonedSearchContext);
		}
		else
		{
			sendOutput(SEARCH_CONTEXT_SOCKET, null);
		}
	}

	protected void updateLanguages(final SearchContextData searchContext)
	{
		final NavigationContextData navigationContext = getNavigationContext();
		final List<LanguageModel> languages = findLanguages(navigationContext);

		if (!CollectionUtils.isEqualCollection(languagesModel.getInnerList(), languages))
		{
			languagesModel.clear();
			languagesModel.addAll(languages);
		}

		if (CollectionUtils.isEmpty(languagesModel))
		{
			searchContext.setLanguage(null);
			languagesModel.setSelection(Collections.emptyList());
			languageSelector.setDisabled(true);
		}
		else
		{
			if (languagesModel.isSelectionEmpty())
			{
				final LanguageModel selected = getSessionLanguage() == null ? languagesModel.get(0) : getSessionLanguage();
				languagesModel.setSelection(Collections.singletonList(selected));
				searchContext.setLanguage(selected.getIsoCode());
			}

			languageSelector.setDisabled(false);
		}
	}

	protected void updateCurrencies(final SearchContextData searchContext)
	{
		final NavigationContextData navigationContext = getNavigationContext();
		final List<CurrencyModel> currencies = findCurrencies(navigationContext);

		if (!CollectionUtils.isEqualCollection(currenciesModel.getInnerList(), currencies))
		{
			currenciesModel.clear();
			currenciesModel.addAll(currencies);
		}

		if (CollectionUtils.isEmpty(currenciesModel))
		{
			searchContext.setCurrency(null);
			currenciesModel.setSelection(Collections.emptyList());
			currencySelector.setDisabled(true);
		}
		else
		{
			if (currenciesModel.isSelectionEmpty())
			{
				final CurrencyModel selected = getSessionCurrency() == null ? currenciesModel.get(0) : getSessionCurrency();
				currenciesModel.setSelection(Collections.singletonList(selected));
				searchContext.setCurrency(selected.getIsoCode());
			}

			currencySelector.setDisabled(false);
		}
	}

	/**
	 * Event handler for language changes.
	 *
	 * @param event
	 *           - the event
	 */
	@ViewEvent(componentID = LANGUAGE_SELECTOR_ID, eventName = Events.ON_SELECT)
	public void onLanguageChanged(final SelectEvent<Comboitem, String> event)
	{
		final SearchContextData searchContext = getSearchContext();
		final String selectedLanguageIso = event.getReference().getValue();

		if (searchContext != null && !Objects.equals(searchContext.getLanguage(), selectedLanguageIso))
		{
			searchContext.setLanguage(selectedLanguageIso);
			updateLanguages(searchContext);
			sendSearchContext(searchContext);
		}
	}

	/**
	 * Event handler for currency changes.
	 *
	 * @param event
	 *           - the event
	 */
	@ViewEvent(componentID = CURRENCY_SELECTOR_ID, eventName = Events.ON_SELECT)
	public void onCurrencyChanged(final SelectEvent<Comboitem, String> event)
	{
		final SearchContextData searchContext = getSearchContext();
		final String selectedCurrencyIso = event.getReference().getValue();

		if (searchContext != null && !Objects.equals(searchContext.getCurrency(), selectedCurrencyIso))
		{
			searchContext.setCurrency(selectedCurrencyIso);
			updateCurrencies(searchContext);
			sendSearchContext(searchContext);
		}
	}

	protected List<LanguageModel> findLanguages(final NavigationContextData navigationContext)
	{
		if (navigationContext == null || navigationContext.getIndexConfiguration() == null
				|| navigationContext.getIndexType() == null)
		{
			return Collections.emptyList();
		}

		final AsSearchProvider searchProvider = asSearchProviderFactory.getSearchProvider();
		final List<de.hybris.platform.core.model.c2l.LanguageModel> languages = searchProvider
				.getSupportedLanguages(navigationContext.getIndexConfiguration(), navigationContext.getIndexType());

		return languages.stream().filter(this::isValidLanguage).map(this::convertLanguage).sorted(this::compareLanguages)
				.collect(Collectors.toList());
	}

	protected boolean isValidLanguage(final de.hybris.platform.core.model.c2l.LanguageModel language)
	{
		return language != null && StringUtils.isNotBlank(language.getIsocode());
	}

	protected LanguageModel convertLanguage(final de.hybris.platform.core.model.c2l.LanguageModel source)
	{
		final LanguageModel target = new LanguageModel();
		target.setName(labelService.getObjectLabel(source));
		target.setIsoCode(source.getIsocode());

		return target;
	}

	protected int compareLanguages(final LanguageModel language1, final LanguageModel language2)
	{
		return language1.getName().compareTo(language2.getName());
	}

	protected List<CurrencyModel> findCurrencies(final NavigationContextData navigationContext)
	{
		if (navigationContext == null || navigationContext.getIndexConfiguration() == null
				|| navigationContext.getIndexType() == null)
		{
			return Collections.emptyList();
		}

		final AsSearchProvider searchProvider = asSearchProviderFactory.getSearchProvider();
		final List<de.hybris.platform.core.model.c2l.CurrencyModel> currencies = searchProvider
				.getSupportedCurrencies(navigationContext.getIndexConfiguration(), navigationContext.getIndexType());

		return currencies.stream().filter(this::isValidCurrency).map(this::convertCurrency).sorted(this::compareCurrencies)
				.collect(Collectors.toList());
	}

	protected boolean isValidCurrency(final de.hybris.platform.core.model.c2l.CurrencyModel currency)
	{
		return currency != null && StringUtils.isNotBlank(currency.getIsocode());
	}

	protected CurrencyModel convertCurrency(final de.hybris.platform.core.model.c2l.CurrencyModel source)
	{
		final CurrencyModel target = new CurrencyModel();
		target.setName(labelService.getObjectLabel(source));
		target.setIsoCode(source.getIsocode());

		return target;
	}

	protected int compareCurrencies(final CurrencyModel currency1, final CurrencyModel currency2)
	{
		return currency1.getName().compareTo(currency2.getName());
	}

	protected LanguageModel getSessionLanguage()
	{
		final Locale locale = cockpitLocaleService.getCurrentLocale();
		final List<LanguageModel> matchingLanguageModels = languagesModel.stream()
				.filter(language -> language.getIsoCode().equals(locale.getLanguage())).collect(Collectors.toList());
		if (CollectionUtils.isEmpty(matchingLanguageModels))
		{
			return null;
		}
		return matchingLanguageModels.get(0);
	}

	protected CurrencyModel getSessionCurrency()
	{
		final Locale locale = cockpitLocaleService.getCurrentLocale();
		final List<Locale> countriesByLanguage = LocaleUtils.countriesByLanguage(locale.getLanguage());

		final List<CurrencyModel> matchingCurrencyModels = new ArrayList<>();
		for (final Locale currencyForLanguage : countriesByLanguage)
		{
			final Currency currency = Currency.getInstance(currencyForLanguage);
			for (final CurrencyModel currencyModel : currenciesModel.getInnerList())
			{
				if (currencyModel.getIsoCode().equals(currency.getCurrencyCode()))
				{
					matchingCurrencyModels.add(currencyModel);
				}
			}
		}

		if (CollectionUtils.isEmpty(matchingCurrencyModels))
		{
			return null;
		}

		return matchingCurrencyModels.get(0);
	}
}
