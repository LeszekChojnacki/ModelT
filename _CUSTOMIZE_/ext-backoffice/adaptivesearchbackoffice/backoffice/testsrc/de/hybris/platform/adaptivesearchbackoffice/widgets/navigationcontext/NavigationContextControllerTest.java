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
package de.hybris.platform.adaptivesearchbackoffice.widgets.navigationcontext;


import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.NAVIGATION_CONTEXT_SOCKET;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.adaptivesearch.data.AsIndexConfigurationData;
import de.hybris.platform.adaptivesearch.data.AsIndexTypeData;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProvider;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProviderFactory;
import de.hybris.platform.adaptivesearchbackoffice.data.CatalogVersionData;
import de.hybris.platform.adaptivesearchbackoffice.data.CategoryData;
import de.hybris.platform.adaptivesearchbackoffice.data.NavigationContextData;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;

import com.hybris.cockpitng.components.Editor;
import com.hybris.cockpitng.labels.LabelService;
import com.hybris.cockpitng.testing.AbstractWidgetUnitTest;
import com.hybris.cockpitng.testing.annotation.DeclaredInput;
import com.hybris.cockpitng.testing.annotation.DeclaredViewEvent;
import com.hybris.cockpitng.testing.annotation.DeclaredViewEvents;
import com.hybris.cockpitng.testing.annotation.NullSafeWidget;


@UnitTest
@NullSafeWidget(true)
@DeclaredInput(value = NavigationContextController.CATEGORY_IN_SOCKET, socketType = CategoryData.class)
@DeclaredViewEvents(
{ @DeclaredViewEvent(componentID = NavigationContextController.INDEX_CONFIGURATION_SELECTOR_ID, eventName = Events.ON_SELECT),
		@DeclaredViewEvent(componentID = NavigationContextController.INDEX_TYPE_SELECTOR_ID, eventName = Events.ON_SELECT),
		@DeclaredViewEvent(componentID = NavigationContextController.CATALOG_VERSION_SELECTOR_ID, eventName = Events.ON_SELECT),
		@DeclaredViewEvent(componentID = NavigationContextController.SEARCH_PROFILE_SELECTOR_ID, eventName = NavigationContextController.ON_VALUE_CHANGED) })
public class NavigationContextControllerTest extends AbstractWidgetUnitTest<NavigationContextController>
{
	protected static final String INDEX_CONFIGURATION_CODE = "indexConfiguration";
	protected static final String INDEX_CONFIGURATION_NAME = "Index configuration";

	protected static final String INDEX_TYPE_CODE = "indexType";
	protected static final String INDEX_TYPE_NAME = "Index type";

	protected static final String CATALOG_ID = "catalog";
	protected static final String CATALOG_NAME = "Default catalog";

	protected static final String ONLINE_CATALOG_VERSION = "onlineCatalogVersion";
	protected static final String STAGED_CATALOG_VERSION = "stagedCatalogVersion";

	protected static final String CATEGORY_CODE = "category";
	protected static final String CATEGORY_PATH = "parent/category";

	@Mock
	private Component component;

	@Mock
	private Combobox indexConfigurationSelector;

	@Mock
	private Combobox indexTypeSelector;

	@Mock
	private Combobox catalogVersionSelector;

	@Mock
	private Editor searchProfileSelector;

	@Mock
	private SessionService sessionService;

	@Mock
	private I18NService i18nService;

	@Mock
	private CatalogVersionService catalogVersionService;

	@Mock
	private AsSearchProviderFactory asSearchProviderFactory;

	@Mock
	private AsSearchProvider asSearchProvider;

	@Mock
	private LabelService labelService;

	@Mock
	private CatalogModel catalog;

	@Mock
	private de.hybris.platform.catalog.model.CatalogVersionModel onlineCatalogVersion;

	@Mock
	private de.hybris.platform.catalog.model.CatalogVersionModel stagedCatalogVersion;

	private AsIndexConfigurationData indexConfiguration;
	private AsIndexTypeData indexType;

	private final NavigationContextController navigationContextController = new NavigationContextController();

