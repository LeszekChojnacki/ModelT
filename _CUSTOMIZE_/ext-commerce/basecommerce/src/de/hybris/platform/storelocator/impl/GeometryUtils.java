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
import de.hybris.platform.storelocator.constants.GeolocationMaths;
import de.hybris.platform.storelocator.exception.GeoLocatorException;

import java.util.ArrayList;
import java.util.List;

import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GeodeticCurve;
import org.gavaghan.geodesy.GlobalCoordinates;


/**
 * Utility class responsible for getting elliptical distances
 */
public class GeometryUtils
{
	private GeometryUtils()
	{
		// prevent instantiation
	}

	/**
	 * Meridian length = 20000km Angular length = 180 degree distance of one degree = 111.11
	 */
	public static final double LAT_DEGREE_DIST = 111.11;

	/**
	 * Approximate earth radius [kilometers]
	 */
	public static final double APP_EARTH_RADIUS = 6366.38;

	/**
	 * Returns Elliptical distance between two gps coordinates
	 *
	 * @param gpsFrom
	 * @param gpsTo
	 * @return double
	 */
	public static double getElipticalDistanceKM(final GPS gpsFrom, final GPS gpsTo)
	{
		final GeodeticCalculator geoCalc = new GeodeticCalculator();

		// select a reference ellipsoid - here International, for global GPS resources or local ellipsoid for Europe
		final Ellipsoid reference;
		if (isPointInEurope(gpsFrom) || isPointInEurope(gpsTo))
		{
			reference = Ellipsoid.KRASSOWSKI;
		}
		else
		{
			reference = Ellipsoid.WGS84;
		}

		// calculate the geodetic curve
		final GeodeticCurve geoCurve = geoCalc.calculateGeodeticCurve(reference, igps2GlobalCoordinates(gpsFrom),
				igps2GlobalCoordinates(gpsTo));
		return geoCurve.getEllipsoidalDistance() / 1000.0;
	}

	protected static GlobalCoordinates igps2GlobalCoordinates(final GPS igps)
	{
		return new GlobalCoordinates(igps.getDecimalLatitude(), igps.getDecimalLongitude());
	}

	protected static double getCircleOfLatitudeLength(final double latitude)
	{
		final double rads = Math.abs(latitude) * Math.PI / 180;
		return 2 * Math.PI * APP_EARTH_RADIUS * Math.cos(rads);
	}

	/**
	 * returns list of coordinates locations being the top-left and bottom-right corners of the tolerance square
	 *
	 * @param center
	 *           {@link GPS}
	 * @param radius
	 *           [kilometers]
	 * @return list of {@link GPS}
	 * @throws GeoLocatorException
	 */
	public static List<GPS> getSquareOfTolerance(final GPS center, final double radius)
	{
		if (center == null)
		{
			throw new GeoLocatorException("Center cannot be null");
		}
		if (radius <= 0)
		{
			throw new GeoLocatorException("Radius must be a positive value");
		}
		final double deltaLongitude = radius / (getCircleOfLatitudeLength(center.getDecimalLatitude()) / 360);
		final double deltaLatitude = radius / LAT_DEGREE_DIST;

		final double lat1 = fixOverlappedLatitude(center.getDecimalLatitude() - deltaLatitude);
		final double lon1 = fixOverlappedLongitude(center.getDecimalLongitude() - deltaLongitude);

		final double lat2 = fixOverlappedLatitude(center.getDecimalLatitude() + deltaLatitude);
		final double lon2 = fixOverlappedLongitude(center.getDecimalLongitude() + deltaLongitude);

		final List<GPS> corners = new ArrayList<>(2);
		final GPS creator = new DefaultGPS();
		corners.add(creator.create(lat1, lon1));
		corners.add(creator.create(lat2, lon2));
		return corners;
	}

	protected static double fixOverlappedLongitude(final double longitude)
	{
		if (longitude < -180)
		{
			return 360 + longitude;
		}
		else if (longitude > 180)
		{
			return -360 + longitude;
		}
		else
		{
			return longitude;
		}
	}

	protected static double fixOverlappedLatitude(final double latitude)
	{
		if (latitude > 90)
		{
			return 90;
		}
		else if (latitude < -90)
		{
			return -90;
		}
		else
		{
			return latitude;
		}
	}

	public static boolean isPointInEurope(final GPS point)
	{
		try
		{
			// look at: http://en.wikipedia.org/wiki/Extreme_points_of_Europe
			final double northernmostPoint = GeolocationMaths.dms2Decimal(new int[]
			{ 81, 48, 24 });
			final double southernmostPoint = GeolocationMaths.dms2Decimal(new int[]
			{ 34, 48, 2 });
			final double westernmostPoint = GeolocationMaths.dms2Decimal(new int[]
			{ -24, 32, 3 });
			final double easternmostPoint = GeolocationMaths.dms2Decimal(new int[]
			{ 69, 2, 0 });

			return northernmostPoint > point.getDecimalLatitude() && southernmostPoint < point.getDecimalLatitude()
					&& westernmostPoint < point.getDecimalLongitude() && easternmostPoint > point.getDecimalLongitude();
		}
		catch (final GeoLocatorException ex)
		{
			throw new IllegalArgumentException(ex);
		}
	}

}
