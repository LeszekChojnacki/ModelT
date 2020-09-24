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
import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.PARENT_OBJECT_KEY;

import de.hybris.platform.adaptivesearch.data.AsIndexConfigurationData;
import de.hybris.platform.adaptivesearch.data.AsIndexTypeData;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchProfileModel;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProvider;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProviderFactory;
import de.hybris.platform.adaptivesearchbackoffice.data.CatalogVersionData;
import de.hybris.platform.adaptivesearchbackoffice.data.CategoryData;
import de.hybris.platform.adaptivesearchbackoffice.data.NavigationContextData;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ListModelList;

import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.annotations.ViewEvent;
import com.hybris.cockpitng.components.Editor;
import com.hybris.cockpitng.labels.LabelService;
import com.hybris.cockpitng.util.DefaultWidgetController;
import com.hybris.cockpitng.widgets.controller.collapsiblecontainer.CollapsibleContainerState;


/**
 * Controller for the navigation context widget.
 */
public class NavigationContextController extends DefaultWidgetController
{
	protected static final String INDEX_CONFIGURATION_SELECTOR_ID = "indexConfigurationSelector";
	protected static final String INDEX_TYPE_SELECTOR_ID = "indexTypeSelector";
	protected static final String CATALOG_VERSION_SELECTOR_ID = "catalogVersionSelector";
	protected static final String SEARCH_PROFILE_SELECTOR_ID = "searchProfileSelector";

	protected static final String ON_VALUE_CHANGED = "onValueChanged";

	protected static final String CATEGORY_IN_SOCKET = "category";
	protected static final String COLLAPSE_STATE_OUT_SOCKET = "collapseState";

	protected static final String NAVIGATION_CONTEXT_KEY = "navigationContext";

	protected static final String INDEX_TYPE_KEY = "indexType";
	protected static final String CATALOG_VERSION_KEY = "catalogVersion";

	protected static final String SEARCH_PROFILE_CREATE_WIZARD_CTX = "create-wizard-with-parent";

	@WireVariable
	protected transient SessionService sessionService;

	@WireVariable
	protected transient I18NService i18nService;

	@WireVariable
	protected transient CatalogVersionService catalogVersionService;

	@WireVariable
	protected transient AsSearchProviderFactory asSearchProviderFactory;

	@WireVariable
	protected transient LabelService labelService;

	protected Combobox indexConfigurationSelector;
	protected Combobox indexTypeSelector;
	protected Combobox catalogVersionSelector;
	protected Editor searchProfileSelector;

	private final ListModelList<IndexConfigurationModel> indexConfigurationsModel = new ListModelList<>();
	private final ListModelList<IndexTypeModel> indexTypesModel = new ListModelList<>();
	private final ListModelList<CatalogVersionModel> catalogVersionsModel = new ListModelList<>();

	public ListModelList<IndexConfigurationModel> getIndexConfigurationsModel()
	{
		return indexConfigurationsModel;
	}

	public ListModelList<IndexTypeModel> getIndexTypesModel()
	{
		return indexTypesModel;
	}

	public ListModelList<CatalogVersionModel> getCatalogVersionsModel()
	{
		return catalogVersionsModel;
	}

	public NavigationContextData getNavigationContext()
	{
		return getModel().getValue(NAVIGATION_CONTEXT_KEY, NavigationContextData.class);
	}

	public void setNavigationContext(final NavigationContextData navigationContext)
	{
		getModel().put(NAVIGATION_CONTEXT_KEY, navigationContext);
	}

	@Override
	public void initialize(final Component component)
	{
		initializeSelectors();

		component.addEventListener(Events.ON_CREATE, event -> {
			final NavigationContextData navigationContext = getNavigationContext();
			updateSelectors(navigationContext);
			sendNavigationContext(navigationContext);
		});
	}

