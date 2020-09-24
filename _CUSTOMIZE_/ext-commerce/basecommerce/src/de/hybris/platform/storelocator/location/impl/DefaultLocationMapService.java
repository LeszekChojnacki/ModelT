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
package de.hybris.platform.storelocator.location.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.storelocator.GPS;
import de.hybris.platform.storelocator.GeoWebServiceWrapper;
import de.hybris.platform.storelocator.data.AddressData;
import de.hybris.platform.storelocator.exception.GeoServiceWrapperException;
import de.hybris.platform.storelocator.exception.LocationMapServiceException;
import de.hybris.platform.storelocator.exception.LocationServiceException;
import de.hybris.platform.storelocator.exception.MapServiceException;
import de.hybris.platform.storelocator.location.Location;
import de.hybris.platform.storelocator.location.LocationMapService;
import de.hybris.platform.storelocator.location.LocationService;
import de.hybris.platform.storelocator.map.Map;
import de.hybris.platform.storelocator.map.MapService;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of the LocationMapService.
 */
public class DefaultLocationMapService implements LocationMapService
{

	private LocationService locationService;
	private GeoWebServiceWrapper geoServiceWrapper;
	private MapService mapService;

	private double radiusStep = 50;
	private double radiusMax = 500;

	private static final String CANNOT_TRANSLATE_ADDRESS_MESSAGE = "Cannot translate AddressData to IGPS.";

	@Override
	public Map getMapOfLocationsForPostcode(final String postalCode, final String countryCode, final int limitLocationsCount,
			final BaseStoreModel baseStore)
	{
		validateInputData(postalCode, "Postal code cannot be empty!");
		validateInputData(countryCode, "Country code cannot be empty!");

		try
		{
			final GPS gps = calculateGPS(null, postalCode, countryCode);
			return getMapOfLocations(gps, limitLocationsCount, baseStore);
		}
		catch (final GeoServiceWrapperException e)
		{
			throw new LocationMapServiceException(CANNOT_TRANSLATE_ADDRESS_MESSAGE, e);
		}
		catch (final MapServiceException e)
		{
			throw new LocationMapServiceException("Cannot create new Map.", e);
		}
	}

	@Override
	public Map getMapOfLocationsForTown(final String town, final int limitLocationsCount, final BaseStoreModel baseStore)
	{
		validateInputData(town, "Town name cannot be empty!");

		try
		{
			final GPS gps = calculateGPS(town, null, null);
			return getMapOfLocations(gps, limitLocationsCount, baseStore);
		}
		catch (final GeoServiceWrapperException e)
		{
			throw new LocationMapServiceException(CANNOT_TRANSLATE_ADDRESS_MESSAGE, e);
		}
		catch (final MapServiceException e)
		{
			throw new LocationMapServiceException("Cannot create new Map.", e);
		}
	}

	@Override
	public Map getMapOfLocations(final String searchTerm, final String countryCode, final int limitLocationsCount,
			final BaseStoreModel baseStore)
	{
		Map result = getMapOfLocationsForTown(searchTerm, limitLocationsCount, baseStore);
		if (CollectionUtils.isEmpty(result.getPointsOfInterest()))
		{
			result = getMapOfLocationsForPostcode(searchTerm, countryCode, limitLocationsCount, baseStore);
		}
		return result;
	}

	/**
	 * Calculating Map, basing on number of points of interest defined by user and proper radius.
	 *
	 * @param gps
	 *           coordinates of center point
	 * @param limitLocationsCount
	 *           number of places to find, defined by user
	 * @param baseStore
	 *           if set, method will return locations bound to the given {@link BaseStoreModel}
	 * @return map from store locator map service
	 * @throws LocationMapServiceException
	 * @throws MapServiceException
	 */
	@Override
	public Map getMapOfLocations(final GPS gps, final int limitLocationsCount, final BaseStoreModel baseStore)
	{
		List<Location> locations;
		double distance = 0;
		try
		{
			do
			{
				distance += getRadiusStep();
				locations = getLocationService().getSortedLocationsNearby(gps, distance, baseStore);
			}
			while (locations.size() < limitLocationsCount && distance < getRadiusMax());
		}
		catch (final LocationServiceException e)
		{
			throw new LocationMapServiceException(CANNOT_TRANSLATE_ADDRESS_MESSAGE, e);
		}

		if (limitLocationsCount < locations.size())
		{
			locations = locations.subList(0, limitLocationsCount);
		}
		return getMapService().getMap(gps, distance, locations, "");
	}

	/**
	 * Validation for input parameters.
	 *
	 * @param input
	 *           parameter to validate
	 * @param message
	 *           message to be thrown
	 */
	protected void validateInputData(final Object input, final String message)
	{
		validateParameterNotNull(input, message);
	}


	protected void validateInputData(final String input, final String message)
	{
		if (StringUtils.isEmpty(input))
		{
			throw new IllegalArgumentException(message);
		}
	}

	/**
	 * Method for calculating GPS coordinates based on address.
	 *
	 * @param town
	 *           searching condition for locations, name of the specified city
	 * @param postalCode
	 *           searching condition for locations, postal code
	 * @param countryCode
	 *           searching condition for locations, code for the country
	 * @return coordinates of location, based on given address
	 * @throws GeoServiceWrapperException
	 */
	protected GPS calculateGPS(final String town, final String postalCode, final String countryCode)
	{
		final AddressData addressData = new AddressData();
		addressData.setCity(town);
		addressData.setCountryCode(countryCode);
		addressData.setZip(postalCode);
		return getGeoServiceWrapper().geocodeAddress(addressData);
	}

	protected GeoWebServiceWrapper getGeoServiceWrapper()
	{
		return geoServiceWrapper;
	}

	@Required
	public void setGeoServiceWrapper(final GeoWebServiceWrapper geoServiceWrapper)
	{
		this.geoServiceWrapper = geoServiceWrapper;
	}

	public double getRadiusStep()
	{
		return radiusStep;
	}

	public void setRadiusStep(final double radiusStep)
	{
		this.radiusStep = radiusStep;
	}

	public double getRadiusMax()
	{
		return radiusMax;
	}

	public void setRadiusMax(final double radiusMax)
	{
		this.radiusMax = radiusMax;
	}

	public MapService getMapService()
	{
		return mapService;
	}

	@Required
	public void setMapService(final MapService mapService)
	{
		this.mapService = mapService;
	}

	@Required
	public void setLocationService(final LocationService distanceAwareLocationService)
	{
		this.locationService = distanceAwareLocationService;
	}

	public LocationService getLocationService()
	{
		return locationService;
	}
}
