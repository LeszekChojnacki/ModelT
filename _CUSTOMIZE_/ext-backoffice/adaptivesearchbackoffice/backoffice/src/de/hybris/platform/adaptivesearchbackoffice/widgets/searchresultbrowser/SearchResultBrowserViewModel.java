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


import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.FROM_SEARCH_CONFIGURATION_SCLASS;
import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.FROM_SEARCH_PROFILE_SCLASS;
import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.OVERRIDE_FROM_SEARCH_PROFILE_SCLASS;
import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.OVERRIDE_SCLASS;
import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.SEARCH_REQUEST_SOCKET;
import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.SEARCH_RESULT_SOCKET;

import de.hybris.platform.adaptivesearch.data.AbstractAsBoostItemConfiguration;
import de.hybris.platform.adaptivesearch.data.AbstractAsItemConfiguration;
import de.hybris.platform.adaptivesearch.data.AsConfigurationHolder;
import de.hybris.platform.adaptivesearch.data.AsDocumentData;
import de.hybris.platform.adaptivesearch.data.AsPromotedItem;
import de.hybris.platform.adaptivesearch.data.AsSearchProfileResult;
import de.hybris.platform.adaptivesearch.data.AsSearchResultData;
import de.hybris.platform.adaptivesearch.data.AsSortData;
import de.hybris.platform.adaptivesearch.model.AbstractAsConfigurableSearchConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchConfigurationModel;
import de.hybris.platform.adaptivesearch.services.AsConfigurationService;
import de.hybris.platform.adaptivesearchbackoffice.common.HTMLSanitizer;
import de.hybris.platform.adaptivesearchbackoffice.data.NavigationContextData;
import de.hybris.platform.adaptivesearchbackoffice.data.PaginationRequestData;
import de.hybris.platform.adaptivesearchbackoffice.data.SearchContextData;
import de.hybris.platform.adaptivesearchbackoffice.data.SearchResultData;
import de.hybris.platform.adaptivesearchbackoffice.data.SortRequestData;
import de.hybris.platform.adaptivesearchbackoffice.facades.AsSearchConfigurationFacade;
import de.hybris.platform.adaptivesearchbackoffice.widgets.AbstractWidgetViewModel;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.DependsOn;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.ListModelList;

import com.hybris.cockpitng.annotations.SocketEvent;


/**
 * Controller for the search result browser widget.
 */
public class SearchResultBrowserViewModel extends AbstractWidgetViewModel
{
	protected static final int DEFAULT_PAGE_SIZE = 20;

	protected static final String RESULT_SCLASS = "yas-result";
	protected static final String PROMOTED_SCLASS = "yas-promoted";
	protected static final String HIGHLIGHT_SCLASS = "yas-highlighted";
	protected static final String SHOW_ON_TOP_SCLASS = "yas-show-on-top";
	protected static final String IN_SEARCH_RESULT_SCLASS = "yas-in-search-result";

	protected static final String PAGINATION_REQUEST_OUT_SOCKET = "paginationRequest";
	protected static final String REFRESH_SEARCH_OUT_SOCKET = "refreshSearch";

	protected static final String SETTING_PAGE_SIZES = "pageSizes";

	protected static final String SEARCH_RESULT_KEY = "searchResult";

	protected static final String SCORE_FIELD = "score";
	protected static final String PK_FIELD = "pk";

	@WireVariable
	protected CommonI18NService commonI18NService;

	@WireVariable
	protected AsConfigurationService asConfigurationService;

	@WireVariable
	protected AsSearchConfigurationFacade asSearchConfigurationFacade;

	private int activePage;
	private int pageSize;
	private int resultCount;

	private final ListModelList<Integer> pageSizes = new ListModelList<>();
	private final ListModelList<SortModel> sorts = new ListModelList<>();

	private boolean resultActionsEnabled;
	private final ListModelList<DocumentModel> promotedItems = new ListModelList<>();
	private final ListModelList<DocumentModel> defaultResults = new ListModelList<>();

	@DependsOn(SEARCH_RESULT_KEY)
	public int getActivePage()
	{
		return activePage;
	}

	public void setActivePage(final int activePage)
	{
		this.activePage = activePage;
	}

	@DependsOn(SEARCH_RESULT_KEY)
	public int getPageSize()
	{
		return pageSize;
	}

	public void setPageSize(final int pageSize)
	{
		this.pageSize = pageSize;
	}

