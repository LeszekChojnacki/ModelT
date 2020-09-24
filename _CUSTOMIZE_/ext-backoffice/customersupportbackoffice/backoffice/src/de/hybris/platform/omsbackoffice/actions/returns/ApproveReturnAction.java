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
import de.hybris.platform.returns.ReturnActionResponse;
import de.hybris.platform.returns.ReturnCallbackService;
import de.hybris.platform.returns.model.ReturnRequestModel;
import de.hybris.platform.servicelayer.model.ModelService;

import javax.annotation.Resource;

import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.engine.impl.AbstractComponentWidgetAdapterAware;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.zkoss.lang.Strings;


/**
 * Action that will open the Return Approval pop up
 */
public class ApproveReturnAction extends AbstractComponentWidgetAdapterAware
		implements CockpitAction<ReturnRequestModel, ReturnRequestModel>
{
	protected static final String APPROVAL_SUCCESS = "customersupportbackoffice.returnrequest.approval.success";
	protected static final String APPROVAL_FAILURE = "customersupportbackoffice.returnrequest.approval.failure";
	protected static final String MODIFIED_RETURN_REQUEST = "customersupportbackoffice.returnrequest.approval.modified";
	protected static final String APPROVAL_CANCELLED_FAILURE = "customersupportbackoffice.returnrequest.approval.cancelled.failure";

	private static final Logger LOG = Logger.getLogger(ApproveReturnAction.class);

	@Resource
	private ReturnCallbackService returnCallbackService;
	@Resource
	private ModelService modelService;
	@Resource
	private NotificationService notificationService;

	@Override
	public boolean canPerform(final ActionContext<ReturnRequestModel> actionContext)
	{
		boolean result = false;
		final ReturnRequestModel returnRequest = actionContext.getData();

		if (returnRequest != null)
		{
			result = !CollectionUtils.isEmpty(returnRequest.getReturnEntries()) && returnRequest.getStatus()
					.equals(ReturnStatus.APPROVAL_PENDING);
		}
		return result;
	}

	@Override
	public String getConfirmationMessage(final ActionContext<ReturnRequestModel> actionContext)
	{
		return actionContext.getLabel(MODIFIED_RETURN_REQUEST);
	}

	@Override
	public boolean needsConfirmation(final ActionContext<ReturnRequestModel> actionContext)
	{
		final ReturnRequestModel returnRequest = actionContext.getData();
		final ReturnRequestModel upToDateReturnRequest = getModelService().get(returnRequest.getPk());
		boolean result = false;

		if (returnRequest.getStatus().equals(upToDateReturnRequest.getStatus()))
		{
			result = !getModelService().isUpToDate(actionContext.getData()) || actionContext.getData().getReturnEntries().stream()
					.anyMatch(entry -> !getModelService().isUpToDate(entry));
		}
		return result;
	}

	@Override
	public ActionResult<ReturnRequestModel> perform(final ActionContext<ReturnRequestModel> actionContext)
	{
		ActionResult<ReturnRequestModel> actionResult;
		final ReturnRequestModel returnRequest = actionContext.getData();
		final ReturnRequestModel upToDateReturnRequest = getModelService().get(returnRequest.getPk());

		if (returnRequest.getStatus().equals(upToDateReturnRequest.getStatus()))
		{
			returnRequest.getReturnEntries().forEach(entry -> getModelService().save(entry));
			getModelService().save(actionContext.getData());

			final ReturnActionResponse returnActionResponse = new ReturnActionResponse(returnRequest);
			try
			{
				getReturnCallbackService().onReturnApprovalResponse(returnActionResponse);
				notificationService.notifyUser(Strings.EMPTY, CustomersupportbackofficeConstants.NOTIFICATION_TYPE,
						NotificationEvent.Level.SUCCESS, actionContext.getLabel(APPROVAL_SUCCESS));
				actionResult = new ActionResult<>(ActionResult.SUCCESS);
			}
			catch (final OrderReturnException e) //NOSONAR
			{
				LOG.error(e.getMessage());
				notificationService.notifyUser(Strings.EMPTY, CustomersupportbackofficeConstants.NOTIFICATION_TYPE,
						NotificationEvent.Level.FAILURE, actionContext.getLabel(APPROVAL_FAILURE));
				actionResult = new ActionResult<>(ActionResult.ERROR);
			}
		}
		else
		{
			notificationService
					.notifyUser(Strings.EMPTY, CustomersupportbackofficeConstants.NOTIFICATION_TYPE, NotificationEvent.Level.FAILURE,
							actionContext.getLabel(APPROVAL_CANCELLED_FAILURE));
			actionResult = new ActionResult<>(ActionResult.ERROR);
		}


		actionResult.getStatusFlags().add(ActionResult.StatusFlag.OBJECT_PERSISTED);
		return actionResult;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	protected ReturnCallbackService getReturnCallbackService()
	{
		return returnCallbackService;
	}

}