	@Override
	protected NavigationContextController getWidgetController()
	{
		return navigationContextController;
	}

	@Before
	public void setUp()
	{
		MockitoAnnotations.initMocks(this);

		indexConfiguration = new AsIndexConfigurationData();
		indexConfiguration.setCode(INDEX_CONFIGURATION_CODE);
		indexConfiguration.setName(INDEX_CONFIGURATION_NAME);

		indexType = new AsIndexTypeData();
		indexType.setCode(INDEX_TYPE_CODE);
		indexType.setName(INDEX_TYPE_NAME);

		when(catalog.getId()).thenReturn(CATALOG_ID);
		when(catalog.getName()).thenReturn(CATALOG_NAME);

		when(onlineCatalogVersion.getCatalog()).thenReturn(catalog);
		when(onlineCatalogVersion.getVersion()).thenReturn(ONLINE_CATALOG_VERSION);

		when(stagedCatalogVersion.getCatalog()).thenReturn(catalog);
		when(stagedCatalogVersion.getVersion()).thenReturn(STAGED_CATALOG_VERSION);

		when(labelService.getObjectLabel(onlineCatalogVersion)).thenReturn(CATALOG_NAME + " : " + ONLINE_CATALOG_VERSION);
		when(labelService.getObjectLabel(stagedCatalogVersion)).thenReturn(CATALOG_NAME + " : " + STAGED_CATALOG_VERSION);

		when(sessionService.executeInLocalView(any())).thenAnswer(new Answer<Object>()
		{
			public Object answer(final InvocationOnMock invocation)
			{
				return ((SessionExecutionBody) invocation.getArguments()[0]).execute();
			}
		});

		when(asSearchProviderFactory.getSearchProvider()).thenReturn(asSearchProvider);

		when(asSearchProvider.getIndexConfigurations()).thenReturn(Arrays.asList(indexConfiguration));
		when(asSearchProvider.getIndexTypes(INDEX_CONFIGURATION_CODE)).thenReturn(Arrays.asList(indexType));
		when(asSearchProvider.getSupportedCatalogVersions(INDEX_CONFIGURATION_CODE, INDEX_TYPE_CODE))
				.thenReturn(Arrays.asList(onlineCatalogVersion, stagedCatalogVersion));

		navigationContextController.indexConfigurationSelector = indexConfigurationSelector;
		navigationContextController.indexTypeSelector = indexTypeSelector;
		navigationContextController.catalogVersionSelector = catalogVersionSelector;
		navigationContextController.searchProfileSelector = searchProfileSelector;
		navigationContextController.sessionService = sessionService;
		navigationContextController.i18nService = i18nService;
		navigationContextController.catalogVersionService = catalogVersionService;
		navigationContextController.asSearchProviderFactory = asSearchProviderFactory;
		navigationContextController.labelService = labelService;
	}

	private void initializeController()
	{
		navigationContextController.initialize(component);

		final NavigationContextData navigationContext = navigationContextController.getNavigationContext();

		navigationContextController.updateSelectors(navigationContext);
		navigationContextController.sendNavigationContext(navigationContext);
	}

	private void updateIndexConfiguration(final String code)
	{
		final Comboitem item = mock(Comboitem.class);
		final SelectEvent<Comboitem, String> event = mock(SelectEvent.class);

		when(event.getReference()).thenReturn(item);
		when(item.getValue()).thenReturn(code);

		executeViewEvent(NavigationContextController.INDEX_CONFIGURATION_SELECTOR_ID, Events.ON_SELECT, event);
	}

	private void updateIndexType(final String code)
	{
		final Comboitem item = mock(Comboitem.class);
		final SelectEvent<Comboitem, String> event = mock(SelectEvent.class);

		when(event.getReference()).thenReturn(item);
		when(item.getValue()).thenReturn(code);

		executeViewEvent(NavigationContextController.INDEX_TYPE_SELECTOR_ID, Events.ON_SELECT, event);
	}

