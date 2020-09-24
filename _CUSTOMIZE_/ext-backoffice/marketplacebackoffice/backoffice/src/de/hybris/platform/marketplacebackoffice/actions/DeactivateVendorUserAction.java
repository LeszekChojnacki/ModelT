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

import de.hybris.platform.marketplaceservices.model.VendorUserModel;
import de.hybris.platform.marketplaceservices.vendor.VendorUserService;

import java.util.EnumSet;

import javax.annotation.Resource;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.ActionResult.StatusFlag;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.util.notifications.NotificationService;
import com.hybris.cockpitng.util.notifications.event.NotificationEvent;


/**
 * An action for deactivating a vendor.
 */
public class DeactivateVendorUserAction implements CockpitAction<VendorUserModel, Object>
{

	private static final String VENDOR_USER_DEACTIVATE_CONFIRM_MSG = "vendor.user.deactivate.confirm";
	private static final String DEACTIVATE_VENDORUSER_EVENT_TYPE = "DeactivateVendorUser";

	@Resource
	private VendorUserService vendorUserService;

	@Resource
	private NotificationService notificationService;

	@Override
	public ActionResult<Object> perform(final ActionContext<VendorUserModel> ctx)
	{

		final VendorUserModel vendorUser = ctx.getData();
		final ActionResult<Object> actionResult = new ActionResult<>(ActionResult.SUCCESS);
		final EnumSet<StatusFlag> statusFlags = EnumSet.of(StatusFlag.OBJECT_MODIFIED);

		vendorUserService.deactivateUser(vendorUser);
		notificationService.notifyUser(notificationService.getWidgetNotificationSource(ctx), DEACTIVATE_VENDORUSER_EVENT_TYPE,
				NotificationEvent.Level.SUCCESS, vendorUser);
		actionResult.setStatusFlags(statusFlags);

		return actionResult;
	}


	@Override
	public boolean canPerform(final ActionContext<VendorUserModel> ctx)
	{
		final VendorUserModel vendorUser = ctx.getData();

		return vendorUser != null && !vendorUser.isLoginDisabled();
	}

	@Override
	public boolean needsConfirmation(final ActionContext<VendorUserModel> ctx)
	{
		return true;
	}

	@Override
	public String getConfirmationMessage(final ActionContext<VendorUserModel> ctx)
	{
		return ctx.getLabel(VENDOR_USER_DEACTIVATE_CONFIRM_MSG);
	}

}
