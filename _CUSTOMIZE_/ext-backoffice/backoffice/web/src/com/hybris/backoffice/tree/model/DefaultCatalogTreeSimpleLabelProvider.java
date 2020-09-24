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
package com.hybris.backoffice.tree.model;

import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.model.CategoryModel;

import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;


public class DefaultCatalogTreeSimpleLabelProvider implements CatalogTreeSimpleLabelProvider
{

	@Override
	public Optional<String> getNodeLabel(final Object parentNodeData, final Object nodeData)
	{
		String label = null;
		if (nodeData instanceof CatalogModel)
		{
			label = getCatalogNodeLabel(parentNodeData, (CatalogModel) nodeData);
		}
		if (nodeData instanceof CatalogVersionModel)
		{
			label = getCatalogVersionNodeLabel(parentNodeData, (CatalogVersionModel) nodeData);
		}
		if (nodeData instanceof CategoryModel)
		{
			label = getCategoryNodeLabel(parentNodeData, (CategoryModel) nodeData);
		}
		return Optional.ofNullable(label);
	}

	protected String getCatalogNodeLabel(final Object parentNodeData, final CatalogModel nodeData)
	{
		return StringUtils.isNotBlank(nodeData.getName()) ? nodeData.getName() : nodeData.getId();
	}

	protected String getCatalogVersionNodeLabel(final Object parentNodeData, final CatalogVersionModel nodeData)
	{
		return nodeData.getVersion();
	}

	protected String getCategoryNodeLabel(final Object parentNodeData, final CategoryModel category)
	{
		if (parentNodeData instanceof CategoryModel && category.getCatalogVersion() != null
				&& !Objects.equals(((CategoryModel) parentNodeData).getCatalogVersion(), category.getCatalogVersion()))
		{
			return getCategoryNodeLabelWithCatalogVersion(category, category.getCatalogVersion());
		}
		return category.getName();
	}

	protected String getCategoryNodeLabelWithCatalogVersion(final CategoryModel category, final CatalogVersionModel catalogVersion)
	{
		return String.format("%s [%s:%s]", category.getName(), catalogVersion.getCatalog().getName(), catalogVersion.getVersion());
	}

}
