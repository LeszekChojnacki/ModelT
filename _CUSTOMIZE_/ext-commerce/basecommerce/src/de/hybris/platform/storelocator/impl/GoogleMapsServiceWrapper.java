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
import de.hybris.platform.storelocator.GeoWebServiceWrapper;
import de.hybris.platform.storelocator.data.AddressData;
import de.hybris.platform.storelocator.data.RouteData;
import de.hybris.platform.storelocator.exception.GeoServiceWrapperException;
import de.hybris.platform.storelocator.location.Location;
import de.hybris.platform.storelocator.route.DistanceAndRoute;
import de.hybris.platform.storelocator.route.Route;
import de.hybris.platform.storelocator.route.impl.DefaultDistanceAndRoute;
import de.hybris.platform.storelocator.route.impl.DefaultRoute;
import de.hybris.platform.util.Config;

import org.springframework.beans.factory.annotation.Required;


/**
 * Wrapper that performs communication with google geo-location services.
 */
public class GoogleMapsServiceWrapper implements GeoWebServiceWrapper
{
	public static final String GOOGLE_MAPS_URL = "google.maps.url";
	public static final String GOOGLE_GEOCODING_URL = "google.geocoding.url";

	private GoogleMapTools googleMapTools;


	protected GoogleMapTools getMapTools(final String url)
	{
		googleMapTools.setBaseUrl(url);
		return googleMapTools;
	}

	@Override
	public GPS geocodeAddress(final Location address) throws GeoServiceWrapperException
	{
		final GoogleMapTools mapTools = getMapTools(Config.getString(GOOGLE_GEOCODING_URL, null));
		return mapTools.geocodeAddress(address);
	}

	@Override
	public GPS geocodeAddress(final AddressData address) throws GeoServiceWrapperException
	{
		final GoogleMapTools mapTools = getMapTools(Config.getString(GOOGLE_GEOCODING_URL, null));
		return mapTools.geocodeAddress(address);
	}

	@Override
	public DistanceAndRoute getDistanceAndRoute(final Location start, final Location destination) throws GeoServiceWrapperException
	{

		final GoogleMapTools geocodingModule = getMapTools(Config.getString(GOOGLE_MAPS_URL, null));
		final RouteData routeData = geocodingModule.getDistanceAndRoute(start, destination);
		final Route route = new DefaultRoute(start.getGPS(), destination, routeData.getCoordinates());
		return new DefaultDistanceAndRoute(routeData.getDistance(), routeData.getEagleFliesDistance(), route);
	}

	@Override
	public String formatAddress(final Location address) throws GeoServiceWrapperException
	{
		//using google query format for addressess 
		return new GoogleMapTools().getGoogleQuery(address.getAddressData());
	}

	@Override
	public DistanceAndRoute getDistanceAndRoute(final GPS start, final Location destination) throws GeoServiceWrapperException
	{
		final GoogleMapTools geocodingModule = getMapTools(Config.getString(GOOGLE_MAPS_URL, null));
		final RouteData routeData = geocodingModule.getDistanceAndRoute(start, destination.getGPS());
		final Route route = new DefaultRoute(start, destination, routeData.getCoordinates());
		return new DefaultDistanceAndRoute(routeData.getDistance(), routeData.getEagleFliesDistance(), route);
	}

	@Required
	public void setGoogleMapTools(final GoogleMapTools googleMapTools)
	{
		this.googleMapTools = googleMapTools;
	}
}
