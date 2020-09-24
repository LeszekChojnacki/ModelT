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

import static com.hybris.cockpitng.tree.util.TreeUtils.matchFilterString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.zkoss.zul.AbstractTreeModel;
import org.zkoss.zul.TreeModel;

import com.hybris.cockpitng.tree.util.TreeUtils;


/**
 * Tree model that can be filtered.
 *
 * @param <E>
 *           see {@link TreeItemSelectable}
 */
public class FilteredTreeModel<E extends TreeItemSelectable> extends org.zkoss.zul.AbstractTreeModel<E>
{
	/** Determines which attributes should be shown. */
	public enum IncludedMode
	{
		ALL, INCLUDED_ONLY, NOT_INCLUDED_ONLY, EMPTY;

		/**
		 * Resolves {@link IncludedMode} value.
		 *
		 * @param showIncluded
		 *           show attributes included in synchronization
		 * @param showNotIncluded
		 *           show attributes not included in synchronization
		 * @return calculated {@link IncludedMode}
		 */
		public static FilteredTreeModel.IncludedMode resolveIncludeMode(final boolean showIncluded, final boolean showNotIncluded)
		{
			if (showIncluded && showNotIncluded)
			{
				return FilteredTreeModel.IncludedMode.ALL;
			}
			else if (showIncluded)
			{
				return FilteredTreeModel.IncludedMode.INCLUDED_ONLY;
			}
			else if (showNotIncluded)
			{
				return FilteredTreeModel.IncludedMode.NOT_INCLUDED_ONLY;
			}
			else
			{
				return FilteredTreeModel.IncludedMode.EMPTY;
			}
		}
	}

	private final transient Map<E, List<E>> childrenMap = new HashMap<>();
	private final String filterString;
	private final transient TreeUtils.FilterStringResolver<E> filterStringResolver;
	private final TreeUtils.MatchMode matchMode;
	private final transient TreeModel<E> originalModel;

	/**
	 * Constructor that creates a filtered version of an {@link AbstractTreeModel}.
	 *
	 * @param originalModel
	 *           the treemodel that should be filtered.
	 * @param filterString
	 *           the string that must match the treeitems.
	 * @param filterStringResolver
	 *           needed, if treenodes not of type {@link String}
	 */
	public FilteredTreeModel(final TreeModel<E> originalModel, final String filterString,
			final TreeUtils.FilterStringResolver<E> filterStringResolver, final TreeUtils.MatchMode matchMode,
			final IncludedMode includedMode)
	{
		super(originalModel.getRoot());
		this.filterString = StringUtils.defaultString(filterString, StringUtils.EMPTY);
		this.originalModel = originalModel;
		this.filterStringResolver = filterStringResolver;
		this.matchMode = matchMode;

		FilteredTreeModel.this.registerFilteredChildren(FilteredTreeModel.this.getRoot(), includedMode);

		removeEmptyNodes();
	}

	private void removeEmptyNodes()
	{
		final Map<E, List<E>> tmpMap = new HashMap<>();
		childrenMap.forEach((key, value) -> {
			if (!value.isEmpty())
			{
				tmpMap.put(key, value);
			}
		});
		childrenMap.clear();
		childrenMap.putAll(tmpMap);
	}

	@Override
	public E getChild(final E parent, final int index)
	{
		E ret = null;
		final List<E> list = this.childrenMap.get(parent);
		if (list != null && index < list.size())
		{
			ret = list.get(index);
		}
		return ret;
	}

	@Override
	public int getChildCount(final E parent)
	{
		final List<E> list = this.childrenMap.get(parent);
		return list == null ? 0 : list.size();
	}

	/**
	 * Getter for original model.
	 *
	 * @return original model, before filtration
	 */
	public TreeModel<E> getOriginalModel()
	{
		return this.originalModel;
	}

	@Override
	public boolean isLeaf(final E node)
	{
		return CollectionUtils.isEmpty(childrenMap.get(node));
	}

	/**
	 * Returns mapping Parent -> list of children. List of children may contain parent for another children. First parent is
	 * {@link AbstractTreeModel#getRoot()}.
	 *
	 * @return mapping parent -> list of children
	 */
	public Map<E, List<E>> getChildrenMap()
	{
		return childrenMap;
	}

	protected boolean registerFilteredChildren(final E parent, final IncludedMode includedMode)
	{
		final boolean includeIt = shouldBeIncluded(parent, includedMode);

		final boolean matchesFilter = matchFilterString(
				this.filterStringResolver == null ? parent : this.filterStringResolver.getStringRepresentation(parent),
				this.filterString, false, matchMode);


		boolean hasVisibleChildren = false;
		final int childCount = this.getOriginalModel().getChildCount(parent);

		final List<E> list = this.childrenMap.computeIfAbsent(parent, k -> new ArrayList<>());

		for (int i = 0; i < childCount; i++)
		{
			final E child = this.getOriginalModel().getChild(parent, i);
			final boolean displayChild = this.registerFilteredChildren(child, includedMode);
			if (displayChild)
			{
				list.add(child);
			}
			hasVisibleChildren |= displayChild;
		}

		return (hasVisibleChildren || matchesFilter) && includeIt;
	}

	private boolean shouldBeIncluded(final E parent, final IncludedMode includedMode)
	{
		if (parent == getRoot())
		{
			return true;
		}
		if (IncludedMode.EMPTY == includedMode)
		{
			return false;
		}

		final boolean isParentSelected = parent.getSelection() == TreeItemSelectable.SelectionType.ALL
				|| parent.getSelection() == TreeItemSelectable.SelectionType.PARTIALLY;
		final boolean showIncluded = IncludedMode.INCLUDED_ONLY == includedMode && isParentSelected;

		final boolean isParentDeSelected = parent.getSelection() == TreeItemSelectable.SelectionType.NONE
				|| parent.getSelection() == TreeItemSelectable.SelectionType.PARTIALLY;
		final boolean showNotIncluded = IncludedMode.NOT_INCLUDED_ONLY == includedMode && isParentDeSelected;

		return IncludedMode.ALL == includedMode || showIncluded || showNotIncluded;
	}
}
