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
package de.hybris.platform.ticket.event.impl;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.fraud.events.OrderFraudEmployeeNotificationEvent;
import de.hybris.platform.servicelayer.event.impl.AbstractEventListener;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.servicelayer.util.ServicesUtil;
import de.hybris.platform.ticket.enums.CsTicketCategory;
import de.hybris.platform.ticket.enums.CsTicketPriority;
import de.hybris.platform.ticket.model.CsAgentGroupModel;
import de.hybris.platform.ticket.model.CsTicketModel;
import de.hybris.platform.ticket.service.TicketBusinessService;
import de.hybris.platform.ticketsystem.data.CsTicketParameter;
import de.hybris.platform.util.localization.Localization;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Event Listener for {@link OrderFraudEmployeeNotificationEvent} which creates a {@link CsTicketModel} for a ticket
 * agent user group. You can customize the target user group, ticket priority. The headline and ticket notes are taken
 * from the following localized properties:
 * <ul>
 * <li><code>csticket.fraud.content.default</li></code>
 * <li><code>csticket.fraud.headline.default</li></code>
 * </ul>
 * 
 * @author krzysztof.kwiatosz
 * 
 */
public class DefaultOrderFraudEmployeeNotificationEventListener extends
		AbstractEventListener<OrderFraudEmployeeNotificationEvent>
{
	private String fraudUserGroup;
	private CsTicketPriority priority;

	private TicketBusinessService ticketBusinessService;
	private UserService userService;


	public static final String DEFAULT_FRAUD_USER_GROUP = "fraudAgentGroup";
	public static final CsTicketPriority DEFAULT_PRIORITY = CsTicketPriority.HIGH;


	@Override
	protected void onEvent(final OrderFraudEmployeeNotificationEvent event)
	{
		ServicesUtil.validateParameterNotNull(event, "Event cannot be null");
		final OrderModel order = event.getOrder();
		ServicesUtil.validateParameterNotNull(order, "Order in event cannot be null");

		final String headline = getFraudTicketHeadline(order);
		final String ticketText = getFraudTicketText(order);

		final CsAgentGroupModel fraudGroup = getUserService().getUserGroupForUID(getFraudUserGroupId(), CsAgentGroupModel.class);


		final CsTicketParameter ticketParameter = new CsTicketParameter();
		ticketParameter.setPriority(getPriority());
		ticketParameter.setReason(null);
		ticketParameter.setAssociatedTo(order);
		ticketParameter.setAssignedGroup(fraudGroup);
		ticketParameter.setCategory( CsTicketCategory.FRAUD);
		ticketParameter.setHeadline(headline);
		ticketParameter.setInterventionType(null);
		ticketParameter.setCreationNotes(ticketText);
		ticketParameter.setCustomer(order.getUser());
		getTicketBusinessService().createTicket(ticketParameter);

	}

	protected String getFraudTicketText(final AbstractOrderModel order)
	{
		return Localization.getLocalizedString("csticket.fraud.content.default");
	}

	protected String getFraudTicketHeadline(final AbstractOrderModel order)
	{
		return Localization.getLocalizedString("csticket.fraud.headline.default", new Object[]
		{ order.getCode() });
	}

	protected CsTicketPriority getPriority()
	{
		if (priority == null)
		{
			return DEFAULT_PRIORITY;
		}
		return priority;
	}

	protected String getFraudUserGroupId()
	{
		if (StringUtils.isEmpty(fraudUserGroup))
		{
			return DEFAULT_FRAUD_USER_GROUP;
		}
		return fraudUserGroup;
	}

	/**
	 * @return the ticketBusinessService
	 */
	protected TicketBusinessService getTicketBusinessService()
	{
		return ticketBusinessService;
	}

	/**
	 * @param ticketBusinessService
	 *           the ticketBusinessService to set
	 */
	@Required
	public void setTicketBusinessService(final TicketBusinessService ticketBusinessService)
	{
		this.ticketBusinessService = ticketBusinessService;
	}

	/**
	 * @return the userService
	 */
	protected UserService getUserService()
	{
		return userService;
	}

	/**
	 * @param userService
	 *           the userService to set
	 */
	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	public void setFraudUserGroup(final String fraudUserGroup)
	{
		this.fraudUserGroup = fraudUserGroup;
	}

	public void setPriority(final CsTicketPriority priority)
	{
		this.priority = priority;
	}
}