	@DependsOn(SEARCH_RESULT_KEY)
	public int getResultCount()
	{
		return resultCount;
	}

	public void setResultCount(final int resultCount)
	{
		this.resultCount = resultCount;
	}

	@DependsOn(SEARCH_RESULT_KEY)
	public ListModelList<Integer> getPageSizes()
	{
		return pageSizes;
	}

	@DependsOn(SEARCH_RESULT_KEY)
	public ListModelList<SortModel> getSorts()
	{
		return sorts;
	}

	@DependsOn(SEARCH_RESULT_KEY)
	public boolean isResultActionsEnabled()
	{
		return resultActionsEnabled;
	}

	public void setResultActionsEnabled(final boolean resultActionsEnabled)
	{
		this.resultActionsEnabled = resultActionsEnabled;
	}

	@DependsOn(SEARCH_RESULT_KEY)
	public ListModelList<DocumentModel> getPromotedItems()
	{
		return promotedItems;
	}

	@DependsOn(SEARCH_RESULT_KEY)
	public ListModelList<DocumentModel> getDefaultResults()
	{
		return defaultResults;
	}

	public SearchResultData getSearchResult()
	{
		return getModel().getValue(SEARCH_RESULT_KEY, SearchResultData.class);
	}

	protected void setSearchResult(final SearchResultData searchResult)
	{
		getModel().put(SEARCH_RESULT_KEY, searchResult);
	}

	public NavigationContextData getNavigationContext()
	{
		final SearchResultData searchResult = getSearchResult();

		if (searchResult == null)
		{
			return null;
		}

		return searchResult.getNavigationContext();
	}

	public SearchContextData getSearchContext()
	{
		final SearchResultData searchResult = getSearchResult();

		if (searchResult == null)
		{
			return null;
		}

		return searchResult.getSearchContext();
	}

	@Init
	public void init()
	{
		pageSize = DEFAULT_PAGE_SIZE;
	}

	/**
	 * Event handler for search result changes.
	 *
	 * @param searchResult
	 *           - the search result
	 */
	@SocketEvent(socketId = SEARCH_RESULT_SOCKET)
	public void onSearchResultChanged(final SearchResultData searchResult)
	{
		setSearchResult(searchResult);

		populatePagination(searchResult);
		populatePageSizes(searchResult);
		populateSorts(searchResult);
		populateResults(searchResult);

		BindUtils.postNotifyChange(null, null, this, SEARCH_RESULT_KEY);
	}

	@Command
	public void changePage(@BindingParam("activePage") final int activePage, @BindingParam("pageSize") final int pageSize)
	{
		final PaginationRequestData paginationRequest = new PaginationRequestData();
		paginationRequest.setActivePage(activePage);
		paginationRequest.setPageSize(pageSize);

		sendOutput(PAGINATION_REQUEST_OUT_SOCKET, paginationRequest);
	}

	@Command
	public void changeSort(@BindingParam("sort") final String sort)
	{
		final SortRequestData searchRequest = new SortRequestData();
		searchRequest.setSort(sort);

		sendOutput(SEARCH_REQUEST_SOCKET, searchRequest);
	}

	@Command
	public void dropPromotedItem(@BindingParam("draggedResult") final DocumentModel draggedResult,
			@BindingParam("targetResult") final DocumentModel targetResult)
	{
		if (draggedResult == null || targetResult == null)
		{
			// YTODO show warning
			return;
		}

		final NavigationContextData navigationContext = getNavigationContext();
		final SearchContextData searchContext = getSearchContext();

		if (navigationContext == null || searchContext == null)
		{
			// YTODO show warning
			return;
		}

		final AbstractAsConfigurableSearchConfigurationModel searchConfiguration = asSearchConfigurationFacade
				.getOrCreateSearchConfiguration(navigationContext, searchContext);

		final String targetUid = targetResult.getPromotedItemUid();
		final String uid = draggedResult.getPromotedItemUid();

		if (targetResult.getIndex() < draggedResult.getIndex())
		{
			asConfigurationService.rankBeforeConfiguration(searchConfiguration,
					AbstractAsConfigurableSearchConfigurationModel.PROMOTEDITEMS, targetUid, uid);
		}
		else
		{
			asConfigurationService.rankAfterConfiguration(searchConfiguration,
					AbstractAsConfigurableSearchConfigurationModel.PROMOTEDITEMS, targetUid, uid);
		}

		refreshSearchResults();
	}

