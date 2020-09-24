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
package de.hybris.platform.customersupportbackoffice.widgets;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEventTypes;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.customersupportbackoffice.data.CsCreateCustomerForm;
import de.hybris.platform.customersupportbackoffice.strategies.CsCreateCustomerStrategy;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.Map;

import org.springframework.beans.factory.annotation.Required;
import org.zkoss.util.resource.Labels;

import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.config.jaxb.wizard.CustomType;
import com.hybris.cockpitng.dataaccess.context.Context;
import com.hybris.cockpitng.dataaccess.context.impl.DefaultContext;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectCRUDHandler;
import com.hybris.cockpitng.dataaccess.facades.object.impl.DefaultObjectFacade;
import com.hybris.cockpitng.widgets.configurableflow.ConfigurableFlowController;
import com.hybris.cockpitng.widgets.configurableflow.FlowActionHandler;
import com.hybris.cockpitng.widgets.configurableflow.FlowActionHandlerAdapter;

public class CreateCustomerWizardHandler extends CsCreateWizardBaseHandler implements FlowActionHandler
{
	private CsCreateCustomerStrategy csCreateCustomerStrategy;
	private UserService userService;
	private NotificationService notificationService;

	@Override
	public void perform(final CustomType customType, final FlowActionHandlerAdapter adapter, final Map<String, String> parameters)
	{
		try
		{
			final CsCreateCustomerForm form = adapter.getWidgetInstanceManager().getModel()
					.getValue("customersupport_backoffice_customerForm", CsCreateCustomerForm.class);

			getCsCreateCustomerStrategy().createCustomer(form);

			final UserModel userModel = userService.getUserForUID(form.getEmail().toLowerCase());
			// notifying session widget
			adapter.getWidgetInstanceManager().sendOutput("wizardResult", userModel);

			//Refresh collection list
			final Context internalContext = new DefaultContext();
			internalContext.addAttribute(DefaultObjectFacade.CTX_PARAM_UPDATED_OBJECT_IS_NEW, Boolean.TRUE);
			this.publishEvent(ObjectCRUDHandler.OBJECTS_UPDATED_EVENT, userModel, internalContext);
		}
		catch (final DuplicateUidException e)
		{
			final ConfigurableFlowController controller = (ConfigurableFlowController) adapter.getWidgetInstanceManager()
					.getWidgetslot().getAttribute("widgetController");

			if ("step2".equals(controller.getCurrentStep().getId()))
			{
				adapter.back();
			}

			notificationService.notifyUser(
					notificationService.getWidgetNotificationSource(adapter.getWidgetInstanceManager()),
				NotificationEventTypes.EVENT_TYPE_OBJECT_CREATION,
				NotificationEvent.Level.FAILURE,
				Labels.getLabel("customersupport_backoffice_create_customer_error"));
			return;
		}
		adapter.done();
	}

	protected CsCreateCustomerStrategy getCsCreateCustomerStrategy()
	{
		return csCreateCustomerStrategy;
	}

	@Required
	public void setCsCreateCustomerStrategy(final CsCreateCustomerStrategy csCreateCustomerStrategy)
	{
		this.csCreateCustomerStrategy = csCreateCustomerStrategy;
	}

	protected UserService getUserService()
	{
		return userService;
	}

	protected NotificationService getNotificationService()
	{
		return notificationService;
	}

	@Required
	public void setNotificationService(NotificationService notificationService)
	{
		this.notificationService = notificationService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}
}
