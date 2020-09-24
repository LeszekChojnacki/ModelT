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
package de.hybris.platform.commerceservices.backoffice.orgunits.actions;

import de.hybris.platform.commerceservices.model.OrgUnitModel;
import de.hybris.platform.commerceservices.organization.services.OrgUnitService;
import de.hybris.platform.commerceservices.organization.strategies.OrgUnitAuthorizationStrategy;
import de.hybris.platform.servicelayer.user.UserService;

import javax.annotation.Resource;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEventTypes;
import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;


public class DisableOrgUnitAction implements CockpitAction<OrgUnitModel, Object>
{
	private static final String CONFIRMATION_MESSAGE = "organization.unit.confirm.disable.msg";
	private static final String SUCCESS_MESSAGE = "organization.unit.confirm.disable.success.msg";

	@Resource(name = "userService")
	private UserService userService;

	@Resource(name = "orgUnitService")
	private OrgUnitService orgUnitService;

	@Resource(name = "orgUnitAuthorizationStrategy")
	private OrgUnitAuthorizationStrategy orgUnitAuthorizationStrategy;

	@Resource(name = "notificationService")
	private NotificationService notificationService;

	@Override
	public boolean canPerform(final ActionContext<OrgUnitModel> ctx)
	{
		final Object data = ctx.getData();

		if ((data != null) && (data instanceof OrgUnitModel))
		{
			return ((OrgUnitModel) data).getActive().booleanValue()
					&& orgUnitAuthorizationStrategy.canEditUnit(userService.getCurrentUser());
		}
		return false;
	}

	@Override
	public String getConfirmationMessage(final ActionContext<OrgUnitModel> ctx)
	{
		return ctx.getLabel(CONFIRMATION_MESSAGE);
	}

	@Override
	public boolean needsConfirmation(final ActionContext<OrgUnitModel> ctx)
	{
		return true;
	}

	@Override
	public ActionResult<Object> perform(final ActionContext<OrgUnitModel> ctx)
	{
		final Object data = ctx.getData();

		if ((data != null) && (data instanceof OrgUnitModel))
		{
			final OrgUnitModel orgUnitModel = (OrgUnitModel) data;
			orgUnitService.deactivateUnit(orgUnitModel);

			getNotificationService().notifyUser(getNotificationService().getWidgetNotificationSource(ctx),
					NotificationEventTypes.EVENT_TYPE_OBJECT_UPDATE, NotificationEvent.Level.SUCCESS, ctx.getLabel(SUCCESS_MESSAGE));
			return new ActionResult(ActionResult.SUCCESS, orgUnitModel);
		}
		else
		{
			return new ActionResult(ActionResult.ERROR);
		}
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
