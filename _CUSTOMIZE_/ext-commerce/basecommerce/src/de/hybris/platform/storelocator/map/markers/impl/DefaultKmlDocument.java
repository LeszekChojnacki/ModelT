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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import de.hybris.platform.storelocator.GPS;
import de.hybris.platform.storelocator.exception.KmlDocumentException;
import de.hybris.platform.storelocator.location.Location;
import de.hybris.platform.storelocator.map.markers.KmlDocument;
import de.hybris.platform.storelocator.map.markers.KmlIconStyle;
import de.hybris.platform.storelocator.map.markers.KmlPlacemark;
import de.hybris.platform.storelocator.map.markers.KmlRoute;
import de.hybris.platform.storelocator.map.markers.KmlStyle;
import de.hybris.platform.storelocator.route.DistanceAndRoute;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * DefaultKmlDocument is the default implementation for KmlDocument
 */
public class DefaultKmlDocument implements KmlDocument
{
	private static final String DEFAULT_BEGINING = "<kml><Document>";
	private static final String DEFAULT_ENDING = "</Document></kml>";

	private String name;
	private String description;

	private final List<KmlPlacemark> placemarks;
	private List<KmlStyle> styles;
	private List<KmlRoute> routes;

	/**
	 * @throws KmlDocumentException
	 */
	public DefaultKmlDocument(final GPS center, final List<Location> pois)
	{
		this(center, pois, null);
	}

	/**
	 * @param center
	 *           not used
	 * @throws KmlDocumentException
	 */
	public DefaultKmlDocument(final GPS center, final List<Location> pois, final DistanceAndRoute route) //NOSONAR
	{
		try
		{
			List<KmlRoute> theRoutes = null;
			final List<KmlPlacemark> thePlacemarks = new ArrayList<>();
			if (nonNull(pois))
			{
				pois.stream().filter(Objects::nonNull).forEach(poi ->
				{
					if (nonNull(poi.getMapIconUrl()))
					{
						final int styleCount = nonNull(this.styles) ? this.styles.size() : 0;
						final KmlIconStyle iconStyle = new DefaultIconStyle("style" + styleCount, poi.getMapIconUrl());
						addDocumentStyle(iconStyle);
						thePlacemarks.add(new DefaultKmlPlacemark(poi.getName(), new DefaultKmlDescription(poi.getDescription()),
								new DefaultKmlGps(poi.getGPS()), iconStyle));
					}
					else
					{
						thePlacemarks.add(new DefaultKmlPlacemark(poi.getName(), new DefaultKmlDescription(poi.getDescription()),
								new DefaultKmlGps(poi.getGPS())));
					}

				});
			}
			this.placemarks = thePlacemarks;
			if (nonNull(route) && nonNull(route.getRoute()))
			{
				if (isNull(this.styles))
				{
					this.styles = new ArrayList<>();
				}
				this.styles.add(route.getRoute().getRouteStyle());
				theRoutes = new ArrayList<>();
				theRoutes.add(route.getRoute());
				this.routes = theRoutes;
			}
		}
		catch (final UnsupportedOperationException | ClassCastException | IllegalArgumentException e)
		{
			throw new KmlDocumentException(e);
		}
	}


	/**
	 * @param placemarks
	 */
	public DefaultKmlDocument(final List<KmlPlacemark> placemarks)
	{
		this.placemarks = placemarks;
	}

	/**
	 * @param name
	 *           the name
	 * @param description
	 *           the description
	 * @param placemarks
	 *           the placemarks
	 * @param styles
	 *           the styles
	 */
	public DefaultKmlDocument(final String name, final String description, final List<KmlPlacemark> placemarks,
			final List<KmlStyle> styles)
	{
		this.name = name;
		this.description = description;
		this.placemarks = placemarks;
		this.styles = styles;
	}

	@Override
	public List<KmlPlacemark> getPlacemarks()
	{
		return placemarks;
	}

	@Override
	public List<KmlStyle> getStyles()
	{
		return styles;
	}

	@Override
	public String getBeginning()
	{
		return DEFAULT_BEGINING;
	}

	@Override
	public String getElement()
	{
		final StringBuilder document = new StringBuilder(getBeginning());
		if (nonNull(styles))
		{
			for (final KmlStyle style : styles)
			{
				document.append(style.getElement());
			}
		}
		if (nonNull(placemarks))
		{
			for (final KmlPlacemark placemark : placemarks)
			{
				document.append(placemark.getElement());
			}
		}
		if (nonNull(routes))
		{
			for (final KmlRoute route : routes)
			{
				document.append(route.getElement());
			}
		}
		document.append(getEnding());
		return document.toString();
	}

	@Override
	public String getEnding()
	{
		return DEFAULT_ENDING;
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	@Override
	public String getName()
	{
		return name;
	}

	protected void addDocumentStyle(final KmlIconStyle iconStyle)
	{
		if (isNull(this.styles))
		{
			this.styles = new ArrayList<>();
		}
		this.styles.add(iconStyle);
	}

}
