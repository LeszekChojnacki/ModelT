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
package de.hybris.platform.adaptivesearchbackoffice.widgets.maincontroller;


import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.NAVIGATION_CONTEXT_SOCKET;
import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.SEARCH_CONTEXT_SOCKET;
import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.SEARCH_REQUEST_SOCKET;
import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.SEARCH_RESULT_SOCKET;

import de.hybris.platform.adaptivesearch.AsException;
import de.hybris.platform.adaptivesearch.context.AsSearchProfileContext;
import de.hybris.platform.adaptivesearch.data.AsFacetData;
import de.hybris.platform.adaptivesearch.data.AsSearchQueryData;
import de.hybris.platform.adaptivesearch.data.AsSearchResultData;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchProfileModel;
import de.hybris.platform.adaptivesearch.services.AsSearchProfileActivationService;
import de.hybris.platform.adaptivesearch.services.AsSearchProfileService;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProvider;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProviderFactory;
import de.hybris.platform.adaptivesearchbackoffice.data.AbstractSearchRequestData;
import de.hybris.platform.adaptivesearchbackoffice.data.AsCategoryData;
import de.hybris.platform.adaptivesearchbackoffice.data.CatalogVersionData;
import de.hybris.platform.adaptivesearchbackoffice.data.FacetFiltersRequestData;
import de.hybris.platform.adaptivesearchbackoffice.data.FacetRequestData;
import de.hybris.platform.adaptivesearchbackoffice.data.FacetStateData;
import de.hybris.platform.adaptivesearchbackoffice.data.NavigationContextData;
import de.hybris.platform.adaptivesearchbackoffice.data.PaginationRequestData;
import de.hybris.platform.adaptivesearchbackoffice.data.SearchContextData;
import de.hybris.platform.adaptivesearchbackoffice.data.SearchRequestData;
import de.hybris.platform.adaptivesearchbackoffice.data.SearchResultData;
import de.hybris.platform.adaptivesearchbackoffice.data.SearchStateData;
import de.hybris.platform.adaptivesearchbackoffice.data.SortRequestData;
import de.hybris.platform.adaptivesearchbackoffice.facades.AsCategoryFacade;
import de.hybris.platform.adaptivesearchbackoffice.facades.AsSearchProfileContextFacade;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Label;

import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.util.DefaultWidgetController;


/**
 * Main controller for adaptive search.
 */
public class MainController extends DefaultWidgetController
{
	private static final Logger LOG = LoggerFactory.getLogger(MainController.class);

	protected static final String SOCKET_IN_SEARCH_TEXT = "searchText";
	protected static final String SOCKET_IN_REFRESH_SEARCH = "refreshSearch";
	protected static final String SOCKET_IN_PAGINATION_REQUEST = "paginationRequest";

	protected static final String SOCKET_OUT_CLEAR_QUERY = "clearQuery";

	protected static final String NAVIGATION_CONTEXT_KEY = "navigationContext";
	protected static final String SEARCH_CONTEXT_KEY = "searchContext";
	protected static final String SEARCH_STATE_KEY = "searchState";

	protected static final String SETTING_DEFAULT_PAGE_SIZE = "defaultPageSize";

	@WireVariable
	protected transient SessionService sessionService;

	@WireVariable
	protected transient I18NService i18nService;

	@WireVariable
	protected transient CatalogVersionService catalogVersionService;

	@WireVariable
	protected transient AsSearchProfileService asSearchProfileService;

	@WireVariable
	protected transient AsSearchProfileActivationService asSearchProfileActivationService;

	@WireVariable
	protected transient AsSearchProviderFactory asSearchProviderFactory;

	@WireVariable
	protected transient AsSearchProfileContextFacade asSearchProfileContextFacade;

	@WireVariable
	protected transient AsCategoryFacade asCategoryFacade;

	@Wire
	protected Label categoryBreadcrumbs;

	protected NavigationContextData getNavigationContext()
	{
		return getModel().getValue(NAVIGATION_CONTEXT_KEY, NavigationContextData.class);
	}

	protected void setNavigationContext(final NavigationContextData navigationContext)
	{
		getModel().put(NAVIGATION_CONTEXT_KEY, navigationContext);
	}

	protected SearchContextData getSearchContext()
	{
		return getModel().getValue(SEARCH_CONTEXT_KEY, SearchContextData.class);
	}

	protected void setSearchContext(final SearchContextData searchContext)
	{
		getModel().put(SEARCH_CONTEXT_KEY, searchContext);
	}

	protected SearchStateData getSearchState()
	{
		return getModel().getValue(SEARCH_STATE_KEY, SearchStateData.class);
	}

	protected void setSearchState(final SearchStateData searchState)
	{
		getModel().put(SEARCH_STATE_KEY, searchState);
	}

	@Override
	public void preInitialize(final Component comp)
	{
		setSearchState(createSearchState());
	}

