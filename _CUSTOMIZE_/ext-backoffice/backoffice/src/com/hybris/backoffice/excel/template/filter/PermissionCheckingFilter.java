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
package com.hybris.backoffice.excel.template.filter;

import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.servicelayer.security.permissions.PermissionCRUDService;

import javax.annotation.Nonnull;

import org.springframework.beans.factory.annotation.Required;


/**
 * Filter which checks whether current user can read given {@link AttributeDescriptorModel} or not
 */
public class PermissionCheckingFilter implements ExcelFilter<AttributeDescriptorModel>
{
	private PermissionCRUDService permissionCRUDService;

	@Override
	public boolean test(@Nonnull final AttributeDescriptorModel attributeDescriptor)
	{
		return permissionCRUDService.canReadAttribute(attributeDescriptor);
	}

	@Required
	public void setPermissionCRUDService(final PermissionCRUDService permissionCRUDService)
	{
		this.permissionCRUDService = permissionCRUDService;
	}
}
