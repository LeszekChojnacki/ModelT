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
package de.hybris.platform.ticket.jalo;

import de.hybris.platform.comments.constants.CommentsConstants;
import de.hybris.platform.jalo.Item;
import de.hybris.platform.jalo.JaloBusinessException;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.link.Link;
import de.hybris.platform.jalo.type.ComposedType;
import de.hybris.platform.ticket.constants.TicketsystemConstants;
import de.hybris.platform.ticket.events.jalo.CsTicketEvent;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;


public class CsTicket extends GeneratedCsTicket
{
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(CsTicket.class.getName());

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
	 *             {@link de.hybris.platform.ticket.service.TicketService#getEventsForTicket(de.hybris.platform.ticket.model.CsTicketModel)}
	 *             instead.
	 */
	@Deprecated
	@Override
	public List<CsTicketEvent> getEvents(final SessionContext ctx)
	{
		final String query = "SELECT {e:" + Item.PK + "}, {c2i:" + Link.REVERSE_SEQUENCE_NUMBER + "} " + "FROM {"
				+ TicketsystemConstants.TC.CSTICKETEVENT + " AS e " + "JOIN " + CommentsConstants.Relations.COMMENTITEMRELATION
				+ " AS c2i " + "ON {c2i:" + Link.SOURCE + "}={e:" + Item.PK + "} }" + "WHERE {c2i:" + Link.TARGET + "}=?ticket "
				+ "ORDER BY {c2i:" + Link.REVERSE_SEQUENCE_NUMBER + "} ASC";

		return JaloSession.getCurrentSession().getFlexibleSearch()
				.search(query, Collections.singletonMap("ticket", this), CsTicketEvent.class).getResult();
	}
}
