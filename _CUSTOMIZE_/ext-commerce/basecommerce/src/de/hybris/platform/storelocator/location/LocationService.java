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

import java.util.List;


/**
 * Handles CRUD operations of {@link Location}. It is able to return Locations in the proximity of the given GPS that
 * are sorted in terms of distances.
 *
 * @param <T>
 * 		the Location (sub)type
 */
public interface LocationService<T extends Location>
{

	/**
	 * Get all known addresses which are located in the circle centered in the given gps location and a given radius
	 *
	 * @param gps
	 * 		GPS coordinates
	 * @param distance
	 * 		in kilometers
	 * @return List of addresses
	 */
	List<T> getLocationsNearby(GPS gps, double distance);

	/**
	 * Get all known addresses which are located in the circle centered in the given gps location and a given radius,
	 * which belong to a given store
	 *
	 * @param gps
	 * 		GPS coordinates
	 * @param distance
	 * 		in kilometers
	 * @param baseStore
	 * 		base store
	 * @return List of addresses
	 */
	List<T> getLocationsNearby(GPS gps, double distance, BaseStoreModel baseStore);


	/**
	 * Adds a new address or update existing one
	 *
	 * @param location
	 * 		GPS coordinates
	 * @return true if operation was successful
	 */
	boolean saveOrUpdateLocation(T location);

	/**
	 * Delete the given IAddress from the collection of stored addresses
	 *
	 * @param location
	 * 		GPS coordinates
	 * @return true if address was deleted
	 */
	boolean deleteLocation(T location);

	/**
	 * Returns persisted Location by name
	 *
	 * @param name
	 * 		name of location
	 * @return {@link Location} matched by unique name
	 */
	T getLocationByName(String name);

	/**
	 * Get temporary, non-persisted location
	 *
	 * @param streetName
	 * 		street name
	 * @param streetNumber
	 * 		street number
	 * @param postalCode
	 * 		postal code
	 * @param town
	 * 		name of town
	 * @param countryCode
	 * 		country code
	 * @param geocode
	 * 		- set to true if return {@link Location} should have {@link GPS} data populated
	 * @return {@link Location}
	 */
	T getLocation(String streetName, String streetNumber, String postalCode, String town, String countryCode, boolean geocode);

	/**
	 * Method returns the nearest locations (points of interest) to the given coordinates, in the number adequate to user
	 * definition. Use when you have got defined coordinates and need only sorted locations nearby, without any map.
	 *
	 * @param gps
	 * 		coordinates of center point
	 * @param limitLocationsCount
	 * 		number of POIs chosen by user
	 * @param baseStore
	 * 		if set, method will return locations bound to the given {@link BaseStoreModel}
	 * @return list of proper Locations
	 */
	List<T> getLocationsForPoint(GPS gps, int limitLocationsCount, BaseStoreModel baseStore);

	/**
	 * Method looks for nearby locations (points of interest) in the place defined by search term (that can be postal
	 * code or town name) and country. Basically it combines two other methods that retrieves locations for town name and
	 * postal code.
	 *
	 * @param searchTerm
	 * 		the search term - can be postal code or town name
	 * @param countryCode
	 * 		the country code - country code to look for (valid if given value is postal code)
	 * @param limitLocationsCount
	 * 		number of locations to return
	 * @param baseStore
	 * 		if set, method will return locations bound to the given {@link BaseStoreModel}
	 * @return list of found locations
	 */
	List<T> getLocationsForSearch(String searchTerm, String countryCode, int limitLocationsCount, BaseStoreModel baseStore);

	/**
	 * Method looks for nearest locations (points of interest) to the place defined by postal code and country. Number of
	 * returned locations is adequate to user definition. Use when you don't need any map, only list of sorted locations
	 * is enough.
	 *
	 * @param postalCode
	 * 		searching condition for locations - postal code
	 * @param countryCode
	 * 		searching condition for locations - country code
	 * @param limitLocationsCount
	 * 		number of locations to return
	 * @param baseStore
	 * 		if set, method will return locations bound to the given {@link BaseStoreModel}
	 * @return list of found locations
	 */
	List<T> getLocationsForPostcode(String postalCode, String countryCode, int limitLocationsCount, BaseStoreModel baseStore);

	/**
	 * Method looks for nearest locations (points of interest) to the place defined by city name. Number of returned
	 * locations is adequate to user definition. Use when you don't need any map, only list of sorted locations is
	 * enough.
	 *
	 * @param town
	 * 		searching condition for locations
	 * @param limitLocationsCount
	 * 		number of locations to return
	 * @param baseStore
	 * 		if set, method will return locations bound to the given {@link BaseStoreModel}
	 * @return list of found locations
	 */
	List<T> getLocationsForTown(String town, int limitLocationsCount, BaseStoreModel baseStore);

	/**
	 * Returns list of sorted {@link DistanceAwareLocation}s. Sorting order is ascending - the closest locations come
	 * first.
	 *
	 * @param gps
	 * 		- reference location. Center.
	 * @param distance
	 * 		- radius in [km]. The returned locations are contained in the square that the circle describes.
	 * @param baseStore
	 * 		- if set, method will return locations bound to the given {@link BaseStoreModel}
	 * @return a sorted list of nearby locations
	 */
	List<T> getSortedLocationsNearby(final GPS gps, final double distance, final BaseStoreModel baseStore);
}
