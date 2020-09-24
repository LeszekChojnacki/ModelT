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
package com.hybris.backoffice.cockpitng.dataaccess.facades.permissions.custom.impl;

import de.hybris.platform.util.ViewResultItem;

import com.hybris.backoffice.cockpitng.dataaccess.facades.permissions.custom.InstancePermissionAdvisor;


public class ViewResultItemPermissionAdvisor implements InstancePermissionAdvisor<ViewResultItem>
{

	@Override
	public boolean canModify(final ViewResultItem instance)
	{
		return false;
	}

	@Override
	public boolean canDelete(final ViewResultItem instance)
	{
		return false;
	}

	@Override
	public boolean isApplicableTo(final Object instance)
	{
		return instance instanceof ViewResultItem;
	}
}
