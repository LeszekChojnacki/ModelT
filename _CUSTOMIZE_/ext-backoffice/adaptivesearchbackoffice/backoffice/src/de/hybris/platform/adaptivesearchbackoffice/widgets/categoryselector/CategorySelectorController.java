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

import de.hybris.platform.adaptivesearch.model.AbstractAsSearchProfileModel;
import de.hybris.platform.adaptivesearch.services.AsSearchConfigurationService;
import de.hybris.platform.adaptivesearch.services.AsSearchProfileService;
import de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants;
import de.hybris.platform.adaptivesearchbackoffice.data.AsCategoryData;
import de.hybris.platform.adaptivesearchbackoffice.data.CatalogVersionData;
import de.hybris.platform.adaptivesearchbackoffice.data.CategoryData;
import de.hybris.platform.adaptivesearchbackoffice.data.NavigationContextData;
import de.hybris.platform.adaptivesearchbackoffice.facades.AsCategoryFacade;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.DefaultTreeModel;
import org.zkoss.zul.DefaultTreeNode;
import org.zkoss.zul.Tree;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.TreeNode;
import org.zkoss.zul.Treeitem;

import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.annotations.ViewEvent;
import com.hybris.cockpitng.util.DefaultWidgetController;


/**
 * Controller for category selector widget.
 */
public class CategorySelectorController extends DefaultWidgetController
{
	protected static final String CATEGORY_SELECTOR_ID = "categorySelector";

	protected static final String SELECTED_CATEGORY_OUT_SOCKET = "selectedCategory";

	protected static final int[] ROOT_NODE_PATH =
	{ 0 };

	@WireVariable
	protected transient CatalogVersionService catalogVersionService;

	@WireVariable
	protected transient SessionService sessionService;

	@WireVariable
	protected transient I18NService i18nService;

	@WireVariable
	protected transient AsCategoryFacade asCategoryFacade;

	@WireVariable
	protected transient AsSearchProfileService asSearchProfileService;

	@WireVariable
	protected transient AsSearchConfigurationService asSearchConfigurationService;

	protected Tree categorySelector;

	protected transient TreeModel<TreeNode<CategoryModel>> categoriesModel;

	private Set<String> qualifiers;

	private CatalogVersionData catalogVersion;
	private CategoryData selectedCategory;
	private String searchProfile;

	private NavigationContextData navigationContext;

	@Override
	public void initialize(final Component component)
	{
		initializeCategoriesTree();
	}

	protected void initializeCategoriesTree()
	{
		final TreeNode<CategoryModel> rootNode = new DefaultTreeNode<>(null, new ArrayList<>());
		categoriesModel = new DefaultTreeModel<>(rootNode);
		categorySelector.setModel(categoriesModel);

		populateCategoriesTree();
	}

	protected void updateCategoriesTree()
	{
		sessionService.executeInLocalView(new SessionExecutionBody()
		{
			@Override
			public void executeWithoutResult()
			{
				i18nService.setLocalizationFallbackEnabled(true);

				populateCategoriesTree();
			}
		});
	}

	protected void populateCategoriesTree()
	{
		categoriesModel.getRoot().getChildren().clear();

		final AsCategoryData globalCategory = catalogVersion == null ? asCategoryFacade.getCategoryHierarchy()
				: asCategoryFacade.getCategoryHierarchy(catalogVersion.getCatalogId(), catalogVersion.getVersion());

		if (globalCategory != null)
		{
			populateCategoriesTreeNode(categoriesModel.getRoot(), globalCategory, qualifiers);

			if (categoriesModel instanceof DefaultTreeModel)
			{
				final DefaultTreeModel treeModel = (DefaultTreeModel) categoriesModel;
				treeModel.clearOpen();
				treeModel.addOpenPath(ROOT_NODE_PATH);

				treeModel.clearSelection();
				treeModel.addSelectionPath(ROOT_NODE_PATH);
			}
		}
	}

	protected Set<String> populateCategoriesTreeNode(final TreeNode<CategoryModel> parentNode, final AsCategoryData category,
			final Set<String> qualifiers)
	{
		final TreeNode<CategoryModel> treeNode;
		final Set<String> childrenConfigurationQualifiers = new HashSet<String>();

		final CategoryModel categoryModel = new CategoryModel();
		categoryModel.setCode(category.getCode());
		categoryModel.setName(category.getName());

		if (qualifiers != null && qualifiers.contains(category.getCode()))
		{
			categoryModel.setHasSearchConfiguration(true);

			//Using a set to return the unique number of children configurations
			childrenConfigurationQualifiers.add(category.getCode());
		}

		if (CollectionUtils.isEmpty(category.getChildren()))
		{
			treeNode = new DefaultTreeNode<>(categoryModel);
		}
		else
		{
			treeNode = new DefaultTreeNode<>(categoryModel, new ArrayList<>());
			for (final AsCategoryData childCategory : category.getChildren())
			{
				//populate the children categories...
				childrenConfigurationQualifiers.addAll(populateCategoriesTreeNode(treeNode, childCategory, qualifiers));
			}

		}

		categoryModel.setNumberOfConfigurations(childrenConfigurationQualifiers.size());
		parentNode.add(treeNode);

		return childrenConfigurationQualifiers;
	}

