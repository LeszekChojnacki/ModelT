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

import de.hybris.platform.comments.model.CommentAttachmentModel;
import de.hybris.platform.comments.model.CommentTypeModel;
import de.hybris.platform.commons.renderer.RendererService;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.ticket.email.context.AbstractTicketContext;
import de.hybris.platform.ticket.enums.CsEmailRecipients;
import de.hybris.platform.ticket.enums.CsInterventionType;
import de.hybris.platform.ticket.events.model.CsTicketChangeEventEntryModel;
import de.hybris.platform.ticket.events.model.CsTicketEmailModel;
import de.hybris.platform.ticket.events.model.CsTicketEventModel;
import de.hybris.platform.ticket.events.model.CsTicketResolutionEventModel;
import de.hybris.platform.ticket.model.CsTicketEventEmailConfigurationModel;
import de.hybris.platform.ticket.model.CsTicketModel;
import de.hybris.platform.ticket.strategies.TicketEventEmailStrategy;
import de.hybris.platform.util.mail.MailUtils;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Maps;

/**
 * Default implementation of {@link TicketEventEmailStrategy}
 */
public class DefaultTicketEventEmailStrategy implements TicketEventEmailStrategy
{
	private static final String NO_EMAIL_EVENTS_FOUND = "No email events found for type [%s]";

	private static final Logger LOG = Logger.getLogger(DefaultTicketEventEmailStrategy.class);

	private FlexibleSearchService flexibleSearch;
	private MediaService mediaService;
	private RendererService rendererService;
	private ModelService modelService;
	private Map<CsEmailRecipients, String> recipientTypeToContextClassMap = new EnumMap<>(CsEmailRecipients.class);

	@Override
	public void sendEmailsForEvent(final CsTicketModel ticket, final CsTicketEventModel event)
	{
		CsEmailRecipients recepientType = null;
		if (event instanceof CsTicketResolutionEventModel
				&& CsInterventionType.PRIVATE.equals(((CsTicketResolutionEventModel) event).getInterventionType()))
		{
			recepientType = CsEmailRecipients.ASSIGNEDAGENT;
		}
		final List<CsTicketEventEmailConfigurationModel> filteredConfigurations = getApplicableConfigs(event, recepientType);
		if (filteredConfigurations.isEmpty())
		{
			LOG.info(String.format(NO_EMAIL_EVENTS_FOUND, getTicketEventCommentTypeString(event)));
			return;
		}

		final String originalText = event.getText();
		for (final CsTicketEventEmailConfigurationModel config : filteredConfigurations)
		{
			final AbstractTicketContext ticketContext = createContextForEvent(config, ticket, event);
			if (ticketContext != null)
			{
				final CsTicketEmailModel email = constructAndSendEmail(ticketContext, config);
				if (email != null)
				{
					final List<CsTicketEmailModel> emails = new ArrayList<>();
					emails.addAll(event.getEmails());
					emails.add(email);
					event.setEmails(emails);
				}
			}
			event.setText(originalText);
		}

		getModelService().save(event);
	}

	@Override
	public void sendEmailsForAssignAgentTicketEvent(final CsTicketModel ticket, final CsTicketEventModel event,
			final CsEmailRecipients recepientType)
	{
		final List<CsTicketEventEmailConfigurationModel> filteredConfigurations = getApplicableConfigs(event, recepientType);
		if (filteredConfigurations.isEmpty())
		{
			LOG.info(String.format(NO_EMAIL_EVENTS_FOUND, getTicketEventCommentTypeString(event)));
			return;
		}

		for (final CsTicketEventEmailConfigurationModel config : filteredConfigurations)
		{
			final AbstractTicketContext ticketContext = createContextForEvent(config, ticket, event);
			if (ticketContext != null)
			{
				final CsTicketEmailModel email = constructAndSendEmail(ticketContext, config);
				if (email != null)
				{
					final List<CsTicketEmailModel> emails = new ArrayList<>();
					emails.addAll(event.getEmails());
					emails.add(email);
					event.setEmails(emails);
				}
			}
		}

		getModelService().save(event);
	}

