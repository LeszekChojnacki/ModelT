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
import de.hybris.platform.storelocator.constants.GeolocationConstants;
import de.hybris.platform.storelocator.data.AddressData;
import de.hybris.platform.storelocator.exception.GeoLocatorException;
import de.hybris.platform.storelocator.model.PointOfServiceModel;

import java.io.Serializable;



public abstract class AbstractAddress implements Serializable
{

	private AddressData addressData;
	private GPS gps;
	private String textualAddress;
	private Object owner; // NOSONAR
	private boolean gpsUpdate = false;

	public AbstractAddress()
	{
		super();
	}

	public AbstractAddress(final String plzOrEquivalent)
	{
		super();
		this.addressData = new AddressData(plzOrEquivalent);
	}

	public AbstractAddress(final String name, final String street, final String buildingNo, final String city,
			final String plzOrEquivalent, final String countryCode)
	{
		this();
		this.addressData = new AddressData(name, street, buildingNo, plzOrEquivalent, city, countryCode);
	}

	public AddressData getAddressData()
	{
		return addressData;
	}

	protected String getAllowedChars()
	{
		return GeolocationConstants.TEXTUAL_ADDRESS_ALLOWED_CHARS_COMMON;
	}

	public String getPlzOrEquivalent()
	{
		return this.addressData.getZip();
	}

	public GPS getGps()
	{
		return gps;
	}

	public void setGps(final GPS gps)
	{
		this.gps = gps;
	}

	public String getTextualAddress()
	{
		return textualAddress;
	}

	/**
	 * @param textualAddress
	 *           textualAddress lines - textualAddress to set
	 * @throws GeoLocatorException
	 */
	public abstract void setTextualAddress(String... textualAddress);

	public Object getOwner()
	{
		return owner;
	}

	protected AddressData extractAddressData(final PointOfServiceModel pos)
	{
		final AddressData newAddressData = new AddressData();
		final AddressModel addressModel = pos.getAddress();
		newAddressData.setStreet(addressModel.getStreetname());
		newAddressData.setBuilding(addressModel.getStreetnumber());
		newAddressData.setCity(addressModel.getTown());
		newAddressData.setCountryCode(addressModel.getCountry().getIsocode());
		newAddressData.setZip(addressModel.getPostalcode());
		return newAddressData;
	}

	public boolean isGpsUpdate()
	{
		return gpsUpdate;
	}

	public void setGpsUpdate(final boolean gpsUpdate)
	{
		this.gpsUpdate = gpsUpdate;
	}

}
