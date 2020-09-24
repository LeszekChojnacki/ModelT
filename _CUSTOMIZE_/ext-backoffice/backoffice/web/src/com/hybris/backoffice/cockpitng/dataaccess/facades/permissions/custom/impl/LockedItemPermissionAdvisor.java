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

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.locking.ItemLockingService;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.cockpitng.dataaccess.facades.permissions.custom.InstancePermissionAdvisor;

/**
 * PermissionAdvisor responsible for evaluating whether it's possible to modify or delete an instance.
 * Implementation is based on {#ItemLockingService}.
 */
public class LockedItemPermissionAdvisor implements InstancePermissionAdvisor<ItemModel>
{

	private ItemLockingService itemLockingService;

	@Override
	public boolean canModify(final ItemModel instance)
	{
		return !getItemLockingService().isLocked(instance);
	}

	@Override
	public boolean canDelete(final ItemModel instance)
	{
		return !getItemLockingService().isLocked(instance);
	}

	@Override
	public boolean isApplicableTo(final Object instance)
	{
		return instance instanceof ItemModel;
	}

	public ItemLockingService getItemLockingService()
	{
		return itemLockingService;
	}

	@Required
	public void setItemLockingService(final ItemLockingService itemLockingService)
	{
		this.itemLockingService = itemLockingService;
	}
}
