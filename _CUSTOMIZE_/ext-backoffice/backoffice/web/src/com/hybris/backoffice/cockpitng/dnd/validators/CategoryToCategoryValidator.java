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
package com.hybris.backoffice.cockpitng.dnd.validators;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.servicelayer.model.ItemModelContext;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.zkoss.util.resource.Labels;

import com.hybris.cockpitng.dnd.DragAndDropContext;
import com.hybris.cockpitng.dnd.DropOperationData;
import com.hybris.cockpitng.validation.impl.DefaultValidationInfo;
import com.hybris.cockpitng.validation.model.ValidationInfo;
import com.hybris.cockpitng.validation.model.ValidationSeverity;


public class CategoryToCategoryValidator implements DragAndDropValidator
{

	public static final String MOVED_TO_CATALOG_VERSION_MSG = "dnd.validation.category.to.category.moved.to.catalog.version.msg";

	@Override
	public boolean isApplicable(final DropOperationData operationData,
								final DragAndDropContext dragAndDropContext)
	{
		return CategoryModel.class.isAssignableFrom(operationData.getDragged().getClass())
				&& CategoryModel.class.isAssignableFrom(operationData.getTarget().getClass());
	}

	@Override
	public List<ValidationInfo> validate(final DropOperationData operationData, final DragAndDropContext dragAndDropContext)
	{
		if (!validate((CategoryModel) operationData.getModified()))
		{
			return Collections.singletonList(
					createValidationInfo(ValidationSeverity.WARN, MOVED_TO_CATALOG_VERSION_MSG));
		}
		else
		{
			return Collections.emptyList();
		}
	}

	private DefaultValidationInfo createValidationInfo(final ValidationSeverity severity, final String labelKey,
			final Object... labelArgs)
	{
		final DefaultValidationInfo validationInfo = new DefaultValidationInfo();
		validationInfo.setValidationMessage(Labels.getLabel(labelKey, labelArgs));
		validationInfo.setValidationSeverity(severity);
		return validationInfo;
	}

	protected boolean validate(final CategoryModel category)
	{
		if (anySupercategoryInCatalogVersion(category.getSupercategories(), category.getCatalogVersion()))
		{
			return true;
		}

		final ItemModelContext context = category.getItemModelContext();
		if (context.isDirty(CategoryModel.SUPERCATEGORIES))
		{
			final List<CategoryModel> originalSupercategories = context.getOriginalValue(CategoryModel.SUPERCATEGORIES);
			if (anySupercategoryInCatalogVersion(originalSupercategories, category.getCatalogVersion()))
			{
				return false;
			}
		}
		return true;
	}

	private boolean anySupercategoryInCatalogVersion(final Collection<CategoryModel> supercategories,
													 final CatalogVersionModel catalogVersion)
	{
		return supercategories.stream().anyMatch(c -> c.getCatalogVersion().equals(catalogVersion));
	}
}
