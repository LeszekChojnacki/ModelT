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
package com.hybris.backoffice.excel.export.wizard.renderer.attributechooser;

import de.hybris.platform.core.model.type.AttributeDescriptorModel;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zul.AbstractTreeModel;
import org.zkoss.zul.DefaultTreeNode;
import org.zkoss.zul.TreeNode;
import org.zkoss.zul.Treeitem;

import com.hybris.backoffice.excel.data.SelectedAttribute;


/**
 * Handler which provides tree model nodes operations.
 * 
 * @deprecated since 6.7 {@link com.hybris.backoffice.attributechooser.NodeOperationsHandler}
 */
@Deprecated
public interface NodeOperationsHandler
{
	/**
	 * Moves selected nodes from source tree to target tree. If moved node is a sub node and target tree doesn't have
	 * grouping node it will be created.
	 * 
	 * @param sourceModel
	 *           source tree from which selected nodes will be moved.
	 * @param targetModel
	 *           target tree where nodes will be moved.
	 */
	void moveNodesBetweenTrees(AbstractTreeModel<TreeNode<SelectedAttribute>> sourceModel,
			AbstractTreeModel<TreeNode<SelectedAttribute>> targetModel);

	/**
	 * Updates groping nodes selection based on select event data. If all sub nodes are selected then grouping node is
	 * also selected. If grouping node is selected then all sub nodes are selected. etc.
	 * 
	 * @param treeModel
	 *           tree model which contains nodes to be updated.
	 * @param event
	 *           select event.
	 */
	void updateGroupingNodesSelection(final AbstractTreeModel<TreeNode<SelectedAttribute>> treeModel,
			final SelectEvent<Treeitem, TreeNode<SelectedAttribute>> event);

	/**
	 * Sorts tree model based on nodes data.
	 * 
	 * @param treeModel
	 *           tree model to sort.
	 * @param ascending
	 *           sort direction.
	 */
	void sort(final AbstractTreeModel<TreeNode<SelectedAttribute>> treeModel, final boolean ascending);


	/**
	 * Filter tree model.
	 * 
	 * @param model
	 *           source tree model.
	 * @param searchText
	 *           filter text.
	 * @return filtered model.
	 */
	AbstractTreeModel<TreeNode<SelectedAttribute>> filterTreeModel(final AbstractTreeModel<TreeNode<SelectedAttribute>> model,
			final String searchText);

	/**
	 * Extracts data from tree model's nodes. Grouping nodes data is skipped.
	 * 
	 * @param treeModel
	 *           source tree model.
	 * @return flat list representing tree data.
	 */
	List<SelectedAttribute> extractAttributes(final AbstractTreeModel<TreeNode<SelectedAttribute>> treeModel);


	/**
	 * Creates tree nodes with for given attributes.
	 * 
	 * @param attributes
	 *           all available attributes.
	 * @param filter
	 *           filter which allows to exclude create nodes for given attributes.
	 * @return
	 */
	List<DefaultTreeNode<SelectedAttribute>> createTreeNodes(final Collection<AttributeDescriptorModel> attributes,
			final Predicate<SelectedAttribute> filter);

}
