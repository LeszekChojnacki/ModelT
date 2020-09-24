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
package de.hybris.platform.storelocator.route.impl;

import com.google.common.base.Joiner;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import de.hybris.platform.servicelayer.util.ServicesUtil;
import de.hybris.platform.storelocator.GPS;
import de.hybris.platform.storelocator.constants.GeolocationConstants;
import de.hybris.platform.storelocator.data.AddressData;
import de.hybris.platform.storelocator.exception.GeoServiceWrapperException;
import de.hybris.platform.storelocator.route.GeolocationDirectionsUrlBuilder;
import org.apache.commons.lang.StringUtils;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;


/**
 * Builds GET directions URL according to google maps API.
 */
public class DefaultGeolocationDirectionsUrlBuilder implements GeolocationDirectionsUrlBuilder
{

	private static final String DEFAULT_RESPONSE_TYPE = "xml";
	private static final String DEFAULT_MODE = "driving";
	private static final String SEPARATOR = "+";
	private String responseType;
	private boolean sensor = true;
	private String mode;

	@Override
	public String getWebServiceUrl(final String baseUrl, final GPS start, final GPS destination, final Map params)
	{
		ServicesUtil.validateParameterNotNull(baseUrl, "Base Url cannot be null");
		ServicesUtil.validateParameterNotNull(start, "START cannot be null");
		ServicesUtil.validateParameterNotNull(destination, "DESTINATION cannot be null");
		final StringBuilder result = new StringBuilder(baseUrl);
		result.append("/api/directions/").append(getResponseType()).append("?origin=").append(formatCoordinates(start))
				.append("&destination=").append(formatCoordinates(destination)).append("&sensor=").append(isSensor()).append("&mode=")
				.append(getMode());

		return result.toString();
	}

	@Override
	public String getWebServiceUrl(final String baseUrl, final AddressData startAddress, final AddressData destinationAddress,
			final Map params)
	{

		ServicesUtil.validateParameterNotNull(baseUrl, "Base Url cannot be null");
		ServicesUtil.validateParameterNotNull(startAddress, "START cannot be null");
		ServicesUtil.validateParameterNotNull(destinationAddress, "DESTINATION cannot be null");
		final StringBuilder result = new StringBuilder(baseUrl);
		try
		{
			result.append("/api/directions/").append(getResponseType()).append("?origin=").append(addressData2String(startAddress))
					.append("&destination=").append(addressData2String(destinationAddress)).append("&sensor=").append(isSensor())
					.append("&mode=").append(getMode());
		}
		catch (final UnsupportedEncodingException e)
		{
			throw new GeoServiceWrapperException(e);
		}

		return result.toString();
	}

	protected String getMode()
	{
		if (StringUtils.isEmpty(mode))
		{
			return DEFAULT_MODE;
		}
		return mode;
	}

	protected boolean isSensor()
	{
		return sensor;
	}

	protected String getResponseType()
	{
		if (StringUtils.isEmpty(responseType))
		{
			return DEFAULT_RESPONSE_TYPE;
		}
		return responseType;
	}

	protected String formatCoordinates(final GPS coordinates)
	{
		final DecimalFormat format = new DecimalFormat(GeolocationConstants.DECIMAL_COORDINATES_FORMAT);
		return format.format(coordinates.getDecimalLatitude()).replace(',', '.') + ","
				+ format.format(coordinates.getDecimalLongitude()).replace(',', '.');
	}

	public void setSensor(final boolean sensor)
	{
		this.sensor = sensor;
	}

	public void setMode(final String mode)
	{
		this.mode = mode;
	}

	public void setResponseType(final String responseType)
	{
		this.responseType = responseType;
	}

	protected String addressData2String(final AddressData addressData) throws UnsupportedEncodingException // NOSONAR
	{
		final List<String> collection = Lists.newArrayList(addressData.getStreet(), addressData.getBuilding(), addressData.getZip(),
				addressData.getCity(), addressData.getCountryCode());

		return Joiner.on(SEPARATOR).join(Iterables.filter(collection, Predicates.notNull()));
	}

}
