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
package com.hybris.backoffice.spring.security;

import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.jalo.user.LoginToken;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.spring.security.CoreRememberMeService;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.core.userdetails.UserDetails;


/**
 * This class extends {@link CoreRememberMeService}, to allow setting correct locale in ZK session, when the user logs
 * in using the remember-me functionality.<br/>
 */
public class BackofficeRememberMeService extends CoreRememberMeService
{

	protected static final String ORG_ZKOSS_WEB_PREFERRED_LOCALE = "org.zkoss.web.preferred.locale";

	private CommonI18NService commonI18NService;

	@Override
	public UserDetails processAutoLoginCookie(final LoginToken token, final HttpServletRequest request,
			final HttpServletResponse response)
	{
		final UserDetails userDetails = super.processAutoLoginCookie(token, request, response);

		final LanguageModel currentLanguage = commonI18NService.getCurrentLanguage();
		final Locale locale = Locale.forLanguageTag(currentLanguage.getIsocode());
		request.getSession().setAttribute(ORG_ZKOSS_WEB_PREFERRED_LOCALE, locale);

		return userDetails;
	}

	@Required
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}
}
