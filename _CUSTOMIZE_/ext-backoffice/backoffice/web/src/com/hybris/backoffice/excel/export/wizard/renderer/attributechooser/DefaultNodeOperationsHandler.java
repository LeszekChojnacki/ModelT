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

import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zul.AbstractTreeModel;
import org.zkoss.zul.DefaultTreeModel;
import org.zkoss.zul.DefaultTreeNode;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.TreeNode;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.event.TreeDataEvent;
import org.zkoss.zul.ext.SelectionControl;

import com.google.common.collect.Lists;
import com.hybris.backoffice.excel.data.SelectedAttribute;
import com.hybris.cockpitng.tree.util.TreeUtils;


/**
 * Default implementation of {@link NodeOperationsHandler}
 *
 * @deprecated since 6.7 {@link com.hybris.backoffice.attributechooser.AttributesChooserNodesOperationsHandler}
 */
@Deprecated
public class DefaultNodeOperationsHandler implements NodeOperationsHandler
{

	private CommonI18NService commonI18NService;

	@Override
	public void moveNodesBetweenTrees(final AbstractTreeModel<TreeNode<SelectedAttribute>> sourceModel,
			final AbstractTreeModel<TreeNode<SelectedAttribute>> targetModel)
	{

		final Set<TreeNode<SelectedAttribute>> selection = targetModel.getSelection();
		final Set<TreeNode<SelectedAttribute>> openObjects = targetModel.getOpenObjects();
		sourceModel.getSelection().forEach(selectedNode -> {

			if (!isNodeSelectable(selectedNode))
			{
				return; // cannot move mandatory items
			}


			if (!selectedNode.getData().isLocalized())
			{
				removeFromParent(selectedNode);
				targetModel.getRoot().add((selectedNode));
			}
			else if (!selectedNode.isLeaf())
			{
				moveGroupingNode(targetModel, selectedNode);
			}
			else
			{
				moveSubNode(targetModel, selectedNode);
			}
		});
		sourceModel.clearSelection();
		refreshFilteredTree(sourceModel);
		sort(targetModel, true);
		targetModel.setOpenObjects(openObjects);
		targetModel.setSelection(selection);
	}

	protected void refreshFilteredTree(final AbstractTreeModel<TreeNode<SelectedAttribute>> sourceModel)
	{
		if (sourceModel instanceof TreeUtils.FilteredTreeModel)
		{
			((TreeUtils.FilteredTreeModel) sourceModel).refreshChildren(sourceModel.getRoot(), sourceModel.getRoot().getChildren());
		}
		sourceModel.fireEvent(TreeDataEvent.STRUCTURE_CHANGED, null, 0, 0);
	}

	protected void removeFromParent(final TreeNode<SelectedAttribute> node)
	{
		if (node.getParent() != null)
		{
			node.getParent().remove(node);
		}
	}

	protected void moveSubNode(final AbstractTreeModel<TreeNode<SelectedAttribute>> targetModel,
			final TreeNode<SelectedAttribute> selectedNode)
	{
		final TreeNode<SelectedAttribute> oldGroupingNode = selectedNode.getParent();

		final Optional<TreeNode<SelectedAttribute>> parentTreeNode = getGroupingNodeByAttribute(targetModel,
				selectedNode.getParent().getData());
		removeFromParent(selectedNode);
		if (parentTreeNode.isPresent())
		{
			parentTreeNode.get().add(selectedNode);
			targetModel.removeFromSelection(parentTreeNode.get());
		}
		else
		{
			targetModel.getRoot().add(createParent(selectedNode));
		}

		if (CollectionUtils.isEmpty(oldGroupingNode.getChildren()))
		{
			oldGroupingNode.getParent().remove(oldGroupingNode);
		}
	}

	protected DefaultTreeNode<SelectedAttribute> createParent(final TreeNode<SelectedAttribute> selectedNode)
	{
		final SelectedAttribute child = selectedNode.getData();
		final SelectedAttribute parent = new SelectedAttribute(null, child.getAttributeDescriptor());
		parent.setIsoCode(null);

		return new DefaultTreeNode<>(parent, Lists.newArrayList(selectedNode));
	}

