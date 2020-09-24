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

import de.hybris.platform.storelocator.map.markers.KmlStyle;


/**
 *
 */
public class DefaultRouteStyle implements KmlStyle
{

	private final String name;
	private final String element;

	public static final DefaultRouteStyle DEFAULT_STYLE = new DefaultRouteStyle("roadStyle",
			"<Style id=\"roadStyle\"><LineStyle><color>7fcf0064</color><width>6</width></LineStyle></Style>");

	/**
	 * @param element
	 */
	public DefaultRouteStyle(final String name, final String element)
	{
		super();
		this.element = element;
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.hybris.platform.geolocation.map.markers.IKmlElement#getBegining()
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
	 * @see de.hybris.platform.geolocation.map.markers.IKmlElement#getElemet()
	 */
	@Override
	public String getElement()
	{
		return element;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.hybris.platform.geolocation.map.markers.IKmlElement#getEnding()
	 */
	@Override
	public String getEnding()
	{
		// YTODO Auto-generated method stub
		return null;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.hybris.platform.geolocation.map.markers.IKmlStyle#getPlacemarkInjectionElement()
	 */
	@Override
	public String getPlacemarkInjectionElement()
	{
		return "<styleUrl>#" + name + "</styleUrl>";
	}

}
