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

import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.b2b.services.B2BReportingService;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;


public class GenerateReportingSetAction implements CockpitAction<B2BUnitModel, Object>
{

	private static final String GENERATE_REPORT_B2B_UNIT_EVENT = "b2bunitreportingset";

	@Resource(name = "b2bReportingService")
	private B2BReportingService b2bReportingService;

	@Resource(name = "notificationService")
	private NotificationService notificationService;

	@Override
	public boolean canPerform(final ActionContext<B2BUnitModel> ctx)
	{
		return true;
	}

	@Override
	public String getConfirmationMessage(final ActionContext<B2BUnitModel> ctx)
	{
		return StringUtils.EMPTY;
	}

	@Override
	public boolean needsConfirmation(final ActionContext<B2BUnitModel> arg0)
	{
		return false;
	}

	@Override
	public ActionResult<Object> perform(final ActionContext<B2BUnitModel> ctx)
	{
		final Object data = ctx.getData();

		if ((data != null) && (data instanceof B2BUnitModel))
		{
			final B2BUnitModel b2bUnitModel = (B2BUnitModel) data;
			b2bReportingService.setReportSetForUnit(b2bUnitModel);

			notificationService.notifyUser(notificationService.getWidgetNotificationSource(ctx), GENERATE_REPORT_B2B_UNIT_EVENT,
					NotificationEvent.Level.SUCCESS, b2bUnitModel);
			return new ActionResult<Object>(ActionResult.SUCCESS, b2bUnitModel);
		}
		else
		{
			return new ActionResult(ActionResult.ERROR);
		}
	}

}
