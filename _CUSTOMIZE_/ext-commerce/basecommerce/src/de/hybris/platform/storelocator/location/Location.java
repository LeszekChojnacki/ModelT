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

import de.hybris.platform.storelocator.GPS;
import de.hybris.platform.storelocator.data.AddressData;



/**
 * Representation of single address
 */
public interface Location
{

	/**
	 * Return name
	 *
	 * @return String
	 */
	String getName();

	/**
	 * Returns a POS description
	 *
	 * @return String
	 */
	String getDescription();

	/**
	 * Getting the geographic coordinates of the IAddress
	 *
	 * @return GPS
	 */
	GPS getGPS();

	/**
	 *
	 * Get the address'es country ISO code
	 *
	 * @return String
	 */
	String getCountry();

	/**
	 * Getting the textual address of the IAddress
	 *
	 * @return String
	 */
	String getTextualAddress();

	/**
	 * Get AddressData object
	 *
	 * @return String
	 */
	AddressData getAddressData();

	/**
	 * returns MapIcon that is to be displayed on the map to represent this location
	 *
	 * @return String
	 */
	String getMapIconUrl();

	/**
	 *
	 * @return String describing location type
	 */
	String getType();
}
