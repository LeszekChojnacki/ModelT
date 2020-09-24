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
package de.hybris.platform.ticket.render.context;

import de.hybris.platform.ticket.events.model.CsTicketEventModel;
import org.apache.velocity.VelocityContext;


/**
 */
public class TicketEventRenderContext extends VelocityContext
{
	private final CsTicketEventModel event;

	public TicketEventRenderContext(final CsTicketEventModel event)
	{
		super();
		this.event = event;
	}

	public CsTicketEventModel getEvent()
	{
		return event;
	}

	public String getText()
	{
		return event.getText();
	}

}
