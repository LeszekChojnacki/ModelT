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
package de.hybris.platform.marketplacebackoffice.actions;

import de.hybris.platform.marketplaceservices.vendor.VendorService;
import de.hybris.platform.ordersplitting.model.VendorModel;

import java.util.EnumSet;

import javax.annotation.Resource;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.ActionResult.StatusFlag;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.core.impl.NotificationStack;
import com.hybris.cockpitng.util.notifications.NotificationService;
import com.hybris.cockpitng.util.notifications.event.NotificationEvent;


/**
 * An action for deactivating a vendor.
 */
public class DeactivateVendorAction implements CockpitAction<VendorModel, Object>
{

	private static final String VENDOR_DEACTIVATE_CONFIRM_MSG = "vendor.deactivate.confirm";
	private static final String DEACTIVATE_VENDOR_EVENT_TYPE = "DeactivateVendor";

	@Resource
	private VendorService vendorService;

	@Resource
	private NotificationStack notificationStack;

	@Resource
	private NotificationService notificationService;

	@Override
	public ActionResult<Object> perform(final ActionContext<VendorModel> ctx)
	{

		final VendorModel vendor = ctx.getData();
		final ActionResult<Object> actionResult = new ActionResult<>(ActionResult.SUCCESS);
		final EnumSet<StatusFlag> statusFlags = EnumSet.of(StatusFlag.OBJECT_MODIFIED);

		vendorService.deactivateVendor(vendor);
		notificationService.notifyUser(notificationService.getWidgetNotificationSource(ctx), DEACTIVATE_VENDOR_EVENT_TYPE,
				NotificationEvent.Level.SUCCESS, vendor);
		actionResult.setStatusFlags(statusFlags);

		return actionResult;
	}


	@Override
	public boolean canPerform(final ActionContext<VendorModel> ctx)
	{
		final VendorModel vendor = ctx.getData();

		return vendor != null && vendor.isActive();
	}

	@Override
	public boolean needsConfirmation(final ActionContext<VendorModel> ctx)
	{
		return true;
	}

	@Override
	public String getConfirmationMessage(final ActionContext<VendorModel> ctx)
	{
		return ctx.getLabel(VENDOR_DEACTIVATE_CONFIRM_MSG);
	}

}
