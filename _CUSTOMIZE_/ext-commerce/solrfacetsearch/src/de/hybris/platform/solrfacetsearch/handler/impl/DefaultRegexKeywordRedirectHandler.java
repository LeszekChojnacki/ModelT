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
package de.hybris.platform.solrfacetsearch.handler.impl;

import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.solrfacetsearch.handler.KeywordRedirectHandler;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


public class DefaultRegexKeywordRedirectHandler implements KeywordRedirectHandler
{
	private static final Logger LOG = Logger.getLogger(DefaultRegexKeywordRedirectHandler.class);

	private I18NService i18NService;

	private final Map<String, Pattern> precompiledPatterns = new ConcurrentHashMap<>();

	@Override
	public boolean keywordMatches(final String theQuery, final String keyword, final boolean ignoreCase)
	{
		try
		{
			final Locale currentLocale = i18NService.getCurrentLocale();
			return prepareRegexMatcher(ignoreCase ? theQuery.toLowerCase(currentLocale) : theQuery, keyword).matches();
		}
		catch (final PatternSyntaxException e)
		{
			LOG.warn("Illegal pattern provided: " + keyword, e);
			return false;
		}

	}

	protected Matcher prepareRegexMatcher(final String theQuery, final String keyToCompile)
	{
		Pattern pattern = precompiledPatterns.get(keyToCompile);
		if (pattern == null)
		{
			pattern = Pattern.compile(keyToCompile);
			precompiledPatterns.put(keyToCompile, pattern);
		}

		return pattern.matcher(theQuery);
	}

	public I18NService getI18NService()
	{
		return i18NService;
	}

	@Required
	public void setI18NService(final I18NService i18nService)
	{
		i18NService = i18nService;
	}
}
