/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.ticket.utils;

/**
 * Helper class for ticketing attachment media URL in cockpit and backoffice.
 */
public class AttachmentMediaUrlHelper
{
	private static final String TILDE = "~";
	private static final String FORWARD_SLASH = "/";

	private AttachmentMediaUrlHelper()
	{
	}

	/**
	 * Helper method to build media url for ZK link.
	 * This help to add a ~ if the url starts with /, which is for the non-secured media.
	 *
	 * @param url the given media url
	 * @return rebuilt url
	 */
	public static String urlHelper(final String url)
	{
		assert url != null;

		final StringBuilder sb = new StringBuilder(url);

		if (url.startsWith(FORWARD_SLASH))
		{
			sb.insert(0, TILDE);
		}

		return sb.toString();
	}
}
