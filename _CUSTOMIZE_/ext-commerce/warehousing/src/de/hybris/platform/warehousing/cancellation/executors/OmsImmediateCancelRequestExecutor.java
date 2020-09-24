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
 *
 */
package de.hybris.platform.warehousing.cancellation.executors;

import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.ordercancel.OrderCancelRequest;
import de.hybris.platform.ordercancel.OrderCancelResponse;
import de.hybris.platform.ordercancel.OrderUtils;
import de.hybris.platform.ordercancel.impl.executors.ImmediateCancelRequestExecutor;
import de.hybris.platform.processengine.BusinessProcessEvent;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.processengine.model.ProcessTaskModel;

import java.util.Collection;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;


/**
 * Warehousing implementation of {@link ImmediateCancelRequestExecutor}
 */
public class OmsImmediateCancelRequestExecutor extends ImmediateCancelRequestExecutor
{
	protected static final String WAIT_FOR_MANUAL_ORDER_CHECK_ACTION = "waitForManualOrderCheckCSA";
	protected static final String CSA_VERIFIED_ORDER_EVENT_NAME = "CSAOrderVerified";
	protected static final String WAIT_FOR_ORDER_ACTION = "waitForOrderAction";
	protected static final String ORDER_ACTION_EVENT_NAME = "OrderActionEvent";
	protected static final String CANCELLED_CHOICE = "cancelled";
	private static final Logger LOG = LoggerFactory.getLogger(OmsImmediateCancelRequestExecutor.class.getName());
	private BusinessProcessService businessProcessService;

	@Override
	protected void updateOrderProcess(final OrderCancelRequest orderCancelRequest)
	{
		validateParameterNotNull(orderCancelRequest, "No Order Cancellation Request found to be cancelled");
		validateParameterNotNull(orderCancelRequest.getOrder(), "No Order found to be cancelled");

		final OrderModel order = orderCancelRequest.getOrder();
		if (!OrderUtils.hasLivingEntries(order) || OrderStatus.CANCELLED.equals(order.getStatus()))
		{
			//Complete Order Cancellation
			LOG.info("Moving order process as complete order cancellation is requested for the order: [{}]", order.getCode());
			order.getOrderProcess().stream()
					.filter(process -> process.getCode().startsWith(order.getStore().getSubmitOrderProcessCode()))
					.forEach(filteredProcess -> {
						final Collection<ProcessTaskModel> currentTasks = filteredProcess.getCurrentTasks();
						Assert.isTrue(CollectionUtils.isNotEmpty(currentTasks),
								String.format("No available process tasks found for the Order to be cancelled [%s]", order.getCode()));
						if (currentTasks.stream().anyMatch(task -> WAIT_FOR_ORDER_ACTION.equals(task.getAction())))
						{
							getBusinessProcessService().triggerEvent(
									BusinessProcessEvent.builder(filteredProcess.getCode() + "_" + ORDER_ACTION_EVENT_NAME)
											.withChoice(CANCELLED_CHOICE).withEventTriggeringInTheFutureDisabled().build());
						}
						else if (currentTasks.stream().anyMatch(task -> WAIT_FOR_MANUAL_ORDER_CHECK_ACTION.equals(task.getAction())))
						{
							getBusinessProcessService().triggerEvent(filteredProcess.getCode() + "_" + CSA_VERIFIED_ORDER_EVENT_NAME);
						}
					});
		}
		else
		{
			LOG.info("Performing immediate partial cancellation. No update required on the order process");
		}

	}

	@Override
	protected OrderCancelResponse makeInternalResponse(final OrderCancelRequest request, final boolean success,
			final String message)
	{
		OrderCancelResponse.ResponseStatus orderCancelResponseStatus = OrderCancelResponse.ResponseStatus.full;
		if (request.isPartialCancel())
		{
			orderCancelResponseStatus = OrderCancelResponse.ResponseStatus.partial;
		}

		return new OrderCancelResponse(request.getOrder(), request.getEntriesToCancel(),
				success ? orderCancelResponseStatus : OrderCancelResponse.ResponseStatus.error, message);
	}

	protected BusinessProcessService getBusinessProcessService()
	{
		return businessProcessService;
	}

	@Required
	public void setBusinessProcessService(final BusinessProcessService businessProcessService)
	{
		this.businessProcessService = businessProcessService;
	}
}
