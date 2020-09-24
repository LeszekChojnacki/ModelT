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
package com.hybris.backoffice.excel.exporting.data.filter;

import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.servicelayer.security.permissions.PermissionCRUDService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * Predicate which checks if user has 'read' permissions to export an {@link AttributeDescriptorModel} to Excel files.
 *
 * @see com.hybris.backoffice.excel.exporting.DefaultExcelExportService
 */
public class PermissionCrudAttributePredicate implements ExcelExportAttributePredicate
{
	private static final Logger LOG = LoggerFactory.getLogger(PermissionCrudAttributePredicate.class);
	private PermissionCRUDService permissionCRUDService;

	@Override
	public boolean test(final AttributeDescriptorModel attributeDescriptorModel)
	{
		final boolean canReadAttribute = getPermissionCRUDService().canReadAttribute(attributeDescriptorModel);
		if (!canReadAttribute)
		{
			logWarn("Insufficient permissions for attribute: '{}'", attributeDescriptorModel.getQualifier());
		}
		return canReadAttribute;
	}

	private void logWarn(final String message, final Object... params)
	{
		LOG.warn(message, params);
	}

	public PermissionCRUDService getPermissionCRUDService()
	{
		return permissionCRUDService;
	}

	@Required
	public void setPermissionCRUDService(final PermissionCRUDService permissionCRUDService)
	{
		this.permissionCRUDService = permissionCRUDService;
	}
}
