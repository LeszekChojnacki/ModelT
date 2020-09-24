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
import de.hybris.platform.servicelayer.interceptor.InitDefaultsInterceptor;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.ticket.enums.CsTicketState;
import de.hybris.platform.ticket.model.CsTicketModel;
import org.springframework.beans.factory.annotation.Required;


/**
 */
public class CsTicketPrepareInterceptor implements PrepareInterceptor, InitDefaultsInterceptor
{
	private CsTicketState initialTicketState;

	private KeyGenerator keyGenerator;


	@Override
	public void onInitDefaults(final Object model, final InterceptorContext ctx) throws InterceptorException
	{
		if (model instanceof CsTicketModel)
		{
			// If this is a new ticket set the initial values
			final CsTicketModel ticket = (CsTicketModel) model;
			ticket.setState(initialTicketState);
		}
	}

	@Override
	public void onPrepare(final Object model, final InterceptorContext ctx) throws InterceptorException
	{
		if (model instanceof CsTicketModel)
		{
			// If this is a new ticket set the initial values
			final CsTicketModel ticket = (CsTicketModel) model;
			if (ticket.getTicketID() == null)
			{
				ticket.setTicketID(createTicketId());
			}

			if (ticket.getState() == null)
			{
				ticket.setState(initialTicketState);
			}
		}
	}

	protected String createTicketId()
	{
		return keyGenerator.generate().toString();
	}

	@Required
	public void setInitialTicketState(final CsTicketState initialTicketState)
	{
		this.initialTicketState = initialTicketState;
	}

	@Required
	public void setKeyGenerator(final KeyGenerator keyGenerator)
	{
		this.keyGenerator = keyGenerator;
	}
}