	protected void sendSelectedCategory()
	{
		if (selectedCategory != null)
		{
			// creates a clone to prevent modifications outside the widget
			final CategoryData clonedSelectedCategory = new CategoryData();
			clonedSelectedCategory.setCode(selectedCategory.getCode());
			clonedSelectedCategory.setPath(selectedCategory.getPath());

			sendOutput(SELECTED_CATEGORY_OUT_SOCKET, clonedSelectedCategory);
		}
		else
		{
			sendOutput(SELECTED_CATEGORY_OUT_SOCKET, null);
		}
	}

	/**
	 * Event handler for navigation context changes.
	 *
	 * @param newNavigationContext
	 *           - the new navigation context
	 */
	@SocketEvent(socketId = NAVIGATION_CONTEXT_SOCKET)
	public void onNavigationContextChanged(final NavigationContextData newNavigationContext)
	{
		if (isNavigationContextChanged(newNavigationContext))
		{
			this.navigationContext = newNavigationContext;
			this.catalogVersion = navigationContext == null ? null : navigationContext.getCatalogVersion();
			this.searchProfile = navigationContext == null ? null : navigationContext.getCurrentSearchProfile();
			this.selectedCategory = null;

			updateQualifiers(navigationContext);
			updateCategoriesTree();
			sendSelectedCategory();
		}
	}

	protected boolean isNavigationContextChanged(final NavigationContextData newNavigationContext)
	{
		if (newNavigationContext == null)
		{
			return false;
		}

		if (!Objects.equals(newNavigationContext.getCatalogVersion(), this.catalogVersion))
		{
			return true;
		}

		if (!Objects.equals(newNavigationContext.getCategory(), this.selectedCategory))
		{
			return true;
		}

		return !Objects.equals(newNavigationContext.getCurrentSearchProfile(), this.searchProfile);
	}

	/**
	 * Event handler for selected category changes.
	 *
	 * @param event
	 *           - the event
	 */
	@ViewEvent(componentID = CATEGORY_SELECTOR_ID, eventName = Events.ON_SELECT)
	public void onCategorySelected(final SelectEvent<Treeitem, String> event)
	{
		final Treeitem selectedItem = event.getReference();
		final CategoryModel selectedCategoryModel = selectedItem.getValue();
		selectedCategory = new CategoryData();
		selectedCategory.setCode(selectedCategoryModel.getCode());

		final List<String> categoryPath = new ArrayList<>();
		buildCategoryPath(selectedItem, categoryPath);
		selectedCategory.setPath(categoryPath);

		sendSelectedCategory();
	}

	protected void buildCategoryPath(final Treeitem selectedItem, final List<String> path)
	{
		if (selectedItem.getParentItem() != null)
		{
			buildCategoryPath(selectedItem.getParentItem(), path);
		}

		if (selectedItem.getValue() instanceof CategoryModel)
		{
			final CategoryModel category = selectedItem.getValue();
			if (category.getCode() != null)
			{
				path.add(category.getCode());
			}
		}
	}

	protected CatalogVersionModel resolveCatalogVersion(final CatalogVersionData catalogVersion)
	{
		if (catalogVersion == null)
		{
			return null;
		}

		return catalogVersionService.getCatalogVersion(catalogVersion.getCatalogId(), catalogVersion.getVersion());
	}


	@SocketEvent(socketId = AdaptivesearchbackofficeConstants.REFRESH_SOCKET)
	public void refreshCategoryAfterChange(final Object obj)
	{
		if (categoriesModel instanceof DefaultTreeModel)
		{
			final Set<String> oldQualifiers = qualifiers;
			updateQualifiers(navigationContext);

			if (!Objects.equals(qualifiers, oldQualifiers))
			{
				final DefaultTreeModel treeModel = (DefaultTreeModel) categoriesModel;
				final int[] selectionPath = treeModel.getSelectionPath();
				final int[][] openPaths = treeModel.getOpenPaths();

				updateCategoriesTree();

				treeModel.addOpenPaths(openPaths);
				treeModel.addSelectionPath(selectionPath);
			}

		}

	}

	protected void updateQualifiers(final NavigationContextData navigationContext)
	{
		if (navigationContext != null && StringUtils.isNotEmpty(navigationContext.getCurrentSearchProfile()))
		{
			final CatalogVersionModel catalog = resolveCatalogVersion(navigationContext.getCatalogVersion());
			final Optional<AbstractAsSearchProfileModel> searchProfileOptional = asSearchProfileService
					.getSearchProfileForCode(catalog, navigationContext.getCurrentSearchProfile());
			if (searchProfileOptional.isPresent())
			{
				qualifiers = asSearchConfigurationService.getSearchConfigurationQualifiers(searchProfileOptional.get());
			}
			else
			{
				qualifiers = Collections.emptySet();
			}
		}
		else
		{
			qualifiers = Collections.emptySet();
		}
	}

	public TreeModel<TreeNode<CategoryModel>> getCategoriesModel()
	{
		return categoriesModel;
	}

	protected CatalogVersionData getCatalogVersion()
	{
		return catalogVersion;
	}

	protected void setCatalogVersion(final CatalogVersionData catalogVersion)
	{
		this.catalogVersion = catalogVersion;
	}

	protected CategoryData getSelectedCategory()
	{
		return selectedCategory;
	}

	protected void setSelectedCategory(final CategoryData selectedCategory)
	{
		this.selectedCategory = selectedCategory;
	}

	public void setNavigationContext(final NavigationContextData navigationContext)
	{
		this.navigationContext = navigationContext;
	}

	public NavigationContextData getNavigationContext()
	{
		return navigationContext;
	}

}
