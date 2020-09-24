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

import de.hybris.platform.storelocator.map.markers.KmlDescription;
import de.hybris.platform.storelocator.map.markers.KmlGps;
import de.hybris.platform.storelocator.map.markers.KmlIconStyle;
import de.hybris.platform.storelocator.map.markers.KmlPlacemark;
import de.hybris.platform.storelocator.map.markers.KmlStyle;


/**
 * 
 */
public class DefaultKmlPlacemark implements KmlPlacemark
{

	private static String BEGINING = "<Placemark>";
	private static String ENDING = "</Placemark>";
	private final String name;
	private KmlDescription description;
	private final KmlGps gps;
	private KmlIconStyle style;



	public DefaultKmlPlacemark(final String name, final KmlGps gps, final KmlIconStyle style)
	{
		super();
		this.name = name;
		this.gps = gps;
		this.style = style;
	}

	/**
	 * @param name
	 * @param gps
	 */
	public DefaultKmlPlacemark(final String name, final KmlGps gps)
	{
		super();
		this.name = name;
		this.gps = gps;
	}


	/**
	 * @param name
	 * @param description
	 * @param gps
	 */
	public DefaultKmlPlacemark(final String name, final KmlDescription description, final KmlGps gps)
	{
		super();
		this.name = name;
		this.description = description;
		this.gps = gps;
	}


	/**
	 * @param name
	 * @param description
	 * @param gps
	 * @param style
	 */
	public DefaultKmlPlacemark(final String name, final KmlDescription description, final KmlGps gps, final KmlIconStyle style)
	{
		super();
		this.name = name;
		this.description = description;
		this.gps = gps;
		this.style = style;
	}


	@Override
	public KmlDescription getDescription()
	{
		return description;
	}


	@Override
	public KmlGps getGPS()
	{
		return gps;
	}

	@Override
	public String getName()
	{
		return name;
	}

	public String getNameElement()
	{
		return "<name>" + name + "</name>";
	}

	@Override
	public KmlStyle getStyle()
	{
		return style;
	}

	@Override
	public String getElement()
	{
		final StringBuilder builder = new StringBuilder(BEGINING);
		builder.append(getNameElement());
		if (null != description)
		{
			builder.append(description.getElement());
		}
		builder.append(gps.getElement());
		if (style != null)
		{
			builder.append(style.getPlacemarkInjectionElement());
		}
		builder.append(ENDING);
		return builder.toString();
	}


	@Override
	public String getBeginning()
	{
		return BEGINING;
	}


	@Override
	public String getEnding()
	{
		return ENDING;
	}

}
