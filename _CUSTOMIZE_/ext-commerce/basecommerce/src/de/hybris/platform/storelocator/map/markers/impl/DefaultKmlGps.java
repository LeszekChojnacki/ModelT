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

import de.hybris.platform.storelocator.GPS;
import de.hybris.platform.storelocator.map.markers.KmlGps;


/**
 * 
 */
public class DefaultKmlGps implements KmlGps
{



	private static String BEGINING = "<Point><coordinates>";
	private static String ENDING = "</coordinates></Point>";
	private final GPS gps;


	/**
	 * @param gps
	 */
	public DefaultKmlGps(final GPS gps)
	{
		super();
		this.gps = gps;
	}

	@Override
	public GPS getGps()
	{
		return gps;
	}

	@Override
	public String getBeginning()
	{
		return BEGINING;
	}


	@Override
	public String getElement()
	{
		final StringBuilder element = new StringBuilder(BEGINING);
		element.append(gps.getDecimalLongitude());
		element.append(",");
		element.append(gps.getDecimalLatitude());
		element.append(ENDING);
		return element.toString();
	}

	@Override
	public String getEnding()
	{
		return ENDING;
	}



}
