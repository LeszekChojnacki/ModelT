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
 *
 */
public class CustomerTicketContext extends AbstractTicketContext
{
	public CustomerTicketContext(final CsTicketModel ticket, final CsTicketEventModel event)
	{
		super(ticket, event);
	}

	@Override
	public String getName()
	{
		return getTicket().getCustomer().getName();
	}

	@Override
	public String getTo()
	{
		return (getTicket().getCustomer() != null && getTicket().getCustomer().getDefaultPaymentAddress() != null) ? getTicket()
				.getCustomer().getDefaultPaymentAddress().getEmail() : null;
	}
}
