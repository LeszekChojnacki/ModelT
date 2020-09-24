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
package de.hybris.platform.storelocator.map.markers.impl;

import de.hybris.platform.storelocator.map.markers.KmlIconStyle;


/**
 * 
 */
public class DefaultIconStyle implements KmlIconStyle
{

	private final String name;
	private final String iconHref;



	/**
	 * @param name
	 * @param iconHref
	 */
	public DefaultIconStyle(final String name, final String iconHref)
	{
		super();
		this.name = name;
		this.iconHref = iconHref;
	}


	@Override
	public String getIconHref()
	{
		return iconHref;
	}


	@Override
	public String getPlacemarkInjectionElement()
	{
		return "<styleUrl>#" + name + "</styleUrl>";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.hybris.platform.geolocation.map.markers.KmlElement#getBegining()
	 */
	@Override
	public String getBeginning()
	{
		// YTODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.hybris.platform.geolocation.map.markers.KmlElement#getElemet()
	 */
	@Override
	public String getElement()
	{
		return "<Style id=\"" + name + "\"><IconStyle><colorMode>normal</colorMode><scale>1</scale><Icon><href>" + iconHref
				+ "</href></Icon></IconStyle></Style>";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.hybris.platform.geolocation.map.markers.KmlElement#getEnding()
	 */
	@Override
	public String getEnding()
	{
		// YTODO Auto-generated method stub
		return null;
	}

}
