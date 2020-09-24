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
package com.hybris.backoffice.widgets.selectivesync.tree;

import de.hybris.platform.catalog.model.SyncAttributeDescriptorConfigModel;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;

import com.hybris.backoffice.widgets.selectivesync.detailsview.DetailsView;
import com.hybris.backoffice.widgets.selectivesync.detailsview.DetailsViewAttributeValueChangeListener;
import com.hybris.backoffice.widgets.selectivesync.renderer.SelectiveSyncRenderer;
import com.hybris.cockpitng.dataaccess.facades.permissions.PermissionFacade;


/**
 * A custom TreeitemRenderer implementation to render synchronization attributes in a tree structure and the types they
 * belong to. Used by the {@link SelectiveSyncRenderer}. It allows the changes made on attributes and type levels to be
 * propagated up and down the tree to show the status of the type accordingly.
 */
public class SyncTypeAttributeDataTreeItemRenderer implements TreeitemRenderer<SyncTypeAttributeDataTreeNode>
{
	private static final String SCLASS_TREE_ROW = "ye-selsync-tree-row";
	private static final String SCLASS_TREE_ITEM = "ye-selsync-tree-item";
	private static final String SCLASS_TREE_CELL = "ye-selsync-tree-cell";
	private static final String SCLASS_TREE_TYPE_CONTAINER = "ye-selsync-tree-type-container";
	private static final String SCLASS_TREE_CHECKBOX_CONTAINER = "ye-selsync-tree-checkbox-container";
	private static final String SCLASS_TREE_LABEL_CONTAINER = "ye-selsync-tree-label-container";
	private static final String SCLASS_TREE_ICON_CONTAINER = "ye-selsync-tree-icon-container";
	private static final String SCLASS_TREE_TYPE_LABEL = "ye-selsync-tree-type-label";
	private static final String SCLASS_TREE_ATTRIBUTE_CONTAINER = "ye-selsync-tree-attribute-container";
	private static final String SCLASS_TREE_ATTRIBUTE_LABEL = "ye-selsync-tree-attribute-label";
	private static final String BACKOFFICE_DATA_NOT_VISIBLE_LABEL = "backoffice.data.not.visible";

	private final SelectiveSyncModelChangeListener selectiveSyncModelChangeListener;
	private final DetailsView detailsView;
	private final SyncAttributeTreeModel dataModel;
	private final PermissionFacade permissionFacade;
	private final boolean editable;

	public SyncTypeAttributeDataTreeItemRenderer(final CreationContext creationContext)
	{
		this.selectiveSyncModelChangeListener = creationContext.selectiveSyncModelChangeListener;
		this.detailsView = creationContext.detailsView;
		this.editable = creationContext.editable;
		this.dataModel = creationContext.dataModel;
		this.dataModel.setOnFilterExecutedListener(detailsView::clearView);
		this.permissionFacade = creationContext.permissionFacade;
	}

	@Override
	public void render(final Treeitem treeitem, final SyncTypeAttributeDataTreeNode treeNode, final int index)
	{
		treeitem.setValue(treeNode.getData());

		final Treerow treeRow = createTreerow(treeitem);
		final Treecell treeCell = createTreecell(treeRow);
		final Div treeCellInnerCnt = createTreeCellInnerCnt(treeCell);

		renderNode(treeNode, treeCellInnerCnt, treeCell);
	}

	protected Treerow createTreerow(final Treeitem treeitem)
	{
		final Treerow treeRow = new Treerow();
		treeRow.setParent(treeitem);
		treeRow.setSclass(SCLASS_TREE_ROW);
		return treeRow;
	}

	protected Treecell createTreecell(final Treerow treeRow)
	{
		final Treecell treeCell = new Treecell();
		treeCell.setParent(treeRow);
		treeRow.setSclass(SCLASS_TREE_CELL);
		return treeCell;
	}

	protected Div createTreeCellInnerCnt(final Treecell treeCell)
	{
		final Div treeCellInnerCnt = new Div();
		treeCellInnerCnt.setSclass(SCLASS_TREE_ITEM);
		treeCellInnerCnt.setParent(treeCell);
		return treeCellInnerCnt;
	}

	protected void renderNode(final SyncTypeAttributeDataTreeNode treeNode, final Div treeCellInnerCnt, final Treecell treecell)
	{
		if (!canBeRead(treeNode.getData()))
		{
			renderNoReadAccess(treeCellInnerCnt, treecell);
			return;
		}

		if (treeNode.isType())
		{
			renderTypeNode(treeNode, treeCellInnerCnt, treecell);
		}
		else if (treeNode.isAttribute())
		{
			renderAttributeNode(treeNode, treeCellInnerCnt, treecell);
		}
	}

