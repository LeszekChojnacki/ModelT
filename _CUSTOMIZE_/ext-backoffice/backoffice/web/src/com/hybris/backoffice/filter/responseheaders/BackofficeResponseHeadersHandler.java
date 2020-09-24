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

import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.cockpitng.core.util.CockpitProperties;


/**
 * Handles processing {@link javax.servlet.ServletRequest servletRequest} and {@link javax.servlet.ServletResponse
 * servletResponse} in order to apply response headers in <code>servletResponse</code> basing on the
 * {@link com.hybris.cockpitng.core.util.CockpitProperties cockpitProperties}. Example configuration is available
 * through <code>cockpitProperties</code>: <code>backoffice.response.header.X-Frame-Options=SAMEORIGIN</code>.
 */
public class BackofficeResponseHeadersHandler
{
	private static final String RESPONSE_HEADER_PREFIX = "response.header.";
	private static final int RESPONSE_HEADER_PREFIX_LENGTH = RESPONSE_HEADER_PREFIX.length();

	private CockpitProperties cockpitProperties;

	/**
	 * Applies response headers in <code>servletResponse</code> basing on the <code>CockpitProperties</code>.
	 *
	 * @param servletRequest
	 *           - servletRequest.
	 * @param servletResponse
	 *           - servletResponse.
	 */
	public void handleResponseHeaders(final ServletRequest servletRequest, final ServletResponse servletResponse)
	{
		final Map<String, String> responseHeaders = resolveResponseHeaders();
		applyResponseHeaders(responseHeaders, (HttpServletResponse) servletResponse);
	}

	protected Map<String, String> resolveResponseHeaders()
	{
		final Map<String, String> headers = cockpitProperties.getProperties().keySet() //
				.stream().filter(this::isPropertyWithHeader)//
				.collect(Collectors.toMap(property -> property.substring(RESPONSE_HEADER_PREFIX_LENGTH),
						cockpitProperties::getProperty)); //
		return headers;
	}

	protected boolean isPropertyWithHeader(final String property)
	{
		return cockpitProperties.getProperty(property) != null && //
				property.startsWith(RESPONSE_HEADER_PREFIX) && //
				property.length() > RESPONSE_HEADER_PREFIX_LENGTH;
	}

	protected void applyResponseHeaders(final Map<String, String> responseHeaders, final HttpServletResponse servletResponse)
	{
		responseHeaders.keySet().forEach(header -> servletResponse.setHeader(header, responseHeaders.get(header)));
	}

	public CockpitProperties getCockpitProperties()
	{
		return cockpitProperties;
	}

	@Required
	public void setCockpitProperties(final CockpitProperties cockpitProperties)
	{
		this.cockpitProperties = cockpitProperties;
	}
}
