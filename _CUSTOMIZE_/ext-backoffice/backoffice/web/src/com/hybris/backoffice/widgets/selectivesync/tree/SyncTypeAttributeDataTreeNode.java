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
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;

import java.util.Objects;

import org.zkoss.zul.Checkbox;

import com.hybris.cockpitng.labels.LabelService;
import com.hybris.cockpitng.util.BackofficeSpringUtil;
import com.hybris.cockpitng.util.UITools;


/**
 * Model for attribute tree node. There are nodes of two kinds: TYPE (node) or ATTRIBUTE (leaf).
 */
public class SyncTypeAttributeDataTreeNode implements TreeItemSelectable
{
	private static final String SCLASS_TREE_ATTRIBUTE_CHECK_BOX = "ye-selsync-tree-attribute-check-box";
	private static final String SCLASS_TREE_TYPE_CHECK_BOX = "ye-selsync-tree-type-check-box";
	private static final String SCLASS_TREE_TYPE_PARTIALLY_SELECTED = "ye-selsync-partially-selected";
	private static final String SCLASS_TREE_TYPE_ALL_SELECTED = "ye-selsync-all-selected";
	private static final String SCLASS_TREE_TYPE_NONE_SELECTED = "ye-selsync-none-selected";

	private static final String ROOT_NODE_NAME = "ROOT_NODE";

	public enum SyncTypeAttributeTreeNodeType
	{
		TYPE, ATTRIBUTE
	}

	private final Checkbox checkbox;
	private final String text;
	private final Object data;
	private final SyncTypeAttributeTreeNodeType nodeType;
	private SelectionType selectionType;

	private static LabelService labelService;

	private SyncTypeAttributeDataTreeNode(final SyncTypeAttributeTreeNodeType nodeType, final String text, final Object data,
			final SelectionType selectionType, final String cssClass)
	{
		this.checkbox = new Checkbox();
		this.checkbox.setSclass(cssClass);
		this.nodeType = nodeType;
		this.text = text;
		this.data = data;
		SyncTypeAttributeDataTreeNode.this.setSelection(selectionType);
	}

	/** Creates type tree node. It can contain children (attributes nodes and type nodes). */
	public static SyncTypeAttributeDataTreeNode createTypeNode(final ComposedTypeModel typeModel)
	{
		return new SyncTypeAttributeDataTreeNode(SyncTypeAttributeTreeNodeType.TYPE, createLabel(typeModel), typeModel,
				SelectionType.NONE, SCLASS_TREE_TYPE_CHECK_BOX);
	}

	/** Create attribute tree node, it is always a leaf. */
	public static SyncTypeAttributeDataTreeNode createAttributeNode(
			final SyncAttributeDescriptorConfigModel attributeDescriptorConfigModel)
	{
		return new SyncTypeAttributeDataTreeNode(SyncTypeAttributeTreeNodeType.ATTRIBUTE,
				createLabel(attributeDescriptorConfigModel), attributeDescriptorConfigModel,
				attributeDescriptorConfigModel.getIncludedInSync() ? SelectionType.ALL : SelectionType.NONE,
				SCLASS_TREE_ATTRIBUTE_CHECK_BOX);
	}

	/** Creates root node. It should be added as root in tree model, ZK component tree, do not show this root node. */
	public static SyncTypeAttributeDataTreeNode createRootNode()
	{
		return new SyncTypeAttributeDataTreeNode(SyncTypeAttributeTreeNodeType.ATTRIBUTE, ROOT_NODE_NAME, null, SelectionType.ALL,
				SCLASS_TREE_TYPE_CHECK_BOX);
	}

	/**
	 * Getter for data with node. It can be of type: {@link ComposedTypeModel} or {@link SyncAttributeDescriptorConfigModel}
	 * or null for root node.
	 *
	 * @return data connected with node
	 */
	public Object getData()
	{
		return data;
	}

	public String getText()
	{
		return text;
	}

	public Checkbox getCheckbox()
	{
		return checkbox;
	}

	/**
	 * Determines if this is an attribute node.
	 *
	 * @return true if it is attribute node, false for type node
	 */
	public boolean isAttribute()
	{
		return SyncTypeAttributeTreeNodeType.ATTRIBUTE == nodeType;
	}

	/**
	 * Determines if this is an type node.
	 *
	 * @return true if it is type node, false for attribute node.
	 */
	public boolean isType()
	{
		return SyncTypeAttributeTreeNodeType.TYPE == nodeType;
	}

	@Override
	public void setSelection(final SelectionType selectionType)
	{
		checkbox.setChecked(SelectionType.NONE != selectionType);
		this.selectionType = selectionType;

		updateCheckboxSClass();
	}

	@Override
	public SelectionType getSelection()
	{
		return selectionType;
	}

	protected void updateCheckboxSClass()
	{
		UITools.removeSClass(checkbox, SCLASS_TREE_TYPE_PARTIALLY_SELECTED);
		UITools.removeSClass(checkbox, SCLASS_TREE_TYPE_ALL_SELECTED);
		UITools.removeSClass(checkbox, SCLASS_TREE_TYPE_NONE_SELECTED);
		if (selectionType == SelectionType.PARTIALLY)
		{
			UITools.addSClass(checkbox, SCLASS_TREE_TYPE_PARTIALLY_SELECTED);
		}
		else if (selectionType == SelectionType.ALL)
		{
			UITools.addSClass(checkbox, SCLASS_TREE_TYPE_ALL_SELECTED);
		}
		else
		{
			UITools.addSClass(checkbox, SCLASS_TREE_TYPE_NONE_SELECTED);
		}
	}

	private static String createLabel(final ItemModel object)
	{
		if (labelService == null)
		{
			labelService = BackofficeSpringUtil.getBean("labelService", LabelService.class);
		}
		return labelService.getObjectLabel(object);
	}

	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || this.getClass() != o.getClass())
		{
			return false;
		}
		final SyncTypeAttributeDataTreeNode myNode = (SyncTypeAttributeDataTreeNode) o;
		return Objects.equals(text, myNode.text) && Objects.equals(data, myNode.data);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(text, data);
	}
}
