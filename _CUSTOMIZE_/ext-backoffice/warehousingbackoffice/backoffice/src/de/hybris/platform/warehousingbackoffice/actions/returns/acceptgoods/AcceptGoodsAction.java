/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 *
 */
package de.hybris.platform.warehousingbackoffice.actions.returns.acceptgoods;


import de.hybris.platform.basecommerce.enums.ReturnStatus;
import de.hybris.platform.returns.OrderReturnException;
import de.hybris.platform.returns.ReturnActionResponse;
import de.hybris.platform.returns.ReturnCallbackService;
import de.hybris.platform.returns.model.ReturnRequestModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.warehousingbackoffice.constants.WarehousingBackofficeConstants;

import javax.annotation.Resource;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.engine.impl.AbstractComponentWidgetAdapterAware;
import org.apache.commons.collections.CollectionUtils;
import org.zkoss.lang.Strings;


/**
 * Action to open a accept goods popup in order to manually accept all or partial part of refund entries
 */
public class AcceptGoodsAction extends AbstractComponentWidgetAdapterAware
		implements CockpitAction<ReturnRequestModel, ReturnRequestModel>
{
	protected static final String ACCEPT_GOODS_SUCCESS = "warehousingbackoffice.returnrequest.accept.goods.success";
	protected static final String ACCEPT_GOODS_FAILURE = "warehousingbackoffice.returnrequest.accept.goods.failure";
	protected static final String ACCEPT_GOODS_MODIFIED_FAILURE = "warehousingbackoffice.returnrequest.accept.goods.modified.failure";

	@Resource
	private ModelService modelService;
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
		final ReturnRequestModel returnRequest = actionContext.getData();

		getModelService().refresh(returnRequest);

		if (canPerform(actionContext))
		{
			final ReturnActionResponse returnActionResponse = new ReturnActionResponse(returnRequest);
			try
			{
				getReturnCallbackService().onReturnReceptionResponse(returnActionResponse);
				getNotificationService()
						.notifyUser(Strings.EMPTY, WarehousingBackofficeConstants.NOTIFICATION_TYPE, NotificationEvent.Level.SUCCESS,
								actionContext.getLabel(ACCEPT_GOODS_SUCCESS));
				actionResult = new ActionResult<>(ActionResult.SUCCESS);
			}
			catch (final OrderReturnException e)  // NOSONAR
			{
				getNotificationService()
						.notifyUser(Strings.EMPTY, WarehousingBackofficeConstants.NOTIFICATION_TYPE, NotificationEvent.Level.FAILURE,
								actionContext.getLabel(ACCEPT_GOODS_FAILURE));
				actionResult = new ActionResult<>(ActionResult.ERROR);
			}
		}
		else
		{
			getNotificationService()
					.notifyUser(Strings.EMPTY, WarehousingBackofficeConstants.NOTIFICATION_TYPE, NotificationEvent.Level.FAILURE,
							actionContext.getLabel(ACCEPT_GOODS_MODIFIED_FAILURE));
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

	protected NotificationService getNotificationService()
	{
		return notificationService;
	}
}
