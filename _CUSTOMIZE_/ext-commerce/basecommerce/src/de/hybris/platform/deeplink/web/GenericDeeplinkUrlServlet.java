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
package de.hybris.platform.deeplink.web;

import de.hybris.platform.core.Registry;
import de.hybris.platform.deeplink.DeeplinkUtils;
import de.hybris.platform.deeplink.services.DeeplinkUrlService;
import de.hybris.platform.deeplink.services.DeeplinkUrlService.LongUrlInfo;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.validator.GenericValidator;
import org.apache.log4j.Logger;
import org.springframework.web.bind.ServletRequestUtils;


/**
 * The Class GenericDeeplinkUrlServlet. Servlet responsible for translating URLs from barcodes and doing redirection (or
 * forwarding) to destination URL based on configuration taken from {@code DeeplinkUrlRule}.
 *
 *
 *
 */
public class GenericDeeplinkUrlServlet extends HttpServlet
{
	private static final Logger LOG = Logger.getLogger(GenericDeeplinkUrlServlet.class);

	@SuppressWarnings("findsecbugs:UNVALIDATED_REDIRECT")
	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
	{
		final String barcodeToken = ServletRequestUtils.getStringParameter(req, DeeplinkUtils.getDeeplinkParameterName());
		if (GenericValidator.isBlankOrNull(barcodeToken))
		{
			return;
		}

		final LongUrlInfo generatedUrl = getDeeplinkUrlService().generateUrl(barcodeToken);

		if (generatedUrl != null)
		{
			if (generatedUrl.isUseForward())
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug("Forward was triggered with destination address: " + generatedUrl.getUrl());
				}
				final RequestDispatcher requestDispatcher = req.getRequestDispatcher(generatedUrl.getUrl());
				requestDispatcher.forward(req, resp);
			}
			else
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug("Redirect was triggered with destination address: " + generatedUrl.getUrl());
				}
				resp.sendRedirect(generatedUrl.getUrl());
			}
		}
		else
		{
			LOG.info("There was no generated URL to use");
		}

	}

	/**
	 * Gets the deeplink url service.
	 *
	 * @return the deeplink url service
	 */
	protected DeeplinkUrlService getDeeplinkUrlService()
	{
		return (DeeplinkUrlService) Registry.getApplicationContext().getBean("deeplinkUrlService");
	}

}
