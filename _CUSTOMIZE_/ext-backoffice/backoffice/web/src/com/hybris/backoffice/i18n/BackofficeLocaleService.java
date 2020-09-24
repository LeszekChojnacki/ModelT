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
package com.hybris.backoffice.i18n;

import de.hybris.platform.servicelayer.i18n.I18NService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.cockpitng.i18n.impl.DefaultCockpitLocaleService;


/**
 * Manages the UI locale. Uses the ZK locale and the on-premise i18nService locale as well.
 */
public class BackofficeLocaleService extends DefaultCockpitLocaleService
{
	private I18NService i18nService;

	@Override
	public List<Locale> getAllLocales()
	{
		return new ArrayList<Locale>(i18nService.getSupportedLocales());
	}

	@Override
	public void setCurrentLocale(final Locale locale)
	{
		super.setCurrentLocale(locale);
		this.i18nService.setCurrentLocale(locale);
	}


	@Required
	public void setI18nService(final I18NService i18nService)
	{
		this.i18nService = i18nService;
	}

}
