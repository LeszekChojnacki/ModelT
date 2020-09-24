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

import de.hybris.platform.storelocator.data.MapLocationData;
import de.hybris.platform.storelocator.exception.GeoDocumentParsingException;

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
 * Implementation of {@link ResponseExtractor} that parses google geo-location response document into a
 * {@link MapLocationData} object. It uses a first win strategy, so the first placemark in the response is parsed.
 */
@SuppressWarnings("deprecation")
public class FirstPlacemarkWinsGoogleResponseParser implements ResponseExtractor<MapLocationData> // NOSONAR
{
	@Override
	public MapLocationData extractData(final ClientHttpResponse response) throws IOException
	{
		final MapLocationData locationData = new MapLocationData();
		final XPath xpath = XPathFactory.newInstance().newXPath();
		final InputSource inputSource = new InputSource(response.getBody());
		try
		{
			final Node root = (Node) xpath.evaluate("/", inputSource, XPathConstants.NODE);

			final String lat = xpath.evaluate("/GeocodeResponse/result[1]/geometry/location/lat", root);
			final String longitude = xpath.evaluate("/GeocodeResponse/result[1]/geometry/location/lng", root);
			locationData.setLatitude(lat);
			locationData.setLongitude(longitude);
		}
		catch (final XPathExpressionException e)
		{
			throw new GeoDocumentParsingException(e.getMessage(), e);
		}
		return locationData;
	}
}
