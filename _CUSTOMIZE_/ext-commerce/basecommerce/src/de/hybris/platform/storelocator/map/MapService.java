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
package de.hybris.platform.storelocator.map;

import de.hybris.platform.storelocator.GPS;
import de.hybris.platform.storelocator.exception.GeoLocatorException;
import de.hybris.platform.storelocator.exception.MapServiceException;
import de.hybris.platform.storelocator.location.Location;
import de.hybris.platform.storelocator.map.utils.MapBounds;

import java.util.List;


/**
 * Provides maps query point.
 */
public interface MapService
{
	/**
	 * get titled Map centered around the {@link GPS} location with a given radius. As no additional placemarks are
	 * required, returned map object that has null KML member {@link Map#getKml()}.
	 *
	 * @param gps
	 * 		{@link GPS}
	 * @param title
	 * 		{@link String}
	 * @param radius
	 * 		radius size as double
	 * @return {@link Map}
	 * @throws MapServiceException
	 * 		in the case of error
	 */
	Map getMap(GPS gps, String title, double radius) throws MapServiceException;

	/**
	 * get titled Map centered around the {@link GPS} location. Default radius 5[km] will be used. As no additional
	 * placemarks are required, returned map object that has null KML member {@link Map#getKml()}.
	 *
	 * @param gps
	 * 		{@link GPS}
	 * @param title
	 * 		{@link String}
	 * @return {@link Map}
	 * @throws MapServiceException
	 * 		in the case of error
	 */
	Map getMap(final GPS gps, final String title) throws MapServiceException;

	/**
	 * get titled {@link Map} with all the points of interest and the given radius
	 *
	 * @param gps
	 * 		{@link GPS}
	 * @param radius
	 * 		radius size as double
	 * @param poi
	 * 		{@link List} of {@link Location}
	 * @param title
	 * 		{@link String}
	 * @return {@link Map}
	 * @throws MapServiceException
	 * 		in the case of error
	 */
	Map getMap(GPS gps, double radius, List<Location> poi, String title) throws MapServiceException;

	/**
	 * Returns the {@link Map} that contains list of POIs and a route from the center {@link GPS} location to the
	 * 'routeTo' {@link Location}
	 *
	 * @param gps
	 * 		{@link GPS}
	 * @param radius
	 * 		radius size as double
	 * @param poi
	 * 		{@link List} of {@link Location}
	 * @param title
	 * 		{@link String}
	 * @param routeTo
	 * 		{@link Location}
	 * @return {@link Map}
	 * @throws MapServiceException
	 * 		in the case of error
	 */
	Map getMap(final GPS gps, final double radius, final List<Location> poi, final String title, final Location routeTo)
			throws MapServiceException;

	/**
	 * Gets the map bounds of the given map. Calculations are perfomed basing on rectangular coordinates, not just a
	 * radius.
	 *
	 * @param map
	 * 		the map to calculate bounds for
	 * @return the calculated map bounds
	 * @throws GeoLocatorException
	 * 		the geo locator exception
	 */
	MapBounds getMapBoundsForMap(final Map map) throws GeoLocatorException;
}
