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

import de.hybris.platform.storelocator.GPS;
import de.hybris.platform.storelocator.GeoWebServiceWrapper;
import de.hybris.platform.storelocator.exception.GeoServiceWrapperException;
import de.hybris.platform.storelocator.exception.RouteServiceException;
import de.hybris.platform.storelocator.location.Location;
import de.hybris.platform.storelocator.route.DistanceAndRoute;
import de.hybris.platform.storelocator.route.RouteService;

import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of the RouteService.
 */
public class DefaultRouteService implements RouteService
{
	private GeoWebServiceWrapper geoServiceWrapper;

	@Override
	public DistanceAndRoute getDistanceAndRoute(final Location start, final Location dest)
	{
		try
		{
			return getGeoServiceWrapper().getDistanceAndRoute(start, dest);
		}
		catch (final GeoServiceWrapperException e)
		{
			throw new RouteServiceException(e);
		}
	}

	@Override
	public DistanceAndRoute getDistanceAndRoute(final GPS start, final Location dest)
	{
		try
		{
			return getGeoServiceWrapper().getDistanceAndRoute(start, dest);
		}
		catch (final GeoServiceWrapperException e)
		{
			throw new RouteServiceException(e);
		}
	}

	protected GeoWebServiceWrapper getGeoServiceWrapper()
	{
		return geoServiceWrapper;
	}

	@Required
	public void setGeoServiceWrapper(final GeoWebServiceWrapper geoServiceWrapper)
	{
		this.geoServiceWrapper = geoServiceWrapper;
	}

}
