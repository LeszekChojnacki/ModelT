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
package de.hybris.platform.storelocator.map.utils;

import de.hybris.platform.storelocator.GPS;
import de.hybris.platform.storelocator.exception.GeoLocatorException;
import de.hybris.platform.storelocator.impl.GeometryUtils;

import java.util.List;


/**
 *
 */
public class MapBounds
{

	private final GPS northEast;
	private final GPS southWest;

	public MapBounds(final GPS center, final double radius) throws GeoLocatorException
	{
		final List<GPS> corners = GeometryUtils.getSquareOfTolerance(center, radius);
		this.southWest = corners.get(0);
		this.northEast = corners.get(1);

	}

	public MapBounds(final GPS northEast, final GPS southWest) throws GeoLocatorException
	{
		this.southWest = southWest;
		this.northEast = northEast;

	}


	/**
	 * @return the northWest
	 */
	public GPS getNorthEast()
	{
		return northEast;
	}

	/**
	 * @return the southEast
	 */
	public GPS getSouthWest()
	{
		return southWest;
	}



}
