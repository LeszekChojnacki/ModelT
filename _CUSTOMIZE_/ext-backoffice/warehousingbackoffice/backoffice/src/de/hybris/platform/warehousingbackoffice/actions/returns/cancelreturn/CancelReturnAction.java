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
package de.hybris.platform.warehousingbackoffice.actions.returns.cancelreturn;

import de.hybris.platform.basecommerce.enums.ReturnStatus;
import de.hybris.platform.returns.model.ReturnRequestModel;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.engine.impl.AbstractComponentWidgetAdapterAware;


/**
 * Perform the Cancellation of a return. <br>
 * It allows to cancel the return
 */
public class CancelReturnAction extends AbstractComponentWidgetAdapterAware
		implements CockpitAction<ReturnRequestModel, ReturnRequestModel>
{
	protected static final String WAREHOUSING_SOCKET_OUT_CONTEXT = "warehousingCancelReturnContext";

	@Override
	public boolean canPerform(final ActionContext<ReturnRequestModel> actionContext)
	{
		final Object data = actionContext.getData();
		boolean decision = false;
		if (data instanceof ReturnRequestModel)
		{
			final ReturnStatus returnStatus = ((ReturnRequestModel) data).getStatus();
			decision = returnStatus.equals(ReturnStatus.WAIT) || returnStatus.equals(ReturnStatus.PAYMENT_REVERSAL_FAILED);
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
		final ReturnRequestModel returnToUpdate = actionContext.getData();
		sendOutput(WAREHOUSING_SOCKET_OUT_CONTEXT, returnToUpdate);

		final ActionResult<ReturnRequestModel> actionResult = new ActionResult<>(ActionResult.SUCCESS);
		actionResult.getStatusFlags().add(ActionResult.StatusFlag.OBJECT_PERSISTED);
		return actionResult;
	}
}