	protected void moveGroupingNode(final AbstractTreeModel<TreeNode<SelectedAttribute>> targetModel,
			final TreeNode<SelectedAttribute> selectedNode)
	{
		final Optional<TreeNode<SelectedAttribute>> targetParent = getGroupingNodeByAttribute(targetModel, selectedNode.getData());
		if (targetParent.isPresent())
		{
			new ArrayList<>(selectedNode.getChildren()).forEach(child -> {
				selectedNode.remove(child);
				targetParent.get().getChildren().add(child);
			});
			if (CollectionUtils.isEmpty(selectedNode.getChildren()))
			{
				removeFromParent(selectedNode);
			}
		}
		else
		{
			removeFromParent(selectedNode);
			targetModel.getRoot().add(selectedNode);
		}
	}

	protected Optional<TreeNode<SelectedAttribute>> getGroupingNodeByAttribute(
			final AbstractTreeModel<TreeNode<SelectedAttribute>> targetModel, final SelectedAttribute attribute)
	{
		return targetModel.getRoot().getChildren().stream().filter(node -> !node.isLeaf())
				.filter(node -> Objects.equals(attribute, node.getData())).findFirst();
	}


	@Override
	public void updateGroupingNodesSelection(final AbstractTreeModel<TreeNode<SelectedAttribute>> treeModel,
			final SelectEvent<Treeitem, TreeNode<SelectedAttribute>> event)
	{
		selectGroupingNodes(treeModel, event);
		deselectGroupingNodes(treeModel, event);
	}

	protected void deselectGroupingNodes(final AbstractTreeModel model,
			final SelectEvent<Treeitem, TreeNode<SelectedAttribute>> event)
	{
		getGroupingNodes(event.getUnselectedObjects()).forEach(groupingNode -> {
			if (event.getUnselectedObjects().contains(groupingNode))
			{
				groupingNode.getChildren().forEach(model::removeFromSelection);
			}
			else if (event.getUnselectedObjects().stream().anyMatch(node -> groupingNode.getChildren().contains(node)))
			{
				model.removeFromSelection(groupingNode);
			}
		});
	}

	protected void selectGroupingNodes(final AbstractTreeModel model,
			final SelectEvent<Treeitem, TreeNode<SelectedAttribute>> event)
	{
		final Set<TreeNode<SelectedAttribute>> newSelection = new HashSet<>(event.getSelectedObjects());
		newSelection.removeAll(event.getPreviousSelectedObjects());

		getGroupingNodes(newSelection).forEach(groupingNode -> {

			if (newSelection.contains(groupingNode))
			{
				//noinspection unchecked
				groupingNode.getChildren().forEach(model::addToSelection);
			}
			else if (event.getSelectedObjects().containsAll(groupingNode.getChildren()))
			{
				//noinspection unchecked
				model.addToSelection(groupingNode);
			}
		});
	}

	protected Set<TreeNode<SelectedAttribute>> getGroupingNodes(final Set<TreeNode<SelectedAttribute>> selectedNodes)
	{
		return selectedNodes.stream()//
				.map(node -> {
					if (!node.isLeaf())
					{
						return node;
					}
					else if (node.getData().isLocalized() && StringUtils.isNotEmpty(node.getData().getIsoCode()))
					{
						return node.getParent();
					}
					return null;
				})//
				.filter(node -> !Objects.isNull(node))//
				.collect(Collectors.toSet());
	}

	@Override
	public void sort(final AbstractTreeModel<TreeNode<SelectedAttribute>> treeModel, final boolean ascending)
	{
		if (treeModel instanceof DefaultTreeModel)
		{
			sort((DefaultTreeModel<SelectedAttribute>) treeModel, ascending);
		}
		else if (treeModel instanceof TreeUtils.FilteredTreeModel)
		{
			final DefaultTreeModel<SelectedAttribute> model = (DefaultTreeModel) ((TreeUtils.FilteredTreeModel) treeModel)
					.getOriginalModel();
			sort(model, ascending);
			refreshFilteredTree(treeModel);
		}
	}

	protected void sort(final DefaultTreeModel<SelectedAttribute> model, final boolean ascending)
	{
		model.sort(Comparator.comparing(node -> {
			if (StringUtils.isNotEmpty(node.getData().getIsoCode()))
			{
				return node.getData().getName() + node.getData().getIsoCode();
			}
			return node.getData().getName();
		}), ascending);
	}

