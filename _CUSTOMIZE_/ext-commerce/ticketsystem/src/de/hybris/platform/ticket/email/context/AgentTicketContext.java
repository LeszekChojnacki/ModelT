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
package de.hybris.platform.ticket.email.context;

import de.hybris.platform.ticket.events.model.CsTicketEventModel;
import de.hybris.platform.ticket.model.CsTicketModel;


/**
 */
public class AgentTicketContext extends AbstractTicketContext
{
	public AgentTicketContext(final CsTicketModel ticket, final CsTicketEventModel event)
	{
		super(ticket, event);
	}

	@Override
	public String getName()
	{
		return getTicket().getAssignedAgent().getName();
	}

	@Override
	public String getTo()
	{
		return (getTicket().getAssignedAgent() != null && getTicket().getAssignedAgent().getDefaultPaymentAddress() != null) ? getTicket()
				.getAssignedAgent().getDefaultPaymentAddress().getEmail()
				: null;
	}
}