	public void refreshSearchResults()
	{
		sendOutput(REFRESH_SEARCH_OUT_SOCKET, null);
	}

	protected void populatePagination(final SearchResultData searchResult)
	{
		if (searchResult == null || searchResult.getAsSearchResult() == null)
		{
			activePage = 0;
			resultCount = 0;
		}
		else
		{
			activePage = searchResult.getAsSearchResult().getActivePage();
			pageSize = searchResult.getAsSearchResult().getPageSize();
			resultCount = searchResult.getAsSearchResult().getResultCount();
		}
	}

	protected void populatePageSizes(final SearchResultData searchResult)
	{
		pageSizes.clear();
		pageSizes.clearSelection();

		if (searchResult == null || searchResult.getAsSearchResult() == null)
		{
			return;
		}

		final String configuredPageSizes = getWidgetSettings().getString(SETTING_PAGE_SIZES);
		if (StringUtils.isNotBlank(configuredPageSizes))
		{
			pageSizes.addAll(Arrays.stream(configuredPageSizes.split(",")).map(Integer::valueOf).collect(Collectors.toList()));
		}

		final AsSearchResultData asSearchResult = searchResult.getAsSearchResult();
		pageSizes.setSelection(Collections.singletonList(Integer.valueOf(asSearchResult.getPageSize())));
	}

	protected void populateSorts(final SearchResultData searchResult)
	{
		sorts.clear();
		sorts.clearSelection();

		if (searchResult == null || searchResult.getAsSearchResult() == null)
		{
			return;
		}

		final AsSearchResultData asSearchResult = searchResult.getAsSearchResult();

		if (asSearchResult.getAvailableSorts() == null)
		{
			return;
		}

		final List<SortModel> availableSorts = asSearchResult.getAvailableSorts().stream().filter(this::isValidSort)
				.map(this::convertSort).collect(Collectors.toList());
		sorts.addAll(availableSorts);

		final String sortCode = asSearchResult.getCurrentSort() != null ? asSearchResult.getCurrentSort().getCode() : null;

		if (StringUtils.isNotBlank(sortCode))
		{
			final Optional<SortModel> selectedSort = availableSorts.stream()
					.filter(sort -> StringUtils.equals(sort.getCode(), sortCode)).findFirst();
			if (selectedSort.isPresent())
			{
				sorts.setSelection(Collections.singletonList(selectedSort.get()));
			}
		}
	}

	protected boolean isValidSort(final AsSortData sort)
	{
		return sort != null && StringUtils.isNotBlank(sort.getCode());
	}

	protected SortModel convertSort(final AsSortData source)
	{
		final String name = StringUtils.isNotBlank(source.getName()) ? source.getName() : source.getCode();

		final SortModel target = new SortModel();
		target.setCode(source.getCode());
		target.setName(name);
		return target;
	}

	protected void populateResults(final SearchResultData searchResult)
	{
		resultActionsEnabled = false;
		promotedItems.clear();
		defaultResults.clear();

		if (!canPopulateResults(searchResult))
		{
			return;
		}

		final AsSearchResultData asSearchResult = searchResult.getAsSearchResult();
		final AsSearchProfileResult searchProfileResult = asSearchResult.getSearchProfileResult();

		final Optional<AbstractAsSearchConfigurationModel> searchConfiguration = resolveSearchConfiguration(searchResult);
		resultActionsEnabled = searchConfiguration.isPresent();

		int index = 0;
		for (final AsDocumentData document : asSearchResult.getResults())
		{
			final Float score = extractScoreFromDocument(document);
			final PK pk = extractPkFromDocument(document);
			final AsConfigurationHolder<AsPromotedItem, AbstractAsBoostItemConfiguration> promotedItemHolder = searchProfileResult
					.getPromotedItems().get(pk);

			final boolean promoted = isPromoted(asSearchResult, promotedItemHolder);
			final String promotedItemUid = promoted ? promotedItemHolder.getConfiguration().getUid() : null;
			final boolean highlighted = isHighlighted(asSearchResult, promotedItemHolder);
			final boolean showOnTop = isShowOnTop(asSearchResult, promotedItemHolder);


			final boolean fromSearchProfile = promoted
					&& isConfigurationFromSearchProfile(promotedItemHolder.getConfiguration(), searchResult.getNavigationContext());
			final boolean fromSearchConfiguration = promoted && searchConfiguration.isPresent()
					&& isConfigurationFromSearchConfiguration(promotedItemHolder.getConfiguration(), searchConfiguration.get());

			final boolean override = fromSearchConfiguration
					&& CollectionUtils.isNotEmpty(promotedItemHolder.getReplacedConfigurations());
			final boolean overrideFromSearchProfile = override && isConfigurationFromSearchProfile(
					promotedItemHolder.getReplacedConfigurations().get(0), searchResult.getNavigationContext());

			final DocumentModel result = new DocumentModel();
			result.setIndex(index);
			result.setScore(score);
			result.setPk(pk);
			result.setDocument(document);
			result.setPromoted(promoted);
			result.setHighlight(highlighted);
			result.setShowOnTop(showOnTop);
			result.setPromotedItemUid(promotedItemUid);
			result.setFromSearchProfile(fromSearchProfile);
			result.setFromSearchConfiguration(fromSearchConfiguration);
			result.setOverride(override);
			result.setOverrideFromSearchProfile(overrideFromSearchProfile);
			result.setStyleClass(buildResultStyleClass(result));

			if (result.isShowOnTop())
			{
				promotedItems.add(result);
			}
			else
			{
				defaultResults.add(result);
			}

			index++;
		}
	}

