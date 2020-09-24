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
package de.hybris.platform.adaptivesearchbackoffice.widgets.categoryselector;


import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.NAVIGATION_CONTEXT_SOCKET;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.adaptivesearchbackoffice.data.AsCategoryData;
import de.hybris.platform.adaptivesearchbackoffice.data.CatalogVersionData;
import de.hybris.platform.adaptivesearchbackoffice.data.NavigationContextData;
import de.hybris.platform.adaptivesearchbackoffice.facades.AsCategoryFacade;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Tree;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.TreeNode;

import com.hybris.cockpitng.testing.AbstractWidgetUnitTest;
import com.hybris.cockpitng.testing.annotation.DeclaredInput;
import com.hybris.cockpitng.testing.annotation.DeclaredViewEvent;
import com.hybris.cockpitng.testing.annotation.NullSafeWidget;


@NullSafeWidget(true)
@DeclaredInput(value = NAVIGATION_CONTEXT_SOCKET, socketType = NavigationContextData.class)
@DeclaredViewEvent(componentID = CategorySelectorController.CATEGORY_SELECTOR_ID, eventName = Events.ON_SELECT)
@UnitTest
public class CategorySelectorControllerTest extends AbstractWidgetUnitTest<CategorySelectorController>
{
	protected static final String GLOBAL_CATEGORY_CODE = "global";
	protected static final String GLOBAL_CATEGORY_NAME = "Global category";

	protected static final String INDEX_CONFIGURATION_CODE = "indexConfiguration";
	protected static final String INDEX_TYPE_CODE = "indexType";

	protected static final String CATALOG_ID = "catalog";
	protected static final String ONLINE_CATALOG_VERSION = "onlineCatalogVersion";

	@Mock
	private Component component;

	@Mock
	private Tree categorySelector;

	@Mock
	private AsCategoryFacade asCategoryFacade;

	private AsCategoryData globalCategory;

	private final CategorySelectorController categorySelectorController = new CategorySelectorController();

	@Override
	protected CategorySelectorController getWidgetController()
	{
		return categorySelectorController;
	}

	@Before
	public void setUp()
	{
		MockitoAnnotations.initMocks(this);

		globalCategory = new AsCategoryData();
		globalCategory.setCode(GLOBAL_CATEGORY_CODE);
		globalCategory.setName(GLOBAL_CATEGORY_NAME);
		categorySelectorController.categorySelector = categorySelector;
		categorySelectorController.asCategoryFacade = asCategoryFacade;
	}

	@Test
	public void emptyInitialCategory()
	{
		// given
		when(asCategoryFacade.getCategoryHierarchy()).thenReturn(globalCategory);

		// when
		categorySelectorController.initialize(component);

		// then
		assertNoSocketOutputInteractions(CategorySelectorController.SELECTED_CATEGORY_OUT_SOCKET);
	}

	@Test
	public void updateCategoriesForNullNavigationContext()
	{
		// when
		categorySelectorController.initialize(component);
		executeInputSocketEvent(NAVIGATION_CONTEXT_SOCKET, (NavigationContextData) null);

		// then
		final TreeModel<TreeNode<CategoryModel>> categoriesModel = categorySelectorController.getCategoriesModel();
		assertNotNull(categoriesModel.getRoot());
		assertEquals(0, categoriesModel.getRoot().getChildCount());
	}

	@Test
	public void updateCategoriesForNavigationContext()
	{
		// given
		final CatalogVersionData catalogVersion = new CatalogVersionData();
		catalogVersion.setCatalogId(CATALOG_ID);
		catalogVersion.setVersion(ONLINE_CATALOG_VERSION);

		final NavigationContextData navigationContext = new NavigationContextData();
		navigationContext.setIndexConfiguration(INDEX_CONFIGURATION_CODE);
		navigationContext.setIndexType(INDEX_TYPE_CODE);
		navigationContext.setCatalogVersion(catalogVersion);
		navigationContext.setSearchProfiles(new ArrayList<>());

		when(asCategoryFacade.getCategoryHierarchy(CATALOG_ID, ONLINE_CATALOG_VERSION)).thenReturn(globalCategory);

		// when
		categorySelectorController.initialize(component);
		executeInputSocketEvent(NAVIGATION_CONTEXT_SOCKET, navigationContext);

		// then
		final TreeModel<TreeNode<CategoryModel>> categoriesModel = categorySelectorController.getCategoriesModel();
		assertNotNull(categoriesModel.getRoot());
		assertEquals(1, categoriesModel.getRoot().getChildCount());
		final CategoryModel cat1 = categoriesModel.getRoot().getChildAt(0).getData();
		assertEquals(GLOBAL_CATEGORY_CODE, cat1.getCode());
		assertEquals(GLOBAL_CATEGORY_NAME, cat1.getName());
	}
}
