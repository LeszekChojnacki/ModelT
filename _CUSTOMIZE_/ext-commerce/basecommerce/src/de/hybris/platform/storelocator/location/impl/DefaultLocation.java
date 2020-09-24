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
package de.hybris.platform.storelocator.location.impl;

import de.hybris.platform.storelocator.location.DistanceAwareLocation;
import de.hybris.platform.storelocator.location.Location;
import de.hybris.platform.storelocator.model.PointOfServiceModel;


/**
 * Distance aware implementation of the {@link Location}. Validates during creation if given distance is not null.
 *
 */
public class DefaultLocation extends DistanceUnawareLocation implements DistanceAwareLocation
{
	private final Double distance;

	public DefaultLocation(final PointOfServiceModel posModel, final Double distance)
	{
		super(posModel);
		if (distance == null)
		{
			throw new IllegalArgumentException("Provided distance should be not null");
		}
		this.distance = distance;
	}

	@Override
	public Double getDistance()
	{
		return distance;
	}

	@Override
	public int compareTo(final DistanceAwareLocation distanceAwareLocation)
	{
		return this.getDistance().compareTo(distanceAwareLocation.getDistance());
	}


}
