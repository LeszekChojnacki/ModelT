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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.user.UserService;

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

import com.hybris.cockpitng.dataaccess.facades.permissions.PermissionFacade;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.labels.LabelService;
import com.hybris.cockpitng.testing.util.CockpitTestUtil;


@RunWith(MockitoJUnitRunner.class)
public class CatalogVersionFacetChartFiltersRendererTest
{
	@Spy
	@InjectMocks
	private CatalogVersionFacetChartFiltersRenderer catalogVersionFacetChartFiltersRenderer;
	private WidgetInstanceManager widgetInstanceManager;
	@Mock
	private UserService userService;
	@Mock
	private CatalogVersionService catalogVersionService;
	@Mock
	private PermissionFacade permissionFacade;
	@Mock
	private LabelService labelService;
	@Spy
	private Div filterContainer;
	@Mock
	private BiConsumer<String, Set<String>> facetSelectionListener;
	@Mock
	private CatalogVersionModel catalogVersionModel;

	@Before
	public void setUp()
	{
		CockpitTestUtil.mockZkEnvironment();
		widgetInstanceManager = CockpitTestUtil.mockWidgetInstanceManager();
		when(userService.getCurrentUser()).thenReturn(mock(UserModel.class));
		when(permissionFacade.canReadInstance(any())).thenReturn(true);
		when(labelService.getObjectLabel(any())).thenReturn("Label");

		final List<CatalogVersionModel> allReadableCatalog = new ArrayList<>();
		allReadableCatalog.add(catalogVersionModel);
		when(catalogVersionService.getAllReadableCatalogVersions(any())).thenReturn(allReadableCatalog);
	}

	@Test
	public void shouldFireEventForOneSearchCondition()
	{
		//given
		catalogVersionFacetChartFiltersRenderer.renderFilters(widgetInstanceManager, filterContainer, facetSelectionListener);
		final Set<String> selected = Collections.singleton("some catalog");

		//when
		catalogVersionFacetChartFiltersRenderer.onSelectCatalogVersion(selected);

		//then
		final ArgumentCaptor<String> facetNameArgumentCaptor = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<Set> facetSelectionArgumentCaptor = ArgumentCaptor.forClass(Set.class);
		verify(facetSelectionListener).accept(facetNameArgumentCaptor.capture(), facetSelectionArgumentCaptor.capture());
		assertThat(facetSelectionArgumentCaptor.getValue()).hasSize(1);
	}

	@Test
	public void shouldFireEventForEmptyConditionList()
	{
		//given
		catalogVersionFacetChartFiltersRenderer.renderFilters(widgetInstanceManager, filterContainer, facetSelectionListener);

		//when
		catalogVersionFacetChartFiltersRenderer.onSelectCatalogVersion(Collections.emptySet());

		final ArgumentCaptor<String> facetNameArgumentCaptor = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<Set> facetSelectionArgumentCaptor = ArgumentCaptor.forClass(Set.class);
		verify(facetSelectionListener).accept(facetNameArgumentCaptor.capture(), facetSelectionArgumentCaptor.capture());
		assertThat(facetSelectionArgumentCaptor.getValue()).isEmpty();
	}

	@Test
	public void shouldStoreIndexAndValueOnSelectingCategory()
	{
		// given
		catalogVersionFacetChartFiltersRenderer.renderFilters(widgetInstanceManager, filterContainer, facetSelectionListener);
		final Set<String> selected = Collections.singleton("some catalog");

		// when
		catalogVersionFacetChartFiltersRenderer.onSelectCatalogVersion(selected);

		// then
		verify(catalogVersionFacetChartFiltersRenderer).storeSelectedValues(any());
	}

	@Test
	public void shouldStoreIndexAndValueOnSelectingEmptyCategory()
	{
		// given
		catalogVersionFacetChartFiltersRenderer.renderFilters(widgetInstanceManager, filterContainer, facetSelectionListener);
		final Set<String> selected = Collections.emptySet();

		// when
		catalogVersionFacetChartFiltersRenderer.onSelectCatalogVersion(selected);

		// then
		verify(catalogVersionFacetChartFiltersRenderer).storeSelectedValues(any());
	}

