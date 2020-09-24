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

import de.hybris.platform.storelocator.exception.GeoLocatorException;


/**
 * Utility class providing useful coordinates conversions
 *
 */
public class GeolocationMaths
{
	public static final int SIGNIFICANT_DIGITS = 6;

	private GeolocationMaths()
	{
		// prevent instantiation
	}

	/**
	 * Converts decimal representation to DMS coordinate representation (degree, minute, second)
	 *
	 * @param decimal
	 * @return primitive array int[]
	 * @throws GeoLocatorException
	 */
	public static int[] decimal2DMS(double decimal)
	{
		GeolocationUtils.validateLongitude(decimal);

		final int[] dmc =
		{ 0, 0, 0 };
		final int sign = decimal > 0 ? 1 : -1;
		decimal = Math.abs(decimal); //NOSONAR
		dmc[0] = (int) Math.floor(decimal);

		decimal -= dmc[0]; //NOSONAR
		decimal *= 60; //NOSONAR
		dmc[1] = (int) Math.floor(Math.abs(decimal));

		decimal -= dmc[1]; //NOSONAR
		decimal *= 60; // NOSONAR
		dmc[2] = (int) Math.round(Math.abs(decimal));
		dmc[0] *= sign;

		return dmc;
	}

	/**
	 * Converts DMS coordinate representation to decimal
	 *
	 * @param dms
	 * @return double
	 * @throws GeoLocatorException
	 */
	public static double dms2Decimal(final int[] dms)
	{
		if (dms == null || dms.length != 3)
		{
			throw new GeoLocatorException("Invalid Degree, Minutes, Seconds format");
		}
		final int sign = dms[0] > 0 ? 1 : -1;
		double decimal = 0d;
		decimal += Math.abs(dms[0]);
		final double secondsTotal = dms[1] * 60 + dms[2]; //NOSONAR
		decimal += secondsTotal / 3600;

		return truncateDecimal(decimal) * sign;
	}

	public static double truncateDecimal(final double decimal)
	{
		final double factor = Math.pow(10, SIGNIFICANT_DIGITS);
		return Math.rint(decimal * factor) / factor;
	}
}
