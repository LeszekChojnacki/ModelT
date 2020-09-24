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
package de.hybris.platform.storelocator.map;

import de.hybris.platform.storelocator.GPS;
import de.hybris.platform.storelocator.location.Location;
import de.hybris.platform.storelocator.map.markers.KmlDocument;
import de.hybris.platform.storelocator.map.utils.MapBounds;
import de.hybris.platform.storelocator.route.DistanceAndRoute;

import java.util.List;


/**
 * Represents of map in the store locator service
 * 
 */
public interface Map
{
	/**
	 * Get the value of map radius in kilometers
	 * 
	 * @return radius
	 */
	double getRadius();

	/**
	 * Get the POIs marked on the map.
	 * 
	 * @return List
	 */
	List<Location> getPointsOfInterest();

	/**
	 * Get the map's title
	 * 
	 * @return {@link List} of {@link Location}
	 */
	String getTitle();

	/**
	 * Get the map's image
	 * 
	 * @return {@link KmlDocument}
	 */
	KmlDocument getKml();

	/**
	 * get the IGPS representing the map's central point.
	 * 
	 * @return {@link GPS}
	 */
	GPS getGps();

	/**
	 * get the route element form the map;
	 * 
	 * @return {@link DistanceAndRoute}
	 */
	DistanceAndRoute getDistanceAndRoute();

	/**
	 * returns the {@link MapBounds} of the map instance. Mapbounds consists of NW and SE corners.
	 */
	MapBounds getMapBounds();
}