	protected boolean canBeRead(final Object object)
	{
		return permissionFacade.canReadInstance(object);
	}

	protected void renderNoReadAccess(final Div treeCellInnerCnt, final Treecell treecell)
	{
		final Div typeNodeDiv = new Div();
		typeNodeDiv.setSclass(SCLASS_TREE_TYPE_CONTAINER);
		typeNodeDiv.setParent(treeCellInnerCnt);
		final Div labelDiv = createTreeLabelContainer(typeNodeDiv);
		createTypeLabel(labelDiv, Labels.getLabel(BACKOFFICE_DATA_NOT_VISIBLE_LABEL));

		treecell.addEventListener(Events.ON_CLICK, (final MouseEvent event) -> noReadAccessOnClick());
	}

	protected void renderTypeNode(final SyncTypeAttributeDataTreeNode treeNode, final Div treeCellInnerCnt,
			final Treecell treecell)
	{
		final Div typeNodeDiv = new Div();
		typeNodeDiv.setSclass(SCLASS_TREE_TYPE_CONTAINER);
		typeNodeDiv.setParent(treeCellInnerCnt);

		final Div checkboxDiv = new Div();
		checkboxDiv.setSclass(SCLASS_TREE_CHECKBOX_CONTAINER);
		checkboxDiv.setParent(typeNodeDiv);

		treeNode.getCheckbox().setParent(checkboxDiv);
		final boolean hasPermissionToChange = permissionFacade.canChangeProperty(SyncAttributeDescriptorConfigModel._TYPECODE,
				SyncAttributeDescriptorConfigModel.INCLUDEDINSYNC);
		treeNode.getCheckbox().setDisabled(!this.editable || !hasPermissionToChange);
		treeNode.getCheckbox().addEventListener(Events.ON_CHECK, (final CheckEvent event) -> typeNodeOnCheck(treeNode, event));

		final Div labelDiv = createTreeLabelContainer(typeNodeDiv);

		final String typeLabel = treeNode.getText();
		createTypeLabel(labelDiv, typeLabel);

		treecell.addEventListener(Events.ON_CLICK, (EventListener<MouseEvent>) this::typeNodeOnClick);

	}

	protected Div createTreeLabelContainer(final Div typeNodeDiv)
	{
		final Div labelDiv = new Div();
		labelDiv.setSclass(SCLASS_TREE_LABEL_CONTAINER);
		labelDiv.setParent(typeNodeDiv);
		return labelDiv;
	}

	protected Div createIconDiv(final Div typeNodeDiv)
	{
		final Div iconDiv = new Div();
		iconDiv.setSclass(SCLASS_TREE_ICON_CONTAINER);
		iconDiv.setParent(typeNodeDiv);
		return iconDiv;
	}

	protected Label createTypeLabel(final Div labelDiv, final String typeLabel)
	{
		final Label label = new Label(typeLabel);
		label.setParent(labelDiv);
		label.setSclass(SCLASS_TREE_TYPE_LABEL);
		return label;
	}

	/**
	 * @deprecated since 6.7 use
	 *             {@link SyncTypeAttributeDataTreeItemRenderer#typeNodeOnClick(MouseEvent event)}
	 */
	@Deprecated
	protected void typeNodeOnClick()
	{
		// do nothing
	}

	protected void typeNodeOnClick(final MouseEvent event)
	{
		typeNodeOnClick();
		detailsView.clearView();
		toggleNode(event);
	}

	private static void toggleNode(final MouseEvent event)
	{
		if (event.getTarget() instanceof Treecell)
		{
			final Treecell cel = (Treecell) event.getTarget();
			if (cel.getParent() instanceof Treerow)
			{
				final Treerow row = (Treerow) cel.getParent();
				if (row.getParent() instanceof Treeitem)
				{
					final Treeitem item = (Treeitem) row.getParent();
					item.setOpen(!item.isOpen());
				}
			}
		}
	}

	protected void noReadAccessOnClick()
	{
		detailsView.clearView();
	}

	protected void typeNodeOnCheck(final SyncTypeAttributeDataTreeNode treeNode, final CheckEvent event)
	{
		dataModel.nodeOnCheck(treeNode, event.isChecked());

		detailsView.clearView();
		selectiveSyncModelChangeListener.onValueChanged(this, dataModel.getOriginalData());
	}

