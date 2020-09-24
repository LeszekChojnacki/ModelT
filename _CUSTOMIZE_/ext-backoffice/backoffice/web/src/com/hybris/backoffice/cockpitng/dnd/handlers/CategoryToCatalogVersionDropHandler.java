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
import java.util.List;
import java.util.stream.Collectors;

import com.hybris.cockpitng.dnd.DragAndDropContext;
import com.hybris.cockpitng.dnd.DropOperationData;


/**
 * Drop handler responsible for replacing catalog version for categories.
 */
public class CategoryToCatalogVersionDropHandler extends AbstractReferenceDropHandler<CategoryModel, CatalogVersionModel>
{
	public static final String NOTIFICATION_KEY = "dragAndDropCategoryToCatalogVersion";
	public static final String PARENT_OBJECT = "parentObject";

	@Override
	protected List<DropOperationData<CategoryModel, CatalogVersionModel, Object>> handleAppend(final List<CategoryModel> dragged,
			final CatalogVersionModel catalogVersionModel, final DragAndDropContext context)
	{
		return handle(dragged, catalogVersionModel, context);
	}

	@Override
	protected List<DropOperationData<CategoryModel, CatalogVersionModel, Object>> handleReplace(final List<CategoryModel> dragged,
			final CatalogVersionModel catalogVersionModel, final DragAndDropContext context)
	{
		return handle(dragged, catalogVersionModel, context);
	}

	protected List<DropOperationData<CategoryModel, CatalogVersionModel, Object>> handle(final List<CategoryModel> dragged,
			final CatalogVersionModel catalogVersionModel, final DragAndDropContext context)
	{
		final List<DropOperationData<CategoryModel, CatalogVersionModel, Object>> result = new ArrayList<>();

		for (final CategoryModel categoryModel : dragged)
		{
			final DropOperationData<CategoryModel, CatalogVersionModel, Object> operationData;
			if (canHandleDrop(categoryModel, catalogVersionModel, context))
			{
				final CategoryModel resultModel = assignCategoryToCatalogVersion(categoryModel, catalogVersionModel, context);
				operationData = new DropOperationData<>(categoryModel, catalogVersionModel, resultModel, context, NOTIFICATION_KEY);
			}
			else
			{
				operationData = new DropOperationData(categoryModel, catalogVersionModel, context);
			}

			result.add(operationData);
		}

		return result;
	}


	protected CategoryModel assignCategoryToCatalogVersion(final CategoryModel category, final CatalogVersionModel catalogVersion,
			final DragAndDropContext context)
	{
		addRelatedObjectToUpdateToContext(category, catalogVersion, context);
		if (category.getSupercategories().size() <= 1)
		{
			category.setSupercategories(Collections.emptyList());
		}
		else
		{
			final Object parentModel = context.getDraggedContext().getParameter(PARENT_OBJECT);
			if (parentModel instanceof CategoryModel)
			{
				final List<CategoryModel> updatedSupercategories = category.getSupercategories().stream()
						.filter(supercategory -> !supercategory.equals(parentModel)).collect(Collectors.toList());
				category.setSupercategories(updatedSupercategories);
			}
		}
		return category;
	}

	protected void addRelatedObjectToUpdateToContext(final CategoryModel category, final CatalogVersionModel catalogVersion,
			final DragAndDropContext context)
	{
		final List<Object> relatedObjectsToUpdate = new ArrayList<>();
		relatedObjectsToUpdate.add(context.getDraggedContext().getParameter(PARENT_OBJECT));
		if (category.getSupercategories().size() == 1)
		{
			relatedObjectsToUpdate.add(catalogVersion);
		}

		context.setParameter("relatedObjectsToUpdate", relatedObjectsToUpdate);
	}

	protected boolean canHandleDrop(final CategoryModel category, final CatalogVersionModel catalogVersion,
			final DragAndDropContext context)
	{
		return category.getCatalogVersion().equals(catalogVersion);
	}

	@Override
	public List<String> findSupportedTypes()
	{
		return Collections.singletonList(CategoryModel._TYPECODE);
	}
}
