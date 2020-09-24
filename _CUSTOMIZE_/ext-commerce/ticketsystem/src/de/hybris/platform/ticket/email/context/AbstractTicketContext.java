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

import de.hybris.platform.comments.model.CommentAttachmentModel;
import de.hybris.platform.ticket.events.model.CsTicketEventModel;
import de.hybris.platform.ticket.model.CsTicketModel;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.velocity.VelocityContext;

import java.util.Collection;


/**
 *
 */
public abstract class AbstractTicketContext extends VelocityContext
{
	private final CsTicketModel ticket;
	private final CsTicketEventModel event;

	public AbstractTicketContext(final CsTicketModel ticket, final CsTicketEventModel event)
	{
		super();
		this.ticket = ticket;
		this.event = event;
	}

	public abstract String getName();

	public abstract String getTo();

	public String getTicketId()
	{
		return ticket.getTicketID();
	}

	/**
	 * The text of the event
	 * 
	 * @return The ext of the event
	 */
	public String getText()
	{
		return event.getText();
	}

	/**
	 * A version of the text of the email with Html characters escaped and newlines replaced with paragraphs
	 * 
	 * @return An HTML 'safe' version fo the text.
	 */
	public String getHtmlText()
	{
		final String escapedValue = escapeHtml(event.getText());
		final String paragraphs = buildParagraphs(escapedValue);

		return paragraphs;
	}

	public CsTicketModel getTicket()
	{
		return ticket;
	}

	public CsTicketEventModel getEvent()
	{
		return event;
	}

	public String getSubject()
	{
		return event.getSubject();
	}

	public Collection<CommentAttachmentModel> getAttachments()
	{
		return event.getAttachments();
	}

	protected String escapeHtml(final String text)
	{
		return StringEscapeUtils.escapeHtml(text);
	}

	protected String buildParagraphs(final String text)
	{
		// Wrap all newline separated blocks in <p> tags
		return "<p>" + text.replaceAll("(\\n\\r?)+", "</p><p>") + "</p>";
	}


}
