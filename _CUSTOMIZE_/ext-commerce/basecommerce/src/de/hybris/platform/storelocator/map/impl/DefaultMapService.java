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
package de.hybris.platform.storelocator.map.impl;

import de.hybris.platform.storelocator.GPS;
import de.hybris.platform.storelocator.exception.GeoLocatorException;
import de.hybris.platform.storelocator.exception.GoogleMapException;
import de.hybris.platform.storelocator.exception.MapServiceException;
import de.hybris.platform.storelocator.exception.RouteServiceException;
import de.hybris.platform.storelocator.location.Location;
import de.hybris.platform.storelocator.map.Map;
import de.hybris.platform.storelocator.map.MapService;
import de.hybris.platform.storelocator.map.utils.MapBounds;
import de.hybris.platform.storelocator.route.RouteService;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of the MapService.
 */
public class DefaultMapService implements MapService
{
	public static final double DEFAULT_RADIUS = 5;

	private RouteService routeService;

	@Override
	public Map getMap(final GPS gps, final String title)
	{
		return getMap(gps, title, DEFAULT_RADIUS);
	}

	@Override
	public Map getMap(final GPS gps, final String title, final double radius)
	{
		try
		{
			return DefaultMap.create(gps, radius, title);
		}
		catch (final GoogleMapException e)
		{
			throw new MapServiceException("Could not get map due to " + e.getMessage(), e);
		}
	}

	@Override
	public Map getMap(final GPS gps, final double radius, final List<Location> poi, final String title)
	{
		try
		{
			return DefaultMap.create(gps, radius, title, poi);
		}
		catch (final GoogleMapException e)
		{
			throw new MapServiceException("Could not get map due to " + e.getMessage(), e);
		}
	}

	@Override
	public Map getMap(final GPS center, final double radius, final List<Location> poi, final String title, final Location routeTo)
	{
		try
		{
			return DefaultMap.create(center, radius, title, poi, getRouteService().getDistanceAndRoute(center, routeTo));
		}
		catch (final GoogleMapException | RouteServiceException e)
		{
			throw new MapServiceException(e);
		}
	}

	@Override
	public MapBounds getMapBoundsForMap(final Map map)
	{
		if (map != null && CollectionUtils.isNotEmpty(map.getPointsOfInterest()))
		{
			GPS northEast = map.getPointsOfInterest().get(0).getGPS();
			GPS southWest = map.getPointsOfInterest().get(0).getGPS();

			for (final Location location : map.getPointsOfInterest())
			{
				northEast = determineNorthEastCorner(northEast, location);
				southWest = determineSouthWestCorner(southWest, location);
			}
			return recalculateBoundsAgainstMapCenter(southWest, northEast, map.getGps());
		}
		return null;
	}

	/**
	 * Determine south west corner for given southWest corner and location. If location is outside south west corner the
	 * corner coordinates will be expanded to contain location.
	 *
	 * @param southWest
	 *           the south west bounds corner
	 * @param location
	 *           the location that will be assured to be placed inside the rectangle
	 * @return the new GPS corner location that will contain given location
	 * @throws GeoLocatorException
	 */
	protected GPS determineSouthWestCorner(final GPS southWest, final Location location)
	{
		final double maxSouth = Math.min(location.getGPS().getDecimalLatitude(), southWest.getDecimalLatitude());
		final double maxWest = Math.min(location.getGPS().getDecimalLongitude(), southWest.getDecimalLongitude());
		return southWest.create(maxSouth, maxWest);
	}


	/**
	 * Determine north east corner for given southWest corner and location. If location is outside south west corner the
	 * corner coordinates will be expanded to contain location.
	 *
	 * @param northEast
	 *           the north east bounds cornder
	 * @param location
	 *           the location that will be assured to be placed inside the rectangle
	 * @return the new GPS corner location that will contain given location
	 * @throws GeoLocatorException
	 *            the geo locator exception
	 */
	protected GPS determineNorthEastCorner(final GPS northEast, final Location location)
	{
		final double maxNorth = Math.max(location.getGPS().getDecimalLatitude(), northEast.getDecimalLatitude());
		final double maxEast = Math.max(location.getGPS().getDecimalLongitude(), northEast.getDecimalLongitude());
		return northEast.create(maxNorth, maxEast);
	}


	/**
	 * Recalculate map corners against map center. Map corners are cal
	 *
	 * @param southWest
	 *           the south west
	 * @param northEast
	 *           the north east
	 * @param centerPosition
	 *           the center position
	 * @return the new GPS rectangle object that contains calculated values for the corners
	 * @throws GeoLocatorException
	 */
	protected MapBounds recalculateBoundsAgainstMapCenter(final GPS southWest, final GPS northEast, final GPS centerPosition)
	{
		final double wSpan = southWest.getDecimalLongitude() - centerPosition.getDecimalLongitude();
		final double eSpan = northEast.getDecimalLongitude() - centerPosition.getDecimalLongitude();
		final double sSpan = southWest.getDecimalLatitude() - centerPosition.getDecimalLatitude();
		final double nSpan = northEast.getDecimalLatitude() - centerPosition.getDecimalLatitude();
		final double maxE;
		final double maxW;
		final double maxN;
		final double maxS;
		if (Math.abs(eSpan) > Math.abs(wSpan))
		{
			maxW = centerPosition.getDecimalLongitude() - Math.abs(eSpan);
			maxE = centerPosition.getDecimalLongitude() + Math.abs(eSpan);
		}
		else
		{
			maxE = centerPosition.getDecimalLongitude() + Math.abs(wSpan);
			maxW = centerPosition.getDecimalLongitude() - Math.abs(wSpan);
		}
		if (Math.abs(sSpan) > Math.abs(nSpan))
		{
			maxN = centerPosition.getDecimalLatitude() + Math.abs(sSpan);
			maxS = centerPosition.getDecimalLatitude() - Math.abs(sSpan);
		}
		else
		{
			maxS = centerPosition.getDecimalLatitude() - Math.abs(nSpan);
			maxN = centerPosition.getDecimalLatitude() + Math.abs(nSpan);
		}

		return new MapBounds(northEast.create(maxN, maxE), southWest.create(maxS, maxW));
	}

	protected RouteService getRouteService()
	{
		return routeService;
	}

	@Required
	public void setRouteService(final RouteService routeService)
	{
		this.routeService = routeService;
	}

}
