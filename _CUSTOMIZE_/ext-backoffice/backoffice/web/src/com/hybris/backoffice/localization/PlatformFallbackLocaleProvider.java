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
package com.hybris.backoffice.localization;


import de.hybris.platform.servicelayer.i18n.I18NService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Lists;
import com.hybris.cockpitng.i18n.FallbackLocaleProvider;


public class PlatformFallbackLocaleProvider implements FallbackLocaleProvider
{
	private static final Logger LOG = LoggerFactory.getLogger(PlatformFallbackLocaleProvider.class);
	private final HashMap<Locale, List<Locale>> localeMap = new HashMap<>();
	private I18NService i18NService;

	@Override
	public List<Locale> getFallbackLocales(final Locale locale)
	{
		List<Locale> locales = localeMap.get(locale);
		if (CollectionUtils.isEmpty(locales))
		{

			final Locale[] fallbackLocales = getI18NService().getFallbackLocales(locale);
			if (fallbackLocales != null)
			{
				locales = new ArrayList<>(Arrays.asList(getI18NService().getFallbackLocales(locale)));
				localeMap.put(locale, locales);
			}
			else
			{
				LOG.warn("Cannot find fallback locale for selected language:{}. Loading {}", locale, Locale.ENGLISH);

				locales = Lists.newArrayList(Locale.ENGLISH);
				localeMap.put(locale, locales);
			}
		}
		return Collections.unmodifiableList(locales);
	}

	public I18NService getI18NService()
	{
		return i18NService;
	}

	@Required
	public void setI18NService(final I18NService i18NService)
	{
		this.i18NService = i18NService;
	}
}
