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
package com.hybris.backoffice.workflow;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;

import java.util.List;
import java.util.Optional;


/**
 * Defines available attachments types and allows operation on attachments.
 */
public interface WorkflowsTypeFacade
{
	/**
	 * Based on configuration returns supported attachment classes names for current user.
	 * 
	 * @return list of attachments class names.
	 */
	List<String> getSupportedAttachmentClassNames();


	/**
	 * Based on configuration returns all supported attachment class names as opposed to
	 * {@link #getSupportedAttachmentClassNames()}
	 *
	 * @return list of attachments class names.
	 */
	List<String> getAllAttachmentClassNames();

	/**
	 * Based on configuration returns supported type codes for current user.
	 * 
	 * @return list of type codes.
	 */
	List<String> getSupportedAttachmentTypeCodes();

	/**
	 * Based on configuration returns supported ComposedTypes for current user.
	 * 
	 * @return list of ComposedTypes
	 */
	List<ComposedTypeModel> getSupportedAttachmentTypes();

	/**
	 * Finds common attachment type - gets common super type of given items and checks if it can be assigned to one of
	 * defined supported types {@link #getSupportedAttachmentTypeCodes()}.
	 *
	 * @param attachments
	 *           list of items to be added as attachments.
	 * @return supported attachment type - one of {@link #getSupportedAttachmentTypeCodes()}.
	 */
	Optional<ComposedTypeModel> findCommonAttachmentType(List<ItemModel> attachments);
}
