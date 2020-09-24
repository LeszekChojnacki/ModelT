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
package de.hybris.platform.commerceservices.backoffice.widgets.organization.util;

import de.hybris.platform.commerceservices.model.OrgUnitModel;

import org.apache.commons.lang3.StringUtils;

import com.hybris.cockpitng.actions.ActionContext;


/**
 * @deprecated since 18.11
 */
@Deprecated
public final class OrgUnitUtils
{
	public static final String SETTING_NOTIFICATION_SOURCE = "notificationSource";

	private OrgUnitUtils()
	{
		// private constructor to avoid instantiation
	}

	public static String getNotificationSource(final ActionContext<OrgUnitModel> ctx)
	{
		final String parameter = (String) ctx.getParameter(SETTING_NOTIFICATION_SOURCE);
		return StringUtils.isNotBlank(parameter) ? parameter : ctx.getCode();
	}
}
