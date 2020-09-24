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
package com.hybris.backoffice.excel.exporting;

import de.hybris.platform.core.model.ItemModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hybris.backoffice.excel.data.SelectedAttribute;


/**
 * Allows grouping items and attributes by their type
 */
public interface ExcelExportDivider
{
	/**
	 * Groups collection of item models by variants
	 *
	 * @param items
	 *           to group
	 * @return map
	 */
	default Map<String, Set<ItemModel>> groupItemsByType(final Collection<ItemModel> items)
	{
		return groupItemsByType(new ArrayList<>(items));
	}

	/**
	 * Groups collection of selected attributes by variants. Attributes of every variant are extended by unique and required
	 * attributes for a specific variant.
	 *
	 * @param typeCodes
	 *           type codes of extracted variants
	 * @param selectedAttributes
	 *           by user
	 * @return map
	 */
	default Map<String, Set<SelectedAttribute>> groupAttributesByType(final Collection<String> typeCodes,
			final Collection<SelectedAttribute> selectedAttributes)
	{
		return groupAttributesByType(new HashSet<>(typeCodes), new ArrayList<>(selectedAttributes));
	}

	/**
	 * Groups collection of item models by variants
	 *
	 * @param items
	 *           to group
	 * @return map
	 * @deprecated since 1808, use {@link #groupItemsByType(Collection)} instead as it is more convenient
	 */
	@Deprecated
	Map<String, Set<ItemModel>> groupItemsByType(final List<ItemModel> items);

	/**
	 * Groups collection of selected attributes by variants. Attributes of every variant are extended by unique and required
	 * attributes for a specific variant.
	 *
	 * @param typeCodes
	 *           type codes of extracted variants
	 * @param selectedAttributes
	 *           by user
	 * @return map
	 * @deprecated since 1808, use {@link #groupAttributesByType(Collection, Collection)} instead as it is more convenient
	 */
	@Deprecated
	Map<String, Set<SelectedAttribute>> groupAttributesByType(final Set<String> typeCodes,
			final List<SelectedAttribute> selectedAttributes);

}
