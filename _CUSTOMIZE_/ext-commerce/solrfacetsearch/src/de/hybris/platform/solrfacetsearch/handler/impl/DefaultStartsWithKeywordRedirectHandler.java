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

import org.springframework.beans.factory.annotation.Required;


public class DefaultStartsWithKeywordRedirectHandler implements KeywordRedirectHandler
{
	private I18NService i18NService;

	@Override
	public boolean keywordMatches(final String theQuery, final String keyword, final boolean ignoreCase)
	{
		if (ignoreCase)
		{
			final Locale currentLocale = i18NService.getCurrentLocale();
			return theQuery.toLowerCase(currentLocale).startsWith(keyword.toLowerCase(currentLocale));
		}

		return theQuery.startsWith(keyword);
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
