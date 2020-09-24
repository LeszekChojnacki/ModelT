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

import de.hybris.platform.ticket.events.model.CsTicketEventModel;


/**
 * Strategy for rendering ticket information using templates. Can be used for reports etc. to render ticket event data
 * in a specific way. Templates for events are specified as velocity templates.
 */
public interface TicketRenderStrategy
{
	/**
	 * Render the specific ticket event using the template setup for that event type
	 * 
	 * @param ticketEvent
	 *           The event to render
	 * @return The rendered representation of that event
	 */
	String renderTicketEvent(CsTicketEventModel ticketEvent);
}
