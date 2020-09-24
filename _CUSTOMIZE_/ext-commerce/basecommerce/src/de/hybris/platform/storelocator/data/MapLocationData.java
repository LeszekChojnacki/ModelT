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
package de.hybris.platform.storelocator.data;

/**
 * Simple Data Object for Map Location
 */
public class MapLocationData
{

	private String code;
	private String addressDescription;
	private String latitude;
	private String longitude;

	/**
	 * @return the code
	 */
	public String getCode()
	{
		return code;
	}

	/**
	 * @param code
	 *           the code to set
	 */
	public void setCode(final String code)
	{
		this.code = code;
	}

	/**
	 * @return the latitude
	 */
	public String getLatitude()
	{
		return latitude;
	}

	/**
	 * @param latitude
	 *           the latitude to set
	 */
	public void setLatitude(final String latitude)
	{
		this.latitude = latitude;
	}

	/**
	 * @return the longitude
	 */
	public String getLongitude()
	{
		return longitude;
	}

	/**
	 * @param longitude
	 *           the longitude to set
	 */
	public void setLongitude(final String longitude)
	{
		this.longitude = longitude;
	}

	/**
	 * @return the addressDescription
	 */
	public String getAddressDescription()
	{
		return addressDescription;
	}

	/**
	 * @param addressDescription
	 *           the addressDescription to set
	 */
	public void setAddressDescription(final String addressDescription)
	{
		this.addressDescription = addressDescription;
	}

}
