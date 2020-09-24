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
package com.hybris.backoffice.mobile.filter;

import de.hybris.platform.util.Config;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Filters user-agent header for mobile devices. In case a mobile device is using the application the user-agent should
 * be changed to a non-mobile application closest to the used (for example iPad -> macintosh, android -> chrome). Use
 * <i>mobile.user.agent.detection.enabled=true|false<i/> to enable/disable the filter. By default the filter is enabled.
 */
public class BackofficeMobileFilter implements Filter
{
	// Rendered with "mobile" - rewrite needed
	// mozilla/5.0 (iphone; u; cpu iphone os 3_0 like mac os x; en-us) applewebkit/528.18 (khtml,
	// like gecko) version/4.0 mobile/7a341 safari/528.16
	// mozilla/5.0 (linux; u; android 3.0.1; en-us; gt-p7100 build/hri83) applewebkit/534.13 (khtml,
	// like gecko) version/4.0 safari/534.13

	// Rendered with the default - ok
	// mozilla/5.0 (macintosh; intel mac os x 10.9; rv:29.0) gecko/20100101 firefox/29.0
	// mozilla/5.0 (windows nt 6.1; wow64; rv:29.0) gecko/20100101 firefox/29.0

	public static final String MOBILE_USER_AGENT_DETECTION_ENABLED = "mobile.user.agent.detection.enabled";
	public static final String USER_AGENT_HTTP_HEADER = "user-agent";
	public static final String MACINTOSH = "macintosh";
	public static final String CHROME = "chrome";
	private static final Pattern apple = Pattern.compile("(ipad|ipod|iphone)", Pattern.CASE_INSENSITIVE);
	private static final Pattern android = Pattern.compile("android", Pattern.CASE_INSENSITIVE);
	private static final Logger LOG = LoggerFactory.getLogger(BackofficeMobileFilter.class);

	@Override
	public void doFilter(final ServletRequest req, final ServletResponse resp, final FilterChain chain)
			throws ServletException, IOException
	{
		chain.doFilter(new HttpServletRequestWrapper((HttpServletRequest) req)
		{
			private final ServletContext servletContext = req.getServletContext();

			@Override
			public String getHeader(final String name)
			{
				if (Config.getBoolean(MOBILE_USER_AGENT_DETECTION_ENABLED, true) && USER_AGENT_HTTP_HEADER.equalsIgnoreCase(name))
				{
					return prepareUserAgentHeader((HttpServletRequest) req);
				}
				return super.getHeader(name);
			}

			@Override
			public ServletContext getServletContext()
			{
				try
				{
					return super.getServletContext();
				}
				catch (final NullPointerException e)
				{
					if (LOG.isDebugEnabled())
					{
						LOG.debug("Cannot return servlet context from parent. Use fallback context.", e);
					}
					// TSB-2935 Avoid NullPointer in background thread after parent thread's request is recycled.
					return servletContext;
				}
			}
		}, resp);
	}

	protected String prepareUserAgentHeader(final HttpServletRequest req)
	{
		final String ua = req.getHeader(USER_AGENT_HTTP_HEADER);
		if (StringUtils.isNotBlank(ua))
		{
			final Matcher appleMatcher = apple.matcher(ua);
			final String result = appleMatcher.replaceAll(MACINTOSH);

			final Matcher androidMatcher = android.matcher(result);
			return androidMatcher.replaceAll(CHROME);
		}
		return "";
	}

	@Override
	public void destroy()
	{
		// NOP
	}

	@Override
	public void init(final FilterConfig config)
	{
		// NOP
	}

}