	@Required
	public void setFlexibleSearch(final FlexibleSearchService flexibleSearch)
	{
		this.flexibleSearch = flexibleSearch;
	}

	@Required
	public void setMediaService(final MediaService mediaService)
	{
		this.mediaService = mediaService;
	}

	@Required
	public void setRecipientTypeToContextClassMap(final Map<CsEmailRecipients, String> recipientTypeToContextClassMap)
	{
		this.recipientTypeToContextClassMap = recipientTypeToContextClassMap;
	}

	@Required
	public void setRendererService(final RendererService rendererService)
	{
		this.rendererService = rendererService;
	}

	protected CsTicketEmailModel constructAndSendEmail(final AbstractTicketContext ticketContext,
			final CsTicketEventEmailConfigurationModel config)
	{
		try
		{
			if (ticketContext.getTo() == null)
			{
				LOG.warn("Could not send email for event [" + ticketContext.getEvent() + "]. With config [" + config
						+ "] No recipient could be found.");
				return null;
			}

			//Create mail instance using commons-mail and the hybris MailUtility class.
			final HtmlEmail email = (HtmlEmail) getPreConfiguredEmail(); //creates a mail instance with set mail properties read from project.properties
			setMailEncoding(email, "UTF-8");

			final VelocityContext ctx = new VelocityContext();
			ctx.put("ctx", ticketContext);
			final StringWriter subj = new StringWriter();
			Velocity.evaluate(ctx, subj, "logtag", new StringReader(config.getSubject()));
			email.setSubject(subj.toString());
			email.addTo(ticketContext.getTo());

			final StringWriter htmlVersion = new StringWriter();
			// Create the HTML version of the email from the template
			rendererService.render(config.getHtmlTemplate(), ticketContext, htmlVersion);
			email.setHtmlMsg(htmlVersion.toString());

			final StringWriter textVersion = new StringWriter();
			if (config.getPlainTextTemplate() != null)
			{
				// Create the plain text version of the email from the template
				rendererService.render(config.getPlainTextTemplate(), ticketContext, textVersion);
				email.setTextMsg(textVersion.toString());
			}

			final Collection<CommentAttachmentModel> attachments = ticketContext.getAttachments();
			attachMediaToMail(email, attachments);

			// Send the email and capture the message ID
			final CsTicketEmailModel storedEmail = getModelService().create(CsTicketEmailModel.class);
			storedEmail.setTo(ticketContext.getTo());
			storedEmail.setFrom(email.getFromAddress().toString());
			storedEmail.setSubject(email.getSubject());
			storedEmail.setBody(textVersion.toString() + System.getProperty("line.separator") + htmlVersion.toString());
			final String messageID = email.send();
			storedEmail.setMessageId(messageID);
			return storedEmail;
		}
		catch (final EmailException e)
		{
			LOG.error("Error sending email to [" + config.getRecipientType() + "]. Context was [" + ticketContext + "]", e);
		}

		return null;
	}

	private void setMailEncoding(final HtmlEmail email, final String encoding)
	{
		try
		{
			email.setCharset(encoding);
		}
		catch (final IllegalArgumentException iae)
		{
			LOG.error(String.format("Setting charset to '%s' failed.", encoding), iae);
		}
	}

	private void attachMediaToMail(final HtmlEmail email, final Collection<CommentAttachmentModel> attachments) throws EmailException
	{
		if (attachments == null || attachments.isEmpty())
		{
			return;
		}

		for (final CommentAttachmentModel attachment : attachments)
		{
			if (!(attachment.getItem() instanceof MediaModel))
			{
				continue;
			}
			try
			{
				final MediaModel mediaAttachment = (MediaModel) attachment.getItem();
				final DataSource dataSource = new ByteArrayDataSource(mediaService.getStreamFromMedia(mediaAttachment),
						mediaAttachment.getMime());
				email.attach(dataSource, mediaAttachment.getRealFileName(), mediaAttachment.getDescription());
			}
			catch (final IOException ex)
			{
				LOG.error("Failed to load attachment data into data source [" + attachment + "]", ex);
			}
		}
	}


