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
package de.hybris.platform.omsbackoffice.renderers;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;

import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.customersupportbackoffice.constants.CustomersupportbackofficeConstants;
import de.hybris.platform.fraud.model.FraudReportModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.model.ModelService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.lang.Strings;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Listcell;

import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.core.config.impl.jaxb.listview.ListColumn;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.widgets.common.WidgetComponentRenderer;


/**
 * This cell renderer displays a accept potentially fraudulent order button and handles the button click.
 */
public class FraudCheckButtonCellRenderer implements WidgetComponentRenderer<Listcell, ListColumn, FraudReportModel>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(FraudCheckButtonCellRenderer.class);

	protected static final String FRAUD_BUTTON = "fraudbutton";
	protected static final String REJECT_FRAUD_ITEM = "rejectfrauditem";
	protected static final String ACCEPT_FRAUD_ITEM = "acceptfrauditem";
	protected static final String ORDER_EVENT_NAME = "CSAOrderVerified";
	protected static final String DISABLED = "disabled";

	private BusinessProcessService businessProcessService;
	private ModelService modelService;
	private NotificationService notificationService;

	private WidgetInstanceManager widgetInstanceManager;

	@Override
	public void render(final Listcell parent, final ListColumn columnConfiguration, final FraudReportModel object,
			final DataType dataType, final WidgetInstanceManager widgetInstanceManger)
	{
		// Should only render the button if the order status is "WAIT_FRAUD_MANUAL_CHECK".
		LOGGER.debug("Rendering accept and reject potentially fraudulent order button.");
		final OrderModel order = object.getOrder();
		setWidgetInstanceManager(widgetInstanceManger);

		final Button acceptButton = new Button();
		String acceptButtonClass = FRAUD_BUTTON + " " + ACCEPT_FRAUD_ITEM;
		acceptButton.setParent(parent);
		acceptButton.addEventListener(Events.ON_CLICK, event -> acceptPotentiallyFraudulentOrder(order));

		final Button rejectButton = new Button();
		String rejectButtonClass = FRAUD_BUTTON + " " + REJECT_FRAUD_ITEM;
		rejectButton.setParent(parent);

		rejectButton.addEventListener(Events.ON_CLICK, event -> rejectPotentiallyFraudulentOrder(order));

		if (!canPerformOperation(order))
		{
			acceptButton.setDisabled(true);
			acceptButtonClass = acceptButtonClass + " " + DISABLED;
			rejectButton.setDisabled(true);
			rejectButtonClass = rejectButtonClass + " " + DISABLED;
		}

		acceptButton.setSclass(acceptButtonClass);
		rejectButton.setSclass(rejectButtonClass);
	}

	/**
	 * Reject the order which had a fraud notice
	 *
	 * @param order
	 * 		a {@link OrderModel}
	 */
	protected void rejectPotentiallyFraudulentOrder(final OrderModel order)
	{
		if (canPerformOperation(order))
		{
			try
			{
				if (LOGGER.isDebugEnabled())
				{
					LOGGER.debug(String.format("Rejected potentially fraudulent order with code: %s", order.getCode()));
				}
				order.setFraudulent(Boolean.TRUE);
				executeFraudulentOperation(order);
				getNotificationService().notifyUser(Strings.EMPTY, CustomersupportbackofficeConstants.NOTIFICATION_TYPE,
						NotificationEvent.Level.SUCCESS, Labels.getLabel("customersupportbackoffice.order.fraud.rejected.success"));
			}
			catch (final ModelSavingException e)
			{
				LOGGER.warn(e.getMessage(), e);
				getNotificationService().notifyUser(Strings.EMPTY, CustomersupportbackofficeConstants.NOTIFICATION_TYPE,
						NotificationEvent.Level.FAILURE, Labels.getLabel("customersupportbackoffice.order.fraud.rejected.failure"));
			}
		}
	}

	/**
	 * Accept the order which had a fraud notice.
	 *
	 * @param order
	 * 		a {@link OrderModel}
	 */
	protected void acceptPotentiallyFraudulentOrder(final OrderModel order)
	{
		if (canPerformOperation(order))
		{
			try
			{
				if (LOGGER.isDebugEnabled())
				{
					LOGGER.debug(String.format("Accepted potentially fraudulent order with code: %s", order.getCode()));
				}
				order.setFraudulent(Boolean.FALSE);
				executeFraudulentOperation(order);
				notificationService.notifyUser(Strings.EMPTY, CustomersupportbackofficeConstants.NOTIFICATION_TYPE,
						NotificationEvent.Level.SUCCESS,	Labels.getLabel("customersupportbackoffice.order.fraud.accepted.success"));
			}
			catch (final ModelSavingException e)
			{
				LOGGER.info(e.getMessage(), e);
				notificationService.notifyUser(Strings.EMPTY, CustomersupportbackofficeConstants.NOTIFICATION_TYPE,
						NotificationEvent.Level.FAILURE,	Labels.getLabel("customersupportbackoffice.order.fraud.accepted.failure"));
			}
		}
	}

	/**
	 * check if the fraud can be rejected or accepted.
	 *
	 * @param order
	 * 		the {@link OrderModel}
	 * @return true if the operation can be executed.
	 */
	protected boolean canPerformOperation(final OrderModel order)
	{
		return order.getStatus() == OrderStatus.WAIT_FRAUD_MANUAL_CHECK;
	}

	/**
	 * Save the order with the new attribute value for fraudulent. <br>
	 * Send the event to business process service
	 *
	 * @param order
	 * 		an {@link OrderModel}
	 * @throws ModelSavingException
	 * 		a {@link ModelSavingException}
	 */
	protected void executeFraudulentOperation(final OrderModel order)
	{
		getModelService().save(order);
		order.getOrderProcess().stream()
				.filter(process -> process.getCode().startsWith(order.getStore().getSubmitOrderProcessCode())).forEach(
				filteredProcess -> getBusinessProcessService().triggerEvent(filteredProcess.getCode() + "_" + ORDER_EVENT_NAME));
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

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	protected NotificationService getNotificationService()
	{
		return notificationService;
	}

	@Required
	public void setNotificationService(final NotificationService notificationService)
	{
		this.notificationService = notificationService;
	}

	public WidgetInstanceManager getWidgetInstanceManager()
	{
		return widgetInstanceManager;
	}

	public void setWidgetInstanceManager(final WidgetInstanceManager widgetInstanceManager)
	{
		this.widgetInstanceManager = widgetInstanceManager;
	}
}
