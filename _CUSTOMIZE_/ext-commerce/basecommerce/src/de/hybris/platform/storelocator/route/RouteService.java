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
import de.hybris.platform.storelocator.exception.RouteServiceException;
import de.hybris.platform.storelocator.location.Location;


/**
 * Query point for routes and distances
 */
public interface RouteService
{
	/**
	 * Get {@link DistanceAndRoute} that two {@link Location}es create
	 *
	 * @param start
	 * 		{@link Location}
	 * @param dest
	 * 		{@link Location}
	 * @return {@link DistanceAndRoute}
	 * @throws RouteServiceException
	 * 		in the case of error during route retrieval
	 */
	DistanceAndRoute getDistanceAndRoute(Location start, Location dest) throws RouteServiceException;

	/**
	 * Get {@link DistanceAndRoute} that is between {@link GPS} and {@link Location}
	 *
	 * @param start
	 * 		{@link GPS}
	 * @param dest
	 * 		{@link Location}
	 * @return {@link DistanceAndRoute}
	 * @throws RouteServiceException
	 * 		in the case of error during route retrieval
	 */
	DistanceAndRoute getDistanceAndRoute(GPS start, Location dest) throws RouteServiceException;
}
