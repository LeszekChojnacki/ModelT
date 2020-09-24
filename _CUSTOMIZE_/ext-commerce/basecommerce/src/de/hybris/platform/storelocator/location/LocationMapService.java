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
package de.hybris.platform.storelocator.location;

import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.storelocator.GPS;
import de.hybris.platform.storelocator.exception.LocationMapServiceException;
import de.hybris.platform.storelocator.exception.MapServiceException;
import de.hybris.platform.storelocator.map.Map;


/**
 * Main purpose of this service's is to provide {@link Map} objects for {@link Location} objects depending on given
 * parameters such as postal code, country code, town name or GPS coordinates.
 *
 * @see LocationMapService#getMapOfLocations
 * @see LocationMapService#getMapOfLocationsForTown
 * @see LocationMapService#getMapOfLocationsForPostcode
 */
public interface LocationMapService
{
	/**
	 * Method looks for nearby locations for given postal code or town name and creates Map object with found data.
	 *
	 * @param searchTerm
	 *           the search term - can be postal code or town name
	 * @param countryCode
	 *           the country code - country code to look for (valid if given value is postal code)
	 * @param limitLocationsCount
	 *           number of locations to return
	 * @param baseStore
	 *           if set, method will return locations bound to the given {@link BaseStoreModel}
	 * @return map object with found locations
	 * @throws LocationMapServiceException
	 */
	Map getMapOfLocations(String searchTerm, String countryCode, int limitLocationsCount, BaseStoreModel baseStore);

	/**
	 * Method looks for nearest locations (points of interest) to the place defined by postal code and country, and
	 * creates Map object with given data. Number of returned locations is adequate to user definition. Use when map
	 * representation is required.
	 *
	 * @param postalCode
	 *           searching condition for locations - postal code
	 * @param countryCode
	 *           searching condition for locations - country code
	 * @param limitLocationsCount
	 *           number of locations to return
	 * @param baseStore
	 *           if set, method will return locations bound to the given {@link BaseStoreModel}
	 * @return map object with found locations
	 * @throws LocationMapServiceException
	 */
	Map getMapOfLocationsForPostcode(String postalCode, String countryCode, int limitLocationsCount, BaseStoreModel baseStore);

	/**
	 * Method looks for nearest locations (points of interest) to the place defined by city name, and creates Map object
	 * with given data. Number of returned locations is adequate to user definition. Use when map representation is
	 * required.
	 *
	 * @param town
	 *           searching condition for locations
	 * @param limitLocationsCount
	 *           number of locations to return
	 * @param baseStore
	 *           if set, method will return locations bound to the given {@link BaseStoreModel}
	 * @return map object with found locations
	 * @throws LocationMapServiceException
	 */
	Map getMapOfLocationsForTown(String town, int limitLocationsCount, BaseStoreModel baseStore);

	/**
	 * Calculating Map, basing on number of points of interest defined by user and proper radius.
	 *
	 * @param gps
	 *           coordinates of center point
	 * @param limitLocationsCount
	 *           number of places to find, defined by user
	 * @param baseStore
	 *           if set, method will return locations bound to the given {@link BaseStoreModel}
	 * @return map from store locator map service
	 * @throws LocationMapServiceException
	 * @throws MapServiceException
	 */
	Map getMapOfLocations(final GPS gps, final int limitLocationsCount, final BaseStoreModel baseStore);
}
