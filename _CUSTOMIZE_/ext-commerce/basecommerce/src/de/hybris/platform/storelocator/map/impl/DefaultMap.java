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
import de.hybris.platform.storelocator.exception.KmlDocumentException;
import de.hybris.platform.storelocator.location.Location;
import de.hybris.platform.storelocator.map.Map;
import de.hybris.platform.storelocator.map.markers.KmlDocument;
import de.hybris.platform.storelocator.map.markers.impl.DefaultKmlDocument;
import de.hybris.platform.storelocator.map.utils.MapBounds;
import de.hybris.platform.storelocator.route.DistanceAndRoute;

import java.util.List;

import org.apache.log4j.Logger;


/**
 * 
 */
public class DefaultMap implements Map
{

	private static final Logger LOG = Logger.getLogger(DefaultMap.class);
	/**
	 * center point
	 */
	private final GPS gps;

	/**
	 * radius
	 */
	private final double radius;

	/**
	 * image containing the information on how to get the mapplet from Google API
	 */
	private final KmlDocument kml;

	/**
	 * POIs on the map
	 */
	private final List<Location> pois;

	/**
	 * map's title
	 */
	private final String title;

	/**
	 * IDistance and route related to this map.
	 */
	private final DistanceAndRoute route;

	private MapBounds mapBounds;



	/**
	 * @param gps
	 * @param radius
	 * @param pois
	 * @param title
	 */
	public DefaultMap(final GPS gps, final double radius, final List<Location> pois, final DistanceAndRoute route,
			final String title, final KmlDocument kml)
	{
		super();
		this.gps = gps;
		this.radius = radius;
		this.pois = pois;
		this.title = title;
		this.kml = kml;
		this.route = route;
		try
		{
			this.mapBounds = new MapBounds(gps, radius);
		}
		catch (final GeoLocatorException e)
		{
			LOG.warn("Could not create map bounds due to : " + e.getMessage(), e);
		}
	}

	public static DefaultMap create(final GPS center, final double radius, final String title) throws GoogleMapException
	{
		return new DefaultMap(center, radius, null, null, title, null);
	}

	public static DefaultMap create(final GPS center, final double radius, final String title, final List<Location> poi)
			throws GoogleMapException
	{
		return DefaultMap.create(center, radius, title, poi, null);
	}

	public static DefaultMap create(final GPS center, final double radius, final String title, final List<Location> poi,
			final DistanceAndRoute route) throws GoogleMapException
	{

		KmlDocument kml;
		try
		{
			kml = new DefaultKmlDocument(center, poi, route);
			return new DefaultMap(center, radius, poi, route, title, kml);
		}
		catch (final KmlDocumentException e)
		{
			throw new GoogleMapException("Could not create Google Map", e);
		}
	}



	@Override
	public GPS getGps()
	{
		return this.gps;
	}


	@Override
	public List<Location> getPointsOfInterest()
	{
		return this.pois;
	}

	@Override
	public double getRadius()
	{
		return this.radius;
	}


	@Override
	public String getTitle()
	{
		return this.title;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.hybris.platform.geolocation.map.IMap#getKml()
	 */
	@Override
	public KmlDocument getKml()
	{
		return kml;
	}

	/**
	 * @return the route
	 */
	@Override
	public DistanceAndRoute getDistanceAndRoute()
	{
		return route;
	}

	/**
	 * @return mapBounds
	 */
	@Override
	public MapBounds getMapBounds()
	{
		return mapBounds;
	}

}
