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
package de.hybris.platform.storelocator.route.impl;

import de.hybris.platform.storelocator.GPS;
import de.hybris.platform.storelocator.location.Location;
import de.hybris.platform.storelocator.map.markers.KmlStyle;
import de.hybris.platform.storelocator.map.markers.impl.DefaultRouteStyle;
import de.hybris.platform.storelocator.route.Route;


/**
 * 
 */
public class DefaultRoute implements Route
{

	private final static String BEGINING = "<Placemark><name>Route</name>";
	private final static String ENDING = "</Placemark>";
	private final String kmlElement;
	private final GPS start;
	private final Location destination;
	private final KmlStyle style = DefaultRouteStyle.DEFAULT_STYLE;

	/**
	 * @param kmlElement
	 */
	public DefaultRoute(final GPS start, final Location destination, final String kmlElement)
	{
		super();
		this.kmlElement = kmlElement;
		this.start = start;
		this.destination = destination;
	}

	@Override
	public String getCoordinates()
	{
		return kmlElement;
	}



	@Override
	public String getBeginning()
	{
		return BEGINING;
	}

	@Override
	public String getElement()
	{
		return BEGINING + kmlElement + style.getPlacemarkInjectionElement() + ENDING;
	}


	@Override
	public String getEnding()
	{
		return ENDING;
	}


	@Override
	public KmlStyle getRouteStyle()
	{
		return style;
	}

	/**
	 * @return the start
	 */
	@Override
	public GPS getStart()
	{
		return start;
	}

	/**
	 * @return the destination
	 */
	@Override
	public Location getDestination()
	{
		return destination;
	}




}
