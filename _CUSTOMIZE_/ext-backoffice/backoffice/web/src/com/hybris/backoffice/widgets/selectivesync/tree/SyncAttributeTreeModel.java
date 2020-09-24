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

import static com.hybris.backoffice.widgets.selectivesync.tree.TreeItemSelectable.SelectionType.resolveSelectionType;

import de.hybris.platform.catalog.model.SyncAttributeDescriptorConfigModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zul.AbstractTreeModel;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.ext.TreeOpenableModel;

import com.hybris.cockpitng.tree.util.TreeUtils;


/** Tree model for synchronization attributes. */
public class SyncAttributeTreeModel extends AbstractTreeModel<SyncTypeAttributeDataTreeNode>
{
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private transient Collection<SyncAttributeDescriptorConfigModel> originalData;
	private transient Map<SyncTypeAttributeDataTreeNode, List<SyncTypeAttributeDataTreeNode>> treeData;
	private transient Map<SyncTypeAttributeDataTreeNode, List<SyncTypeAttributeDataTreeNode>> filteredTreeData;
	private transient FilterExecutionListener filterExecutionListener;

	/**
	 * Creates a new instance.
	 *
	 * @param syncAttributeDescriptors
	 *           original collection of attributes from which tree model was created
	 * @param tree
	 *           map (parent node) -> list of (children nodes) created from syncAttributeDescriptors
	 * @see SyncAttributeTreeModelFactory#create(Collection, ComposedTypeModel)
	 */
	public SyncAttributeTreeModel(final Collection<SyncAttributeDescriptorConfigModel> syncAttributeDescriptors,
			final Map<SyncTypeAttributeDataTreeNode, List<SyncTypeAttributeDataTreeNode>> tree)
	{
		super(SyncTypeAttributeDataTreeNode.createRootNode());
		originalData = syncAttributeDescriptors;
		treeData = unmodifiableCopy(tree);
		filteredTreeData = treeData;

		updateNodeSelection();
	}

	private static Map<SyncTypeAttributeDataTreeNode, List<SyncTypeAttributeDataTreeNode>> unmodifiableCopy(
			final Map<SyncTypeAttributeDataTreeNode, List<SyncTypeAttributeDataTreeNode>> data)
	{
		final Map<SyncTypeAttributeDataTreeNode, List<SyncTypeAttributeDataTreeNode>> retVal = new HashMap<>();
		data.forEach((key, value) -> retVal.put(key, Collections.unmodifiableList(value)));
		return Collections.unmodifiableMap(retVal);
	}

	private static void changeIncludedInSync(final SyncTypeAttributeDataTreeNode treeNode, final Boolean includeInSync)
	{
		((SyncAttributeDescriptorConfigModel) treeNode.getData()).setIncludedInSync(includeInSync);
		treeNode.setSelection(includeInSync ? TreeItemSelectable.SelectionType.ALL : TreeItemSelectable.SelectionType.NONE);
	}

	private static void changeIncludeInSyncForAttributes(final List<SyncTypeAttributeDataTreeNode> subNodes,
			final Boolean includeInSync)
	{
		subNodes.stream()//
				.filter(SyncTypeAttributeDataTreeNode::isAttribute)//
				.forEach(subNode -> changeIncludedInSync(subNode, includeInSync));
	}

	public void init()
	{
		expandMainNode(this);
	}

	@Override
	public boolean isLeaf(final SyncTypeAttributeDataTreeNode node)
	{
		return CollectionUtils.isEmpty(treeData.get(node));
	}

	@Override
	public SyncTypeAttributeDataTreeNode getChild(final SyncTypeAttributeDataTreeNode parent, final int index)
	{
		final List<SyncTypeAttributeDataTreeNode> list = this.filteredTreeData.get(parent);
		if (list != null && index < list.size())
		{
			return list.get(index);
		}
		return null;
	}

	/**
	 * Returns list of children for given tree node.
	 *
	 * @param treeNode
	 *           parent node
	 * @return list of children
	 */
	public List<SyncTypeAttributeDataTreeNode> getChildren(final SyncTypeAttributeDataTreeNode treeNode)
	{
		return treeData.get(treeNode);
	}

	/**
	 * Returns parent for given tree node.
	 *
	 * @param treeNode
	 *           child node
	 * @return optional parent
	 */
	public Optional<SyncTypeAttributeDataTreeNode> getParent(final SyncTypeAttributeDataTreeNode treeNode)
	{
		return treeData.entrySet().stream()//
				.filter(entry -> entry.getValue().stream()//
						.anyMatch(leaf -> leaf.equals(treeNode)))//
				.findAny()//
				.map(Map.Entry::getKey);
	}

	@Override
	public int getChildCount(final SyncTypeAttributeDataTreeNode parent)
	{
		final List<SyncTypeAttributeDataTreeNode> list = this.filteredTreeData.get(parent);
		return list == null ? 0 : list.size();
	}

	public TreeModel<SyncTypeAttributeDataTreeNode> filter(final FilterContext context)
	{
		onFilterExecuted();
		resetFilter();
		updateNodeSelection();

		final FilteredTreeModel.IncludedMode includeMode = FilteredTreeModel.IncludedMode
				.resolveIncludeMode(context.getShowIncluded(), context.getShowNotIncluded());
		final FilteredTreeModel<SyncTypeAttributeDataTreeNode> filteredTreeModel = new FilteredTreeModel<>(this,
				context.getFilterQuery(), SyncTypeAttributeDataTreeNode::getText, TreeUtils.MatchMode.CONTAINS, includeMode);

		this.filteredTreeData = unmodifiableCopy(filteredTreeModel.getChildrenMap());

		if (context.getShowIncluded() || context.getShowNotIncluded())
		{
			expandMainNode(filteredTreeModel);
		}

		if (StringUtils.isNotEmpty(context.getFilterQuery()))
		{
			expandFilteredNodes(filteredTreeModel);
		}

		clearSelection(filteredTreeModel);

		return filteredTreeModel;
	}

