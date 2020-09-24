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

import de.hybris.platform.storelocator.GPS;
import de.hybris.platform.storelocator.location.Location;
import de.hybris.platform.storelocator.map.markers.KmlRoute;





/**
 * Represents route information between two points
 * 
 */
public interface Route extends KmlRoute
{

	/**
	 * Get the string representation of the route. Contains implementation dependant data
	 * 
	 * @return {@link String}
	 */
	String getCoordinates();

	/**
	 * Get route's start
	 * 
	 * @return {@link GPS}
	 */
	GPS getStart();

	/**
	 * Get route's destination
	 * 
	 * @return {@link Location}
	 */
	Location getDestination();
}
