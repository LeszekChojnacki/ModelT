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
package com.hybris.backoffice.widgets.actions.locking;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.locking.ItemLockingService;

import java.util.Collection;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.impl.DefaultActionRenderer;


public class ToggleItemLockActionRenderer extends DefaultActionRenderer<Object, Object>
{
	protected static final String ICON_ACTION_UNLOCK_ITEM_HOVER_PNG = "icon_action_unlock_item_hover.png";
	protected static final String ICON_ACTION_LOCK_ITEM_HOVER_PNG = "icon_action_lock_item_hover.png";
	protected static final String ICON_ACTION_UNLOCK_ITEM_DEFAULT_PNG = "icon_action_unlock_item_default.png";
	protected static final String ICON_ACTION_LOCK_ITEM_DEFAULT_PNG = "icon_action_lock_item_default.png";
	protected static final String ICON_ACTION_UNLOCK_ITEM_DISABLED_PNG = "icon_action_unlock_item_disabled.png";
	protected static final String ICON_ACTION_LOCK_ITEM_DISABLED_PNG = "icon_action_lock_item_disabled.png";
	protected static final String I18N_UNLOCK_ACTION_TOOLTIP = "perform.unlock.tooltip";
	protected static final String I18N_LOCK_ACTION_TOOLTIP = "perform.lock.tooltip";
	private static final String ICON_ROOT_PATH = "icons/";

	@Resource
	private ItemLockingService itemLockingService;

	@Override
	protected String getIconHoverUri(final ActionContext<Object> context, final boolean canPerform)
	{
		final Object data = context.getData();

		if (!canPerform)
		{
			return adjustUri(context, getLockedIconUri(data, ICON_ACTION_UNLOCK_ITEM_DISABLED_PNG, ICON_ACTION_LOCK_ITEM_DISABLED_PNG));
		}
		return adjustUri(context, getLockedIconUri(data, ICON_ACTION_UNLOCK_ITEM_HOVER_PNG, ICON_ACTION_LOCK_ITEM_HOVER_PNG));
	}

	@Override
	protected String getIconUri(final ActionContext<Object> context, final boolean canPerform)
	{
		final Object data = context.getData();

		if (!canPerform)
		{
			return adjustUri(context, getLockedIconUri(data, ICON_ACTION_UNLOCK_ITEM_DISABLED_PNG, ICON_ACTION_LOCK_ITEM_DISABLED_PNG));
		}
		return adjustUri(context, getLockedIconUri(data, ICON_ACTION_UNLOCK_ITEM_DEFAULT_PNG, ICON_ACTION_LOCK_ITEM_DEFAULT_PNG));
	}

	protected String getLockedIconUri(final Object data, final String lockedFileName, final String unlockedFileName)
	{
		if (isLocked(data))
		{
			return ICON_ROOT_PATH + lockedFileName;
		}
		else
		{
			return ICON_ROOT_PATH + unlockedFileName;
		}
	}

	@Override
	protected String getLocalizedName(final ActionContext<?> context)
	{
		final String result;
		if (isLocked(context.getData()))
		{
			result = context.getLabel(I18N_UNLOCK_ACTION_TOOLTIP);
		}
		else
		{
			result = context.getLabel(I18N_LOCK_ACTION_TOOLTIP);
		}
		return StringUtils.defaultIfBlank(result, super.getLocalizedName(context));
	}

	protected boolean isLocked(final Object data)
	{
		final ItemLockingService lockingService = getItemLockingService();
		if (data instanceof ItemModel)
		{
			return lockingService.isLocked((ItemModel) data);
		}
		else if (data instanceof Collection)
		{
			return ((Collection<?>) data).stream().allMatch(this::isLocked);
		}
		return false;
	}

	protected ItemLockingService getItemLockingService()
	{
		return itemLockingService;
	}
}
