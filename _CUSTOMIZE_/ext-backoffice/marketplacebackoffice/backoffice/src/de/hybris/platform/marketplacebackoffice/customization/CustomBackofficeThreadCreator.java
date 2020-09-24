/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.marketplacebackoffice.customization;

import de.hybris.platform.servicelayer.session.SessionService;

import java.util.Map;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.cockpitng.util.BackofficeThreadContextCreator;


public class CustomBackofficeThreadCreator extends BackofficeThreadContextCreator
{
	private SessionService sessionService;

	@Override
	public Map<String, Object> createThreadContext()
	{
		final Map<String, Object> ctx = super.createThreadContext();
		ctx.putIfAbsent("vendorCategories", getSessionService().getAttribute("vendorCategories"));//NOSONAR
		ctx.putIfAbsent("productCarouselComponents", getSessionService().getAttribute("productCarouselComponents"));//NOSONAR
		return ctx;
	}

	@Override
	public void initThreadContext(final Map<String, Object> ctx)
	{
		if (ctx != null)
		{
			super.initThreadContext(ctx);
			getSessionService().setAttribute("vendorCategories", ctx.get("vendorCategories"));
			getSessionService().setAttribute("productCarouselComponents", ctx.get("productCarouselComponents"));
		}
	}

	@Required
	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
	}

	protected SessionService getSessionService()
	{
		return sessionService;
	}

}
