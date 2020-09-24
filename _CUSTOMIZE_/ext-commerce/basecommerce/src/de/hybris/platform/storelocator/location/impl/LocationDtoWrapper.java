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

import de.hybris.platform.storelocator.GPS;
import de.hybris.platform.storelocator.data.AddressData;
import de.hybris.platform.storelocator.exception.GeoLocatorException;
import de.hybris.platform.storelocator.impl.DefaultGPS;
import de.hybris.platform.storelocator.location.Location;

import org.apache.log4j.Logger;


/**
 * Implementation of {@link Location} applicable for {@link LocationDTO} originating from external services
 */
public class LocationDtoWrapper implements Location
{

	private static final Logger LOGGER = Logger.getLogger(LocationDtoWrapper.class.getName());
	private LocationDTO locationDto;
	private AddressData addressData;
	private GPS gps;

	/**
	 * @param addressData
	 * @param gps
	 */
	public LocationDtoWrapper(final AddressData addressData, final GPS gps)
	{
		super();
		this.addressData = addressData;
		this.gps = gps;
	}

	public LocationDtoWrapper(final LocationDTO locationDto)
	{
		super();
		this.locationDto = locationDto;
	}

	@Override
	public AddressData getAddressData()
	{
		if (this.addressData != null)
		{
			return this.addressData;
		}
		else if (locationDto != null)
		{
			final String street = locationDto.getStreet();
			final String city = locationDto.getCity();
			final String buildingNo = locationDto.getBuildingNo();
			final String postCode = locationDto.getPostalCode();
			this.addressData = new AddressData(getName(), street, buildingNo, postCode, city, getCountry());
			return this.addressData;
		}
		return null;
	}

	@Override
	public String getCountry()
	{
		if (locationDto != null)
		{
			return locationDto.getCountryIsoCode();
		}
		return null;
	}

	@Override
	public String getDescription()
	{
		if (locationDto != null)
		{
			return locationDto.getDescription();
		}
		return null;
	}

	@Override
	public GPS getGPS()
	{
		if (this.gps != null)
		{
			return gps;
		}
		else
		{
			this.gps = extractGPS(locationDto);
			return this.gps;
		}
	}

	@Override
	public String getMapIconUrl()
	{
		if (locationDto != null)
		{
			return locationDto.getMapIconUrl();
		}
		return null;
	}

	@Override
	public String getName()
	{
		if (locationDto != null)
		{
			return locationDto.getName();
		}
		return null;
	}

	@Override
	public String getTextualAddress()
	{
		final AddressData address = getAddressData();
		if (address != null)
		{
			return address.toString();
		}
		return null;
	}

	private GPS extractGPS(final LocationDTO locationDTO)
	{
		if (locationDTO != null && locationDTO.getLatitude() != null && locationDTO.getLongitude() != null)
		{
			try
			{
				return new DefaultGPS().create(Double.parseDouble(locationDTO.getLatitude()),
						Double.parseDouble(locationDTO.getLongitude()));
			}
			catch (final NumberFormatException e)
			{
				LOGGER.error("Latitude and longitude in the LocationDTO must be numbers");
			}
			catch (final GeoLocatorException e)
			{
				LOGGER.error("Latitude and longitude in the LocationDTO must be valid geographical coordinates", e);
			}
		}
		return null;
	}

	@Override
	public String getType()
	{
		if (locationDto != null)
		{
			return locationDto.getType();
		}
		return null;
	}
}
