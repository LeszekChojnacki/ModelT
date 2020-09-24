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
package de.hybris.platform.storelocator.constants;

import static java.util.Objects.nonNull;

import de.hybris.platform.storelocator.exception.GeoLocatorException;



/**
 * Utility class providing coordinates validation methods
 */
public class GeolocationUtils
{
	private static final String IS_INVALID = " is invalid";

	private GeolocationUtils()
	{
		// prevent instantiation
	}

	/**
	 * Validates latitude in DMS format
	 *
	 * @param latitude
	 * @return boolean
	 * @throws GeoLocatorException
	 */
	public static boolean validateDMSLatitude(final String latitude)
	{
		if (latitude == null)
		{
			throw new GeoLocatorException("Latitude is required");
		}
		if (!latitude.matches(GeolocationConstants.DMS_LATITUDE_PATTERN))
		{
			throw new GeoLocatorException("Latitude: " + latitude + IS_INVALID);
		}
		else
		{
			//check additionally boundary conditions
			final String trmdLatitude = latitude.trim();
			final String latitudeDegrees = trmdLatitude.substring(0, trmdLatitude.indexOf(GeolocationConstants.DMS_DEGREE)).trim();
			final String theRest = trmdLatitude.substring(trmdLatitude.indexOf(GeolocationConstants.DMS_DEGREE)).trim();
			//if the degree part is equal to 90, the minute and second parts are only allowed to be 0
			if (Integer.parseInt(latitudeDegrees) == 90 && theRest.matches("(.*)[1-9](.*)"))
			{
				throw new GeoLocatorException("Latitude [dms] " + trmdLatitude + IS_INVALID);
			}
			return true;
		}
	}

	/**
	 * Validates longitude in DMS format
	 *
	 * @param longitude
	 * @return boolean
	 * @throws GeoLocatorException
	 */
	public static boolean validateDMSLongitude(final String longitude)
	{
		if (longitude == null)
		{
			throw new GeoLocatorException("Longitude is required");
		}
		if (!longitude.matches(GeolocationConstants.DMS_LONGITUDE_PATTERN))
		{
			throw new GeoLocatorException("Longitude " + longitude + IS_INVALID);
		}
		else
		{
			//check additionally boundary conditions
			final String trmdLongitude = longitude.trim();
			final String latitudeDegrees = trmdLongitude.substring(0, trmdLongitude.indexOf(GeolocationConstants.DMS_DEGREE)).trim();
			final String theRest = trmdLongitude.substring(trmdLongitude.indexOf(GeolocationConstants.DMS_DEGREE)).trim();
			//if degree part is equal to 180, the minute and second parts are only allowed to be 0
			if (Integer.parseInt(latitudeDegrees) == 180 && theRest.matches("(.*)[1-9](.*)"))
			{
				throw new GeoLocatorException("Longitude [dms] " + trmdLongitude + IS_INVALID);
			}
			return true;
		}
	}

	/**
	 * Validates decimal longitude
	 *
	 * @param longitude
	 * @return boolean
	 * @throws GeoLocatorException
	 */
	public static boolean validateLongitude(final double longitude)
	{
		if (longitude < -180 || longitude > 180)
		{
			throw new GeoLocatorException("Longitude out of range :" + longitude);
		}
		else
		{
			return true;
		}
	}

	/**
	 * Validates decimal latitude
	 *
	 * @param latitude
	 * @return boolean
	 * @throws GeoLocatorException
	 */
	public static boolean validateLatitude(final double latitude)
	{
		if (latitude < -90 || latitude > 90)
		{
			throw new GeoLocatorException("Latitude out of range :" + latitude);
		}
		else
		{
			return true;
		}
	}

	/**
	 * Extracts particular DMS values from string
	 *
	 * @param dms
	 * @return array of primitives int[]
	 */
	public static int[] separateDMS(final String dms)
	{
		int sign = 1;
		final int[] result =
		{ 0, 0, 0 };
		final String noSpacesDms = removeSpaces(dms);
		final String degrees = getSubstringBetween(noSpacesDms, null, GeolocationConstants.DMS_DEGREE);
		if (degrees != null)
		{
			result[0] = Integer.parseInt(degrees);
		}
		final String minutes = getSubstringBetween(noSpacesDms, GeolocationConstants.DMS_DEGREE, GeolocationConstants.DMS_MINUTES);
		if (minutes != null)
		{
			result[1] = Integer.parseInt(minutes);
		}
		final String seconds = getSubstringBetween(noSpacesDms, GeolocationConstants.DMS_MINUTES, GeolocationConstants.DMS_SECONDS);
		if (seconds != null)
		{
			result[2] = Integer.parseInt(seconds);
		}
		if (nonNull(noSpacesDms) && (noSpacesDms.contains("S") || dms.contains("W")))
		{
			sign *= -1;
		}
		result[0] *= sign;
		return result;
	}

	protected static String removeSpaces(final String input)
	{
		return input.replaceAll("\\s", "");
	}

	protected static String getSubstringBetween(final String input, final String subFrom, final String subTo)
	{
		String substring = input;
		if (subFrom != null)
		{
			if (!input.contains(subFrom))
			{
				return null;
			}
			substring = input.substring(input.indexOf(subFrom) + 1);
		}
		if (subTo != null)
		{
			if (!substring.contains(subTo))
			{
				return null;
			}
			substring = substring.substring(0, substring.indexOf(subTo));
		}

		return substring;
	}

}