	protected AbstractTicketContext createContextForEvent(final CsTicketEventEmailConfigurationModel config,
			final CsTicketModel ticket, final CsTicketEventModel event)
	{
		final String contextClassName = recipientTypeToContextClassMap.get(config.getRecipientType());

		try
		{
			final Class contextClass = Class.forName(contextClassName);
			final Constructor constructor = contextClass.getConstructor(CsTicketModel.class, CsTicketEventModel.class);
			final AbstractTicketContext ticketContext = (AbstractTicketContext) constructor.newInstance(ticket, event);

			// add changes
			final StringBuilder text = new StringBuilder();
			for (final CsTicketChangeEventEntryModel e : event.getEntries())
			{
				text.append(e.getAlteredAttribute().getName() + ": " + e.getOldStringValue() + " -> " + e.getNewStringValue() + "\n");
			}
			text.append("\n").append(event.getText());
			event.setText(text.toString());

			return ticketContext;
		}
		catch (final ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException
				| NoSuchMethodException e)
		{
			LOG.error("Error finding context class for email target [" + config.getRecipientType() + "]. Context class was ["
					+ contextClassName + "]", e);
		}
		return null;
	}

	protected List<CsTicketEventEmailConfigurationModel> getApplicableConfigs(final CsTicketEventModel event,
			final CsEmailRecipients recepientType)
	{
		// need to consider each class of user when sending emails.
		SearchResult<CsTicketEventEmailConfigurationModel> result = null;
		List<CsTicketEventEmailConfigurationModel> configurations = null;
		if (recepientType != null)
		{
			final Map<String, Object> parameters = Maps.newHashMap();
			parameters.put("eventType", event.getCommentType());
			parameters.put("recipientType", recepientType);

			result = flexibleSearch.search("SELECT {pk} FROM {" + CsTicketEventEmailConfigurationModel._TYPECODE + "} WHERE {"
					+ CsTicketEventEmailConfigurationModel.EVENTTYPE + "} = ?eventType AND {"
					+ CsTicketEventEmailConfigurationModel.RECIPIENTTYPE + "} = ?recipientType", parameters);
		}
		else
		{
			result = flexibleSearch.search(
					"SELECT {pk} FROM {" + CsTicketEventEmailConfigurationModel._TYPECODE + "} WHERE {"
							+ CsTicketEventEmailConfigurationModel.EVENTTYPE + "} = ?eventType",
					Collections.singletonMap("eventType", event.getCommentType()));
		}
		configurations = result.getResult();
		if (configurations.isEmpty())
		{
			LOG.info(String.format(NO_EMAIL_EVENTS_FOUND, getTicketEventCommentTypeString(event)));
			return Collections.emptyList();
		}

		List<CsTicketEventEmailConfigurationModel> filteredConfigurations = null;

		filteredConfigurations = new ArrayList<>();
		final List<AttributeDescriptorModel> attributes = new ArrayList<>();
		for (final CsTicketChangeEventEntryModel entry : event.getEntries())
		{
			attributes.add(entry.getAlteredAttribute());
		}

		for (final CsTicketEventEmailConfigurationModel config : configurations)
		{
			if (!config.getAlteredAttributes().isEmpty())
			{
				if (!CollectionUtils.intersection(attributes, config.getAlteredAttributes()).isEmpty())
				{
					filteredConfigurations.add(config);
				}
				else
				{
					LOG.debug("configuration [" + config
							+ "] was filtered out as none of the changed attributes met its required attributes");
				}
			}
			else
			{
				filteredConfigurations.add(config);
			}
		}

		return filteredConfigurations;
	}

	protected Email getPreConfiguredEmail() throws EmailException
	{
		return MailUtils.getPreConfiguredEmail();
	}

	protected String getTicketEventCommentTypeString(final CsTicketEventModel event)
	{
		if (event == null)
		{
			return "CsTicketEvent is NULL";
		}
		else
		{
			final CommentTypeModel commentType = event.getCommentType();
			if (commentType == null)
			{
				return "CommentType is NULL for [" + event + "]";
			}
			else
			{
				return commentType.getCode();
			}
		}
	}
	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(ModelService modelService)
	{
		this.modelService = modelService;
	}
}
