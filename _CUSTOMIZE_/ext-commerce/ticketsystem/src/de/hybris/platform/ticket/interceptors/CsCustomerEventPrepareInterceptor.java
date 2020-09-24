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
package de.hybris.platform.ticket.interceptors;

import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;
import de.hybris.platform.ticket.events.model.CsCustomerEventModel;
import de.hybris.platform.ticket.model.CsTicketModel;
import de.hybris.platform.ticket.service.TicketService;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * The interceptor sets proper {@link CsCustomerEventModel#SUBJECT} for the purpose of AuditReport
 */
public class CsCustomerEventPrepareInterceptor implements PrepareInterceptor
{
	private TicketService ticketService;

	@Override
	public void onPrepare(final Object model, final InterceptorContext ctx) throws InterceptorException
	{
		if (model instanceof CsCustomerEventModel)
		{
			final CsCustomerEventModel event = (CsCustomerEventModel) model;
			final CsTicketModel ticket = getTicketService().getTicketForTicketEvent(event);

			if(canSubjectBePopulated(event,ticket))
			{
				event.setSubject(ticket.getHeadline());
			}
		}
	}

	protected boolean canSubjectBePopulated(final CsCustomerEventModel event, final CsTicketModel ticket)
	{
		return StringUtils.isNotBlank(event.getText()) && ticket != null;
	}

	protected TicketService getTicketService()
	{
		return ticketService;
	}

	@Required
	public void setTicketService(final TicketService ticketService)
	{
		this.ticketService = ticketService;
	}
}