	protected boolean canPopulateResults(final SearchResultData searchResult)
	{
		if (searchResult == null || searchResult.getNavigationContext() == null || searchResult.getSearchContext() == null)
		{
			return false;
		}

		return searchResult.getAsSearchResult() != null
				&& CollectionUtils.isNotEmpty(searchResult.getAsSearchResult().getResults());
	}

	protected Optional<AbstractAsSearchConfigurationModel> resolveSearchConfiguration(final SearchResultData searchResult)
	{
		final NavigationContextData navigationContext = searchResult.getNavigationContext();
		final SearchContextData searchContext = searchResult.getSearchContext();

		if (navigationContext == null || StringUtils.isBlank(navigationContext.getCurrentSearchProfile()) || searchContext == null)
		{
			return Optional.empty();
		}

		final AbstractAsSearchConfigurationModel searchConfiguration = asSearchConfigurationFacade
				.getOrCreateSearchConfiguration(searchResult.getNavigationContext(), searchResult.getSearchContext());

		if (!(searchConfiguration instanceof AbstractAsConfigurableSearchConfigurationModel))
		{
			return Optional.empty();
		}

		return Optional.of(searchConfiguration);
	}

	protected boolean isHighlighted(final AsSearchResultData asSearchResult,
			final AsConfigurationHolder<AsPromotedItem, AbstractAsBoostItemConfiguration> promotedItemHolder)
	{
		final AsSortData currentSort = asSearchResult.getCurrentSort();
		return currentSort != null && currentSort.isHighlightPromotedItems() && promotedItemHolder != null
				&& promotedItemHolder.getConfiguration() != null;
	}

	protected boolean isShowOnTop(final AsSearchResultData asSearchResult,
			final AsConfigurationHolder<AsPromotedItem, AbstractAsBoostItemConfiguration> promotedItemHolder)
	{
		final AsSortData currentSort = asSearchResult.getCurrentSort();
		return currentSort != null && currentSort.isApplyPromotedItems() && promotedItemHolder != null
				&& promotedItemHolder.getConfiguration() != null;
	}

	protected boolean isPromoted(final AsSearchResultData asSearchResult,
			final AsConfigurationHolder<AsPromotedItem, AbstractAsBoostItemConfiguration> promotedItemHolder)
	{
		return promotedItemHolder != null && promotedItemHolder.getConfiguration() != null;
	}

	protected boolean isConfigurationFromSearchProfile(final AbstractAsItemConfiguration configuration,
			final NavigationContextData navigationContext)
	{
		if (configuration == null || navigationContext == null)
		{
			return false;
		}

		return StringUtils.equals(navigationContext.getCurrentSearchProfile(), configuration.getSearchProfileCode());
	}

	protected boolean isConfigurationFromSearchConfiguration(final AbstractAsItemConfiguration configuration,
			final AbstractAsSearchConfigurationModel searchConfiguration)
	{
		if (configuration == null || searchConfiguration == null)
		{
			return false;
		}

		return StringUtils.equals(searchConfiguration.getUid(), configuration.getSearchConfigurationUid());
	}

