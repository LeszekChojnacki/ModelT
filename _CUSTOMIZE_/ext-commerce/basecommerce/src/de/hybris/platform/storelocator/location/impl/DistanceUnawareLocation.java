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

import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.storelocator.GPS;
import de.hybris.platform.storelocator.data.AddressData;
import de.hybris.platform.storelocator.exception.GeoLocatorException;
import de.hybris.platform.storelocator.exception.LocationInstantiationException;
import de.hybris.platform.storelocator.impl.DefaultGPS;
import de.hybris.platform.storelocator.location.Location;
import de.hybris.platform.storelocator.model.PointOfServiceModel;

import java.io.Serializable;


/**
 * Base implementation of the 'not distance aware' {@link Location}
 */
public class DistanceUnawareLocation implements Location, Serializable
{
	private final PointOfServiceModel posModel;
	private GPS gps;

	/**
	 * creates a new location using the given point of service.
	 *
	 * @param posModel
	 *           the point of service to use
	 * @throws LocationInstantiationException
	 *            if the point of service has invalid geo location data
	 */
	public DistanceUnawareLocation(final PointOfServiceModel posModel)
	{
		this.posModel = posModel;
		if (posModel.getLatitude() != null && posModel.getLongitude() != null)
		{
			try
			{
				this.gps = new DefaultGPS().create(posModel.getLatitude().doubleValue(), posModel.getLongitude().doubleValue());
			}
			catch (final GeoLocatorException e)
			{
				throw new LocationInstantiationException("GPS data contained in the POS entry is invalid", e);
			}
		}
	}

	@Override
	public AddressData getAddressData()
	{
		final AddressModel address = posModel.getAddress();
		if (address != null)
		{
			final String street = address.getStreetname();
			final String city = address.getTown();
			final String buildingNo = address.getStreetnumber();
			final String postCode = address.getPostalcode();
			final AddressData result = new AddressData(getName(), street, buildingNo, postCode, city, getCountry());
			return result;
		}
		return null;
	}


	@Override
	public String getCountry()
	{
		final AddressModel address = posModel.getAddress();
		if (address != null && address.getCountry() != null)
		{
			return address.getCountry().getIsocode();
		}
		return null;
	}


	@Override
	public GPS getGPS()
	{
		return gps;
	}


	@Override
	public String getName()
	{
		if (posModel != null)
		{
			return posModel.getName();
		}
		return null;
	}

	@Override
	public String getTextualAddress()
	{
		if (getAddressData() != null)
		{
			return getAddressData().toString();
		}
		return null;
	}


	@Override
	public String getDescription()
	{
		if (posModel != null)
		{
			return posModel.getDescription();
		}
		return null;
	}


	@Override
	public String getMapIconUrl()
	{
		if (posModel != null && posModel.getMapIcon() != null)
		{
			return posModel.getMapIcon().getURL();
		}
		return null;
	}


	@Override
	public String getType()
	{
		if (posModel != null)
		{
			return posModel.getType().toString();
		}
		return null;
	}


}
