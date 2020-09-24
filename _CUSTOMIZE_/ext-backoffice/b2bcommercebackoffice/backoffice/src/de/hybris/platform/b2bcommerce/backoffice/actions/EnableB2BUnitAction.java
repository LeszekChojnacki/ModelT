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
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;


public class EnableB2BUnitAction implements CockpitAction<B2BUnitModel, Object>
{
	private static final Logger LOG = Logger.getLogger(EnableB2BUnitAction.class);

	private static final String CONFIRMATION_MESSAGE = "hmc.action.b2bunitenable.confirm";
	private static final String ENABLE_B2B_UNIT_EVENT = "b2bunitenable";

	@Resource(name = "modelService")
	private ModelService modelService;

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
			final boolean isUserMemberOfAdminGroup = userService.isMemberOfGroup(currentUser, userService.getAdminUserGroup());
			final boolean isUserMemberOfB2BAdminGroup = userService.isMemberOfGroup(currentUser,
					userService.getUserGroupForUID(B2BConstants.B2BADMINGROUP));
			final boolean isActive = b2bUnitModel.getActive().booleanValue();

			return (isUserMemberOfAdminGroup || isUserMemberOfB2BAdminGroup) && !isActive;
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
			if (!canActivate(b2bUnitModel))
			{
				notificationService.notifyUser(notificationService.getWidgetNotificationSource(ctx), ENABLE_B2B_UNIT_EVENT,
						NotificationEvent.Level.FAILURE, b2bUnitModel);
				return new ActionResult(ActionResult.ERROR, b2bUnitModel);
			}

			b2bUnitModel.setActive(Boolean.TRUE);
			modelService.save(b2bUnitModel);

			notificationService.notifyUser(notificationService.getWidgetNotificationSource(ctx), ENABLE_B2B_UNIT_EVENT,
					NotificationEvent.Level.SUCCESS, b2bUnitModel);
			return new ActionResult<Object>(ActionResult.SUCCESS, b2bUnitModel);
		}
		else
		{
			return new ActionResult(ActionResult.ERROR);

		}
	}

	/**
	 * Checks if a parent b2bunit is active
	 *
	 * @param b2bUnitModel
	 * @return true if parent unit is active.
	 */
	protected boolean canActivate(final B2BUnitModel b2bUnitModel)
	{
		boolean canActivate = false;
		try
		{
			final B2BUnitModel parent = b2bUnitService.getParent(b2bUnitModel);
			if (parent != null)
			{
				return parent.getActive().booleanValue();
			}
			// root unit you can activate no problem.
			canActivate = true;
		}
		catch (final Exception e)
		{
			LOG.error(e.getMessage(), e);
		}
		return canActivate;
	}

}