	protected void search()
	{
		final SearchResultData searchResult = createSearchResult();

		final NavigationContextData navigationContext = getNavigationContext();
		final SearchContextData searchContext = getSearchContext();

		if (navigationContext == null || searchContext == null)
		{
			return;
		}

		if (navigationContext.getIndexConfiguration() == null || navigationContext.getIndexType() == null)
		{
			sendOutput(SEARCH_RESULT_SOCKET, searchResult);
			return;
		}

		try
		{
			final CatalogVersionModel catalogVersion = resolveCatalogVersion(navigationContext.getCatalogVersion());
			final List<AbstractAsSearchProfileModel> searchProfiles = resolveSearchProfiles(catalogVersion,
					navigationContext.getSearchProfiles());
			asSearchProfileActivationService.setCurrentSearchProfiles(searchProfiles);

			final AsSearchProfileContext searchProfileContext = asSearchProfileContextFacade
					.createSearchProfileContext(navigationContext, searchContext);
			final SearchStateData searchState = getSearchState();
			final AsSearchQueryData searchQuery = searchState.getSearchQuery();

			final AsSearchProvider searchProvider = asSearchProviderFactory.getSearchProvider();
			final AsSearchResultData asSearchResult = searchProvider.search(searchProfileContext, searchQuery);

			modifySearchResult(asSearchResult, searchState);

			searchResult.setAsSearchResult(asSearchResult);

			// updates the search state with the sort from the response (might be different from the one that was requested)
			searchQuery.setSort(asSearchResult.getCurrentSort() != null ? asSearchResult.getCurrentSort().getCode() : null);

			sendOutput(SEARCH_RESULT_SOCKET, searchResult);
		}
		catch (final AsException e)
		{
			LOG.error(e.getMessage(), e);
			sendOutput(SEARCH_RESULT_SOCKET, searchResult);
		}
		finally
		{
			asSearchProfileActivationService.clearCurrentSearchProfiles();
		}
	}

	protected void modifySearchResult(final AsSearchResultData searchResult, final SearchStateData searchState)
	{
		if (CollectionUtils.isNotEmpty(searchResult.getFacets()))
		{
			final Map<String, FacetStateData> facetsState = searchState.getFacetsState();

			for (final AsFacetData facet : searchResult.getFacets())
			{
				final FacetStateData facetState = facetsState.get(facet.getIndexProperty());

				if (facetState != null)
				{
					facet.setVisibility(facetState.getFacetVisibility());
				}
			}
		}
	}

	protected SearchStateData createSearchState()
	{
		final AsSearchQueryData searchQuery = new AsSearchQueryData();
		searchQuery.setActivePage(0);
		searchQuery.setPageSize(getWidgetSettings().getInt(SETTING_DEFAULT_PAGE_SIZE));
		searchQuery.setFacetValues(new HashMap<>());

		final SearchStateData searchState = new SearchStateData();
		searchState.setSearchQuery(searchQuery);
		searchState.setFacetsState(new HashMap<>());

		return searchState;
	}

	protected SearchResultData createSearchResult()
	{
		final SearchResultData searchResult = new SearchResultData();

		searchResult.setNavigationContext(getNavigationContext());
		searchResult.setSearchContext(getSearchContext());

		return searchResult;
	}

	protected CatalogVersionModel resolveCatalogVersion(final CatalogVersionData catalogVersion)
	{
		if (catalogVersion == null)
		{
			return null;
		}

		return catalogVersionService.getCatalogVersion(catalogVersion.getCatalogId(), catalogVersion.getVersion());
	}

	protected List<AbstractAsSearchProfileModel> resolveSearchProfiles(final CatalogVersionModel catalogVersion,
			final List<String> searchProfiles)
	{
		if (CollectionUtils.isEmpty(searchProfiles))
		{
			return Collections.emptyList();
		}

		return searchProfiles.stream().map(code -> asSearchProfileService.getSearchProfileForCode(catalogVersion, code).get())
				.collect(Collectors.toList());
	}

	/**
	 * Executes search for a given search term
	 *
	 * @param searchText
	 *           - search term
	 */
	@SocketEvent(socketId = SOCKET_IN_SEARCH_TEXT)
	public void refreshSearchText(final String searchText)
	{
		final int currentPageSize = getSearchState().getSearchQuery().getPageSize();

		final SearchStateData searchState = createSearchState();
		final AsSearchQueryData searchQuery = searchState.getSearchQuery();
		searchQuery.setPageSize(currentPageSize);
		searchQuery.setQuery(searchText);

		setSearchState(searchState);

		search();
	}

	/**
	 * Refreshes the current search
	 */
	@SocketEvent(socketId = SOCKET_IN_REFRESH_SEARCH)
	public void refreshSearch()
	{
		search();
	}

