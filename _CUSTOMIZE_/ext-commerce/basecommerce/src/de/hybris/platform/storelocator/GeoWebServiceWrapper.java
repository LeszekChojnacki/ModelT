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
package de.hybris.platform.storelocator;

import de.hybris.platform.storelocator.data.AddressData;
import de.hybris.platform.storelocator.exception.GeoServiceWrapperException;
import de.hybris.platform.storelocator.location.Location;
import de.hybris.platform.storelocator.route.DistanceAndRoute;



/**
 * Hides communication that resides behind geo-coding and direction fetching
 */
public interface GeoWebServiceWrapper
{

	/**
	 * Translate IAddress to IGPS using 3rd party service
	 *
	 * @param address
	 * @return IGPS
	 * @throws GeoServiceWrapperException
	 */
	GPS geocodeAddress(final Location address);

	/**
	 * Translate AddressData to IGPS using 3rd party service
	 *
	 * @param address
	 * @return IGPS
	 * @throws GeoServiceWrapperException
	 */
	GPS geocodeAddress(final AddressData address);

	/**
	 * Get distance and route that is between two addresses
	 *
	 * @param start
	 *           {@link Location}
	 * @param dest
	 *           {@link Location}
	 * @return {@link DistanceAndRoute}
	 * @throws GeoServiceWrapperException
	 */
	DistanceAndRoute getDistanceAndRoute(final Location start, final Location dest);

	/**
	 * Get distance and route that is between start GPS location and destination address
	 *
	 * @param start
	 *           {@link GPS}
	 * @param dest
	 *           {@link Location}
	 * @return {@link DistanceAndRoute}
	 * @throws GeoServiceWrapperException
	 */
	DistanceAndRoute getDistanceAndRoute(final GPS start, final Location dest);

	/**
	 * Formats the address textual information depending on the implementation
	 *
	 * @param address
	 *           {@link Location}
	 * @return {@link String}
	 * @throws GeoServiceWrapperException
	 */
	String formatAddress(final Location address);
}
