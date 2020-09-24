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
import de.hybris.platform.servicelayer.model.ModelService;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.engine.impl.AbstractComponentWidgetAdapterAware;
import org.zkoss.lang.Strings;


/**
 * This action triggers the tax reverse step in the process engine.
 */
public class ManualRefundAction extends AbstractComponentWidgetAdapterAware
		implements CockpitAction<ReturnRequestModel, ReturnRequestModel>
{

	private static final Logger LOG = Logger.getLogger(ManualRefundAction.class);

	protected static final String SUCCESS_MESSAGE_LABEL = "action.manualrefund.success";
	protected static final String FAILURE_MESSAGE_LABEL = "action.manualrefund.failure";
	protected static final String UNSAVED_OBJECT_WARNING_LABEL = "action.manualrefund.unsaved.object.warning";

	@Resource
	private ModelService modelService;
	@Resource
	private ReturnService returnService;
	@Resource
	private NotificationService notificationService;

	@Override
	public boolean canPerform(final ActionContext<ReturnRequestModel> actionContext)
	{
		final Object data = actionContext.getData();
		boolean decision = false;
		if (data instanceof ReturnRequestModel)
		{
			final ReturnStatus returnStatus = ((ReturnRequestModel) data).getStatus();
			decision = returnStatus.equals(ReturnStatus.RECEIVED) || returnStatus.equals(ReturnStatus.PAYMENT_REVERSAL_FAILED);
		}
		return decision;
	}

	@Override
	public String getConfirmationMessage(final ActionContext<ReturnRequestModel> actionContext)
	{
		return actionContext.getLabel(UNSAVED_OBJECT_WARNING_LABEL);
	}

	@Override
	public boolean needsConfirmation(final ActionContext<ReturnRequestModel> actionContext)
	{
		final Object data = actionContext.getData();
		return getModelService().isModified(data);
	}

	@Override
	public ActionResult<ReturnRequestModel> perform(final ActionContext<ReturnRequestModel> actionContext)
	{
		final ReturnRequestModel returnRequest = actionContext.getData();
		ActionResult<ReturnRequestModel> actionResult;
		try
		{
			getReturnService().requestManualPaymentReversalForReturnRequest(returnRequest);
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

	protected ModelService getModelService()
	{
		return modelService;
	}

	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
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
