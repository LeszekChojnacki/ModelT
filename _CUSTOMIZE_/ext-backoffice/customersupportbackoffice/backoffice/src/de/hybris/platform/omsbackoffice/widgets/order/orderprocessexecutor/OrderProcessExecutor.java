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
package de.hybris.platform.omsbackoffice.widgets.order.orderprocessexecutor;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.customersupportbackoffice.constants.CustomersupportbackofficeConstants;
import de.hybris.platform.processengine.BusinessProcessEvent;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.util.localization.Localization;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.annotations.SocketEvent;
import org.zkoss.zk.ui.select.annotation.WireVariable;


/**
 * This class is handling the process engine execution for orders
 */
public class OrderProcessExecutor
{
	protected static final String TRIGGER_SOURCING_IN_SOCKET = "triggerSourcing";
	protected static final String PUT_ON_HOLD_IN_SOCKET = "putOnHold";
	protected static final String ORDER_ACTION_EVENT_NAME = "OrderActionEvent";
	protected static final String RE_SOURCE_CHOICE = "reSource";
	protected static final String PUT_ON_HOLD_CHOICE = "putOnHold";
	protected static final String TRIGGER_SOURCING_SUCCESS = "customersupportbackoffice.order.trigger.sourcing.success";
	protected static final String PUT_ORDER_ON_HOLD_SUCCESS = "customersupportbackoffice.order.on.hold.success";

	@WireVariable
	private BusinessProcessService businessProcessService;
	@WireVariable
	private NotificationService notificationService;

	/**
	 * This event is triggered whenever there is a request to trigger again the sourcing for a specific {@link OrderModel}
	 *
	 * @param order
	 * 		the order for which the sourcing is requested
	 */
	@SocketEvent(socketId = TRIGGER_SOURCING_IN_SOCKET)
	public void triggerSourcing(final OrderModel order)
	{
		triggerBusinessProcessEvent(order, RE_SOURCE_CHOICE, TRIGGER_SOURCING_SUCCESS);
	}

	/**
	 * This event is triggered whenever there is a request to put a specific {@link OrderModel} on hold
	 *
	 * @param order
	 * 		the order which will be put on hold
	 */
	@SocketEvent(socketId = PUT_ON_HOLD_IN_SOCKET)
	public void putOnHold(final OrderModel order)
	{
		triggerBusinessProcessEvent(order, PUT_ON_HOLD_CHOICE, PUT_ORDER_ON_HOLD_SUCCESS);
	}

	/**
	 * Triggers the business process associated to the {@link OrderModel} with the given choice.
	 *
	 * @param order
	 * 		the {@link OrderModel}
	 * @param choice
	 * 		the choice of the business process
	 * @param notificationMessage
	 * 		the notification success message
	 */
	protected void triggerBusinessProcessEvent(final OrderModel order, final String choice, final String notificationMessage)
	{
		order.getOrderProcess().stream()
				.filter(process -> process.getCode().startsWith(order.getStore().getSubmitOrderProcessCode())).forEach(
				filteredProcess -> getBusinessProcessService().triggerEvent(
						BusinessProcessEvent.builder(filteredProcess.getCode() + "_" + ORDER_ACTION_EVENT_NAME).withChoice(choice)
								.withEventTriggeringInTheFutureDisabled().build()));

		getNotificationService().notifyUser((String) null, CustomersupportbackofficeConstants.NOTIFICATION_TYPE,
				NotificationEvent.Level.SUCCESS, Localization.getLocalizedString(notificationMessage));
	}

	protected BusinessProcessService getBusinessProcessService()
	{
		return businessProcessService;
	}

	protected NotificationService getNotificationService()
	{
		return notificationService;
	}
}
