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
package de.hybris.platform.storelocator;

import de.hybris.platform.storelocator.exception.GeoLocatorException;

import java.io.Serializable;


/**
 * Represents GPS location of a point
 */
public interface GPS extends Serializable
{
	/**
	 * Returns the decimal representation of longitude
	 *
	 * @return double
	 */
	double getDecimalLongitude();

	/**
	 * Returns the decimal representation of latitude
	 *
	 * @return double
	 */
	double getDecimalLatitude();

	/**
	 * Creates IGPS basing on the decimal longitude and latitude
	 *
	 * @param longitude
	 * @param latitude
	 * @return IGPS
	 * @throws GeoLocatorException
	 */
	GPS create(String latitude, String longitude);

	/**
	 * Creates IGPS basing on the string DMS(Degrees, Minutes, Second) representations of longitude and latitude
	 *
	 * @param longitude
	 * @param latitude
	 * @return IGPS
	 * @throws GeoLocatorException
	 */
	GPS create(double latitude, double longitude);

	/**
	 * Returns decimal representation of coordinates
	 *
	 * @return String
	 */
	@Override
	String toString();//NOPMD

	/**
	 * Returns DMS representation of coordinates
	 *
	 * @return String
	 */
	String toDMSString();

	/**
	 * Returns lat/long representation of coordinates in the format acceptable by google Service
	 *
	 * @return String
	 */
	String toGeocodeServiceFormat();



}
