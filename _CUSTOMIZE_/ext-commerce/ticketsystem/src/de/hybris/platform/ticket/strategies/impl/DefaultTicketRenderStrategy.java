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
package de.hybris.platform.ticket.strategies.impl;

import de.hybris.platform.commons.model.renderer.RendererTemplateModel;
import de.hybris.platform.commons.renderer.RendererService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.ticket.events.model.CsTicketEventModel;
import de.hybris.platform.ticket.render.context.TicketEventRenderContext;
import de.hybris.platform.ticket.service.TicketException;
import de.hybris.platform.ticket.strategies.TicketRenderStrategy;

import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

public class DefaultTicketRenderStrategy implements TicketRenderStrategy
{
	@SuppressWarnings(
	{ "unused" })
	private static final Logger LOG = Logger.getLogger(DefaultTicketRenderStrategy.class);

	private String defaultTemplateCode = null;
	private RendererService rendererService;
	private FlexibleSearchService flexibleSearchService;
	private Map<String, String> eventType2TemplateCode = new HashMap<String, String>();

	@Override
	public String renderTicketEvent(final CsTicketEventModel ticketEvent)
	{
		try
		{
			final RendererTemplateModel renderTemplate = getTemplateForEvent(ticketEvent);

			final StringWriter text = new StringWriter();
			rendererService.render(renderTemplate, new TicketEventRenderContext(ticketEvent), text);
			return text.toString();
		}
		catch (final TicketException e)
		{
			LOG.error("could not find template for [" + ticketEvent + "]", e);
			return ticketEvent.getText();
		}

	}

	@Required
	public void setDefaultTemplateCode(final String defaultTemplateCode)
	{
		this.defaultTemplateCode = defaultTemplateCode;
	}

	@Required
	public void setEventType2TemplateCode(final Map<String, String> eventType2TemplateCode)
	{
		this.eventType2TemplateCode = eventType2TemplateCode;
	}

	@Required
	public void setFlexibleSearchService(final FlexibleSearchService flexibleSearchService)
	{
		this.flexibleSearchService = flexibleSearchService;
	}

	@Required
	public void setRendererService(final RendererService rendererService)
	{
		this.rendererService = rendererService;
	}

	protected RendererTemplateModel getTemplateForEvent(final CsTicketEventModel ticketEvent) throws TicketException
	{
		String template = eventType2TemplateCode.get(ticketEvent.getCommentType().getCode());
		if (template == null)
		{
			LOG.info("Did not find specific render template for type [" + ticketEvent.getCommentType().getCode() + "]");
			template = defaultTemplateCode;
		}

		// lookup the template
		final SearchResult<RendererTemplateModel> result = flexibleSearchService.search("SELECT {pk} from {"
				+ RendererTemplateModel._TYPECODE + "} where {" + RendererTemplateModel.CODE + "} = ?code",
				Collections.singletonMap("code", template));
		if (result.getCount() < 1)
		{
			throw new TicketException("Could not find render template [" + template + "]");
		}

		return result.getResult().get(0);
	}
}
