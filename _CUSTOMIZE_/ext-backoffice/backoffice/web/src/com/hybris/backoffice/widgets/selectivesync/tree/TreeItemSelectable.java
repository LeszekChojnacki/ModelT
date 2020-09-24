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

/** Interface determining {@link SelectionType}. */
public interface TreeItemSelectable
{
	/** Selection options for types. */
	enum SelectionType
	{
		ALL, NONE, PARTIALLY;

		/**
		 * Resolves selection basing on sub-nodes presence of each SelectionType.
		 *
		 * @param allSelected
		 *           all sub node are selected
		 * @param allDeselected
		 *           all sub nodes are deselected
		 * @return selection type
		 */
		public static TreeItemSelectable.SelectionType resolveSelectionType(final boolean allSelected, final boolean allDeselected)
		{
			if (allSelected)
			{
				return TreeItemSelectable.SelectionType.ALL;
			}
			else if (allDeselected)
			{
				return TreeItemSelectable.SelectionType.NONE;
			}
			else
			{
				return TreeItemSelectable.SelectionType.PARTIALLY;
			}
		}

		/**
		 * Resolves selection basing on sub-nodes number grouped by SelectionType.
		 *
		 * @param allSelectedCount
		 *           number of selected sub nodes
		 * @param partiallySelectedCount
		 *           number of partially selected sub nodes
		 * @param noneSelectedCount
		 *           number of not selected sub nodes
		 * @return selection type
		 */
		public static TreeItemSelectable.SelectionType resolveSelectionType(final long allSelectedCount,
				final long partiallySelectedCount, final long noneSelectedCount)
		{
			final boolean allSelected = ((noneSelectedCount + partiallySelectedCount) == 0);
			final boolean allDeselected = ((allSelectedCount + partiallySelectedCount) == 0);
			return resolveSelectionType(allSelected, allDeselected);
		}
	}

	SelectionType getSelection();

	void setSelection(final SelectionType selection);
}
