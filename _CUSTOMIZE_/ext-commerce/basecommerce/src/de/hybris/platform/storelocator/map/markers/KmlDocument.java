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
package de.hybris.platform.storelocator.map.markers;

import java.util.List;


/**
 * Representation of KML document
 */
public interface KmlDocument extends KmlElement
{
	/**
	 * Returns placemarks from document
	 * 
	 * @return List
	 */
	List<KmlPlacemark> getPlacemarks();

	/**
	 * Returns styles from document
	 * 
	 * @return List
	 */
	List<KmlStyle> getStyles();

	/**
	 * Returns document name
	 * 
	 * @return String
	 */
	String getName();

	/**
	 * Returns document description
	 * 
	 * @return String
	 */
	String getDescription();
}
