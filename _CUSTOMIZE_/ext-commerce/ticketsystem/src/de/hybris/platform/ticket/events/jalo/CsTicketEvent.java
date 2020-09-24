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
package de.hybris.platform.ticket.events.jalo;

import de.hybris.platform.jalo.Item;
import de.hybris.platform.jalo.JaloBusinessException;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.type.ComposedType;
import de.hybris.platform.ticket.jalo.CsTicket;

import java.util.Collection;
import java.util.Collections;

import org.apache.log4j.Logger;


public class CsTicketEvent extends GeneratedCsTicketEvent
{
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(CsTicketEvent.class.getName());

	@Override
	protected Item createItem(final SessionContext ctx, final ComposedType type, final ItemAttributeMap allAttributes)
			throws JaloBusinessException
	{
		// business code placed here will be executed before the item is created
		// then create the item
		final Item item = super.createItem(ctx, type, allAttributes);
		// business code placed here will be executed after the item was created
		// and return the item
		return item;
	}

	/**
	 * @deprecated since ages use
	 *             {@link de.hybris.platform.ticket.service.TicketService#getTicketForTicketEvent(de.hybris.platform.ticket.events.model.CsTicketEventModel)}
	 *             instead.
	 */
	@Deprecated
	@Override
	public CsTicket getTicket(final SessionContext ctx)
	{
		final Collection<Item> relatedItems = getRelatedItems(ctx);
		if (relatedItems != null)
		{
			if (relatedItems.size() > 1)
			{
				throw new IllegalStateException(
						"A ticket event should only associated with a single ticket. Error occurred on event [" + this.getPK() + "]");
			}
			else if (relatedItems.size() == 1)
			{
				final Item item = relatedItems.iterator().next();
				if (item instanceof CsTicket)
				{
					return (CsTicket) item;
				}
				else
				{
					throw new IllegalStateException("A ticket event must be associated with a ticket. Error occurred on event ["
							+ this.getPK() + "] found related item [" + item + "]");
				}
			}
		}
		return null; // Not yet related to a ticket
	}

	@Override
	public void setTicket(final SessionContext ctx, final CsTicket ticket)
	{
		if (ticket != null)
		{
			this.setRelatedItems(ctx, (Collection) Collections.singleton(ticket));
		}
	}
}
