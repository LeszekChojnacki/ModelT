/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.omsbackoffice.actions.returns;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import de.hybris.platform.basecommerce.enums.ReturnStatus;
import de.hybris.platform.customersupportbackoffice.constants.CustomersupportbackofficeConstants;
import de.hybris.platform.returns.OrderReturnException;
import de.hybris.platform.returns.ReturnService;
import de.hybris.platform.returns.model.ReturnRequestModel;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.engine.impl.AbstractComponentWidgetAdapterAware;
import org.zkoss.lang.Strings;


/**
 * This action specify that the tax reverse has been handle manually and triggers the update inventory step in the
 * process engine.
 */
public class ManualTaxReverseAction extends AbstractComponentWidgetAdapterAware
		implements CockpitAction<ReturnRequestModel, ReturnRequestModel>
{

	private static final Logger LOG = Logger.getLogger(ManualTaxReverseAction.class);

	protected static final String SUCCESS_MESSAGE_LABEL = "action.manualtaxreverse.success";
	protected static final String FAILURE_MESSAGE_LABEL = "action.manualtaxreverse.failure";

	@Resource
	private ReturnService returnService;
	@Resource
	private NotificationService notificationService;

	@Override
	public boolean canPerform(final ActionContext<ReturnRequestModel> actionContext)
	{
		final Object data = actionContext.getData();
		return data instanceof ReturnRequestModel
				&& ((ReturnRequestModel) data).getStatus().equals(ReturnStatus.TAX_REVERSAL_FAILED);
	}

	@Override
	public String getConfirmationMessage(final ActionContext<ReturnRequestModel> actionContext)
	{
		return null;
	}

	@Override
	public boolean needsConfirmation(final ActionContext<ReturnRequestModel> actionContext)
	{
		return false;
	}

	@Override
	public ActionResult<ReturnRequestModel> perform(final ActionContext<ReturnRequestModel> actionContext)
	{
		final ReturnRequestModel returnRequest = actionContext.getData();
		ActionResult<ReturnRequestModel> actionResult;
		try
		{
			getReturnService().requestManualTaxReversalForReturnRequest(returnRequest);
			getNotificationService().notifyUser(Strings.EMPTY, CustomersupportbackofficeConstants.NOTIFICATION_TYPE,
					NotificationEvent.Level.SUCCESS, actionContext.getLabel(SUCCESS_MESSAGE_LABEL));
			actionResult = new ActionResult<>(ActionResult.SUCCESS);
		}
		catch (final OrderReturnException e)
		{
			LOG.error(e.getMessage(), e);
			getNotificationService().notifyUser(Strings.EMPTY, CustomersupportbackofficeConstants.NOTIFICATION_TYPE,
					NotificationEvent.Level.FAILURE, actionContext.getLabel(FAILURE_MESSAGE_LABEL));
			actionResult = new ActionResult<>(ActionResult.ERROR);
		}

		actionResult.getStatusFlags().add(ActionResult.StatusFlag.OBJECT_PERSISTED);
		return actionResult;
	}

	protected ReturnService getReturnService()
	{
		return returnService;
	}

	protected NotificationService getNotificationService()
	{
		return notificationService;
	}
}
