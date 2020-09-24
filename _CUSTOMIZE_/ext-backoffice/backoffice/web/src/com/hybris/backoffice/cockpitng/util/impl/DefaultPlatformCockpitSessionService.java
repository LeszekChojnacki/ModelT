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
package com.hybris.backoffice.cockpitng.util.impl;

import de.hybris.platform.jalo.user.UserManager;
import de.hybris.platform.servicelayer.session.SessionService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;

import com.hybris.cockpitng.util.impl.DefaultCockpitSessionService;


/**
 * Enhanced version of {@link DefaultCockpitSessionService} that does proper hybris session logout handling.
 */
public class DefaultPlatformCockpitSessionService extends DefaultCockpitSessionService
{
	private transient SessionService sessionService;

	@Override
	public void logout()
	{
		deleteLoginTokenCookie();
		sessionService.closeCurrentSession();
		super.logout();
	}

	private void deleteLoginTokenCookie()
	{
		final Execution execution = Executions.getCurrent();
		final HttpServletRequest request = (HttpServletRequest) execution.getNativeRequest();
		final HttpServletResponse response = (HttpServletResponse) execution.getNativeResponse();
		UserManager.getInstance().deleteLoginTokenCookie(request, response);
	}

	@Required
	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
	}
}
