/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 *
 */
package de.hybris.platform.warehousing.sourcing.context.util;

/**
 * Calculate the distance between 2 points on the surface on the planet. This formula uses the radius of the Earth in
 * kilometers as 6372.8 km.
 */
public class HaversineCalculator
{
	private static final double RADIUS = 6372.8; // In kilometers

	private HaversineCalculator()
	{
	}

	/**
	 * Get the distance between 2 points on the surface of the planet (in kilometers).
	 *
	 * @param lat1
	 * 		- the latitude of the first point
	 * @param lon1
	 * 		- the longitude of the first point
	 * @param lat2
	 * 		- the latitude of the second point
	 * @param lon2
	 * 		- the longitude of the second point
	 * @return the distance between the 2 points
	 */
	public static double calculate(final double lat1, final double lon1, final double lat2, final double lon2)
	{
		final double dLat = Math.toRadians(lat2 - lat1);
		final double dLon = Math.toRadians(lon2 - lon1);
		final double latitude1 = Math.toRadians(lat1);
		final double latitude2 = Math.toRadians(lat2);

		final double a =
				Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(latitude1) * Math
						.cos(latitude2);
		final double c = 2 * Math.asin(Math.sqrt(a));
		return RADIUS * c;
	}
}
