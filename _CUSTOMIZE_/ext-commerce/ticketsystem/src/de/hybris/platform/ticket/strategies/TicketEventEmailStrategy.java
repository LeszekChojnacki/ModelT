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
package de.hybris.platform.ticket.strategies;

import de.hybris.platform.ticket.enums.CsEmailRecipients;
import de.hybris.platform.ticket.events.model.CsTicketEventModel;
import de.hybris.platform.ticket.model.CsTicketModel;


/**
 * Interface to support sending of email updates relating to an event added to the ticket.
 */
public interface TicketEventEmailStrategy
{
	/**
	 * Send emails relating to the specified event. It is down to the implementation of this interface to identify how /
	 * who what etc. emails should be sent for the given event
	 *
	 * @param ticket
	 *           The ticket the email relates to
	 * @param event
	 *           The event to send an event for
	 */
	void sendEmailsForEvent(CsTicketModel ticket, CsTicketEventModel event);

	/**
	 * Send emails an email to the cs agent or agent group.
	 * 
	 * @param ticket
	 * @param event
	 * @param recepientType
	 */
	void sendEmailsForAssignAgentTicketEvent(CsTicketModel ticket, CsTicketEventModel event, CsEmailRecipients recepientType);
}