	protected void clearSelection(final FilteredTreeModel<SyncTypeAttributeDataTreeNode> filteredTreeModel)
	{
		if (filteredTreeModel != null)
		{
			filteredTreeModel.clearSelection();
		}
	}

	protected void onFilterExecuted()
	{
		if (filterExecutionListener != null)
		{
			filterExecutionListener.onFilterExecuted();
		}
	}

	protected void expandMainNode(final TreeOpenableModel model)
	{
		if (!filteredTreeData.isEmpty())
		{
			model.addOpenPath(new int[]
			{ 0 });
		}
	}

	protected void expandFilteredNodes(final FilteredTreeModel<SyncTypeAttributeDataTreeNode> model)
	{
		if (!filteredTreeData.isEmpty())
		{
			filteredTreeData.forEach((key, value) -> value.forEach(node -> expandNode(model, node)));
		}
	}

	protected void expandNode(final FilteredTreeModel<SyncTypeAttributeDataTreeNode> model,
			final SyncTypeAttributeDataTreeNode node)
	{
		final int[] selectionPath = model.getPath(node);
		model.addSelectionPath(selectionPath);
		model.addOpenPath(selectionPath);
	}

	public Collection<SyncAttributeDescriptorConfigModel> getOriginalData()
	{
		return originalData;
	}

	/**
	 * Executed when node is being checked/unchecked.
	 *
	 * @param treeNode
	 *           node that changes
	 * @param includeInSync
	 *           current value of node
	 */
	public void nodeOnCheck(final SyncTypeAttributeDataTreeNode treeNode, final Boolean includeInSync)
	{
		if (treeNode.isType())
		{
			typeOnCheck(treeNode, includeInSync);
		}
		else if (treeNode.isAttribute())
		{
			attributeOnCheck(treeNode, includeInSync);
		}
		else
		{
			LOG.error("Unknown tree node type.");
		}
	}

	private void attributeOnCheck(final SyncTypeAttributeDataTreeNode treeNode, final Boolean includeInSync)
	{
		changeIncludedInSync(treeNode, includeInSync);
		updateNodeSelection();
	}

	private void typeOnCheck(final SyncTypeAttributeDataTreeNode treeNode, final Boolean includeInSync)
	{
		changeIncludeInSyncForAllChildAndSubNodes(treeNode, includeInSync);
		updateNodeSelection();
	}

	private void resetFilter()
	{
		filteredTreeData = treeData;//treeData is unmodifiable
	}

	private void updateNodeSelection()
	{
		final SyncTypeAttributeDataTreeNode root = getRoot();
		updateNodeSelection(root);
	}

	private void updateNodeSelection(final SyncTypeAttributeDataTreeNode node)
	{
		updateSubNodesSelection(node);

		final TreeItemSelectable.SelectionType selectionType = computeSelectionType(node);
		node.setSelection(selectionType);
	}

	private void updateSubNodesSelection(final SyncTypeAttributeDataTreeNode node)
	{
		final List<SyncTypeAttributeDataTreeNode> childes = getChildren(node);
		if (childes == null)
		{
			return;
		}
		getChildren(node).stream()//
				.filter(SyncTypeAttributeDataTreeNode::isType)//
				.forEach(this::updateNodeSelection);
	}

	private TreeItemSelectable.SelectionType computeSelectionType(final SyncTypeAttributeDataTreeNode node)
	{
		final Map<TreeItemSelectable.SelectionType, Long> selectionTypeOccurrence = groupBySelectionType(node);

		final long partiallySelectedCount = selectionTypeOccurrence.getOrDefault(TreeItemSelectable.SelectionType.PARTIALLY, 0L);
		final long allSelectedCount = selectionTypeOccurrence.getOrDefault(TreeItemSelectable.SelectionType.ALL, 0L);
		final long noneSelectedCount = selectionTypeOccurrence.getOrDefault(TreeItemSelectable.SelectionType.NONE, 0L);

		return resolveSelectionType(allSelectedCount, partiallySelectedCount, noneSelectedCount);
	}

	private Map<TreeItemSelectable.SelectionType, Long> groupBySelectionType(final SyncTypeAttributeDataTreeNode node)
	{
		return getChildren(node).stream().collect(//
				Collectors.groupingBy(//
						SyncTypeAttributeDataTreeNode::getSelection, //
						Collectors.counting()));
	}

	private void changeIncludeInSyncForAllChildAndSubNodes(final SyncTypeAttributeDataTreeNode treeNode,
			final Boolean includeInSync)
	{
		final List<SyncTypeAttributeDataTreeNode> subNodes = getChildren(treeNode);
		if (subNodes == null)
		{
			return;
		}
		changeIncludeInSyncForSubNodes(subNodes, includeInSync);

		changeIncludeInSyncForAttributes(subNodes, includeInSync);
	}

	private void changeIncludeInSyncForSubNodes(final List<SyncTypeAttributeDataTreeNode> subNodes, final Boolean includeInSync)
	{
		subNodes.stream()//
				.filter(SyncTypeAttributeDataTreeNode::isType)//
				.forEach(subNode -> changeIncludeInSyncForAllChildAndSubNodes(subNode, includeInSync));
	}

	public void setOnFilterExecutedListener(final FilterExecutionListener filterExecutionListener)
	{
		this.filterExecutionListener = filterExecutionListener;
	}
}