	@Test
	public void defaultNavigationContext()
	{
		// given
		final NavigationContextData expectedNavigationContext = new NavigationContextData();
		expectedNavigationContext.setSearchProfiles(Collections.emptyList());

		// when
		initializeController();

		// then
		assertSocketOutput(NAVIGATION_CONTEXT_SOCKET, expectedNavigationContext);
	}

	@Test
	public void updateIndexConfigurationAndIndexType()
	{
		// given
		final CatalogVersionData catalogVersion = new CatalogVersionData();
		catalogVersion.setCatalogId(CATALOG_ID);
		catalogVersion.setVersion(ONLINE_CATALOG_VERSION);

		final NavigationContextData expectedNavigationContext = new NavigationContextData();
		expectedNavigationContext.setIndexConfiguration(INDEX_CONFIGURATION_CODE);
		expectedNavigationContext.setIndexType(INDEX_TYPE_CODE);
		expectedNavigationContext.setCatalogVersion(catalogVersion);
		expectedNavigationContext.setSearchProfiles(Collections.emptyList());

		// when
		initializeController();
		updateIndexConfiguration(INDEX_CONFIGURATION_CODE);
		updateIndexType(INDEX_TYPE_CODE);

		// then
		assertSocketOutputs(NAVIGATION_CONTEXT_SOCKET, expectedNavigationContext);
	}

	@Test
	public void updateIndexConfigurationAndIndexTypeUseActiveCatalogVersion()
	{
		// given
		final CatalogVersionData catalogVersion = new CatalogVersionData();
		catalogVersion.setCatalogId(CATALOG_ID);
		catalogVersion.setVersion(STAGED_CATALOG_VERSION);

		final NavigationContextData expectedNavigationContext = new NavigationContextData();
		expectedNavigationContext.setIndexConfiguration(INDEX_CONFIGURATION_CODE);
		expectedNavigationContext.setIndexType(INDEX_TYPE_CODE);
		expectedNavigationContext.setCatalogVersion(catalogVersion);
		expectedNavigationContext.setSearchProfiles(Collections.emptyList());

		when(onlineCatalogVersion.getActive()).thenReturn(Boolean.FALSE);
		when(stagedCatalogVersion.getActive()).thenReturn(Boolean.TRUE);

		// when
		initializeController();
		updateIndexConfiguration(INDEX_CONFIGURATION_CODE);
		updateIndexType(INDEX_TYPE_CODE);

		// then
		assertSocketOutputs(NAVIGATION_CONTEXT_SOCKET, expectedNavigationContext);
	}

	@Test
	public void updateCategory()
	{
		// given
		final CatalogVersionData catalogVersion = new CatalogVersionData();
		catalogVersion.setCatalogId(CATALOG_ID);
		catalogVersion.setVersion(ONLINE_CATALOG_VERSION);

		final CategoryData category = new CategoryData();
		category.setCode(CATEGORY_CODE);
		category.setPath(Arrays.asList(CATEGORY_PATH.split("/")));

		final NavigationContextData expectedNavigationContext = new NavigationContextData();
		expectedNavigationContext.setIndexConfiguration(INDEX_CONFIGURATION_CODE);
		expectedNavigationContext.setIndexType(INDEX_TYPE_CODE);
		expectedNavigationContext.setCatalogVersion(catalogVersion);
		expectedNavigationContext.setCategory(category);
		expectedNavigationContext.setSearchProfiles(Collections.emptyList());

		// when
		initializeController();
		updateIndexConfiguration(INDEX_CONFIGURATION_CODE);
		updateIndexType(INDEX_TYPE_CODE);
		executeInputSocketEvent(NavigationContextController.CATEGORY_IN_SOCKET, category);

		// then
		assertSocketOutputs(NAVIGATION_CONTEXT_SOCKET, expectedNavigationContext);
	}

	protected void assertSocketOutputs(final String socketId, final Object... outputMatchers)
	{
		final InOrder inOrder = inOrder(widgetInstanceManager);

		for (final Object outputMatcher : outputMatchers)
		{
			inOrder.verify(widgetInstanceManager).sendOutput(socketId, outputMatcher);
		}
	}
}