	protected void initializeSelectors()
	{
		final NavigationContextData navigationContext = new NavigationContextData();
		navigationContext.setIndexConfiguration(null);
		navigationContext.setIndexType(null);
		navigationContext.setCatalogVersion(null);
		navigationContext.setSearchProfiles(null);
		navigationContext.setCurrentSearchProfile(null);

		setNavigationContext(navigationContext);

		indexConfigurationSelector.setModel(indexConfigurationsModel);
		indexTypeSelector.setModel(indexTypesModel);
		catalogVersionSelector.setModel(catalogVersionsModel);
		searchProfileSelector.setValue(null);
	}

	protected void updateSelectors(final NavigationContextData navigationContext)
	{
		sessionService.executeInLocalView(new SessionExecutionBody()
		{
			@Override
			public void executeWithoutResult()
			{
				i18nService.setLocalizationFallbackEnabled(true);

				updateIndexConfigurations(navigationContext);
				updateIndexTypes(navigationContext);
				updateCatalogVersions(navigationContext);
				updateSearchProfiles(navigationContext);

			}
		});
	}

	protected void updateIndexConfigurations(final NavigationContextData navigationContext)
	{
		final Set<IndexConfigurationModel> selection = indexConfigurationsModel.getSelection();
		IndexConfigurationModel selectedIndexConfiguration = selection.isEmpty() ? null : selection.iterator().next();

		final List<IndexConfigurationModel> indexConfigurations = findIndexConfigurations();
		indexConfigurations.add(0, null);

		if (!CollectionUtils.isEqualCollection(indexConfigurationsModel.getInnerList(), indexConfigurations))
		{
			indexConfigurationsModel.clear();
			indexConfigurationsModel.addAll(indexConfigurations);
		}

		if (CollectionUtils.isEmpty(indexConfigurationsModel))
		{
			navigationContext.setIndexConfiguration(null);
			indexConfigurationsModel.setSelection(Collections.emptyList());
		}
		else if (StringUtils.isBlank(navigationContext.getIndexConfiguration())
				|| !indexConfigurationsModel.contains(selectedIndexConfiguration))
		{
			selectedIndexConfiguration = indexConfigurations.get(0);
			navigationContext
					.setIndexConfiguration(selectedIndexConfiguration == null ? null : selectedIndexConfiguration.getCode());
			indexConfigurationsModel.setSelection(Collections.singletonList(selectedIndexConfiguration));
		}

	}

	protected void updateIndexTypes(final NavigationContextData navigationContext)
	{
		final Set<IndexTypeModel> selection = indexTypesModel.getSelection();
		IndexTypeModel selectedIndexType = selection.isEmpty() ? null : selection.iterator().next();

		final List<IndexTypeModel> indexTypes = findIndexTypes(navigationContext.getIndexConfiguration());

		if (!CollectionUtils.isEqualCollection(indexTypesModel.getInnerList(), indexTypes))
		{
			indexTypesModel.clear();
			indexTypesModel.addAll(indexTypes);
		}

		if (CollectionUtils.isEmpty(indexTypesModel))
		{
			navigationContext.setIndexType(null);
			indexTypesModel.setSelection(Collections.emptyList());
			indexTypeSelector.setDisabled(true);
		}
		else if (StringUtils.isBlank(navigationContext.getIndexConfiguration()) || !indexTypesModel.contains(selectedIndexType))
		{
			selectedIndexType = indexTypes.get(0);
			navigationContext.setIndexType(selectedIndexType.getCode());
			indexTypesModel.setSelection(Collections.singletonList(selectedIndexType));
			indexTypeSelector.setDisabled(false);
		}
	}

