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

import java.io.Serializable;


/**
 * Data Transport Object for locations
 */
public class LocationDTO implements Serializable
{
	public final static String LOCATION_TYPE_STORE = "STORE";
	public final static String LOCATION_TYPE_WAREHOUSE = "WAREHOUSE";
	public final static String LOCATION_TYPE_POS = "POS";


	private String name;
	private String description;
	private String street;
	private String buildingNo;
	private String postalCode;
	private String city;
	private String countryIsoCode;
	private String mapIconUrl;
	private String latitude;
	private String longitude;
	private String type;

	/**
	 * 
	 */
	public LocationDTO()
	{
		super();
		// YTODO Auto-generated constructor stub
	}

	/**
	 * @param street
	 * @param buildingNo
	 * @param postalCode
	 * @param city
	 * @param countryIsoCode
	 */
	public LocationDTO(final String street, final String buildingNo, final String postalCode, final String city,
			final String countryIsoCode)
	{
		super();
		this.street = street;
		this.buildingNo = buildingNo;
		this.postalCode = postalCode;
		this.city = city;
		this.countryIsoCode = countryIsoCode;
	}



	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name
	 *           the name to set
	 */
	public void setName(final String name)
	{
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * @param description
	 *           the description to set
	 */
	public void setDescription(final String description)
	{
		this.description = description;
	}

	/**
	 * @return the street
	 */
	public String getStreet()
	{
		return street;
	}

	/**
	 * @param street
	 *           the street to set
	 */
	public void setStreet(final String street)
	{
		this.street = street;
	}

	/**
	 * @return the buildingNo
	 */
	public String getBuildingNo()
	{
		return buildingNo;
	}

	/**
	 * @param buildingNo
	 *           the buildingNo to set
	 */
	public void setBuildingNo(final String buildingNo)
	{
		this.buildingNo = buildingNo;
	}

	/**
	 * @return the postalCode
	 */
	public String getPostalCode()
	{
		return postalCode;
	}

	/**
	 * @param postalCode
	 *           the postalCode to set
	 */
	public void setPostalCode(final String postalCode)
	{
		this.postalCode = postalCode;
	}

	/**
	 * @return the city
	 */
	public String getCity()
	{
		return city;
	}

	/**
	 * @param city
	 *           the city to set
	 */
	public void setCity(final String city)
	{
		this.city = city;
	}

	/**
	 * @return the countryIsoCode
	 */
	public String getCountryIsoCode()
	{
		return countryIsoCode;
	}

	/**
	 * @param countryIsoCode
	 *           the countryIsoCode to set
	 */
	public void setCountryIsoCode(final String countryIsoCode)
	{
		this.countryIsoCode = countryIsoCode;
	}

	/**
	 * @return the mapIconUrl
	 */
	public String getMapIconUrl()
	{
		return mapIconUrl;
	}

	/**
	 * @param mapIconUrl
	 *           the mapIconUrl to set
	 */
	public void setMapIconUrl(final String mapIconUrl)
	{
		this.mapIconUrl = mapIconUrl;
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
	 * @return the type
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * @param type
	 *           the type to set
	 */
	public void setType(final String type)
	{
		this.type = type;
	}
}
