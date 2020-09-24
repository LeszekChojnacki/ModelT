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
package com.hybris.backoffice.widgets.catalog;

import com.hybris.cockpitng.annotations.SocketEvent;
import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.catalog.model.classification.ClassificationSystemModel;
import de.hybris.platform.catalog.model.classification.ClassificationSystemVersionModel;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.model.ItemModel;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zul.Button;
import org.zkoss.zul.Popup;
import org.zkoss.zul.Tree;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.TreeNode;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treechildren;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.Treerow;

import com.hybris.backoffice.tree.model.AllCatalogsTreeNode;
import com.hybris.backoffice.tree.model.CatalogTreeModelPopulator;
import com.hybris.backoffice.tree.model.CatalogTreeSimpleLabelProvider;
import com.hybris.cockpitng.annotations.GlobalCockpitEvent;
import com.hybris.cockpitng.core.context.CockpitContext;
import com.hybris.cockpitng.core.context.impl.DefaultCockpitContext;
import com.hybris.cockpitng.core.events.CockpitEvent;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectCRUDHandler;
import com.hybris.cockpitng.labels.LabelService;
import com.hybris.cockpitng.model.ComponentModelPopulator;
import com.hybris.cockpitng.util.DefaultWidgetController;
import com.hybris.cockpitng.util.UITools;
import com.hybris.cockpitng.widgets.common.explorertree.model.RefreshableTreeModel;


public class CatalogSelectorController extends DefaultWidgetController
{
	public static final String IN_SOCKET_CLEAR = "clear";
	public static final String OUT_SOCKET_SELECTION = "selection";
	public static final String OUT_SOCKET_SYNC_CATALOG_VERSION = "syncCatalogVersion";
	public static final String MODEL_SELECTED_DATA = "selected_data";
	public static final String SCLASS_YW_TREEROW_ALL = "yw-treerow-all";
	public static final String SCLASS_YW_TREEROW_CATALOG = "yw-treerow-catalog";
	public static final String SCLASS_YW_TREEROW_CLASSIFICATION_CATALOG = "yw-treerow-class-catalog";
	public static final String SCLASS_YW_TREEROW_CATALOG_VERSION = "yw-treerow-catalogVersion";
	public static final String SCLASS_YW_TREEROW_CLASSIFICATION_CATALOG_VERSION = "yw-treerow-class-catalogVersion";
	public static final String SCLASS_YW_TREEROW_CATALOG_VERSION_SYNC_BTN = "yw-treerow-catalogVersion-sync-cell-btn";
	public static final String CATALOG_TREE_MODEL_POPULATOR_BEAN_ID = "catalogTreeModelPopulatorBeanId";
	public static final String SETTING_SHOW_CATALOG_VERSION_SYNC_BTN = "showCatalogVersionSyncBtn";
	public static final String SETTING_SIMPLE_LABELS = "simpleLabels";

	public static final String ALL_CATALOGS_LABEL = "all.catalogs.label";

	private static final Logger LOG = LoggerFactory.getLogger(CatalogSelectorController.class);
	public static final String MODEL_POPUP_INITIALIZED = "popupInitialized";

	private transient ComponentModelPopulator<TreeModel<TreeNode<ItemModel>>> catalogTreeModelPopulator;

	private transient LabelService labelService;

	private transient CatalogTreeSimpleLabelProvider catalogTreeSimpleLabelProvider;

	@Wire
	private Button popupOpener;

	@Wire
	private Tree tree;

	@Wire
	private Popup popup;

