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

import de.hybris.platform.comments.model.ComponentModel;
import de.hybris.platform.comments.model.DomainModel;
import de.hybris.platform.comments.services.CommentService;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.interceptor.InitDefaultsInterceptor;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.ticket.events.model.CsTicketEventModel;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import java.util.Calendar;


/**
 *
 */
public class CsTicketEventPrepareInterceptor implements PrepareInterceptor, InitDefaultsInterceptor
{
	private CommentService commentService;
	private UserService userService;

	private KeyGenerator keyGenerator;

	private String ticketSystemDomain = "ticketSystemDomain";
	private String ticketSystemComponent = "ticketSystem";

	@Override
	public void onPrepare(final Object model, final InterceptorContext ctx) throws InterceptorException
	{
		if (model instanceof CsTicketEventModel)
		{
			final CsTicketEventModel event = (CsTicketEventModel) model;
			if (event.getCode() == null)
			{
				event.setCode(keyGenerator.generate().toString());
			}
		}
	}

	@Override
	public void onInitDefaults(final Object model, final InterceptorContext ctx) throws InterceptorException
	{
		if (model instanceof CsTicketEventModel)
		{
			final CsTicketEventModel event = (CsTicketEventModel) model;
			final UserModel user = userService.getCurrentUser();
			final DomainModel domain = commentService.getDomainByCode(ticketSystemDomain);
			final ComponentModel component = commentService.getComponentByCode(domain, ticketSystemComponent);

			event.setAuthor(user);
			event.setComponent(component);

			event.setStartDateTime(Calendar.getInstance().getTime());
			event.setEndDateTime(Calendar.getInstance().getTime());

			event.setSubject(StringUtils.EMPTY);
		}
	}

	@Required
	public void setCommentService(final CommentService commentService)
	{
		this.commentService = commentService;
	}

	@Required
	public void setKeyGenerator(final KeyGenerator keyGenerator)
	{
		this.keyGenerator = keyGenerator;
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

}
