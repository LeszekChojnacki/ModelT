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

import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.variants.model.VariantProductModel;

import java.util.Collections;
import java.util.List;

import org.zkoss.util.resource.Labels;

import com.hybris.backoffice.widgets.contextpopulator.ContextPopulator;
import com.hybris.cockpitng.core.context.CockpitContext;
import com.hybris.cockpitng.dnd.BackofficeDragAndDropContext;
import com.hybris.cockpitng.dnd.DragAndDropActionType;
import com.hybris.cockpitng.dnd.DragAndDropContext;
import com.hybris.cockpitng.dnd.DropOperationData;
import com.hybris.cockpitng.validation.impl.DefaultValidationInfo;
import com.hybris.cockpitng.validation.model.ValidationInfo;
import com.hybris.cockpitng.validation.model.ValidationSeverity;


/**
 * Validator returns errors when user tries to move product variants or user tries to move product to category whereas
 * products is currently assigned to more than one category.
 */
public class ProductToCategoryValidator implements DragAndDropValidator
{

	public static final String DND_VALIDATION_PRODUCT_TO_CATEGORY_WITHOUT_CONTEXT_MSG = "dnd.validation.product.to.category.without.context.msg";
	public static final String DND_VALIDATION_VERIANT_PRODUCT_TO_CATEGORY_MSG = "dnd.validation.variant.product.to.category.msg";

	@Override
	public boolean isApplicable(final DropOperationData operationData, final DragAndDropContext dragAndDropContext)
	{
		return ProductModel.class.isAssignableFrom(operationData.getDragged().getClass())
				&& CategoryModel.class.isAssignableFrom(operationData.getTarget().getClass());
	}

	@Override
	public List<ValidationInfo> validate(final DropOperationData operationData, final DragAndDropContext dragAndDropContext)
	{
		if (operationData.getDragged() instanceof VariantProductModel)
		{
			return Collections
					.singletonList(createValidationInfo(ValidationSeverity.ERROR, DND_VALIDATION_VERIANT_PRODUCT_TO_CATEGORY_MSG));
		}
		if (dragAndDropContext instanceof BackofficeDragAndDropContext
				&& ((BackofficeDragAndDropContext) dragAndDropContext).getActionType().equals(DragAndDropActionType.REPLACE))
		{
			final CockpitContext dragContext = dragAndDropContext.getDraggedContext();
			if (dragContext != null && operationData.getDragged() instanceof ProductModel
					&& operationData.getTarget() instanceof CategoryModel)
			{
				final Object selectedObject = dragContext.getParameter(ContextPopulator.SELECTED_OBJECT);
				final ProductModel draggedProduct = (ProductModel) operationData.getDragged();
				final CategoryModel targetCategory = (CategoryModel) operationData.getTarget();
				if (!(selectedObject instanceof CategoryModel) && (!containsOnlyTargetCategory(draggedProduct, targetCategory)
						|| draggedProduct.getSupercategories().size() > 1))
				{
					return Collections.singletonList(
							createValidationInfo(ValidationSeverity.ERROR, DND_VALIDATION_PRODUCT_TO_CATEGORY_WITHOUT_CONTEXT_MSG));
				}
			}
		}

		return Collections.emptyList();
	}

	private static boolean containsOnlyTargetCategory(final ProductModel product, final CategoryModel category)
	{
		return product.getSupercategories().size() == 1 && product.getSupercategories().contains(category);
	}

	private DefaultValidationInfo createValidationInfo(final ValidationSeverity severity, final String labelKey,
			final Object... labelArgs)
	{
		final DefaultValidationInfo validationInfo = new DefaultValidationInfo();
		validationInfo.setValidationMessage(getLabel(labelKey, labelArgs));
		validationInfo.setValidationSeverity(severity);
		return validationInfo;
	}

	protected String getLabel(final String labelKey, final Object... labelArgs)
	{
		return Labels.getLabel(labelKey, labelArgs);
	}
}
