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
package de.hybris.platform.storelocator.route;

import de.hybris.platform.storelocator.GPS;
import de.hybris.platform.storelocator.data.AddressData;

import java.util.Map;


/**
 * Decides how the directions url will be created.
 */
public interface GeolocationDirectionsUrlBuilder
{

	/**
	 * Builds url for web service serving directions from start coordinates to destination coordinates.
	 * 
	 * @param baseUrl
	 *           - base address of the web service
	 * @param start
	 * @param destination
	 * @param params
	 *           additional parameters.
	 */
	String getWebServiceUrl(String baseUrl, GPS start, GPS destination, Map params);

	/**
	 * Builds url for web service serving directions from start address to destination address.
	 * 
	 * @param baseUrl
	 *           - base address of the web service
	 * @param startAddress
	 * @param destinationAddress
	 * @param params
	 *           additional parameters.
	 */
	String getWebServiceUrl(String baseUrl, AddressData startAddress, AddressData destinationAddress, Map params);

}
