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
package com.hybris.backoffice.filter.responseheaders;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.filter.GenericFilterBean;


/**
 * Backoffice filter with the purpose to apply response headers to {@link javax.servlet.ServletResponse
 * servletResponse}.
 */
public class BackofficeResponseHeadersFilter extends GenericFilterBean
{
	private BackofficeResponseHeadersHandler backofficeResponseHeadersHandler;

	@Override
	public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain)
			throws IOException, ServletException
	{
		backofficeResponseHeadersHandler.handleResponseHeaders(servletRequest, servletResponse);

		filterChain.doFilter(servletRequest, servletResponse);
	}

	public BackofficeResponseHeadersHandler getBackofficeResponseHeadersHandler()
	{
		return backofficeResponseHeadersHandler;
	}

	@Required
	public void setBackofficeResponseHeadersHandler(final BackofficeResponseHeadersHandler backofficeResponseHeadersHandler)
	{
		this.backofficeResponseHeadersHandler = backofficeResponseHeadersHandler;
	}
}
