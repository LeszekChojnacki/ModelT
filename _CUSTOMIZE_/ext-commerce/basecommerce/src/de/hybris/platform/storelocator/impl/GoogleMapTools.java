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

import de.hybris.platform.storelocator.GPS;
import de.hybris.platform.storelocator.data.AddressData;
import de.hybris.platform.storelocator.data.MapLocationData;
import de.hybris.platform.storelocator.data.RouteData;
import de.hybris.platform.storelocator.exception.GeoLocatorException;
import de.hybris.platform.storelocator.exception.GeoServiceWrapperException;
import de.hybris.platform.storelocator.location.Location;
import de.hybris.platform.storelocator.route.GeolocationDirectionsUrlBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;


/**
 * Spring Bean performing the geo-location HTTP requests towards Google Maps Service.
 *
 */
public class GoogleMapTools
{
	private static final String SEPARATOR = "+";
	private static final String CLIEND_ID = "client";
	private static final String API_KEY = "key";
	private ResponseExtractor<MapLocationData> addressLocationParser;
	private ResponseExtractor<RouteData> routeDataParser;
	private GeolocationDirectionsUrlBuilder directionsUrlBuilder;
	private String baseUrl;
	private String cliendId;
	private String cryptoKey;
	private String googleKey;


	public String getGoogleQuery(final AddressData addressData)
	{
		final List<String> collection = Lists.newArrayList(addressData.getStreet(), addressData.getBuilding(), addressData.getZip(),
				addressData.getCity(), addressData.getCountryCode());

		return Joiner.on(SEPARATOR).join(Iterables.filter(collection, Predicates.notNull()));
	}

	/**
	 * geo-codes the given location.
	 *
	 * @param address
	 *           the address to geocode
	 * @return the GPS of the given address
	 * @throws GeoServiceWrapperException
	 */
	public GPS geocodeAddress(final Location address)
	{
		Preconditions.checkNotNull(address, "Geocoding failed! Address cannot be null");
		Preconditions.checkNotNull(address.getAddressData(), "Geocoding failed! Address cannot be null");

		return geocodeAddress(address.getAddressData());
	}

	/**
	 * creates Distance and Route for the given start/destination locations
	 *
	 * @param start
	 *           the start location
	 * @param destination
	 *           the destination location
	 * @return the calculated RouteData for the given locations
	 * @throws GeoServiceWrapperException
	 */
	public RouteData getDistanceAndRoute(final Location start, final Location destination)
	{
		final String urlAddress = appendBusinessParams(directionsUrlBuilder.getWebServiceUrl(baseUrl, start.getAddressData(),
				destination.getAddressData(), Collections.emptyMap()));
		final RouteData routeData = getRouteData(urlAddress);
		final double distance = GeometryUtils.getElipticalDistanceKM(start.getGPS(), destination.getGPS());
		routeData.setEagleFliesDistance(distance);
		return routeData;
	}

	/**
	 * creates Distance and Route for the given start/destination GPS
	 *
	 * @param start
	 *           the start GPS
	 * @param destination
	 *           the destination GPS
	 * @return the calculated RouteData for the given GPS'
	 * @throws GeoServiceWrapperException
	 */
	public RouteData getDistanceAndRoute(final GPS start, final GPS destination)
	{
		final String urlAddress = appendBusinessParams(getDirectionsUrlBuilder().getWebServiceUrl(getBaseUrl(), start, destination,
				Collections.emptyMap()));
		final RouteData routeData = getRouteData(urlAddress);
		final double distance = GeometryUtils.getElipticalDistanceKM(start, destination);
		routeData.setEagleFliesDistance(distance);
		return routeData;
	}

	protected RouteData getRouteData(final String urlAddress)
	{
		final RestTemplate restTemplate = new RestTemplate();
		restTemplate.setErrorHandler(new DefaultResponseErrorHandler()
		{
			@Override
			public void handleError(final ClientHttpResponse response) throws IOException
			{
				if (response.getStatusCode() != org.springframework.http.HttpStatus.OK)
				{
					throw new GeoServiceWrapperException("Google maps unavailable. Status: " + response.getRawStatusCode());
				}
				super.handleError(response);
			}
		});

		if (isBusinessAPI())
		{
			return restTemplate.execute(singAndEncodeURL(urlAddress), HttpMethod.GET, null, getRouteDataParser());
		}
		else
		{
			return restTemplate.execute(urlAddress, HttpMethod.GET, null, getRouteDataParser(), Collections.emptyMap());
		}
	}

