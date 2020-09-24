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
import java.util.Collections;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEventTypes;
import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.core.util.Validate;
import com.hybris.cockpitng.dataaccess.context.impl.DefaultContext;
import com.hybris.cockpitng.dataaccess.util.CockpitGlobalEventPublisher;
import com.hybris.cockpitng.engine.impl.AbstractComponentWidgetAdapterAware;


/**
 * Action allows to lock and unlock an item or items. It is impossible to delete, edit or save locked items.
 */
public class ToggleItemLockAction extends AbstractComponentWidgetAdapterAware implements CockpitAction<Object, Object>
{
	protected static final String GLOBAL_EVENT_ITEM_LOCKED_STATE_CHANGED = "itemLockedStateChangedGlobalEvent";
	protected static final String LOCK_CONFIRMATION_MESSAGE = "perform.lock.message";
	protected static final String UNLOCK_CONFIRMATION_MESSAGE = "perform.unlock.message";
	protected static final String LOCK_CONFIRMATION_MESSAGE_MULTI = "perform.lock.multi.message";
	protected static final String UNLOCK_CONFIRMATION_MESSAGE_MULTI = "perform.unlock.multi.message";

	private static final Logger LOG = LoggerFactory.getLogger(ToggleItemLockAction.class);

	@Resource
	private ItemLockingService itemLockingService;
	@Resource
	private CockpitGlobalEventPublisher eventPublisher;
	@Resource
	private NotificationService notificationService;

	@Override
	public ActionResult<Object> perform(final ActionContext<Object> ctx)
	{
		final Object data = ctx.getData();
		try
		{
			if (data instanceof Collection)
			{
				final Collection<?> collection = (Collection<?>) data;
				bulkChangeLock(collection);
				publishGlobalNotificationOnLockingStateChange(collection);
				showNotifications(ctx, collection);
			}
			else
			{
				final ItemModel model = (ItemModel) data;
				changeLock(model, !getItemLockingService().isLocked(model));
				publishGlobalNotificationOnLockingStateChange(Collections.singletonList(data));
				showNotifications(ctx, model);
			}
			return new ActionResult<>(ActionResult.SUCCESS, data);
		}
		catch (final RuntimeException re)
		{
			LOG.warn("Could not perform action due to unexpected exception", re);
			return new ActionResult<>(ActionResult.ERROR, data);
		}
	}

	protected void bulkChangeLock(final Collection<?> collection)
	{
		final boolean allLocked = collection.stream().allMatch(elem -> getItemLockingService().isLocked((ItemModel) elem));
		for (final Object element : collection)
		{
			final ItemModel model = (ItemModel) element;
			changeLock(model, !allLocked);
		}
	}

	protected void changeLock(final ItemModel data, final boolean lock)
	{
		final ItemLockingService lockingService = getItemLockingService();
		if (lock)
		{
			lockingService.lock(data);
		}
		else
		{
			lockingService.unlock(data);
		}
	}

	protected void showNotifications(final ActionContext<Object> ctx, final ItemModel data)
	{
		showNotifications(ctx, Collections.singletonList(data));
	}

	protected void showNotifications(final ActionContext<Object> ctx, final Collection<?> dataList)
	{
		getNotificationService().notifyUser(ctx, NotificationEventTypes.EVENT_TYPE_OBJECT_UPDATE, NotificationEvent.Level.SUCCESS,
				dataList);
	}

	@Override
	public boolean canPerform(final ActionContext<Object> ctx)
	{
		if (ctx == null)
		{
			return false;
		}
		final Object data = ctx.getData();
		if (data instanceof ItemModel)
		{
			return true;
		}
		if (data instanceof Collection)
		{
			final Collection<?> collection = (Collection<?>) data;
			return !collection.isEmpty() && collection.stream().allMatch(element -> element instanceof ItemModel);
		}
		return false;
	}

	@Override
	public boolean needsConfirmation(final ActionContext<Object> ctx)
	{
		return true;
	}

	@Override
	public String getConfirmationMessage(final ActionContext<Object> ctx)
	{
		Validate.notNull("Context must not be null", ctx);
		final Object data = ctx.getData();
		Validate.notNull("Context data must not be null", data);
		if (data instanceof ItemModel)
		{
			if (getItemLockingService().isLocked((ItemModel) data))
			{
				return ctx.getLabel(UNLOCK_CONFIRMATION_MESSAGE);
			}
			else
			{
				return ctx.getLabel(LOCK_CONFIRMATION_MESSAGE);
			}
		}
		else
		{
			final Collection<ItemModel> items = (Collection<ItemModel>) data;
			if (items.stream().allMatch(item -> getItemLockingService().isLocked(item)))
			{
				return ctx.getLabel(UNLOCK_CONFIRMATION_MESSAGE_MULTI, new Object[]
				{ items.size() });
			}
			else
			{
				return ctx.getLabel(LOCK_CONFIRMATION_MESSAGE_MULTI, new Object[]
				{ items.size() });
			}
		}
	}

	protected void publishGlobalNotificationOnLockingStateChange(final Collection<?> data)
	{
		getEventPublisher().publish(GLOBAL_EVENT_ITEM_LOCKED_STATE_CHANGED, data, new DefaultContext());
	}

	protected ItemLockingService getItemLockingService()
	{
		return itemLockingService;
	}

	public void setItemLockingService(final ItemLockingService itemLockingService)
	{
		this.itemLockingService = itemLockingService;
	}

	protected CockpitGlobalEventPublisher getEventPublisher()
	{
		return eventPublisher;
	}

	public void setEventPublisher(final CockpitGlobalEventPublisher eventPublisher)
	{
		this.eventPublisher = eventPublisher;
	}

	protected NotificationService getNotificationService()
	{
		return notificationService;
	}

	public void setNotificationService(final NotificationService notificationService)
	{
		this.notificationService = notificationService;
	}
}
