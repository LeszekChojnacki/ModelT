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
package de.hybris.platform.omsbackoffice.actions.order;

import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.customersupportbackoffice.constants.CustomersupportbackofficeConstants;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.model.ModelService;

import javax.annotation.Resource;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.engine.impl.AbstractComponentWidgetAdapterAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This action specifies that the delivery cost tax commit operation has been handled manually. It proceeds with the regular order
 * fulfillment flow in the business process.
 */
public class ManualDeliveryCostCommitAction extends AbstractComponentWidgetAdapterAware
		implements CockpitAction<OrderModel, OrderModel>
{
	protected static final String MANUAL_DELIVERY_COST_COMMIT_SUCCESS = "action.manualdeliverycostcommit.success";
	protected static final String MANUAL_DELIVERY_COST_COMMIT_EVENT = "ManualCommitDeliveryCostEvent";

	private static final Logger LOG = LoggerFactory.getLogger(ManualDeliveryCostCommitAction.class);

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

			executeManualDeliveryCostTaxCommitOperation(order);

			actionResult = new ActionResult<>(ActionResult.SUCCESS);
			actionResult.getStatusFlags().add(ActionResult.StatusFlag.OBJECT_PERSISTED);
			getNotificationService().notifyUser(notificationService.getWidgetNotificationSource(actionContext),
					CustomersupportbackofficeConstants.NOTIFICATION_TYPE, NotificationEvent.Level.SUCCESS,
					actionContext.getLabel(MANUAL_DELIVERY_COST_COMMIT_SUCCESS));
		}

		return actionResult;
	}

	@Override
	public boolean canPerform(final ActionContext<OrderModel> ctx)
	{
		final OrderModel order = ctx.getData();
		return order != null && OrderStatus.TAX_NOT_COMMITTED.equals(order.getStatus());
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
	 * Executes the manual delivery cost tax commit release operation by calling the {@link BusinessProcessService}.
	 *
	 * @param order
	 * 		the {@link OrderModel} to be released
	 */
	protected void executeManualDeliveryCostTaxCommitOperation(final OrderModel order)
	{
		order.getOrderProcess().stream()
				.filter(process -> process.getCode().startsWith(order.getStore().getSubmitOrderProcessCode())).forEach(
				filteredProcess -> getBusinessProcessService()
						.triggerEvent(filteredProcess.getCode() + "_" + MANUAL_DELIVERY_COST_COMMIT_EVENT));
		LOG.info("Delivery Cost Tax Commit Manual Release completed. {} triggered.", MANUAL_DELIVERY_COST_COMMIT_EVENT);
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

	protected NotificationService getNotificationService()
	{
		return notificationService;
	}
}