	/**
	 * geo-codes the given address data
	 *
	 * @param addressData
	 *           the address to geocode
	 * @return the calculated GPS
	 * @throws GeoServiceWrapperException
	 */
	public GPS geocodeAddress(final AddressData addressData)
	{
		try
		{
			final RestTemplate restTemplate = new RestTemplate();

			final String urlAddress = appendBusinessParams(getBaseUrl() + "xml?address=" + getGoogleQuery(addressData)
					+ "&sensor=true");

			final MapLocationData locationData = isBusinessAPI() ? restTemplate.execute(singAndEncodeURL(urlAddress),
					HttpMethod.GET, null, getAddressLocationParser()) : restTemplate.execute(urlAddress, HttpMethod.GET, null,
					getAddressLocationParser());

			final String latitude = locationData.getLatitude();
			final String longitude = locationData.getLongitude();

			if (StringUtils.isNotBlank(latitude) && StringUtils.isNotBlank(longitude))
			{
				return new DefaultGPS().create(Double.parseDouble(latitude), Double.parseDouble(longitude));
			}
			else
			{
				throw new GeoServiceWrapperException(GeoServiceWrapperException.errorMessages.get(locationData.getCode()));
			}
		}
		catch (final GeoLocatorException | ResourceAccessException e)
		{
			throw new GeoServiceWrapperException(e);
		}
	}

	/**
	 * Appends business user parameters client and signature. Example URL after appending
	 * http://maps.googleapis.com/maps/api/geocode/xml ?address=1600+Amphitheatre+Parkway,+Mountain+View,+CA
	 * &client=gme-YOUR_CLIENT_ID &signature=YOUR_URL_SIGNATURE
	 *
	 * @param urlAddress
	 * @return url with client id and signature
	 */
	protected String appendBusinessParams(final String urlAddress)
	{
		// Note: Maps for Business users must include client and signature
		// parameters with their requests instead of a key.

		if (isBusinessAPI())
		{
			final StringBuilder sb = new StringBuilder(urlAddress);
			sb.append("&");
			sb.append(CLIEND_ID);
			sb.append("=");
			sb.append(getCliendId());
			return sb.toString();
		}
		else if (!StringUtils.isEmpty(getGoogleKey()))
		{
			// if no client id and signature then api key
			final StringBuilder sb = new StringBuilder(urlAddress);
			sb.append("&");
			sb.append(API_KEY);
			sb.append("=");
			sb.append(getGoogleKey());

			final String result = sb.toString();
			// if api key then ensure that protocol is secured
			if (!result.startsWith("https"))
			{
				return result.replaceFirst("http", "https");
			}
			return result;
		}
		// return unchanged
		return urlAddress;
	}

	/**
	 * Encodes URL query, generates signature and appends it to the query. Return URI to avoid second encoding
	 *
	 * @param urlAddress
	 * @return URI
	 */
	protected URI singAndEncodeURL(final String urlAddress)
	{
		try
		{
			// Sign the URL according to
			// https://developers.google.com/maps/documentation/business/webservices/auth#signature_examples
			final UrlSigner signer = new UrlSigner(getCryptoKey());
			final URL url = new URL(urlAddress);
			// encode query and sign it
			final String singnedUrl = signer.signRequest(url.getPath(), UriUtils.encodeQuery(url.getQuery(), "UTF-8"));

			return new URI(url.getProtocol() + "://" + url.getHost() + singnedUrl);
		}
		catch (final Exception e)
		{
			throw new GeoServiceWrapperException("Couldn't sign the request", e);
		}
	}

	protected boolean isBusinessAPI()
	{
		return !StringUtils.isEmpty(getCliendId()) && !StringUtils.isEmpty(getCryptoKey());
	}

	@Required
	public void setAddressLocationParser(final ResponseExtractor<MapLocationData> addressLocationParser)
	{
		this.addressLocationParser = addressLocationParser;
	}

	protected ResponseExtractor<MapLocationData> getAddressLocationParser()
	{
		return addressLocationParser;
	}

	@Required
	public void setRouteDataParser(final ResponseExtractor<RouteData> routeDataParser)
	{
		this.routeDataParser = routeDataParser;
	}

	protected ResponseExtractor<RouteData> getRouteDataParser()
	{
		return routeDataParser;
	}

	public void setBaseUrl(final String baseUrl)
	{
		this.baseUrl = baseUrl;
	}

	protected String getBaseUrl()
	{
		return baseUrl;
	}

	public void setGoogleKey(final String googleKey)
	{
		this.googleKey = googleKey;
	}

	protected String getGoogleKey()
	{
		return googleKey;
	}

	@Required
	public void setDirectionsUrlBuilder(final GeolocationDirectionsUrlBuilder directionsUrlBuilder)
	{
		this.directionsUrlBuilder = directionsUrlBuilder;
	}

	protected GeolocationDirectionsUrlBuilder getDirectionsUrlBuilder()
	{
		return directionsUrlBuilder;
	}

	public void setCliendId(final String cliendId)
	{
		this.cliendId = cliendId;
	}

	protected String getCliendId()
	{
		return cliendId;
	}

	public void setCryptoKey(final String signature)
	{
		this.cryptoKey = signature;
	}

	protected String getCryptoKey()
	{
		return cryptoKey;
	}
}
