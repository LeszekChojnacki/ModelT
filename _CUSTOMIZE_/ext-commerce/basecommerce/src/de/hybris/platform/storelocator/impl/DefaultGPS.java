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
package de.hybris.platform.storelocator.impl;

import de.hybris.platform.storelocator.GPS;
import de.hybris.platform.storelocator.constants.GeolocationConstants;
import de.hybris.platform.storelocator.constants.GeolocationMaths;
import de.hybris.platform.storelocator.constants.GeolocationUtils;

import java.text.DecimalFormat;


/**
 *
 */
public class DefaultGPS implements GPS
{
	//accessible properties
	private double latitude;
	private double longitude;

	//internal private, internal purposes.
	private int[] latitudeDMS;
	private int[] longitudeDMS;

	static final long serialVersionUID = 999153875285740630L;

	public DefaultGPS()
	{
		super();
	}

	/**
	 * @param longitude
	 * @param latitude
	 */
	public DefaultGPS(final double latitude, final double longitude)
	{
		super();
		this.latitude = latitude;
		this.longitude = longitude;
	}

	/**
	 * @param longitudeDMS
	 * @param latitudeDMS
	 */
	public DefaultGPS(final int[] latitudeDMS, final int[] longitudeDMS)
	{
		super();
		this.latitudeDMS = latitudeDMS;
		this.longitudeDMS = longitudeDMS;
	}


	@Override
	public GPS create(final String latitude, final String longitude)
	{
		if (GeolocationUtils.validateDMSLatitude(latitude) && GeolocationUtils.validateDMSLongitude(longitude))
		{
			final DefaultGPS gps = new DefaultGPS(GeolocationUtils.separateDMS(latitude), GeolocationUtils.separateDMS(longitude));
			gps.latitude = GeolocationMaths.dms2Decimal(gps.latitudeDMS);
			gps.longitude = GeolocationMaths.dms2Decimal(gps.longitudeDMS);
			return gps;
		}
		else
		{
			return null;
		}
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.platform.geolocation.IGPS#create(double, double)
	 */
	@Override
	public GPS create(final double latitude, final double longitude)
	{
		if (GeolocationUtils.validateLatitude(latitude) && GeolocationUtils.validateLongitude(longitude))
		{
			final DefaultGPS gps = new DefaultGPS(latitude, longitude);
			gps.latitudeDMS = GeolocationMaths.decimal2DMS(latitude);
			gps.longitudeDMS = GeolocationMaths.decimal2DMS(longitude);
			return gps;
		}
		else
		{
			return null;
		}
	}


	@Override
	public double getDecimalLatitude()
	{
		return this.latitude;
	}

	@Override
	public double getDecimalLongitude()
	{
		return this.longitude;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.platform.geolocation.IGPS#toDMSString()
	 */
	@Override
	public String toDMSString()
	{
		return String.format("(%1$d\u00b0%2$d'%3$d\"%4$s, %5$d\u00b0%6$d'%7$d\"%8$s)",
				Integer.valueOf(Math.abs(this.latitudeDMS[0])), Integer.valueOf(this.latitudeDMS[1]),
				Integer.valueOf(this.latitudeDMS[2]), this.latitudeDMS[0] > 0 ? "N" : "S",
				Integer.valueOf(Math.abs(this.longitudeDMS[0])), Integer.valueOf(this.longitudeDMS[1]),
				Integer.valueOf(this.longitudeDMS[2]), this.longitudeDMS[0] > 0 ? "E" : "W");

	}

	@Override
	public String toString()
	{
		final DecimalFormat format = new DecimalFormat(GeolocationConstants.DECIMAL_COORDINATES_FORMAT);
		return "(" + format.format(latitude) + ", " + format.format(longitude) + ")";
	}


	@Override
	public String toGeocodeServiceFormat()
	{
		final DecimalFormat format = new DecimalFormat(GeolocationConstants.DECIMAL_COORDINATES_FORMAT);
		return format.format(latitude).replace(',', '.') + ", " + format.format(longitude).replace(',', '.');
	}



}