	@Override
	public AbstractTreeModel<TreeNode<SelectedAttribute>> filterTreeModel(
			final AbstractTreeModel<TreeNode<SelectedAttribute>> model, final String searchText)
	{

		final Set<TreeNode<SelectedAttribute>> openObjects = model.getOpenObjects();
		final TreeModel<TreeNode<SelectedAttribute>> unfiltered = model instanceof TreeUtils.FilteredTreeModel
				? ((TreeUtils.FilteredTreeModel) model).getOriginalModel()
				: model;

		final TreeUtils.FilteredTreeModel<TreeNode<SelectedAttribute>> filtered = new TreeUtils.FilteredTreeModel<>(unfiltered,
				searchText, false, getAttributeSearchTextResolver());
		filtered.setMultiple(true);
		filtered.setOpenObjects(openObjects);
		filtered.setSelectionControl(getSelectionControl(filtered));
		return filtered;
	}

	protected SelectionControl<TreeNode<SelectedAttribute>> getSelectionControl(
			final AbstractTreeModel<TreeNode<SelectedAttribute>> model)
	{
		return new AbstractTreeModel.DefaultSelectionControl<TreeNode<SelectedAttribute>>(model)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isSelectable(final TreeNode<SelectedAttribute> e)
			{
				return isNodeSelectable(e);
			}
		};
	}

	protected boolean isNodeSelectable(final TreeNode<SelectedAttribute> treeNode)
	{
		return treeNode.getData() != null && !treeNode.getData().isRequired(commonI18NService.getCurrentLanguage().getIsocode())
				&& (!isLocalizedRoot(treeNode) || areChildrenSelectable(treeNode));
	}

	protected boolean isLocalizedRoot(final TreeNode<SelectedAttribute> treeNode)
	{
		return treeNode.getData() != null && treeNode.getData().isLocalized()
				&& StringUtils.isEmpty(treeNode.getData().getIsoCode());
	}

	protected boolean areChildrenSelectable(final TreeNode<SelectedAttribute> treeNode)
	{
		if (CollectionUtils.isNotEmpty(treeNode.getChildren()))
		{
			for (final TreeNode<SelectedAttribute> child : treeNode.getChildren())
			{
				if (!isNodeSelectable(child))
				{
					return false;
				}
			}
		}
		return true;
	}

	protected TreeUtils.FilterStringResolver<TreeNode<SelectedAttribute>> getAttributeSearchTextResolver()
	{
		return node -> node.getData() != null ? node.getData().getName() : StringUtils.EMPTY;
	}


	@Override
	public List<SelectedAttribute> extractAttributes(final AbstractTreeModel<TreeNode<SelectedAttribute>> treeModel)
	{
		return treeModel.getRoot().getChildren().stream().flatMap(node -> {
			if (node.getChildCount() == 0)
			{
				return Stream.of(node.getData());
			}
			else
			{
				return node.getChildren().stream().map(TreeNode::getData);
			}
		}).collect(Collectors.toList());
	}

	@Override
	public List<DefaultTreeNode<SelectedAttribute>> createTreeNodes(final Collection<AttributeDescriptorModel> attributes,
			final Predicate<SelectedAttribute> filter)
	{
		final Set<String> langs = getSupportedLanguages();
		return attributes.stream().map(attribute -> {
			final SelectedAttribute selectedAttribute = new SelectedAttribute(attribute);
			if (selectedAttribute.isLocalized())
			{
				final Set<DefaultTreeNode<SelectedAttribute>> localizedChildren = createLocalizedChildren(attribute, langs, filter);
				if (CollectionUtils.isNotEmpty(localizedChildren))
				{
					return new DefaultTreeNode<>(selectedAttribute, localizedChildren);
				}
			}
			else if (filter.test(selectedAttribute))
			{
				return new DefaultTreeNode<>(selectedAttribute);
			}
			return null;
		}).filter(obj -> !Objects.isNull(obj)).collect(Collectors.toList());
	}

	protected Set<DefaultTreeNode<SelectedAttribute>> createLocalizedChildren(final AttributeDescriptorModel attribute,
			final Set<String> languages, final Predicate<SelectedAttribute> includedAttributes)
	{
		return languages.stream().map(language -> new SelectedAttribute(language, attribute)).filter(includedAttributes)
				.map(DefaultTreeNode::new).collect(Collectors.toSet());
	}

	protected Set<String> getSupportedLanguages()
	{
		return commonI18NService.getAllLanguages().stream().filter(LanguageModel::getActive).map(LanguageModel::getIsocode)
				.collect(Collectors.toSet());
	}

	@Required
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}

	public CommonI18NService getCommonI18NService()
	{
		return commonI18NService;
	}


}