	@Override
	public void initialize(final Component comp)
	{
		super.initialize(comp);
		final String populatorBeanId = getWidgetSettings().getString(CATALOG_TREE_MODEL_POPULATOR_BEAN_ID);
		if (StringUtils.isNotBlank(populatorBeanId))
		{
			final Object bean = SpringUtil.getBean(populatorBeanId);
			if (bean instanceof ComponentModelPopulator)
			{
				setCatalogTreeModelPopulator((ComponentModelPopulator<TreeModel<TreeNode<ItemModel>>>) bean);
			}
			else
			{
				LOG.warn("Resolved bean provided via '{}' setting of value '{}' is not compatible with the populator. Resolved: {}",
						CATALOG_TREE_MODEL_POPULATOR_BEAN_ID, populatorBeanId, bean);
			}
		}

		tree.setItemRenderer((item, node, i) -> {
			final Object data;
			final Object parentData;
			if (node instanceof TreeNode)
			{
				data = ((TreeNode) node).getData();
				final TreeNode parent = ((TreeNode) node).getParent();
				parentData = parent != null ? parent.getData() : null;
			}
			else
			{
				data = node;
				parentData = null;
			}
			item.setValue(data);

			Treerow treerow = item.getTreerow();
			if (treerow == null)
			{
				treerow = new Treerow();
				item.appendChild(treerow);
			}

			final Treecell contentCell = new Treecell();
			if (node instanceof AllCatalogsTreeNode)
			{
				contentCell.setLabel(getNullLabel());
				treerow.addEventListener(Events.ON_CLICK, event -> {
					renderItem(item, data);
					popup.close();
				});
			}
			else
			{
				final String label = calculateCellLabel(parentData, data);
				contentCell.setLabel(label);
				treerow.setClientAttribute("title", "catsel_"+label);
				treerow.addEventListener(Events.ON_CLICK, click -> renderItem(item, data));
			}

			treerow.appendChild(contentCell);

			if (data instanceof CatalogVersionModel && getWidgetSettings().getBoolean(SETTING_SHOW_CATALOG_VERSION_SYNC_BTN))
			{
				contentCell.appendChild(createSyncButton((CatalogVersionModel) data));
			}

			selectOnRender(item, getSelectedModels());
			adjustSClass(item, data);
		});

		final String label = calculateLabel(getSelectedModels());
		popupOpener.setLabel(label);
		popupOpener.setTooltiptext(label);
		popupOpener.setPopupAttributes(popup, "after_start", null, null, "toggle");
		lazyInitTreeData(popupOpener);
		tree.setMold("paging");
		tree.setPageSize(15);
	}

	protected void lazyInitTreeData(final Button popupOpener)
	{
		setValue(MODEL_POPUP_INITIALIZED, false);
		popupOpener.addEventListener(Events.ON_CLICK, new EventListener<Event>()
		{
			@Override
			public void onEvent(final Event event) throws Exception
			{
				if (BooleanUtils.isNotTrue(getValue(MODEL_POPUP_INITIALIZED, Boolean.class)))
				{
					popupOpener.removeEventListener(Events.ON_CLICK, this);
					reloadTree();
					setValue(MODEL_POPUP_INITIALIZED, true);
				}
			}
		});
	}

	protected String calculateCellLabel(final Object parentData, final Object nodeData)
	{
		if (getWidgetSettings().getBoolean(SETTING_SIMPLE_LABELS))
		{
			final Optional<String> calculatedLabel = getCatalogTreeSimpleLabelProvider().getNodeLabel(parentData, nodeData);
			if (calculatedLabel.isPresent())
			{
				return calculatedLabel.get();
			}
		}
		return getLabelService().getObjectLabel(nodeData);
	}

	protected HtmlBasedComponent createSyncButton(final CatalogVersionModel catalogVersionModel)
	{
		final Button syncBtn = new Button();
		syncBtn.setSclass(SCLASS_YW_TREEROW_CATALOG_VERSION_SYNC_BTN);
		syncBtn.addEventListener(Events.ON_CLICK, event -> synchronizeCatalogVersion(catalogVersionModel));
		return syncBtn;
	}

	protected void synchronizeCatalogVersion(final CatalogVersionModel data)
	{
		if (data != null)
		{
			sendOutput(OUT_SOCKET_SYNC_CATALOG_VERSION, data);
		}
	}

	protected void reloadTree()
	{
		tree.setCheckmark(true);
		final CockpitContext context = new DefaultCockpitContext();
		context.setParameter(CatalogTreeModelPopulator.MULTI_SELECT, Boolean.TRUE);
		tree.setModel(getCatalogTreeModelPopulator().createModel(context));
	}

