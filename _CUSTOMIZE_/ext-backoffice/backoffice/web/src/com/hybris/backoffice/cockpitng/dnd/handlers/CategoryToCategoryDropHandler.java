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
package com.hybris.backoffice.cockpitng.dnd.handlers;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.model.CategoryModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.hybris.cockpitng.dnd.DragAndDropContext;
import com.hybris.cockpitng.dnd.DropOperationData;


/**
 * Drop handler responsible for appending/replacing supercategories for categories.
 */
public class CategoryToCategoryDropHandler extends AbstractReferenceDropHandler<CategoryModel, CategoryModel>
{
	public static final String APPEND_NOTIFICATION_KEY = "dragAndDropCategoryToCategoryAppend";
	public static final String REPLACE_NOTIFICATION_KEY = "dragAndDropCategoryToCategoryReplace";

	protected static final String PAREMETER_RELATED_OBJECTS_TO_UPDATE = "relatedObjectsToUpdate";
	protected static final String CONTEXT_PARAMETER_PARENT_OBJECT = "parentObject";

	@Override
	protected List<DropOperationData<CategoryModel, CategoryModel, Object>> handleAppend(final List<CategoryModel> dragged,
			final CategoryModel targetCategory, final DragAndDropContext context)
	{
		final List<DropOperationData<CategoryModel, CategoryModel, Object>> result = new ArrayList<>();

		for (final CategoryModel draggedCategory : dragged)
		{
			final CategoryModel modifiedCategory = assignSupercategory(draggedCategory, targetCategory, context);

			final DropOperationData<CategoryModel, CategoryModel, Object> singleResult = new DropOperationData<>(draggedCategory,
					targetCategory, modifiedCategory, context, APPEND_NOTIFICATION_KEY);

			result.add(singleResult);
		}

		return result;
	}

	@Override
	protected List<DropOperationData<CategoryModel, CategoryModel, Object>> handleReplace(final List<CategoryModel> dragged,
			final CategoryModel targetCategory, final DragAndDropContext context)
	{
		final List<DropOperationData<CategoryModel, CategoryModel, Object>> result = new ArrayList<>();

		for (final CategoryModel draggedCategory : dragged)
		{
			final CategoryModel modifiedCategory = replaceSupercategory(draggedCategory, targetCategory, context);

			final DropOperationData<CategoryModel, CategoryModel, Object> singleResult = new DropOperationData<>(draggedCategory,
					targetCategory, modifiedCategory, context, REPLACE_NOTIFICATION_KEY);

			result.add(singleResult);
		}

		return result;
	}

	protected CategoryModel assignSupercategory(final CategoryModel subcategory, final CategoryModel supercategory,
			final DragAndDropContext context)
	{
		addRelatedObjectToUpdateToContextAppend(subcategory, supercategory, context);
		final List<CategoryModel> supercategories = new ArrayList<>(subcategory.getSupercategories());
		if (!supercategories.contains(supercategory))
		{
			supercategories.add(supercategory);
		}
		subcategory.setSupercategories(supercategories);
		return subcategory;
	}

	protected void addRelatedObjectToUpdateToContextAppend(final CategoryModel dragged, final CategoryModel target,
			final DragAndDropContext context)
	{
		final List<Object> objectsToRefresh = new ArrayList<>();
		if (dragged.getCatalogVersion().equals(target.getCatalogVersion())
				&& !dragged.getSupercategories().stream().filter(c -> c.getCatalogVersion().equals(dragged.getCatalogVersion()))
						.findAny().isPresent())
		{
			objectsToRefresh.add(dragged.getCatalogVersion());
		}
		objectsToRefresh.add(target);
		context.setParameter(PAREMETER_RELATED_OBJECTS_TO_UPDATE, objectsToRefresh);
	}

	protected CategoryModel replaceSupercategory(final CategoryModel dragged, final CategoryModel target,
			final DragAndDropContext context)
	{
		addRelatedObjectToUpdateToContextReplace(dragged, target, context);
		final List<CategoryModel> supercategories = new LinkedList<>(dragged.getSupercategories());
		final Object parent = context.getDraggedContext().getParameter(CONTEXT_PARAMETER_PARENT_OBJECT);
		if (parent instanceof CategoryModel)
		{
			supercategories.removeIf(c -> c.equals(parent));
		}
		if (!supercategories.contains(target))
		{
			supercategories.add(target);
		}
		dragged.setSupercategories(supercategories);
		return dragged;
	}

	protected void addRelatedObjectToUpdateToContextReplace(final CategoryModel dragged, final CategoryModel target,
			final DragAndDropContext context)
	{
		final List<Object> objectsToRefresh = new ArrayList<>();
		final Object parent = context.getDraggedContext().getParameter(CONTEXT_PARAMETER_PARENT_OBJECT);
		if (parent instanceof CategoryModel)
		{
			objectsToRefresh.add(parent);
			final CategoryModel parentCategory = (CategoryModel) parent;
			if (!parentCategory.getCatalogVersion().equals(target.getCatalogVersion()))
			{
				final List<CategoryModel> supercategoriesInAssignedCatalogVersion = dragged.getSupercategories().stream()
						.filter(c -> c.getCatalogVersion().equals(dragged.getCatalogVersion())).collect(Collectors.toList());
				supercategoriesInAssignedCatalogVersion.remove(parent);
				if (supercategoriesInAssignedCatalogVersion.isEmpty())
				{
					objectsToRefresh.add(dragged.getCatalogVersion());
				}
			}
		}
		else if (parent instanceof CatalogVersionModel && dragged.getCatalogVersion().equals(target.getCatalogVersion()))
		{
			objectsToRefresh.add(parent);
		}
		objectsToRefresh.add(target);
		context.setParameter(PAREMETER_RELATED_OBJECTS_TO_UPDATE, objectsToRefresh);
	}

	@Override
	public List<String> findSupportedTypes()
	{
		return Collections.singletonList(CategoryModel._TYPECODE);
	}
}
