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
package com.hybris.backoffice.widgets.selectivesync.renderer;


import de.hybris.platform.catalog.model.SyncAttributeDescriptorConfigModel;
import de.hybris.platform.catalog.model.synchronization.CatalogVersionSyncJobModel;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.servicelayer.type.TypeService;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Div;

import com.hybris.backoffice.widgets.selectivesync.SelectiveSyncController;
import com.hybris.backoffice.widgets.selectivesync.detailsview.DetailsView;
import com.hybris.backoffice.widgets.selectivesync.tree.FilteredTreeView;
import com.hybris.backoffice.widgets.selectivesync.tree.SyncAttributeTreeModel;
import com.hybris.backoffice.widgets.selectivesync.tree.SyncAttributeTreeModelFactory;
import com.hybris.backoffice.widgets.selectivesync.tree.SyncTypeAttributeDataTreeItemRenderer;
import com.hybris.cockpitng.dataaccess.facades.permissions.PermissionFacade;
import com.hybris.cockpitng.engine.WidgetInstanceManager;


/**
 * Renderer for the {@link CatalogVersionSyncJobModel}'s attribute syncAttributeConfigurations. It shows the sync
 * attributes and types they belong to in a tree structure.
 */
public class SelectiveSyncRenderer
{
	private static final String SCLASS_SYNC_ATTRIBUTE_CONFIG_CONTAINER = "sync-attribute-config-container";

	private SyncAttributeTreeModel treeModel;
	private WidgetInstanceManager widgetInstanceManager;

	private PermissionFacade permissionFacade;
	private TypeService typeService;
	private SyncAttributeTreeModelFactory syncAttributeTreeModelFactory;

	public void render(final Component parent, final CatalogVersionSyncJobModel model,
			final WidgetInstanceManager widgetInstanceManager)
	{
		if (parent == null)
		{
			return;
		}
		this.widgetInstanceManager = widgetInstanceManager;
		this.treeModel = createSyncAttributeTreeModel(model.getSyncAttributeConfigurations(),
				getTypeService().getComposedTypeForCode(ItemModel._TYPECODE));

		final FilteredTreeView filteredTreeView = createSyncAttributeTreeViewSection();

		final DetailsView detailsView = createDetailsView();

		final Div container = createContainer();
		container.appendChild(filteredTreeView);
		container.appendChild(detailsView);
		parent.appendChild(container);

		final SyncTypeAttributeDataTreeItemRenderer treeItemRenderer = createTreeItemRenderer(model, detailsView);
		filteredTreeView.setTreeItemRenderer(treeItemRenderer);
	}

	protected SyncAttributeTreeModel createSyncAttributeTreeModel(
			final Collection<SyncAttributeDescriptorConfigModel> syncAttributeDescriptors, final ComposedTypeModel rootType)
	{
		return syncAttributeTreeModelFactory.create(syncAttributeDescriptors, rootType);
	}

	protected FilteredTreeView createSyncAttributeTreeViewSection()
	{
		return new FilteredTreeView(treeModel, widgetInstanceManager);
	}

	protected SyncTypeAttributeDataTreeItemRenderer createTreeItemRenderer(final CatalogVersionSyncJobModel model,
			final DetailsView detailsView)
	{
		final SyncTypeAttributeDataTreeItemRenderer.CreationContext creationContext = new SyncTypeAttributeDataTreeItemRenderer.CreationContext();
		creationContext.setSelectiveSyncModelChangeListener((final Object ignoredSrc,
				final Collection<SyncAttributeDescriptorConfigModel> collection) -> valueChanged(model, collection));
		creationContext.setDetailsView(detailsView);
		creationContext.setEditable(isEditable());
		creationContext.setDataModel(treeModel);
		creationContext.setPermissionFacade(permissionFacade);

		return new SyncTypeAttributeDataTreeItemRenderer(creationContext);
	}

	protected boolean isEditable()
	{
		return permissionFacade.canChangeType(CatalogVersionSyncJobModel._TYPECODE)
				&& permissionFacade.canChangeType(SyncAttributeDescriptorConfigModel._TYPECODE);
	}

	protected Div createContainer()
	{
		final Div container = new Div();
		container.setSclass(SCLASS_SYNC_ATTRIBUTE_CONFIG_CONTAINER);
		return container;
	}

	protected DetailsView createDetailsView()
	{
		final DetailsView.CreationContext creationContext = new DetailsView.CreationContext();
		creationContext.setWidgetInstanceManager(widgetInstanceManager);
		creationContext.setEditable(isEditable());
		creationContext.setPermissionFacade(permissionFacade);
		return new DetailsView(creationContext);
	}

	protected void valueChanged(final CatalogVersionSyncJobModel model,
			final Collection<SyncAttributeDescriptorConfigModel> collection)
	{
		model.setSyncAttributeConfigurations(collection);
		getWidgetInstanceManager().getModel().setValue(SelectiveSyncController.MODEL_VALUE_CHANGED, Boolean.TRUE);
	}

	protected WidgetInstanceManager getWidgetInstanceManager()
	{
		return widgetInstanceManager;
	}

	public PermissionFacade getPermissionFacade()
	{
		return permissionFacade;
	}

	@Required
	public void setPermissionFacade(final PermissionFacade permissionFacade)
	{
		this.permissionFacade = permissionFacade;
	}

	public TypeService getTypeService()
	{
		return typeService;
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}

	public SyncAttributeTreeModelFactory getSyncAttributeTreeModelFactory()
	{
		return syncAttributeTreeModelFactory;
	}

	@Required
	public void setSyncAttributeTreeModelFactory(final SyncAttributeTreeModelFactory syncAttributeTreeModelFactory)
	{
		this.syncAttributeTreeModelFactory = syncAttributeTreeModelFactory;
	}
}