	protected void updateCatalogVersions(final NavigationContextData navigationContext)
	{
		final Set<CatalogVersionModel> selection = catalogVersionsModel.getSelection();
		CatalogVersionModel selectedCatalogVersion = selection.isEmpty() ? null : selection.iterator().next();

		final List<CatalogVersionModel> catalogVersions = findCatalogVersions(navigationContext.getIndexConfiguration(),
				navigationContext.getIndexType());

		if (!CollectionUtils.isEqualCollection(catalogVersionsModel.getInnerList(), catalogVersions))
		{
			catalogVersionsModel.clear();
			catalogVersionsModel.addAll(catalogVersions);
		}

		if (CollectionUtils.isEmpty(catalogVersionsModel))
		{
			navigationContext.setCatalogVersion(null);
			catalogVersionsModel.setSelection(Collections.emptyList());
			catalogVersionSelector.setDisabled(true);
		}
		else if (navigationContext.getCatalogVersion() == null || !catalogVersionsModel.contains(selectedCatalogVersion))
		{
			// selects active catalog version by default
			final Optional<CatalogVersionModel> activeCatalogVersion = catalogVersions.stream().filter(CatalogVersionModel::isActive)
					.findFirst();

			selectedCatalogVersion = activeCatalogVersion.isPresent() ? activeCatalogVersion.get() : catalogVersions.get(0);

			navigationContext.setCatalogVersion(selectedCatalogVersion.getCatalogVersion());
			catalogVersionsModel.setSelection(Collections.singletonList(selectedCatalogVersion));
			catalogVersionSelector.setDisabled(false);
		}
	}

	protected void updateSearchProfiles(final NavigationContextData navigationContext)
	{
		final CatalogVersionData catalogVersionData = navigationContext.getCatalogVersion();

		final de.hybris.platform.catalog.model.CatalogVersionModel catalogVersion = catalogVersionData == null ? null
				: catalogVersionService.getCatalogVersion(catalogVersionData.getCatalogId(), catalogVersionData.getVersion());
		final AbstractAsSearchProfileModel searchProfile = (AbstractAsSearchProfileModel) searchProfileSelector.getValue();

		if (searchProfile == null || !Objects.equals(searchProfile.getIndexType(), navigationContext.getIndexType())
				|| !Objects.equals(searchProfile.getCatalogVersion(), catalogVersion))
		{
			navigationContext.setSearchProfiles(Collections.emptyList());
			navigationContext.setCurrentSearchProfile(null);

			final Object parentObject = createParentObject(navigationContext);
			searchProfileSelector.setAttribute(PARENT_OBJECT_KEY, parentObject);

			searchProfileSelector.setValue(null);
			searchProfileSelector.getParameters().put("configurableFlowConfigCtx", SEARCH_PROFILE_CREATE_WIZARD_CTX);
			searchProfileSelector.getParameters().put("referenceSearchCondition_indexType", navigationContext.getIndexType());
			searchProfileSelector.getParameters().put("referenceSearchCondition_catalogVersion", catalogVersion);
			searchProfileSelector.reload();
		}

		searchProfileSelector.setReadOnly(navigationContext.getIndexConfiguration() == null);
	}

	protected Object createParentObject(final NavigationContextData navigationContext)
	{
		final String indexType = navigationContext.getIndexType();
		final de.hybris.platform.catalog.model.CatalogVersionModel catalogVersion = resolveCatalogVersion(
				navigationContext.getCatalogVersion());

		final Map<String, Object> parentObject = new HashMap<>();
		parentObject.put(INDEX_TYPE_KEY, indexType);
		parentObject.put(CATALOG_VERSION_KEY, catalogVersion);

		return parentObject;
	}

	protected void sendNavigationContext(final NavigationContextData navigationContext)
	{
		sendOutput(NAVIGATION_CONTEXT_SOCKET, navigationContext);
	}

	protected void collapseNavigationContext()
	{
		sendOutput(COLLAPSE_STATE_OUT_SOCKET, new CollapsibleContainerState(Boolean.FALSE, Boolean.TRUE, Boolean.FALSE));
	}

	/**
	 * Event handler for index configuration changes.
	 *
	 * @param event
	 *           - the event
	 */
	@ViewEvent(componentID = INDEX_CONFIGURATION_SELECTOR_ID, eventName = Events.ON_SELECT)
	public void onIndexConfigurationChanged(final SelectEvent<Comboitem, String> event)
	{
		final NavigationContextData navigationContext = getNavigationContext();
		final String newIndexConfiguration = event.getReference().getValue();

		if (navigationContext != null && !Objects.equals(navigationContext.getIndexConfiguration(), newIndexConfiguration))
		{
			navigationContext.setIndexConfiguration(newIndexConfiguration);
			updateSelectors(navigationContext);
			sendNavigationContext(navigationContext);
		}
	}

