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
package de.hybris.platform.storelocator.impl;

import de.hybris.platform.storelocator.data.RouteData;
import de.hybris.platform.storelocator.exception.GeoServiceWrapperException;

import java.io.IOException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseExtractor;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;


/**
 * Parses the response xml document from the google directions web service into {@link RouteData} object.
 */
public class RouteDataGoogleResponseParser implements ResponseExtractor<RouteData> // NOSONAR
{
	@Override
	public RouteData extractData(final ClientHttpResponse response) throws IOException
	{
		final RouteData routeData = new RouteData();
		final XPath xpath = XPathFactory.newInstance().newXPath();
		final InputSource inputSource = new InputSource(response.getBody());
		try
		{
			final Node root = (Node) xpath.evaluate("/", inputSource, XPathConstants.NODE);
			final String status = xpath.evaluate("/DirectionsResponse/status", root);
			if (!"OK".equalsIgnoreCase(status))
			{
				throw new GeoServiceWrapperException("Could not get directions : ", status);
			}
			routeData.setCoordinates(xpath.evaluate("/DirectionsResponse/route/overview_polyline/points", root));
			routeData.setDuration(Double.parseDouble(xpath.evaluate("/DirectionsResponse/route/leg/duration/value", root)));
			routeData.setDurationText(xpath.evaluate("/DirectionsResponse/route/leg/duration/text", root));
			routeData.setDistance(Double.parseDouble(xpath.evaluate("/DirectionsResponse/route/leg/distance/value", root)));
			routeData.setDistanceText(xpath.evaluate("/DirectionsResponse/route/leg/distance/text", root));

		}
		catch (final XPathExpressionException e)
		{
			throw new GeoServiceWrapperException("Cannot get Google response due to :", e);
		}
		return routeData;
	}

}
