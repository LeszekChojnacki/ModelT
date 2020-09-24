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
package com.hybris.pcmbackoffice.widgets.charts.facetchart;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.hybris.platform.solrfacetsearch.search.Facet;
import de.hybris.platform.solrfacetsearch.search.FacetSearchException;
import de.hybris.platform.solrfacetsearch.search.FacetValue;
import de.hybris.platform.solrfacetsearch.search.SearchResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.zkoss.zul.Div;
import org.zkoss.zul.ListModelList;

import com.hybris.backoffice.solrsearch.dataaccess.BackofficeSearchQuery;
import com.hybris.backoffice.solrsearch.services.BackofficeFacetSearchService;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.testing.util.CockpitTestUtil;


@RunWith(MockitoJUnitRunner.class)
public class CategoryFacetChartFiltersRendererTest
{
	@Spy
	@InjectMocks
	private CategoryFacetChartFiltersRenderer categoryFacetChartFiltersRenderer;
	private WidgetInstanceManager widgetInstanceManager;
	@Mock
	private BiConsumer<String, Set<String>> facetSelectionListener;
	@Spy
	private Div filterContainer;
	@Mock
	private BackofficeFacetSearchService backofficeFacetSearchService;
	@Mock
	private BackofficeSearchQuery backofficeSearchQuery;
	@Mock
	private SearchResult searchResult;
	@Mock
	private Facet facet;

	@Before
	public void setUp() throws FacetSearchException
	{
		CockpitTestUtil.mockZkEnvironment();
		widgetInstanceManager = CockpitTestUtil.mockWidgetInstanceManager();
		when(backofficeFacetSearchService.createBackofficeSolrSearchQuery(any())).thenReturn(backofficeSearchQuery);
		when(backofficeFacetSearchService.search(any())).thenReturn(searchResult);
		when(searchResult.getFacet(any())).thenReturn(facet);

		final List<FacetValue> facetValues = new ArrayList<>();
		facetValues.add(new FacetValue("name1", 1, false));
		facetValues.add(new FacetValue("name2", 2, false));
		facetValues.add(new FacetValue("name3", 3, false));
		when(facet.getFacetValues()).thenReturn(facetValues);
	}

	@Test
	public void shouldFireEventWithoutSelectedFacets()
	{
		//given
		categoryFacetChartFiltersRenderer.renderFilters(widgetInstanceManager, filterContainer, facetSelectionListener);

		//when
		categoryFacetChartFiltersRenderer.onSelectCategory(Collections.emptySet());

		//then
		final ArgumentCaptor<String> facetNameArgumentCaptor = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<Set> argumentCaptor = ArgumentCaptor.forClass(Set.class);
		verify(facetSelectionListener).accept(facetNameArgumentCaptor.capture(), argumentCaptor.capture());
		assertThat(argumentCaptor.getValue()).isEmpty();
	}

	@Test
	public void shouldFireEventWithSelectedFacet()
	{
		//given
		categoryFacetChartFiltersRenderer.renderFilters(widgetInstanceManager, filterContainer, facetSelectionListener);

		final Set<String> selected = Collections.singleton("one");

		//when
		categoryFacetChartFiltersRenderer.onSelectCategory(selected);

		//then
		final ArgumentCaptor<String> facetNameArgumentCaptor = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<Set> argumentCaptor = ArgumentCaptor.forClass(Set.class);
		verify(facetSelectionListener).accept(facetNameArgumentCaptor.capture(), argumentCaptor.capture());
		assertThat(argumentCaptor.getValue()).hasSize(1);
	}

	@Test
	public void shouldStoreIndexAndValueOnSelectingCategory()
	{
		// given
		categoryFacetChartFiltersRenderer.renderFilters(widgetInstanceManager, filterContainer, facetSelectionListener);
		// when
		categoryFacetChartFiltersRenderer.onSelectCategory(Collections.emptySet());

		// then
		verify(categoryFacetChartFiltersRenderer).storeSelectedValues(any());
	}

	@Test
	public void shouldSelectItemInTheComboBox()
	{
		// given
		final ListModelList<String> listModel = new ListModelList<>();
		listModel.addAll(Arrays.asList("oneNotSelected", "twoSelected", "threeNotSelected"));
		final Collection<String> selected = Collections.singleton("twoSelected");

		// when
		final Set<String> existingSelectedValues = categoryFacetChartFiltersRenderer.getSelectedCategories(listModel, selected);

		// then
		assertThat(existingSelectedValues).isEqualTo(selected);
	}