	/**
	 * Event handler for index type changes.
	 *
	 * @param event
	 *           - the event
	 */
	@ViewEvent(componentID = INDEX_TYPE_SELECTOR_ID, eventName = Events.ON_SELECT)
	public void onIndexTypeChanged(final SelectEvent<Comboitem, String> event)
	{
		final NavigationContextData navigationContext = getNavigationContext();
		final String newIndexType = event.getReference().getValue();

		if (navigationContext != null && !Objects.equals(navigationContext.getIndexType(), newIndexType))
		{
			navigationContext.setIndexType(newIndexType);
			updateSelectors(navigationContext);
			sendNavigationContext(navigationContext);
		}
	}

	/**
	 * Event handler for catalog version changes.
	 *
	 * @param event
	 *           - the event
	 */
	@ViewEvent(componentID = CATALOG_VERSION_SELECTOR_ID, eventName = Events.ON_SELECT)
	public void onCatalogVersionChanged(final SelectEvent<Comboitem, CatalogVersionData> event)
	{
		final NavigationContextData navigationContext = getNavigationContext();
		final CatalogVersionData newCatalogVersion = event.getReference().getValue();

		if (navigationContext != null && !Objects.equals(navigationContext.getCatalogVersion(), newCatalogVersion))
		{
			navigationContext.setCatalogVersion(newCatalogVersion);
			updateSelectors(navigationContext);
			sendNavigationContext(navigationContext);
		}
	}

	/**
	 * Event handler for search profile changes.
	 */
	@ViewEvent(componentID = SEARCH_PROFILE_SELECTOR_ID, eventName = ON_VALUE_CHANGED)
	public void onSearchProfileChanged()
	{
		final NavigationContextData navigationContext = getNavigationContext();
		final AbstractAsSearchProfileModel newSearchProfile = (AbstractAsSearchProfileModel) searchProfileSelector.getValue();

		if (navigationContext != null && !Objects.equals(navigationContext.getCurrentSearchProfile(), newSearchProfile))
		{
			if (newSearchProfile == null)
			{
				navigationContext.setSearchProfiles(Collections.emptyList());
				navigationContext.setCurrentSearchProfile(null);

			}
			else
			{
				navigationContext.setSearchProfiles(Collections.singletonList(newSearchProfile.getCode()));
				navigationContext.setCurrentSearchProfile(newSearchProfile.getCode());
				collapseNavigationContext();
			}

			updateSelectors(navigationContext);
			sendNavigationContext(navigationContext);
		}
	}

	/**
	 * Event handler for category changes.
	 *
	 * @param category
	 *           - the category
	 */
	@SocketEvent(socketId = CATEGORY_IN_SOCKET)
	public void onCategoryChanged(final CategoryData category)
	{
		final NavigationContextData navigationContext = getNavigationContext();

		if (navigationContext != null && !Objects.equals(navigationContext.getCategory(), category))
		{
			navigationContext.setCategory(category);
			sendNavigationContext(navigationContext);
		}
	}

	protected List<IndexConfigurationModel> findIndexConfigurations()
	{
		final AsSearchProvider searchProvider = asSearchProviderFactory.getSearchProvider();
		final List<AsIndexConfigurationData> indexConfigurations = searchProvider.getIndexConfigurations();

		return indexConfigurations.stream().filter(this::isValidIndexConfiguration).map(this::convertIndexConfiguration)
				.sorted(this::compareIndexConfigurations).collect(Collectors.toList());
	}

	protected boolean isValidIndexConfiguration(final AsIndexConfigurationData indexConfiguration)
	{
		return indexConfiguration != null && StringUtils.isNotBlank(indexConfiguration.getCode());
	}