	protected Float extractScoreFromDocument(final AsDocumentData document)
	{
		final Object score = document.getFields().get(SCORE_FIELD);
		if (score instanceof Float)
		{
			return (Float) score;
		}

		return null;
	}

	protected PK extractPkFromDocument(final AsDocumentData document)
	{
		final Object pk = document.getFields().get(PK_FIELD);
		if (pk instanceof Long)
		{
			return PK.fromLong(((Long) pk).longValue());
		}

		return null;
	}

	protected String buildResultStyleClass(final DocumentModel document)
	{
		final StringJoiner styleClass = new StringJoiner(" ");
		styleClass.add(RESULT_SCLASS);

		if (document.isPromoted())
		{
			styleClass.add(PROMOTED_SCLASS);
		}

		if (document.isHighlight())
		{
			styleClass.add(HIGHLIGHT_SCLASS);
		}

		if (document.isShowOnTop())
		{
			styleClass.add(SHOW_ON_TOP_SCLASS);
		}

		if (document.isFromSearchProfile())
		{
			styleClass.add(FROM_SEARCH_PROFILE_SCLASS);
		}

		if (document.isFromSearchConfiguration())
		{
			styleClass.add(FROM_SEARCH_CONFIGURATION_SCLASS);
		}

		if (document.isOverride())
		{
			styleClass.add(OVERRIDE_SCLASS);
		}

		if (document.isOverrideFromSearchProfile())
		{
			styleClass.add(OVERRIDE_FROM_SEARCH_PROFILE_SCLASS);
		}

		return styleClass.toString();
	}

	public String sanitizeHtml(final String value)
	{
		if (StringUtils.isBlank(value))
		{
			return value;
		}

		return HTMLSanitizer.sanitizeHTML(value);
	}

	public String formatCurrency(final Number value)
	{
		if (value == null)
		{
			return StringUtils.EMPTY;
		}

		final SearchContextData searchContext = getSearchContext();
		final NumberFormat currencyFormat = createCurrencyFormat(searchContext);
		return currencyFormat.format(value);
	}

	protected NumberFormat createCurrencyFormat(final SearchContextData searchContext)
	{
		final CurrencyModel currency = getCurrency(searchContext);
		final Locale locale = getLocale(searchContext);

		final DecimalFormat currencyFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(locale);
		adjustDigits(currencyFormat, currency);
		adjustSymbol(currencyFormat, currency);
		return currencyFormat;
	}

	protected CurrencyModel getCurrency(final SearchContextData searchContext)
	{
		if (searchContext == null || StringUtils.isBlank(searchContext.getCurrency()))
		{
			return commonI18NService.getCurrentCurrency();
		}

		return commonI18NService.getCurrency(searchContext.getCurrency());
	}

	protected Locale getLocale(final SearchContextData searchContext)
	{
		if (searchContext == null || StringUtils.isBlank(searchContext.getLanguage()))
		{
			return commonI18NService.getLocaleForIsoCode(commonI18NService.getCurrentLanguage().getIsocode());
		}

		return commonI18NService.getLocaleForIsoCode(searchContext.getLanguage());
	}

	protected DecimalFormat adjustDigits(final DecimalFormat format, final CurrencyModel currencyModel)
	{
		final int tempDigits = currencyModel.getDigits() == null ? 0 : currencyModel.getDigits().intValue();
		final int digits = Math.max(0, tempDigits);

		format.setMaximumFractionDigits(digits);
		format.setMinimumFractionDigits(digits);
		if (digits == 0)
		{
			format.setDecimalSeparatorAlwaysShown(false);
		}

		return format;
	}

	protected DecimalFormat adjustSymbol(final DecimalFormat format, final CurrencyModel currencyModel)
	{
		final String symbol = currencyModel.getSymbol();
		if (symbol != null)
		{
			final DecimalFormatSymbols symbols = format.getDecimalFormatSymbols(); // does cloning
			final String iso = currencyModel.getIsocode();
			boolean changed = false;
			if (!iso.equalsIgnoreCase(symbols.getInternationalCurrencySymbol()))
			{
				symbols.setInternationalCurrencySymbol(iso);
				changed = true;
			}
			if (!symbol.equals(symbols.getCurrencySymbol()))
			{
				symbols.setCurrencySymbol(symbol);
				changed = true;
			}
			if (changed)
			{
				format.setDecimalFormatSymbols(symbols);
			}
		}
		return format;
	}
}