	@Test
	public void shouldSelectFirstItemIfValueIsNotFoundInTheComboBox()
	{
		// given
		final ListModelList<String> listModel = new ListModelList<>();
		listModel.addAll(Arrays.asList("one", "two"));
		final Collection<String> selected = Collections.singleton("three");
		categoryFacetChartFiltersRenderer.renderFilters(widgetInstanceManager, filterContainer, facetSelectionListener);

		// when
		final Set<String> existingSelectedValues = categoryFacetChartFiltersRenderer.getSelectedCategories(listModel, selected);

		// then
		assertThat(existingSelectedValues).isEmpty();
	}

	@Test
	public void shouldSelectFirstItemWhenSelectedValueIsNull()
	{
		// given
		final ListModelList<String> listModel = new ListModelList<>();
		listModel.addAll(Arrays.asList("one", "two"));
		// when
		final Set<String> existingSelectedValues = categoryFacetChartFiltersRenderer.getSelectedCategories(listModel,
				Collections.emptySet());

		// then
		assertThat(existingSelectedValues).isEmpty();
	}

	@Test
	public void shouldClearSelectionAndSearchIfValueIsNotFoundInTheComboBox()
	{
		// given
		final Collection<String> selected = Collections.singleton("name4");
		doReturn(selected).when(categoryFacetChartFiltersRenderer).readSelectedValues();

		// when
		categoryFacetChartFiltersRenderer.renderFilters(widgetInstanceManager, filterContainer, facetSelectionListener);

		// then
		verify(categoryFacetChartFiltersRenderer).storeSelectedValues(any());
		final ArgumentCaptor<ListModelList> argumentCaptor = ArgumentCaptor.forClass(ListModelList.class);
		verify(categoryFacetChartFiltersRenderer).getSelectedCategories(argumentCaptor.capture(), any());
		final ListModelList listModel = argumentCaptor.getValue();
		assertThat(listModel.getSelection()).isEmpty();
	}

	@Test
	public void shouldNotTriggerClearAndSearchOnEmptySelection()
	{
		final ListModelList<String> listModel = new ListModelList<>();
		listModel.addAll(Arrays.asList("one", "two"));

		// when
		categoryFacetChartFiltersRenderer.getSelectedCategories(listModel, Collections.emptySet());

		// then
		verify(categoryFacetChartFiltersRenderer, never()).storeSelectedValues(any());
	}

	@Test
	public void shouldApplySelectedCategories()
	{
		// given
		final Collection<String> selected = Arrays.asList("name1", "name2");
		doReturn(selected).when(categoryFacetChartFiltersRenderer).readSelectedValues();

		// when
		categoryFacetChartFiltersRenderer.renderFilters(widgetInstanceManager, filterContainer, facetSelectionListener);

		// then
		verify(categoryFacetChartFiltersRenderer, never()).storeSelectedValues(any());
		final ArgumentCaptor<ListModelList> argumentCaptor = ArgumentCaptor.forClass(ListModelList.class);
		verify(categoryFacetChartFiltersRenderer).getSelectedCategories(argumentCaptor.capture(), any());
		final ListModelList listModel = argumentCaptor.getValue();
		assertThat(listModel.getSelection()).containsAll(selected);
	}

	@Test
	public void shouldRefreshAfterRemoveFilters()
	{
		//given
		categoryFacetChartFiltersRenderer.renderFilters(widgetInstanceManager, filterContainer, facetSelectionListener);

		// when
		categoryFacetChartFiltersRenderer.removeFilters();

		// then
		final ArgumentCaptor<Collection> collectionArgumentCaptor = ArgumentCaptor.forClass(Collection.class);
		verify(categoryFacetChartFiltersRenderer).storeSelectedValues(collectionArgumentCaptor.capture());
		final Collection selectedCollection = collectionArgumentCaptor.getValue();
		assertThat(selectedCollection.isEmpty());

		final ArgumentCaptor<Set> setArgumentCaptor = ArgumentCaptor.forClass(Set.class);
		verify(facetSelectionListener).accept(any(), setArgumentCaptor.capture());
		final Set selectedSet = setArgumentCaptor.getValue();
		assertThat(selectedSet.isEmpty());
	}

}
