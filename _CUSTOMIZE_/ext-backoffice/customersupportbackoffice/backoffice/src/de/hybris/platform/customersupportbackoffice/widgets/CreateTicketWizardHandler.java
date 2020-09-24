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
import de.hybris.platform.customersupportbackoffice.data.CsCreateTicketForm;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.ticket.model.CsAgentGroupModel;
import de.hybris.platform.ticket.model.CsTicketModel;
import de.hybris.platform.ticket.service.TicketBusinessService;

import java.util.Map;

import de.hybris.platform.ticketsystem.data.CsTicketParameter;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEventTypes;
import com.hybris.cockpitng.config.jaxb.wizard.CustomType;
import com.hybris.cockpitng.dataaccess.context.Context;
import com.hybris.cockpitng.dataaccess.context.impl.DefaultContext;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectCRUDHandler;
import com.hybris.cockpitng.dataaccess.facades.object.impl.DefaultObjectFacade;
import com.hybris.cockpitng.widgets.configurableflow.ConfigurableFlowController;
import com.hybris.cockpitng.widgets.configurableflow.FlowActionHandler;
import com.hybris.cockpitng.widgets.configurableflow.FlowActionHandlerAdapter;


public class CreateTicketWizardHandler extends CsCreateWizardBaseHandler implements FlowActionHandler
{
	private TicketBusinessService ticketBusinessService;
	private UserService userService;
	private DefaultCsFormInitialsFactory defaultCsFormInitialsFactory;
	private ModelService modelService;
	private NotificationService notificationService;

	@Override
	public void perform(final CustomType customType, final FlowActionHandlerAdapter flowActionHandlerAdapter,
			final Map<String, String> map)
	{
		final CsCreateTicketForm form = flowActionHandlerAdapter.getWidgetInstanceManager().getModel()
				.getValue("customersupport_backoffice_ticketForm", CsCreateTicketForm.class);

		final CsTicketModel ticket = createTicket(form);

		getNotificationService().notifyUser(
				getNotificationService().getWidgetNotificationSource(flowActionHandlerAdapter.getWidgetInstanceManager()),
				NotificationEventTypes.EVENT_TYPE_OBJECT_CREATION, NotificationEvent.Level.SUCCESS, ticket);

		//Refresh collection list
		final Context internalContext = new DefaultContext();
		internalContext.addAttribute(DefaultObjectFacade.CTX_PARAM_UPDATED_OBJECT_IS_NEW, Boolean.TRUE);
		this.publishEvent(ObjectCRUDHandler.OBJECTS_UPDATED_EVENT, ticket, internalContext);

		final ConfigurableFlowController controller = (ConfigurableFlowController) flowActionHandlerAdapter
				.getWidgetInstanceManager().getWidgetslot().getAttribute("widgetController");
		controller.setValue("finished", Boolean.TRUE);
		controller.getBreadcrumbDiv().invalidate();
		// notifying session widget
		controller.sendOutput("wizardResult", ticket);
		flowActionHandlerAdapter.done();
	}

	protected CsTicketModel createTicket(CsCreateTicketForm form)
	{
		final CsTicketModel ticket = getTicketBusinessService().createTicket(createCsTicketParameter(form));
		ticket.setBaseSite(form.getBasesite());
		ticket.setOrder(form.getAssignedTo());
		getModelService().save(ticket);
		return ticket;
	}

	protected CsTicketParameter createCsTicketParameter(CsCreateTicketForm form)
	{
		final CsTicketParameter ticketParameter = new CsTicketParameter();
		ticketParameter.setPriority( form.getPriority());
		ticketParameter.setReason(form.getReason());
		ticketParameter.setAssociatedTo(form.getAssignedTo());
		ticketParameter.setAssignedGroup(getCsAgentGroup(form));
		ticketParameter.setAssignedAgent(form.getAssignedAgent());
		ticketParameter.setCategory(form.getCategory());
		ticketParameter.setHeadline(form.getSubject());
		ticketParameter.setInterventionType(form.getIntervention());
		ticketParameter.setCreationNotes(form.getMessage());
		ticketParameter.setCustomer(form.getCustomer());
		return ticketParameter;
	}

	protected CsAgentGroupModel getCsAgentGroup(CsCreateTicketForm form)
	{
		final CsAgentGroupModel csAgentGroupModel;
		if (form.getAssignedAgent() != null && form.getAssignedGroup() == null)
		{
			csAgentGroupModel = (CsAgentGroupModel) getUserService()
					.getUserGroupForUID(getDefaultCsFormInitialsFactory().getDefaultAgentGroup());
		}
		else
		{
			csAgentGroupModel = form.getAssignedGroup();
		}
		return csAgentGroupModel;
	}

	protected UserService getUserService()
	{
		return userService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	protected DefaultCsFormInitialsFactory getDefaultCsFormInitialsFactory()
	{
		return defaultCsFormInitialsFactory;
	}

	@Required
	public void setDefaultCsFormInitialsFactory(final DefaultCsFormInitialsFactory defaultCsFormInitialsFactory)
	{
		this.defaultCsFormInitialsFactory = defaultCsFormInitialsFactory;
	}

	protected TicketBusinessService getTicketBusinessService()
	{
		return ticketBusinessService;
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
	public void setTicketBusinessService(final TicketBusinessService ticketBusinessService)
	{
		this.ticketBusinessService = ticketBusinessService;
	}

	public ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}
}