	protected void renderItem(final Treeitem item, final Object data)
	{
		final Set<Object> selectedModels;
		if (data == null)
		{
			tree.selectItem(item);
			selectedModels = new HashSet<>();
		}
		else
		{
			selectedModels = getSelectedModels();

			final Set treeitemModels = new HashSet<>();
			tree.getSelectedItems().forEach(treeitem -> treeitemModels.add(treeitem.getValue()));
			final boolean select = treeitemModels.contains(data);
			if (select)
			{
				selectedModels.add(data);
				if (data instanceof CatalogModel)
				{
					selectAllChildren(item, selectedModels);
				}
				else if (data instanceof CatalogVersionModel)
				{
					selectParentWhenAllChildrenSelected(item, selectedModels);
				}
				else
				{
					LOG.warn("Only {} and {} are supported.", CatalogModel._TYPECODE, CatalogVersionModel._TYPECODE);
					return;
				}
			}
			else
			{
				selectedModels.remove(data);
				if (data instanceof CatalogModel)
				{
					deselectAllChildren(item, selectedModels);
				}
				else if (data instanceof CatalogVersionModel)
				{
					deselectParent(item, selectedModels);
				}
				else
				{
					LOG.warn("Only {} and {} are supported.", CatalogModel._TYPECODE, CatalogVersionModel._TYPECODE);
					return;
				}
			}
			updateAllRow(tree, selectedModels);
		}

		final String label = calculateLabel(selectedModels);
		popupOpener.setLabel(label);
		popupOpener.setTooltiptext(label);
		sendOutput(OUT_SOCKET_SELECTION, selectedModels);
		setValue(MODEL_SELECTED_DATA, selectedModels);
	}

	protected void selectOnRender(final Treeitem item, final Set<Object> selectedModels)
	{
		if ((item.getValue() == null && selectedModels.isEmpty()) || selectedModels.contains(item.getValue()))
		{
			tree.addItemToSelection(item);
		}
		else if (item.getValue() instanceof CatalogVersionModel && selectedModels.contains(item.getParentItem().getValue()))
		{
			tree.addItemToSelection(item);
			selectedModels.add(item.getValue());
		}
	}

	protected void deselectParent(final Treeitem item, final Set<Object> selectedModels)
	{
		final Treeitem parent = item.getParentItem();
		tree.removeItemFromSelection(parent);
		selectedModels.remove(parent.getValue());
	}

	protected void deselectAllChildren(final Treeitem item, final Set<Object> selectedModels)
	{
		final Treechildren children = item.getTreechildren();
		if (children != null)
		{
			children.getItems().forEach(treeitem -> {
				tree.removeItemFromSelection(treeitem);
				selectedModels.remove(treeitem.getValue());
			});
		}
	}

	protected void selectParentWhenAllChildrenSelected(final Treeitem item, final Set<Object> selectedModels)
	{
		final Treechildren siblings = item.getParentItem().getTreechildren();
		if (siblings != null)
		{
			final Set<ItemModel> siblingModels = new HashSet<ItemModel>();
			siblings.getItems().forEach(treeitem -> siblingModels.add(treeitem.getValue()));
			if (getSelectedModels().containsAll(siblingModels))
			{
				tree.addItemToSelection(item.getParentItem());
				selectedModels.add(item.getParentItem().getValue());
			}
		}
	}

	protected void selectAllChildren(final Treeitem item, final Set<Object> selectedModels)
	{
		final Treechildren children = item.getTreechildren();
		if (children != null)
		{
			children.getItems().forEach(treeitem -> {
				tree.addItemToSelection(treeitem);
				selectedModels.add(treeitem.getValue());
			});
		}
	}

	protected void updateAllRow(final Tree tree, final Set<Object> selectedModels)
	{
		for (final Treeitem item : tree.getItems())
		{
			if (item.getValue() == null)
			{
				if (selectedModels.isEmpty())
				{
					tree.selectItem(item);
				}
				else
				{
					tree.removeItemFromSelection(item);
				}
			}
		}
	}

	protected String calculateLabel(final Set<Object> selectedModels)
	{
		if (selectedModels.isEmpty())
		{
			return getNullLabel();
		}
		final Set<Object> effectiveSelection = selectedModels.stream()//
				.filter(element -> !(element instanceof CatalogVersionModel)
						|| !selectedModels.contains(((CatalogVersionModel) element).getCatalog()))
				.collect(Collectors.toSet());
		if (effectiveSelection.size() == 1)
		{
			return getLabelService().getObjectLabel(effectiveSelection.iterator().next());
		}
		return effectiveSelection.stream().map(model -> getLabelService().getShortObjectLabel(model))
				.filter(StringUtils::isNotBlank).sorted().collect(Collectors.joining(", "));
	}

	protected Set<Object> getSelectedModels()
	{
		final Set<Object> selectedItems = getValue(MODEL_SELECTED_DATA, Set.class);
		return selectedItems == null ? new HashSet<>() : selectedItems;
	}

