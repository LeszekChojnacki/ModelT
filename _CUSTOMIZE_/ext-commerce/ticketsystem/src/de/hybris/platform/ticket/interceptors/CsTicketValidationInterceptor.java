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
import de.hybris.platform.servicelayer.interceptor.ValidateInterceptor;
import de.hybris.platform.ticket.model.CsTicketModel;
import org.apache.commons.lang.StringUtils;


/**
 */
public class CsTicketValidationInterceptor implements ValidateInterceptor
{
	@Override
	public void onValidate(final Object model, final InterceptorContext ctx) throws InterceptorException
	{
		if (model instanceof CsTicketModel)
		{
			final CsTicketModel ticket = (CsTicketModel) model;

			if (ticket.getTicketID() == null)
			{
				throw new InterceptorException("The ticket must have a ticketID specified", this);
			}

			if (ticket.getCategory() == null)
			{
				throw new InterceptorException("The ticket must have a category specified", this);
			}

			if (ticket.getPriority() == null)
			{
				throw new InterceptorException("The ticket must have a priority specified", this);
			}

			if (StringUtils.isEmpty(ticket.getHeadline()))
			{
				throw new InterceptorException("The ticket must have a headline specified", this);
			}

			if (ticket.getOrder() != null)
			{
				if (ticket.getCustomer() == null)
				{
					throw new InterceptorException("If an order is specified on a ticket then a customer must also be specified", this);
				}

				if (!ticket.getCustomer().equals(ticket.getOrder().getUser()))
				{
					throw new InterceptorException(
							"If an customer and order is specified on a ticket then the customer must own the order", this);
				}
			}
		}

	}
}
