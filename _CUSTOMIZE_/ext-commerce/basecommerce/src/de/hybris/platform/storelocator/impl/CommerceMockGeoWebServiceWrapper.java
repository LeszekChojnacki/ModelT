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

import de.hybris.platform.store.services.BaseStoreService;
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

import java.util.Map;

import org.springframework.beans.factory.annotation.Required;


public class CommerceMockGeoWebServiceWrapper implements GeoWebServiceWrapper
{
	private BaseStoreService baseStoreService;
	private Map<String, GPS> countryIsoGPSLocationMap;

	@Override
	public GPS geocodeAddress(final Location address)
	{
		if ("exception".equals(address.getAddressData().getCity()))
		{
			throw new GeoServiceWrapperException();
		}

		return getCountryIsoGPSLocationMap().containsKey(address.getCountry())
				? getCountryIsoGPSLocationMap().get(address.getCountry()) : getCountryIsoGPSLocationMap().get("DEFAULT");
	}

	@Override
	public GPS geocodeAddress(final AddressData address)
	{
		if ("exception".equals(address.getCity()))
		{
			throw new GeoServiceWrapperException();
		}
		return getCountryIsoGPSLocationMap().containsKey(address.getCountryCode())
				? getCountryIsoGPSLocationMap().get(address.getCountryCode()) : getCountryIsoGPSLocationMap().get("DEFAULT");

	}

	@Override
	public DistanceAndRoute getDistanceAndRoute(final Location start, final Location destination)
	{
		final RouteData routeData = new RouteData();
		routeData.setEagleFliesDistance(0.3);
		routeData.setDistance(309.0);
		final Route route = new DefaultRoute(start.getGPS(), destination, routeData.getCoordinates());
		return new DefaultDistanceAndRoute(routeData.getDistance(), routeData.getEagleFliesDistance(), route);
	}

	@Override
	public DistanceAndRoute getDistanceAndRoute(final GPS start, final Location destination)
	{
		final RouteData routeData = new RouteData();
		routeData.setEagleFliesDistance(0.3);
		routeData.setDistance(10.0);
		final Route route = new DefaultRoute(start, destination, routeData.getCoordinates());
		return new DefaultDistanceAndRoute(routeData.getDistance(), routeData.getEagleFliesDistance(), route);
	}

	@Override
	public String formatAddress(final Location address)
	{
		return "Formatted address";
	}

	protected BaseStoreService getBaseStoreService()
	{
		return baseStoreService;
	}

	@Required
	public void setBaseStoreService(final BaseStoreService baseStoreService)
	{
		this.baseStoreService = baseStoreService;
	}

	@Required
	public void setCountryIsoGPSLocationMap(final Map<String, GPS> countryIsoGPSLocationMap)
	{
		this.countryIsoGPSLocationMap = countryIsoGPSLocationMap;
	}

	protected Map<String, GPS> getCountryIsoGPSLocationMap()
	{
		return countryIsoGPSLocationMap;
	}
}