	@Test
	public void shouldSelectItemInTheChosenBox()
	{
		// given
		final ListModelList<String> listModel = new ListModelList<>();
		listModel.addAll(Arrays.asList("oneNotSelected", "twoSelected", "threeNotSelected"));
		final Collection<String> selected = Collections.singleton("twoSelected");

		// when
		final Set<String> selectedObjects = catalogVersionFacetChartFiltersRenderer.getSelectedCatalogVersions(listModel, selected);

		// then
		assertThat(selectedObjects.size()).isEqualTo(selected.size());
		assertThat(selectedObjects).containsAll(selected);
	}

	@Test
	public void shouldClearSelectionAndSearchIfValueIsNotFoundInTheChosenBox()
	{
		// given
		final Collection<String> selected = Collections.singleton("name4");
		doReturn(selected).when(catalogVersionFacetChartFiltersRenderer).readSelectedValues();

		// when
		catalogVersionFacetChartFiltersRenderer.renderFilters(widgetInstanceManager, filterContainer, facetSelectionListener);

		// then
		verify(catalogVersionFacetChartFiltersRenderer).storeSelectedValues(any());
		final ArgumentCaptor<ListModelList> argumentCaptor = ArgumentCaptor.forClass(ListModelList.class);
		verify(catalogVersionFacetChartFiltersRenderer).getSelectedCatalogVersions(argumentCaptor.capture(), any());
		final ListModelList listModel = argumentCaptor.getValue();
		assertThat(listModel.getSelection()).isEmpty();
	}

	@Test
	public void shouldNotTriggerClearAndSearchOnEmptySelection()
	{
		final ListModelList<String> listModel = new ListModelList<>();
		listModel.addAll(Arrays.asList("one", "two"));

		// when
		catalogVersionFacetChartFiltersRenderer.getSelectedCatalogVersions(listModel, Collections.emptySet());

		// then
		verify(catalogVersionFacetChartFiltersRenderer, never()).storeSelectedValues(any());
	}

	@Test
	public void shouldApplySelectedCategories()
	{
		// given
		final Collection<String> selected = Arrays.asList("Label");
		doReturn(selected).when(catalogVersionFacetChartFiltersRenderer).readSelectedValues();

		// when
		catalogVersionFacetChartFiltersRenderer.renderFilters(widgetInstanceManager, filterContainer, facetSelectionListener);

		// then
		verify(catalogVersionFacetChartFiltersRenderer, never()).storeSelectedValues(any());
		final ArgumentCaptor<ListModelList> argumentCaptor = ArgumentCaptor.forClass(ListModelList.class);
		verify(catalogVersionFacetChartFiltersRenderer).getSelectedCatalogVersions(argumentCaptor.capture(), any());
		final ListModelList listModel = argumentCaptor.getValue();
		assertThat(listModel.getSelection()).containsAll(selected);
	}

	@Test
	public void shouldRefreshAfterRemoveFilters()
	{
		//given
		catalogVersionFacetChartFiltersRenderer.renderFilters(widgetInstanceManager, filterContainer, facetSelectionListener);

		// when
		catalogVersionFacetChartFiltersRenderer.removeFilters();

		// then
		final ArgumentCaptor<Collection> collectionArgumentCaptor = ArgumentCaptor.forClass(Collection.class);
		verify(catalogVersionFacetChartFiltersRenderer).storeSelectedValues(collectionArgumentCaptor.capture());
		final Collection selectedCollection = collectionArgumentCaptor.getValue();
		assertThat(selectedCollection.isEmpty());

		final ArgumentCaptor<Set> setArgumentCaptor = ArgumentCaptor.forClass(Set.class);
		verify(facetSelectionListener).accept(any(), setArgumentCaptor.capture());
		final Set selectedSet = setArgumentCaptor.getValue();
		assertThat(selectedSet.isEmpty());
	}

}