	protected void attributeNodeOnCheck(final SyncTypeAttributeDataTreeNode treeNode,
			final SyncAttributeDescriptorConfigModel syncAttributeConfig, final CheckEvent event)
	{
		dataModel.nodeOnCheck(treeNode, Boolean.valueOf(event.isChecked()));

		openSelectedSyncAttribute(syncAttributeConfig, treeNode);
		selectiveSyncModelChangeListener.onValueChanged(this, dataModel.getOriginalData());
	}

	protected void renderAttributeNode(final SyncTypeAttributeDataTreeNode treeNode, final Div treeCellInnerCnt,
			final Treecell treecell)
	{
		final SyncAttributeDescriptorConfigModel syncAttributeConfig = (SyncAttributeDescriptorConfigModel) treeNode.getData();

		final Div attributeNodeDiv = new Div();
		attributeNodeDiv.setSclass(SCLASS_TREE_ATTRIBUTE_CONTAINER);
		attributeNodeDiv.setParent(treeCellInnerCnt);

		final Div checkboxDiv = new Div();
		checkboxDiv.setSclass(SCLASS_TREE_CHECKBOX_CONTAINER);
		checkboxDiv.setParent(attributeNodeDiv);

		final Checkbox checkbox = treeNode.getCheckbox();
		checkbox.setChecked(Boolean.TRUE.equals(syncAttributeConfig.getIncludedInSync()));
		checkbox.setParent(checkboxDiv);
		final boolean hasPermissionToChange = permissionFacade.canChangeInstanceProperty(syncAttributeConfig,
				SyncAttributeDescriptorConfigModel.INCLUDEDINSYNC);
		checkbox.setDisabled(!this.editable || !hasPermissionToChange);
		checkbox.addEventListener(Events.ON_CHECK,
				(final CheckEvent event) -> attributeNodeOnCheck(treeNode, syncAttributeConfig, event));

		final Div labelDiv = createTreeLabelContainer(attributeNodeDiv);

		final String attributeLabelText = treeNode.getText();
		createAttributeLabel(labelDiv, attributeLabelText);

		treecell.addEventListener(Events.ON_CLICK,
				(final MouseEvent event) -> openSelectedSyncAttribute(syncAttributeConfig, treeNode));
	}

	protected void createAttributeLabel(final Div labelDiv, final String attributeLabel)
	{
		final Label attrLabel = new Label(attributeLabel);
		attrLabel.setSclass(SCLASS_TREE_ATTRIBUTE_LABEL);
		attrLabel.setParent(labelDiv);
	}

	protected void openSelectedSyncAttribute(final SyncAttributeDescriptorConfigModel syncAttributeDescriptor,
			final SyncTypeAttributeDataTreeNode treeNode)
	{
		final DetailsViewAttributeValueChangeListener attributeChangeListener = //
				(syncAttributeDescriptorConfigModel, attribute, value) -> {
					if (SyncAttributeDescriptorConfigModel.INCLUDEDINSYNC.equals(attribute))
					{
						dataModel.nodeOnCheck(treeNode, syncAttributeDescriptorConfigModel.getIncludedInSync());
					}
					selectiveSyncModelChangeListener.onValueChanged(this, dataModel.getOriginalData());
				};

		detailsView.display(syncAttributeDescriptor, attributeChangeListener);
	}

	/** Context for the {@link SyncTypeAttributeDataTreeItemRenderer}. */
	public static class CreationContext
	{
		private SelectiveSyncModelChangeListener selectiveSyncModelChangeListener;
		private DetailsView detailsView;
		private boolean editable;
		private SyncAttributeTreeModel dataModel;
		private PermissionFacade permissionFacade;

		public void setSelectiveSyncModelChangeListener(final SelectiveSyncModelChangeListener selectiveSyncModelChangeListener)
		{
			this.selectiveSyncModelChangeListener = selectiveSyncModelChangeListener;
		}

		public void setDetailsView(final DetailsView detailsView)
		{
			this.detailsView = detailsView;
		}

		public void setEditable(final boolean editable)
		{
			this.editable = editable;
		}

		public void setDataModel(final SyncAttributeTreeModel dataModel)
		{
			this.dataModel = dataModel;
		}

		public void setPermissionFacade(final PermissionFacade permissionFacade)
		{
			this.permissionFacade = permissionFacade;
		}
	}
}