	protected IndexConfigurationModel convertIndexConfiguration(final AsIndexConfigurationData source)
	{
		final String name = source.getName() != null ? source.getName() : source.getCode();

		final IndexConfigurationModel target = new IndexConfigurationModel();
		target.setCode(source.getCode());
		target.setName(name);
		return target;
	}

	protected int compareIndexConfigurations(final IndexConfigurationModel indexConfiguration1,
			final IndexConfigurationModel indexConfiguration2)
	{
		return indexConfiguration1.getName().compareTo(indexConfiguration2.getName());
	}

	protected List<IndexTypeModel> findIndexTypes(final String indexConfiguration)
	{
		if (indexConfiguration == null)
		{
			return Collections.emptyList();
		}

		final AsSearchProvider searchProvider = asSearchProviderFactory.getSearchProvider();
		final List<AsIndexTypeData> indexTypes = searchProvider.getIndexTypes(indexConfiguration);

		return indexTypes.stream().filter(this::isValidIndexType).map(this::convertIndexType).sorted(this::compareIndexTypes)
				.collect(Collectors.toList());
	}

	protected boolean isValidIndexType(final AsIndexTypeData indexType)
	{
		return indexType != null && StringUtils.isNotBlank(indexType.getCode());
	}

	protected IndexTypeModel convertIndexType(final AsIndexTypeData source)
	{
		final String name = source.getName() != null ? source.getName() : source.getCode();

		final IndexTypeModel target = new IndexTypeModel();
		target.setCode(source.getCode());
		target.setName(name);
		return target;
	}

	protected int compareIndexTypes(final IndexTypeModel indexType1, final IndexTypeModel indexType2)
	{
		return indexType1.getName().compareTo(indexType2.getName());
	}

	protected List<CatalogVersionModel> findCatalogVersions(final String indexConfiguration, final String indexType)
	{
		if (indexConfiguration == null || indexType == null)
		{
			return Collections.emptyList();
		}

		final AsSearchProvider searchProvider = asSearchProviderFactory.getSearchProvider();
		final List<de.hybris.platform.catalog.model.CatalogVersionModel> catalogVersions = searchProvider
				.getSupportedCatalogVersions(indexConfiguration, indexType);

		return catalogVersions.stream().filter(this::isValidCatalogVersion).map(this::convertCatalogVersion)
				.sorted(this::compareCatalogVersions).collect(Collectors.toList());
	}

	protected boolean isValidCatalogVersion(final de.hybris.platform.catalog.model.CatalogVersionModel catalogVersion)
	{
		return catalogVersion != null && StringUtils.isNotBlank(catalogVersion.getVersion()) && catalogVersion.getCatalog() != null
				&& StringUtils.isNotBlank(catalogVersion.getCatalog().getId());
	}

	protected CatalogVersionModel convertCatalogVersion(final de.hybris.platform.catalog.model.CatalogVersionModel source)
	{
		final CatalogVersionModel target = new CatalogVersionModel();

		final CatalogVersionData catalogVersionData = new CatalogVersionData();
		catalogVersionData.setCatalogId(source.getCatalog().getId());
		catalogVersionData.setVersion(source.getVersion());

		target.setCatalogVersion(catalogVersionData);
		target.setActive(BooleanUtils.toBoolean(source.getActive()));
		target.setName(labelService.getObjectLabel(source));

		return target;
	}

	protected int compareCatalogVersions(final CatalogVersionModel catalogVersion1, final CatalogVersionModel catalogVersion2)
	{
		return catalogVersion1.getName().compareTo(catalogVersion2.getName());
	}

	protected de.hybris.platform.catalog.model.CatalogVersionModel resolveCatalogVersion(final CatalogVersionData catalogVersion)
	{
		if (catalogVersion == null)
		{
			return null;
		}

		return catalogVersionService.getCatalogVersion(catalogVersion.getCatalogId(), catalogVersion.getVersion());
	}
}
