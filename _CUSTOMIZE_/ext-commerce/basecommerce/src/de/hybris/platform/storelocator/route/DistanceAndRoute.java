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
package de.hybris.platform.storelocator.route;

/**
 * Wrapps a route and distance information between two points on the map.
 * 
 * 
 */
public interface DistanceAndRoute
{

	/**
	 * Get the route representation, which holds the start and destination and also all the implementation dependant data
	 * necessary to display a route on a map
	 * 
	 * @return {@link Route}
	 */
	Route getRoute();

	/**
	 * Get road distance
	 * 
	 * @return double
	 */
	double getRoadDistance();

	/**
	 * get distance in straight line.
	 * 
	 * @return double
	 */
	double getEagleFliesDistance();
}
