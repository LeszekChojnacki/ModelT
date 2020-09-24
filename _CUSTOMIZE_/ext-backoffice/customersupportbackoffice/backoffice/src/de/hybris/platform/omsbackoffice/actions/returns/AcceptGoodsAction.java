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

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.engine.impl.AbstractComponentWidgetAdapterAware;
import org.zkoss.lang.Strings;


/**
 * Action to open a accept goods popup in order to manually accept all or partial part of refund entries
 */
public class AcceptGoodsAction extends AbstractComponentWidgetAdapterAware implements
		CockpitAction<ReturnRequestModel, ReturnRequestModel>
{

	private static final Logger LOG = Logger.getLogger(AcceptGoodsAction.class);

	protected static final String ACCEPT_GOODS_SUCCESS = "customersupportbackoffice.returnrequest.accept.goods.success";
	protected static final String ACCEPT_GOODS_FAILURE = "customersupportbackoffice.returnrequest.accept.goods.failure";

	@Resource
	private ReturnCallbackService returnCallbackService;
	@Resource
	private NotificationService notificationService;

	@Override
	public boolean canPerform(final ActionContext<ReturnRequestModel> actionContext)
	{
		final Object data = actionContext.getData();
		ReturnRequestModel returnRequest = null;
		boolean decision = false;

		if (data instanceof ReturnRequestModel)
		{
			returnRequest = (ReturnRequestModel) data;
			if ((returnRequest.getReturnEntries() != null || !CollectionUtils.isEmpty(returnRequest.getReturnEntries()))
					&& (returnRequest.getStatus().equals(ReturnStatus.WAIT)))
			{
				decision = true;
			}
		}

		return decision;
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
		ActionResult<ReturnRequestModel> actionResult;
		final ReturnActionResponse returnActionResponse = new ReturnActionResponse(actionContext.getData());
		try
		{
			getReturnCallbackService().onReturnReceptionResponse(returnActionResponse);
			getNotificationService().notifyUser(Strings.EMPTY, CustomersupportbackofficeConstants.NOTIFICATION_TYPE,
					NotificationEvent.Level.SUCCESS, actionContext.getLabel(ACCEPT_GOODS_SUCCESS));
			actionResult = new ActionResult<>(ActionResult.SUCCESS);
		}
		catch (final OrderReturnException e)
		{
			LOG.error(e.getMessage(), e);
			getNotificationService().notifyUser(Strings.EMPTY, CustomersupportbackofficeConstants.NOTIFICATION_TYPE,
					NotificationEvent.Level.FAILURE, actionContext.getLabel(ACCEPT_GOODS_FAILURE));
			actionResult = new ActionResult<>(ActionResult.ERROR);
		}

		actionResult.getStatusFlags().add(ActionResult.StatusFlag.OBJECT_PERSISTED);
		return actionResult;
	}

	protected ReturnCallbackService getReturnCallbackService()
	{
		return returnCallbackService;
	}

	protected NotificationService getNotificationService()
	{
		return notificationService;
	}
}
