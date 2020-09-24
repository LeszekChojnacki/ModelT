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
package de.hybris.platform.customersupportbackoffice.accessors.impl;

import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.ticket.model.CsTicketModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;


/**
 * Property Accessor concrete implementation to access "tickets" attribute in CsTicket
 *
 */
public class TicketListPropertyAccessor implements PropertyAccessor
{
	private static final String TICKETS_ATTR = "tickets";

	@Override
	public Class<?>[] getSpecificTargetClasses()
	{
		final Class<?>[] classes = { CsTicketModel.class }; // NOSONAR
		return classes;
	}

	@Override
	public boolean canRead(final EvaluationContext evaluationContext, final Object currentObject, final String attribute)
			throws AccessException
	{
		return isTypeSupported(currentObject) && attribute.equalsIgnoreCase(TICKETS_ATTR);
	}

	protected boolean isTypeSupported(final Object object)
	{
		return object instanceof CsTicketModel;
	}

	@Override
	public TypedValue read(final EvaluationContext evaluationContext, final Object target, final String attribute)
			throws AccessException
	{
		final CsTicketModel currentTicket = (CsTicketModel) target;

		final UserModel ticketCustomer = currentTicket.getCustomer();

		if (ticketCustomer instanceof CustomerModel)
		{
			final List<CsTicketModel> ticketList = ((CustomerModel) ticketCustomer).getTickets();

			//return list of tickets and remove the current displayed ticket from this list
			return new TypedValue(ticketList.stream().filter(i -> !i.getTicketID().equals(currentTicket.getTicketID()))
					.collect(Collectors.toList()));
		}
		else
		{
			return new TypedValue(new ArrayList());
		}
	}

	@Override
	public boolean canWrite(final EvaluationContext evaluationContext, final Object currentObject, final String attribute)
			throws AccessException
	{
		return false;
	}

	@Override
	public void write(final EvaluationContext evaluationContext, final Object target, final String attributeName,
			final Object attributeValue) throws AccessException
	{
		//left empty intentionally
	}
}
