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

/**
 * Represents a location that keeps information to a reference GPS position. Such location may be used as Points Of
 * Interest on a map. Such POIs can be easily sorted by the distance value.
 */
public interface DistanceAwareLocation extends Location, Comparable<DistanceAwareLocation>
{
	/**
	 * returns distance [km] to reference GPS position
	 */
	Double getDistance();
}
