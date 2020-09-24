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

import java.util.List;

import com.hybris.cockpitng.dnd.DragAndDropContext;
import com.hybris.cockpitng.dnd.DropOperationData;
import com.hybris.cockpitng.validation.model.ValidationInfo;

/**
 * Validator interface used in {@link com.hybris.backoffice.cockpitng.dnd.DefaultDragAndDropStrategy}
 * to provide more complex validation of drag&drop operations
 */
public interface DragAndDropValidator
{
	/**
	 * Returns true if validator is applicable to current drag&drop operation
	 * @param operationData data used in current drag&drop operation
	 * @param dragAndDropContext context of drag&drop operation
	 * @return true if validator is applicable, otherwise false
	 */
	boolean isApplicable(DropOperationData operationData,
						 DragAndDropContext dragAndDropContext);

	/**
	 * Validates drag&drop operation
	 * @param operationData data used in current drag&drop operation
	 * @param dragAndDropContext context of drag&drop operation
	 * @return list of validation infos
	 */
	List<ValidationInfo> validate(
			DropOperationData operationData,
			DragAndDropContext dragAndDropContext);
}
