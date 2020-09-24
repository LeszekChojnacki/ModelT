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
package de.hybris.platform.security.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

import de.hybris.platform.security.XssEncodeService;

import java.io.UnsupportedEncodingException;

import com.sap.security.core.server.csi.XSSEncoder;


/**
 * Default implementation of {@link XssEncodeService}
 */
public class DefaultXssEncodeService implements XssEncodeService
{
	@Override
	public String encodeHtml(final String input)
	{
		validateParameterNotNull(input, "Input html can't be null");
		try
		{
			return XSSEncoder.encodeHTML(input);
		}
		catch (final UnsupportedEncodingException e)
		{
			throw new IllegalArgumentException("cannot encode input string due to unsupported encoding:" + e.getMessage(), e);
		}
	}
}
