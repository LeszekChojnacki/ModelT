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
*/
package de.hybris.platform.omsbackoffice.actions.returns;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import de.hybris.platform.basecommerce.enums.ConsignmentStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.customersupportbackoffice.constants.CustomersupportbackofficeConstants;
import de.hybris.platform.returns.ReturnService;
import de.hybris.platform.servicelayer.model.ModelService;

import javax.annotation.Resource;

import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.engine.impl.AbstractComponentWidgetAdapterAware;
import org.apache.commons.collections4.CollectionUtils;




/**
 * This action opens a popup to create the {@link de.hybris.platform.returns.model.ReturnRequestModel}.
 */
public class CreateReturnRequestAction extends AbstractComponentWidgetAdapterAware
		implements CockpitAction<OrderModel, OrderModel>
{
	protected static final String SOCKET_OUT_CONTEXT = "createReturnRequestContext";
	protected static final String CREATE_RETURN_REQUEST_MODIFIED_FAILURE = "customersupport.create.returnrequest.modified.failure";

	@Resource
	private ReturnService returnService;
	@Resource
	private ModelService modelService;
	@Resource
	private NotificationService notificationService;

	@Override
	public boolean canPerform(final ActionContext<OrderModel> actionContext)
	{
		final Object data = actionContext.getData();

		OrderModel order = null;
		if (data instanceof OrderModel)
		{
			order = (OrderModel) data;
		}

		// An order is not returnable when no consignment has been shipped/picked up or returnService cannot find any returnable item.
		return containsConsignments(order)
				&& !order.getConsignments().stream().noneMatch(
				consignment -> ConsignmentStatus.SHIPPED.equals(consignment.getStatus()) || ConsignmentStatus.PICKUP_COMPLETE
						.equals(consignment.getStatus())) && !getReturnService().getAllReturnableEntries(order).isEmpty();
	}

	private boolean containsConsignments(final OrderModel order){
		return order != null && !CollectionUtils.isEmpty(order.getEntries()) && !CollectionUtils.isEmpty(order.getConsignments());
	}

	@Override
	public String getConfirmationMessage(final ActionContext<OrderModel> actionContext)
	{
		return null;
	}

	@Override
	public boolean needsConfirmation(final ActionContext<OrderModel> actionContext)
	{
		return false;
	}

	@Override
	public ActionResult<OrderModel> perform(final ActionContext<OrderModel> actionContext)
	{
		ActionResult<OrderModel> actionResult;
		final OrderModel order = actionContext.getData();

		getModelService().refresh(order);

		if (canPerform(actionContext))
		{
			sendOutput(SOCKET_OUT_CONTEXT, actionContext.getData());
			actionResult = new ActionResult<>(ActionResult.SUCCESS);
		}
		else
		{
			notificationService.notifyUser(notificationService.getWidgetNotificationSource(actionContext), CustomersupportbackofficeConstants.NOTIFICATION_TYPE, NotificationEvent.Level.FAILURE,
					actionContext.getLabel(CREATE_RETURN_REQUEST_MODIFIED_FAILURE));
			actionResult = new ActionResult<>(ActionResult.ERROR);
		}

		actionResult.getStatusFlags().add(ActionResult.StatusFlag.OBJECT_PERSISTED);
		return actionResult;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	protected ReturnService getReturnService()
	{
		return returnService;
	}

}
