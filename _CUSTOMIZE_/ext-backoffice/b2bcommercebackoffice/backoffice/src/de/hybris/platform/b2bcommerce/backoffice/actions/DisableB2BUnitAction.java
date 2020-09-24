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
package de.hybris.platform.b2bcommerce.backoffice.actions;

import de.hybris.platform.b2b.constants.B2BConstants;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.b2b.services.B2BUnitService;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.user.UserService;

import javax.annotation.Resource;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;


public class DisableB2BUnitAction implements CockpitAction<B2BUnitModel, Object>
{

	private static final String CONFIRMATION_MESSAGE = "hmc.action.b2bunitdisable.confirm";
	private static final String DISABLE_B2B_UNIT_EVENT = "b2bunitdisable";

	@Resource(name = "userService")
	private UserService userService;

	@Resource(name = "b2bUnitService")
	private B2BUnitService<B2BUnitModel, B2BCustomerModel> b2bUnitService;

	@Resource(name = "notificationService")
	private NotificationService notificationService;

	@Override
	public boolean canPerform(final ActionContext<B2BUnitModel> ctx)
	{
		final Object data = ctx.getData();

		if ((data != null) && (data instanceof B2BUnitModel))
		{
			final B2BUnitModel b2bUnitModel = (B2BUnitModel) data;
			final UserModel currentUser = userService.getCurrentUser();
			final boolean isActive = b2bUnitModel.getActive().booleanValue();
			final boolean isUserMemberOfAdminGroup = userService.isMemberOfGroup(currentUser, userService.getAdminUserGroup());
			final boolean isUserMemberOfB2BAdminGroup = userService.isMemberOfGroup(currentUser,
					userService.getUserGroupForUID(B2BConstants.B2BADMINGROUP));

			return (isUserMemberOfAdminGroup || isUserMemberOfB2BAdminGroup) && isActive;
		}
		else
		{
			return false;
		}
	}

	@Override
	public String getConfirmationMessage(final ActionContext<B2BUnitModel> ctx)
	{
		return ctx.getLabel(CONFIRMATION_MESSAGE);
	}

	@Override
	public boolean needsConfirmation(final ActionContext<B2BUnitModel> ctx)
	{
		return true;
	}

	@Override
	public ActionResult<Object> perform(final ActionContext<B2BUnitModel> ctx)
	{
		final Object data = ctx.getData();

		if ((data != null) && (data instanceof B2BUnitModel))
		{
			final B2BUnitModel b2bUnitModel = (B2BUnitModel) data;
			b2bUnitService.disableBranch(b2bUnitModel);

			notificationService.notifyUser(notificationService.getWidgetNotificationSource(ctx), DISABLE_B2B_UNIT_EVENT,
					NotificationEvent.Level.SUCCESS, b2bUnitModel);
			return new ActionResult(ActionResult.SUCCESS, b2bUnitModel);
		}
		else
		{
			return new ActionResult(ActionResult.ERROR);

		}
	}

}
