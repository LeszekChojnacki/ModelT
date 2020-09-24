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
package de.hybris.platform.deeplink;

import de.hybris.platform.basecommerce.constants.BasecommerceConstants.DeeplinkUrlPropertyKeys;
import de.hybris.platform.util.Config;


/**
 * The Class DeeplinkUtils. Provides utilty methods for Deeplink URL feature.
 *
 *
 *
 */
public class DeeplinkUtils
{
	private static final String DEEPLINK_URL_PARAMETER_DEFAULT_NAME = "id";

	private DeeplinkUtils()
	{

	}

	public static String getDeeplinkParameterName()
	{
		return Config.getString(DeeplinkUrlPropertyKeys.DEEPLINK_URL_PARAMETER_NAME, DEEPLINK_URL_PARAMETER_DEFAULT_NAME);
	}
}
