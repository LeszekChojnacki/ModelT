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
package de.hybris.platform.omsbackoffice.actions.order;


import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.engine.impl.AbstractComponentWidgetAdapterAware;

import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.model.ModelService;

import javax.annotation.Resource;

import java.util.List;


/**
 * Action to release an order and trigger the sourcing again from the UI.
 */
public class TriggerSourcingAction extends AbstractComponentWidgetAdapterAware implements CockpitAction<OrderModel, OrderModel>
{

	protected static final String SOCKET_OUT_CONTEXT = "triggerSourcingContext";
	protected static final String SOURCING_AGAIN_CONFIRMATION_MESSAGE = "re.source.confirmation.message";

	@Resource
	private ModelService modelService;
	@Resource
	private List<OrderStatus> validOrderStatusForResourcing;

	@Override
	public boolean canPerform(final ActionContext<OrderModel> actionContext)
	{
		final Object data = actionContext.getData();

		OrderModel order = null;
		boolean decision = false;

		if (data instanceof OrderModel)
		{
			order = (OrderModel) data;
			if (order.getStatus() != null && (getValidOrderStatusForResourcing().contains(order.getStatus())))
			{
				decision = true;
			}
		}

		return decision;
	}

	@Override
	public String getConfirmationMessage(final ActionContext<OrderModel> actionContext)
	{
		return actionContext.getLabel(SOURCING_AGAIN_CONFIRMATION_MESSAGE);
	}

	@Override
	public boolean needsConfirmation(final ActionContext<OrderModel> actionContext)
	{
		return true;
	}

	@Override
	public ActionResult<OrderModel> perform(final ActionContext<OrderModel> actionContext)
	{
		final OrderModel order = actionContext.getData();
		sendOutput(SOCKET_OUT_CONTEXT, order);

		final ActionResult<OrderModel> actionResult = new ActionResult<>(ActionResult.SUCCESS);
		actionResult.getStatusFlags().add(ActionResult.StatusFlag.OBJECT_PERSISTED);

		return actionResult;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	protected List<OrderStatus> getValidOrderStatusForResourcing()
	{
		return validOrderStatusForResourcing;
	}
}
