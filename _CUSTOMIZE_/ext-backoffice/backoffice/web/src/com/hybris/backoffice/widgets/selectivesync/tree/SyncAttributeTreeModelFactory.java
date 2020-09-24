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
import de.hybris.platform.core.model.type.ComposedTypeModel;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/** Factory for {@link SyncAttributeTreeModel}. */
public class SyncAttributeTreeModelFactory
{
	protected static final Comparator<SyncTypeAttributeDataTreeNode> COMPARE_NODES_BY_TYPE_AND_ATTRIBUTE_NAMES = SyncAttributeTreeModelFactory::compareNodesByTypeAndName;

	/**
	 * Factory method for {@link SyncAttributeTreeModel}.
	 *
	 * @param syncAttributeDescriptors
	 *           collection of attributes from which tree model will be created
	 * @param rootType
	 *           type of root element
	 * @return created tree model
	 */
	public SyncAttributeTreeModel create(final Collection<SyncAttributeDescriptorConfigModel> syncAttributeDescriptors,
			final ComposedTypeModel rootType)
	{
		final SyncAttributeTreeModel model = new SyncAttributeTreeModel(syncAttributeDescriptors,
				createTree(syncAttributeDescriptors, rootType));
		model.init();
		return model;
	}

	protected Map<SyncTypeAttributeDataTreeNode, List<SyncTypeAttributeDataTreeNode>> createTree(
			final Collection<SyncAttributeDescriptorConfigModel> syncAttributeDescriptors, final ComposedTypeModel rootType)
	{
		final Map<ComposedTypeModel, Set<SyncAttributeDescriptorConfigModel>> syncAttributeDescriptorsMap = groupAttributesByParent(
				syncAttributeDescriptors);

		final Map<SyncTypeAttributeDataTreeNode, List<SyncTypeAttributeDataTreeNode>> nodes = convertToNodes(
				syncAttributeDescriptorsMap);

		connectChildrenAndParent(nodes);

		//empty root node is necessary to ZK properly display tree
		createAndAddEmptyRootNode(nodes, rootType);

		sortMapValuesLists(nodes);
		return nodes;
	}

	protected Map<ComposedTypeModel, Set<SyncAttributeDescriptorConfigModel>> groupAttributesByParent(
			final Collection<SyncAttributeDescriptorConfigModel> syncAttributeDescriptors)
	{
		return syncAttributeDescriptors.stream().collect(Collectors.groupingBy(
				syncAttribute -> syncAttribute.getAttributeDescriptor().getEnclosingType(), LinkedHashMap::new, Collectors.toSet()));
	}

	protected Map<SyncTypeAttributeDataTreeNode, List<SyncTypeAttributeDataTreeNode>> convertToNodes(
			final Map<ComposedTypeModel, Set<SyncAttributeDescriptorConfigModel>> syncAttributeDescriptorsMap)
	{
		final Map<SyncTypeAttributeDataTreeNode, List<SyncTypeAttributeDataTreeNode>> converted = new HashMap<>();

		syncAttributeDescriptorsMap.forEach((key, value) -> {
			final SyncTypeAttributeDataTreeNode convertedKey = SyncTypeAttributeDataTreeNode.createTypeNode(key);

			final List<SyncTypeAttributeDataTreeNode> convertedChildren = value.stream()
					.map(SyncTypeAttributeDataTreeNode::createAttributeNode)//
					.collect(Collectors.toList());

			converted.put(convertedKey, convertedChildren);
		});
		return converted;
	}

	protected void sortMapValuesLists(final Map<SyncTypeAttributeDataTreeNode, List<SyncTypeAttributeDataTreeNode>> map)
	{
		map.forEach((k, v) -> v.sort(COMPARE_NODES_BY_TYPE_AND_ATTRIBUTE_NAMES));
	}

	protected static int compareNodesByTypeAndName(final SyncTypeAttributeDataTreeNode node1,
			final SyncTypeAttributeDataTreeNode node2)
	{
		return bothNodesAreTypesOrAttributes(node1, node2)
				? node1.getText().split("\\[")[0].compareTo(node2.getText().split("\\[")[0])
				: Boolean.compare(node1.isType(), node2.isType());
	}

	protected static boolean bothNodesAreTypesOrAttributes(final SyncTypeAttributeDataTreeNode node1,
			final SyncTypeAttributeDataTreeNode node2)
	{
		return node1.isType() == node2.isType();
	}

	protected void connectChildrenAndParent(final Map<SyncTypeAttributeDataTreeNode, List<SyncTypeAttributeDataTreeNode>> nodes)
	{
		nodes.keySet().forEach(key -> {
			ComposedTypeModel type = ((ComposedTypeModel) key.getData()).getSuperType();
			if (type != null)
			{
				type = findParent(nodes, type);
				nodes.get(SyncTypeAttributeDataTreeNode.createTypeNode(type)).add(key);
			}
		});
	}

	protected ComposedTypeModel findParent(final Map<SyncTypeAttributeDataTreeNode, List<SyncTypeAttributeDataTreeNode>> nodes,
			final ComposedTypeModel child)
	{
		ComposedTypeModel parent = child;
		while (!nodes.containsKey(SyncTypeAttributeDataTreeNode.createTypeNode(parent)))
		{
			parent = parent.getSuperType();
		}
		return parent;
	}

	protected void createAndAddEmptyRootNode(final Map<SyncTypeAttributeDataTreeNode, List<SyncTypeAttributeDataTreeNode>> nodes,
			final ComposedTypeModel rootType)
	{
		final List<SyncTypeAttributeDataTreeNode> rootTypes = nodes.keySet().stream()//
				.filter(f -> f.getData().equals(rootType))//
				.collect(Collectors.toList());

		if (rootTypes.size() != 1)
		{
			throw new IllegalStateException("There should be only one root node");
		}

		final SyncTypeAttributeDataTreeNode root = SyncTypeAttributeDataTreeNode.createRootNode();
		nodes.put(root, rootTypes);
	}
}
