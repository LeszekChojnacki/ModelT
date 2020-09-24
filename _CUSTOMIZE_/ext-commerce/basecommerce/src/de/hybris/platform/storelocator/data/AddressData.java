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

import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.user.AddressModel;

import java.io.Serializable;
import java.util.Objects;


/**
 * Simple Data Transport Object for addresses
 *
 */
public class AddressData implements Serializable

{
	private String name;
	private String street;
	private String building;
	private String zip;
	private String city;
	private String countryCode;

	public AddressData()
	{
		super();
	}

	/**
	 * @param zip
	 */
	public AddressData(final String zip)
	{
		super();
		this.zip = zip;
	}

	/**
	 * Constructor
	 *
	 * @param street
	 * @param buildingNo
	 * @param zip
	 * @param city
	 * @param countryCode
	 */
	public AddressData(final String street, final String buildingNo, final String zip, final String city, final String countryCode)
	{
		super();
		this.street = street;
		this.building = buildingNo;
		this.zip = zip;
		this.city = city;
		this.countryCode = countryCode;
	}

	/**
	 * Constructor
	 *
	 * @param name
	 * @param street
	 * @param buildingNo
	 * @param zip
	 * @param city
	 * @param countryCode
	 */
	public AddressData(final String name, final String street, final String buildingNo, final String zip, final String city,
			final String countryCode)
	{
		super();
		this.name = name;
		this.street = street;
		this.building = buildingNo;
		this.zip = zip;
		this.city = city;
		this.countryCode = countryCode;
	}

	/**
	 * Constructor
	 *
	 * @param addressModel
	 *
	 */
	public AddressData(final AddressModel addressModel)
	{
		super();

		if (addressModel != null)
		{
			this.street = addressModel.getStreetname();
			this.building = addressModel.getStreetnumber();
			this.zip = addressModel.getPostalcode();
			this.city = addressModel.getTown();
			final CountryModel country = addressModel.getCountry();
			if (country != null)
			{
				this.countryCode = country.getIsocode();
			}
		}

	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}


	/**
	 * @return the zip
	 */
	public String getZip()
	{
		return zip;
	}

	/**
	 * @param zip
	 *           the zip to set
	 */
	public void setZip(final String zip)
	{
		this.zip = zip;
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
	 * @return the countryCode
	 */
	public String getCountryCode()
	{
		return countryCode;
	}

	/**
	 * @param countryCode
	 *           the countryCode to set
	 */
	public void setCountryCode(final String countryCode)
	{
		this.countryCode = countryCode;
	}

	/**
	 * @return the street
	 */
	public String getStreet()
	{
		return street;
	}

	/**
	 * @return the building
	 */
	public String getBuilding()
	{
		return building;
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
	 * @param street
	 *           the street to set
	 */
	public void setStreet(final String street)
	{
		this.street = street;
	}

	/**
	 * @param building
	 *           the building to set
	 */
	public void setBuilding(final String building)
	{
		this.building = building;
	}


	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder(this.name == null ? "" : name);
		builder.append(", ").append(street).append(" ").append(building).append(", ").append(zip).append(" ").append(city);
		return builder.toString();
	}


	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (obj.getClass().equals(AddressData.class))
		{
			final AddressData address = (AddressData) obj;
			return equalAddressFields(this.street, address.street) && equalAddressFields(this.building, address.building) //NOSONAR
					&& equalAddressFields(this.zip, address.zip) && equalAddressFields(this.city, address.city)
					&& equalAddressFields(this.countryCode, address.countryCode);

		}
		else if (obj.getClass().equals(AddressModel.class))
		{
			final AddressModel address = (AddressModel) obj;
			return addressEquals(address);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(building, city, countryCode, street, zip);
	}

	/**
	 * Determines if this address is equivalent to the one given as model argument
	 *
	 * @param addressModel
	 * @return boolean
	 */
	public boolean addressEquals(final AddressModel addressModel)
	{
		if (addressModel != null)
		{
			boolean result = equalAddressFields(street, addressModel.getStreetname());
			if (!result)
			{
				return result;
			}
			result = equalAddressFields(building, addressModel.getStreetnumber());
			if (!result)
			{
				return result;
			}
			result = equalAddressFields(zip, addressModel.getPostalcode());
			if (!result)
			{
				return result;
			}
			result = equalAddressFields(city, addressModel.getTown());
			if (!result)
			{
				return result;
			}
			result = equalAddressFields(countryCode,
					addressModel.getCountry() == null ? null : addressModel.getCountry().getIsocode());
			if (!result)
			{
				return result;
			}
			return result;
		}
		return false;

	}

	private boolean equalAddressFields(final String field1, final String field2)
	{
		if (field1 == null ^ field2 == null)
		{
			return false;
		}
		else if (field1 != null)
		{
			return field1.equalsIgnoreCase(field2);
		}
		else
		{
			return true;
		}
	}

}