	/**
	 * Refreshes the navigation context
	 *
	 * @param navigationContext
	 *           - navigation context object
	 */
	@SocketEvent(socketId = NAVIGATION_CONTEXT_SOCKET)
	public void refreshNavigationContext(final NavigationContextData navigationContext)
	{
		sendOutput(SOCKET_OUT_CLEAR_QUERY, StringUtils.EMPTY);

		setNavigationContext(navigationContext);
		setSearchContext(null);

		sessionService.executeInLocalView(new SessionExecutionBody()
		{
			@Override
			public void executeWithoutResult()
			{
				i18nService.setLocalizationFallbackEnabled(true);

				buildCategoryBreadcrumbs(navigationContext);
			}
		});

		final int currentPageSize = getSearchState().getSearchQuery().getPageSize();

		final SearchStateData searchState = createSearchState();
		final AsSearchQueryData searchQuery = searchState.getSearchQuery();
		searchQuery.setPageSize(currentPageSize);

		setSearchState(searchState);

		search();
	}

	protected void buildCategoryBreadcrumbs(final NavigationContextData navigationContext)
	{
		CatalogVersionData catalogVersion = null;
		List<String> categoryPath = Collections.emptyList();

		if (navigationContext != null)
		{
			if (navigationContext.getCatalogVersion() != null)
			{
				catalogVersion = navigationContext.getCatalogVersion();
			}

			if (navigationContext.getCategory() != null)
			{
				categoryPath = navigationContext.getCategory().getPath();
			}
		}

		final List<AsCategoryData> breadcrumbs = catalogVersion == null ? asCategoryFacade.buildCategoryBreadcrumbs(categoryPath)
				: asCategoryFacade.buildCategoryBreadcrumbs(catalogVersion.getCatalogId(), catalogVersion.getVersion(), categoryPath);

		categoryBreadcrumbs.setValue(breadcrumbs.stream().map(AsCategoryData::getName).collect(Collectors.joining(" / ")));
	}


	@SocketEvent(socketId = SEARCH_CONTEXT_SOCKET)
	public void refreshSearchContext(final SearchContextData searchContext)
	{
		final SearchContextData currentSearchContext = getSearchContext();

		if (!Objects.equals(currentSearchContext, searchContext))
		{
			setSearchContext(searchContext);
			search();
		}
	}

	/**
	 * Refreshes the pagination
	 *
	 * @param request
	 *           - the pagination request
	 */
	@SocketEvent(socketId = SOCKET_IN_PAGINATION_REQUEST)
	public void refreshPagination(final PaginationRequestData request)
	{
		processPaginationRequest(request);
	}

	@SocketEvent(socketId = SEARCH_REQUEST_SOCKET)
	public void searchRequest(final AbstractSearchRequestData request)
	{
		if (request instanceof SearchRequestData)
		{
			processSearchRequest((SearchRequestData) request);
		}
		else if (request instanceof PaginationRequestData)
		{
			processPaginationRequest((PaginationRequestData) request);
		}
		else if (request instanceof FacetRequestData)
		{
			processFacetRequest((FacetRequestData) request);
		}
		else if (request instanceof FacetFiltersRequestData)
		{
			processFacetFiltersRequest((FacetFiltersRequestData) request);
		}
		else if (request instanceof SortRequestData)
		{
			processSortRequest((SortRequestData) request);
		}
	}

	protected void processSearchRequest(final SearchRequestData request)
	{
		search();
	}

	protected void processPaginationRequest(final PaginationRequestData request)
	{
		final SearchStateData searchState = getSearchState();
		final AsSearchQueryData searchQuery = searchState.getSearchQuery();
		searchQuery.setActivePage(request.getActivePage());
		searchQuery.setPageSize(request.getPageSize());

		search();
	}

	protected void processFacetRequest(final FacetRequestData request)
	{
		final SearchStateData searchState = getSearchState();
		final Map<String, FacetStateData> facetsState = searchState.getFacetsState();

		final FacetStateData facetState = new FacetStateData();
		facetState.setFacetVisibility(request.getFacetVisibility());

		facetsState.put(request.getIndexProperty(), facetState);
	}

	protected void processFacetFiltersRequest(final FacetFiltersRequestData request)
	{
		final SearchStateData searchState = getSearchState();
		final AsSearchQueryData searchQuery = searchState.getSearchQuery();

		final String key = request.getIndexProperty();

		if (CollectionUtils.isEmpty(request.getValues()))
		{
			searchQuery.getFacetValues().remove(key);
		}
		else
		{
			final Set<String> values = new HashSet<>(request.getValues());
			searchQuery.getFacetValues().put(key, values);
		}

		search();
	}

	protected void processSortRequest(final SortRequestData request)
	{
		final SearchStateData searchState = getSearchState();
		final AsSearchQueryData searchQuery = searchState.getSearchQuery();
		searchQuery.setSort(request.getSort());

		search();
	}
}
