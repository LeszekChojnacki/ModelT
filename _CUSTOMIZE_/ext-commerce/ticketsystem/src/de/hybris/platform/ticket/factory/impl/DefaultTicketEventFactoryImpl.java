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
package de.hybris.platform.ticket.factory.impl;

import de.hybris.platform.comments.model.CommentTypeModel;
import de.hybris.platform.comments.model.ComponentModel;
import de.hybris.platform.comments.model.DomainModel;
import de.hybris.platform.comments.services.CommentService;
import de.hybris.platform.core.Tenant;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.ticket.events.model.CsTicketEventModel;
import de.hybris.platform.ticket.factory.TicketEventFactory;

import java.util.Calendar;
import org.springframework.transaction.PlatformTransactionManager;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 *
 */
public class DefaultTicketEventFactoryImpl implements TicketEventFactory
{
	private CommentService commentService;
	private UserService userService;
	private ModelService modelService;
	private Tenant currentTenant;
	private String ticketSystemDomain = "ticketSystemDomain";
	private String ticketSystemComponent = "ticketSystem";
	private String defaultCommentType = "customerNote";
	protected SessionService sessionService;
	/**
	 * @deprecated since 6.7
	 */
	@Deprecated
	protected PlatformTransactionManager txManager;

	@Override
	public CsTicketEventModel createEvent(final String type)
	{
		final UserModel user = userService.getCurrentUser();
		final DomainModel domain = commentService.getDomainForCode(ticketSystemDomain);
		final ComponentModel component = commentService.getComponentForCode(domain, ticketSystemComponent);
		final CommentTypeModel commentType = commentService.getCommentTypeForCode(component,
				StringUtils.isNotEmpty(type) ? type : defaultCommentType);

		final CsTicketEventModel event = getModelService().create(commentType.getMetaType().getCode());

		event.setAuthor(user);
		event.setComponent(component);
		event.setCommentType(commentType);

		event.setStartDateTime(Calendar.getInstance().getTime());
		event.setEndDateTime(Calendar.getInstance().getTime());

		event.setSubject(StringUtils.EMPTY);

		return event;
	}

	@Override
	public CsTicketEventModel ensureTicketSetup(final CsTicketEventModel event, final String type)
	{
		final UserModel user = userService.getCurrentUser();
		final DomainModel domain = commentService.getDomainForCode(ticketSystemDomain);
		final ComponentModel component = commentService.getComponentForCode(domain, ticketSystemComponent);
		final CommentTypeModel commentType = commentService.getCommentTypeForCode(component,
				StringUtils.isNotEmpty(type) ? type : defaultCommentType);

		if (event.getAuthor() == null)
		{
			event.setAuthor(user);
		}
		if (event.getComponent() == null)
		{
			event.setComponent(component);
		}
		if (event.getCommentType() == null)
		{
			event.setCommentType(commentType);
		}

		if (event.getStartDateTime() == null)
		{
			event.setStartDateTime(Calendar.getInstance().getTime());
		}
		if (event.getEndDateTime() == null)
		{
			event.setEndDateTime(Calendar.getInstance().getTime());
		}

		if (event.getSubject() == null)
		{
			event.setSubject(StringUtils.EMPTY);
		}

		return event;
	}

	@Required
	public void setCommentService(final CommentService commentService)
	{
		this.commentService = commentService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	public void setTicketSystemDomain(final String ticketSystemDomain)
	{
		this.ticketSystemDomain = ticketSystemDomain;
	}

	public void setTicketSystemComponent(final String ticketSystemComponent)
	{
		this.ticketSystemComponent = ticketSystemComponent;
	}

	public void setDefaultCommentType(final String defaultCommentType)
	{
		this.defaultCommentType = defaultCommentType;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	public void setCurrentTenant(final Tenant currentTenant)
	{
		this.currentTenant = currentTenant;
	}

	protected Tenant getCurrentTenant()
	{
		return currentTenant;
	}

	@Required
	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
	}

	protected SessionService getSessionService()
	{
		return this.sessionService;
	}
	
	@Required
	public void setTxManager(final PlatformTransactionManager txManager)
	{
		this.txManager = txManager;
	}

	protected PlatformTransactionManager getTxManager()
	{
		return this.txManager;
	}
}