	protected void adjustSClass(final Treeitem item, final Object data)
	{
		final Treerow treerow = item.getTreerow();
		if (data == null)
		{
			UITools.modifySClass(treerow, SCLASS_YW_TREEROW_ALL, true);
		}
		else if (data instanceof CatalogModel)
		{
			UITools.modifySClass(treerow, SCLASS_YW_TREEROW_CATALOG, true);
			if (data instanceof ClassificationSystemModel)
			{
				UITools.modifySClass(treerow, SCLASS_YW_TREEROW_CLASSIFICATION_CATALOG, true);
			}
		}
		else if (data instanceof CatalogVersionModel)
		{
			UITools.modifySClass(treerow, SCLASS_YW_TREEROW_CATALOG_VERSION, true);
			if (data instanceof ClassificationSystemVersionModel)
			{
				UITools.modifySClass(treerow, SCLASS_YW_TREEROW_CLASSIFICATION_CATALOG_VERSION, true);
			}
		}
	}

	protected String getNullLabel()
	{
		return getWidgetInstanceManager().getLabel(ALL_CATALOGS_LABEL);
	}

	@GlobalCockpitEvent(eventName = ObjectCRUDHandler.OBJECTS_UPDATED_EVENT, scope = CockpitEvent.SESSION)
	public void handleObjectUpdatedEvent(final CockpitEvent event)
	{
		handleCrudEvent(event);
	}

	/**
	 * @deprecated since 6.7 this method should be removed. Created event always returns an unsaved model which in turn will
	 *             never update the tree model. This leads to unnecessary calls to refresh on the tree.
	 * @param event
	 *           CRUD event
	 */
	@Deprecated
	@GlobalCockpitEvent(eventName = ObjectCRUDHandler.OBJECT_CREATED_EVENT, scope = CockpitEvent.SESSION)
	public void handleObjectCreatedEvent(final CockpitEvent event)
	{
		handleCrudEvent(event);
	}

	@GlobalCockpitEvent(eventName = ObjectCRUDHandler.OBJECTS_DELETED_EVENT, scope = CockpitEvent.SESSION)
	public void handleObjectDeletedEvent(final CockpitEvent event)
	{
		handleCrudEvent(event);
	}

	protected void handleCrudEvent(final CockpitEvent event)
	{
		boolean requiresRefresh = false;
		for (final Object data : event.getDataAsCollection())
		{
			if (data instanceof CatalogModel || data instanceof CatalogVersionModel || data instanceof CategoryModel)
			{
				if (getCatalogTreeModelPopulator() instanceof RefreshableTreeModel)
				{
					((RefreshableTreeModel) getCatalogTreeModelPopulator()).refreshChildren(data, Collections.emptyList());
				}
				requiresRefresh = true;
			}
		}
		if (requiresRefresh)
		{
			setValue(MODEL_SELECTED_DATA, null);
			tree.clearSelection();
			reloadTree();
		}
	}

	public ComponentModelPopulator<TreeModel<TreeNode<ItemModel>>> getCatalogTreeModelPopulator()
	{
		return catalogTreeModelPopulator;
	}

	@SocketEvent(socketId = IN_SOCKET_CLEAR)
	public void clear()
	{
		setValue(MODEL_SELECTED_DATA, new HashSet<>());
		final String label = calculateLabel(new HashSet<>());
		popupOpener.setLabel(label);
		popupOpener.setTooltiptext(label);
		tree.clearSelection();
		reloadTree();
	}

	@Required
	public void setCatalogTreeModelPopulator(
			final ComponentModelPopulator<TreeModel<TreeNode<ItemModel>>> catalogTreeModelPopulator)
	{
		this.catalogTreeModelPopulator = catalogTreeModelPopulator;
	}

	public LabelService getLabelService()
	{
		return labelService;
	}

	@Required
	public void setLabelService(final LabelService labelService)
	{
		this.labelService = labelService;
	}

	public CatalogTreeSimpleLabelProvider getCatalogTreeSimpleLabelProvider()
	{
		return catalogTreeSimpleLabelProvider;
	}

	public void setCatalogTreeSimpleLabelProvider(final CatalogTreeSimpleLabelProvider catalogTreeSimpleLabelProvider)
	{
		this.catalogTreeSimpleLabelProvider = catalogTreeSimpleLabelProvider;
	}

	public Tree getTree()
	{
		return tree;
	}

	public Popup getPopup()
	{
		return popup;
	}

	public Button getPopupOpener()
	{
		return popupOpener;
	}
}
