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
package com.hybris.pcmbackoffice.widgets.contextpopulator;

import static com.hybris.backoffice.widgets.contextpopulator.ContextPopulator.SELECTED_OBJECT;
import static com.hybris.pcmbackoffice.widgets.contextpopulator.CatalogContextPopulator.CHILD_TYPE_CODE;
import static com.hybris.pcmbackoffice.widgets.contextpopulator.CatalogContextPopulator.SELECTED_TYPE_CODE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.model.CategoryModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.hybris.backoffice.navigation.NavigationNode;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacade;
import com.hybris.cockpitng.widgets.common.explorertree.data.PartitionNodeData;


@RunWith(MockitoJUnitRunner.class)
public class CatalogContextPopulatorTest
{
	@InjectMocks
	private CatalogContextPopulator catalogContextPopulator;

	@Mock
	private TypeFacade typeFacade;

	@Test
	public void shouldPopulateContextForCatalogModel()
	{
		// given
		final CatalogModel catalogModel = new CatalogModel();
		given(typeFacade.getType(catalogModel)).willReturn(CatalogModel._TYPECODE);

		// when
		final Map<String, Object> context = catalogContextPopulator.populate(catalogModel);

		// then
		assertThat(context.keySet()).contains(CHILD_TYPE_CODE, SELECTED_OBJECT, SELECTED_TYPE_CODE);
		assertThat(context.get(CHILD_TYPE_CODE)).isEqualTo(CatalogVersionModel._TYPECODE);
		assertThat(context.get(SELECTED_OBJECT)).isEqualTo(catalogModel);
		assertThat(context.get(SELECTED_TYPE_CODE)).isEqualTo(CatalogModel._TYPECODE);
	}

	@Test
	public void shouldPopulateContextForCatalogVersionModel()
	{
		// given
		final CatalogModel catalogModel = new CatalogModel();
		final CatalogVersionModel catalogVersionModel = mock(CatalogVersionModel.class);
		given(catalogVersionModel.getCatalog()).willReturn(catalogModel);
		given(typeFacade.getType(catalogVersionModel)).willReturn(CatalogVersionModel._TYPECODE);

		// when
		final Map<String, Object> context = catalogContextPopulator.populate(catalogVersionModel);

		// then
		assertThat(context.keySet()).contains(CHILD_TYPE_CODE, SELECTED_OBJECT, SELECTED_TYPE_CODE, CatalogVersionModel.CATALOG);
		assertThat(context.get(CHILD_TYPE_CODE)).isEqualTo(CategoryModel._TYPECODE);
		assertThat(context.get(SELECTED_OBJECT)).isEqualTo(catalogVersionModel);
		assertThat(context.get(SELECTED_TYPE_CODE)).isEqualTo(CatalogVersionModel._TYPECODE);
		assertThat(context.get(CategoryModel.CATALOG)).isEqualTo(catalogModel);
		assertThat(context.get(CategoryModel.CATALOGVERSION)).isEqualTo(catalogVersionModel);
	}

	@Test
	public void shouldPopulateContextForCategoryModel()
	{
		// given
		final CatalogModel catalogModel = mock(CatalogModel.class);
		final CatalogVersionModel catalogVersionModel = mock(CatalogVersionModel.class);
		final CategoryModel categoryModel = mock(CategoryModel.class);
		given(typeFacade.getType(categoryModel)).willReturn(CategoryModel._TYPECODE);
		given(categoryModel.getCatalogVersion()).willReturn(catalogVersionModel);
		given(catalogVersionModel.getCatalog()).willReturn(catalogModel);

		// when
		final Map<String, Object> context = catalogContextPopulator.populate(categoryModel);

		// then
		assertThat(context.keySet()).contains(CHILD_TYPE_CODE, SELECTED_OBJECT, SELECTED_TYPE_CODE, CategoryModel.CATALOG,
				CategoryModel.CATALOGVERSION, CategoryModel.SUPERCATEGORIES);
		assertThat(context.get(CHILD_TYPE_CODE)).isEqualTo(CategoryModel._TYPECODE);
		assertThat(context.get(SELECTED_OBJECT)).isEqualTo(categoryModel);
		assertThat(context.get(SELECTED_TYPE_CODE)).isEqualTo(CategoryModel._TYPECODE);
		assertThat(context.get(CategoryModel.CATALOG)).isEqualTo(catalogModel);
		assertThat(context.get(CategoryModel.CATALOGVERSION)).isEqualTo(catalogVersionModel);
		final List<CategoryModel> supercategories = (List<CategoryModel>) context.get(CategoryModel.SUPERCATEGORIES);
		assertThat(supercategories).hasSize(1);
		assertThat(supercategories).contains(categoryModel);
	}

	@Test
	public void shouldPopulateContextForUnknownModel()
	{
		// given
		final Object unknownModel = new Object();
		final String objectTypeCode = "java.lang.Object";
		given(typeFacade.getType(unknownModel)).willReturn(objectTypeCode);

		// when
		final Map<String, Object> context = catalogContextPopulator.populate(unknownModel);

		// then
		assertThat(context.keySet()).contains(CHILD_TYPE_CODE, SELECTED_OBJECT, SELECTED_TYPE_CODE);
		assertThat(context.get(CHILD_TYPE_CODE)).isEqualTo(CatalogModel._TYPECODE);
		assertThat(context.get(SELECTED_OBJECT)).isEqualTo(unknownModel);
		assertThat(context.get(SELECTED_TYPE_CODE)).isEqualTo(objectTypeCode);
	}

	@Test
	public void shouldPupuletContextIfObjectInstanceOfPartitionNodeData()
	{
		//given
		final List<NavigationNode> children = new ArrayList<>();
		final NavigationNode parent = mock(NavigationNode.class);
		final Object unknownModel = new PartitionNodeData(parent, children);

		//when
		final Map<String, Object> context = catalogContextPopulator.populate(unknownModel);

		//then
		assertThat(context.get(SELECTED_OBJECT)).isEqualTo(unknownModel);
		assertThat(context.size()).isEqualTo(1);
	}
}
