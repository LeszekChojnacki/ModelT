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
package com.hybris.backoffice.tree;

import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.model.CategoryModel;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.cockpitng.tree.factory.impl.DefaultNavigationTreeFactory;
import com.hybris.cockpitng.widgets.common.explorertree.refreshstrategy.ExplorerTreeRefreshStrategy;


public class ExplorerTreeCatalogsDynamicNodeRefreshStrategy implements ExplorerTreeRefreshStrategy
{

	private String rootNodeId;

	@Override
	public Collection<? extends Object> findRelatedObjectsToRefresh(final Object updatedObject)
	{
		Collection<? extends Object> relatedObjects = Collections.emptyList();
		if (updatedObject instanceof CategoryModel)
		{
			relatedObjects = findRelatedObjectsForCategory((CategoryModel) updatedObject);
		}
		if (updatedObject instanceof CatalogModel)
		{
			relatedObjects = findRelatedObjectsForCatalog();
		}
		if (updatedObject instanceof CatalogVersionModel)
		{
			relatedObjects = findRelatedObjectsForCatalogVersion((CatalogVersionModel) updatedObject);
		}
		return relatedObjects;
	}

	private static Collection<? extends Object> findRelatedObjectsForCatalogVersion(final CatalogVersionModel updatedObject)
	{
		return Arrays.asList(updatedObject.getCatalog());
	}

	private Collection<? extends Object> findRelatedObjectsForCatalog()
	{
		return Arrays.asList(DefaultNavigationTreeFactory.ROOT_NODE_ID, getRootNodeId());
	}

	private static Collection<? extends Object> findRelatedObjectsForCategory(final CategoryModel updatedObject)
	{
		final List<CategoryModel> superCategories = updatedObject.getSupercategories();
		return CollectionUtils.isNotEmpty(superCategories) ? superCategories : Arrays.asList(updatedObject.getCatalogVersion());
	}

	public String getRootNodeId()
	{
		return rootNodeId;
	}

	@Required
	public void setRootNodeId(final String rootNodeId)
	{
		this.rootNodeId = rootNodeId;
	}
}
