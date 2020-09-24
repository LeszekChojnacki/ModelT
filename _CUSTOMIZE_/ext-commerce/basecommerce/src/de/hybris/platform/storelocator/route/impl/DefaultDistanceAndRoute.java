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
package de.hybris.platform.storelocator.route.impl;

import de.hybris.platform.storelocator.route.DistanceAndRoute;
import de.hybris.platform.storelocator.route.Route;


/**
 * 
 */
public class DefaultDistanceAndRoute implements DistanceAndRoute
{
	/**
	 * @param roadDistance
	 * @param eagleFliesDistance
	 * @param route
	 */
	public DefaultDistanceAndRoute(final double roadDistance, final double eagleFliesDistance, final Route route)
	{
		super();
		this.roadDistance = roadDistance;
		this.eagleFliesDistance = eagleFliesDistance;
		this.route = route;
	}

	/**
	 * @param route
	 */
	public DefaultDistanceAndRoute(final Route route)
	{
		super();
		this.route = route;
	}

	private double roadDistance;
	private double eagleFliesDistance;

	private final Route route;

	@Override
	public Route getRoute()
	{
		return route;
	}

	/**
	 * @return the roadDistance
	 */
	@Override
	public double getRoadDistance()
	{
		return roadDistance;
	}

	/**
	 * @return the eagleFliesDistance
	 */
	@Override
	public double getEagleFliesDistance()
	{
		return eagleFliesDistance;
	}

}
