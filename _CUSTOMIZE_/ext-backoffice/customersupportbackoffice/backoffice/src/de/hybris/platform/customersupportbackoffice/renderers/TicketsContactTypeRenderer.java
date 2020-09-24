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
package de.hybris.platform.customersupportbackoffice.renderers;

import de.hybris.platform.customersupportbackoffice.constants.CustomersupportbackofficeConstants;
import de.hybris.platform.ticket.enums.CsInterventionType;
import de.hybris.platform.ticket.service.TicketService;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Required;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;

import com.hybris.cockpitng.config.jaxb.wizard.ViewType;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.labels.LabelService;
import com.hybris.cockpitng.widgets.configurableflow.renderer.DefaultCustomViewRenderer;


/**
 *
 * This renderer will be used in the ticket status change wizard to render the reply to and contact type fields.
 *
 */
public class TicketsContactTypeRenderer extends DefaultCustomViewRenderer
{
	protected static final String CONTACT_TYPE_CUSTOMER = "Customer";
	protected static final String CONTACT_TYPE_CUSTOMER_SUPPORT = "CustomerSupport";
	protected static final String DIV_WRAPPER = "yw-wizard-property";

	private TicketService ticketService;
	private LabelService labelService;

	@Override
	public void render(final Component component, final ViewType customView, final Map<String, String> parameters,
			final DataType dataType, final WidgetInstanceManager widgetInstanceManager)
	{
		final Div contactTypePanel = new Div();
		contactTypePanel.setSclass(DIV_WRAPPER);

		//Contact Type Combo
		final Label contactTypeLabel = new Label(Labels.getLabel("customersupport_backoffice_closeTicketForm.contact") + ":");
		final Combobox contactTypeCombo = configureContactTypeCombo();
		contactTypePanel.appendChild(contactTypeLabel);
		contactTypePanel.appendChild(contactTypeCombo);

		//Reply To Combo
		final Div replyToPanel = new Div();
		replyToPanel.setSclass(DIV_WRAPPER);
		final Label replyToCustomerLabel = new Label(Labels.getLabel("customersupport_backoffice_tickets_correspondence_replyto")
				+ ":");
		replyToPanel.appendChild(replyToCustomerLabel);
		final Combobox replyToCombo = configureReplyToCombo();
		replyToPanel.appendChild(replyToCombo);

		//Add event listeners
		replyToCombo.addEventListener(Events.ON_SELECT, addReplyToComboEventListener(replyToCombo, contactTypeCombo));
		contactTypeCombo.addEventListener(Events.ON_SELECT, addContactTypeComboEventListener(contactTypeCombo, replyToCombo));

		widgetInstanceManager.getModel().setValue(CustomersupportbackofficeConstants.REPLY_TYPE, contactTypeCombo);
		component.appendChild(contactTypePanel);
		component.appendChild(replyToPanel);
	}

	/**
	 * /**
	 *
	 * @param replyToCombo
	 * @param contactTypeCombo
	 * @return EventListener
	 */
	protected EventListener addContactTypeComboEventListener(final Combobox contactTypeCombo, final Combobox replyToCombo)
	{
		return new EventListener()
		{
			@Override
			public void onEvent(final Event event) throws Exception
			{
				final CsInterventionType selectedContactTypeInterventionType = contactTypeCombo.getSelectedItem().getValue();
				if (CsInterventionType.PRIVATE.equals(selectedContactTypeInterventionType))
				{
					replyToCombo.setSelectedIndex(1);
				}
				else
				{
					replyToCombo.setSelectedIndex(0);
				}
			}
		};
	}

	/**
	 * @param replyToCombo
	 * @param contactTypeCombo
	 * @return EventListener
	 */
	protected EventListener addReplyToComboEventListener(final Combobox replyToCombo, final Combobox contactTypeCombo)
	{
		return new EventListener()
		{
			@Override
			public void onEvent(final Event event) throws Exception
			{
				final CsInterventionType selectedReplyToInterventionType = replyToCombo.getSelectedItem().getValue();
				for (final Comboitem currentComboItem : contactTypeCombo.getItems())
				{
					if (currentComboItem.getValue().equals(selectedReplyToInterventionType))
					{
						contactTypeCombo.setSelectedItem(currentComboItem);
						break;
					}
				}
			}
		};
	}

	protected Combobox configureReplyToCombo()
	{
		final Combobox replyToCombo = new Combobox();
		final Comboitem customerComboItem = new Comboitem();
		final Comboitem customerSupportComboItem = new Comboitem();

		customerComboItem.setLabel(Labels.getLabel("customersupport_backoffice_tickets_correspondence_replyto_customer"));
		customerComboItem.setValue(CsInterventionType.TICKETMESSAGE);

		customerSupportComboItem.setLabel(Labels
				.getLabel("customersupport_backoffice_tickets_correspondence_replyto_customersupport"));
		customerSupportComboItem.setValue(CsInterventionType.PRIVATE);

		replyToCombo.appendChild(customerComboItem);
		replyToCombo.appendChild(customerSupportComboItem);

		replyToCombo.setSelectedIndex(0);
		replyToCombo.setReadonly(true);
		return replyToCombo;
	}

	protected Combobox configureContactTypeCombo()
	{
		final Combobox contactTypeCombo = new Combobox();
		contactTypeCombo.setReadonly(true);
		final List<CsInterventionType> interventionTypes = ticketService.getInterventionTypes();
		for (final CsInterventionType csInterventionType : interventionTypes)
		{
			final Comboitem comboItem = new Comboitem();
			comboItem.setLabel(getLabelService().getObjectLabel(csInterventionType));
			comboItem.setValue(csInterventionType);
			contactTypeCombo.appendChild(comboItem);
			if (CsInterventionType.TICKETMESSAGE.equals(csInterventionType))
			{
				contactTypeCombo.setSelectedItem(comboItem);
			}
		}
		return contactTypeCombo;
	}

	@Required
	public void setTicketService(final TicketService ticketService)
	{
		this.ticketService = ticketService;
	}

	protected TicketService getTicketService()
	{
		return ticketService;
	}

	/**
	 * @return the labelService
	 */
	public LabelService getLabelService()
	{
		return labelService;
	}

	/**
	 * @param labelService
	 *           the labelService to set
	 */
	@Required
	public void setLabelService(final LabelService labelService)
	{
		this.labelService = labelService;
	}
}
