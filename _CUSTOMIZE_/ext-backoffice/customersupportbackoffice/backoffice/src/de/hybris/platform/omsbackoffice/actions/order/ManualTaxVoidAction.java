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

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.customersupportbackoffice.constants.CustomersupportbackofficeConstants;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.model.ModelService;

import javax.annotation.Resource;

import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.engine.impl.AbstractComponentWidgetAdapterAware;
import org.apache.log4j.Logger;


/**
 * This action specifies that the tax void operation has been handled manually. It proceeds with the regular cancellation
 * flow in the business process.
 */
public class ManualTaxVoidAction extends AbstractComponentWidgetAdapterAware implements CockpitAction<OrderModel, OrderModel>
{
	protected static final String MANUAL_TAX_VOID_SUCCESS = "action.manualtaxvoid.success";
	protected static final String MANUAL_VOID_TAX_EVENT = "ManualVoidTaxEvent";

	private static final Logger LOG = Logger.getLogger(ManualTaxVoidAction.class);

	@Resource
	private BusinessProcessService businessProcessService;
	@Resource
	private ModelService modelService;
	@Resource
	private NotificationService notificationService;

	@Override
	public ActionResult<OrderModel> perform(final ActionContext<OrderModel> actionContext)
	{
		ActionResult<OrderModel> actionResult = null;

		if (actionContext != null && actionContext.getData() != null)
		{
			final OrderModel order = actionContext.getData();

			executeManualTaxVoidOperation(order);

			actionResult = new ActionResult<>(ActionResult.SUCCESS);
			actionResult.getStatusFlags().add(ActionResult.StatusFlag.OBJECT_PERSISTED);
			notificationService.notifyUser(notificationService.getWidgetNotificationSource(actionContext), CustomersupportbackofficeConstants.NOTIFICATION_TYPE, NotificationEvent.Level.SUCCESS,
					actionContext.getLabel(MANUAL_TAX_VOID_SUCCESS));
		}

		return actionResult;
	}

	@Override
	public boolean canPerform(final ActionContext<OrderModel> ctx)
	{
		final OrderModel order = ctx.getData();
		return order != null && OrderStatus.TAX_NOT_VOIDED.equals(order.getStatus());
	}

	@Override
	public boolean needsConfirmation(final ActionContext<OrderModel> ctx)
	{
		return false;
	}

	@Override
	public String getConfirmationMessage(final ActionContext<OrderModel> ctx)
	{
		return null;
	}

	/**
	 * Executes the manual tax void release operation by calling the {@link BusinessProcessService}.
	 *
	 * @param order
	 * 		the {@link OrderModel} to be released
	 */
	protected void executeManualTaxVoidOperation(final OrderModel order)
	{
		order.getOrderProcess().stream()
				.filter(process -> process.getCode().startsWith(order.getStore().getSubmitOrderProcessCode())).forEach(
				filteredProcess -> getBusinessProcessService().triggerEvent(filteredProcess.getCode() + "_" + MANUAL_VOID_TAX_EVENT));
		LOG.info(String.format("Tax Void Manual Release completed. %s triggered.", MANUAL_VOID_TAX_EVENT));
		order.setStatus(OrderStatus.CANCELLED);
		getModelService().save(order);
	}

	protected BusinessProcessService getBusinessProcessService()
	{
		return businessProcessService;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}
}
