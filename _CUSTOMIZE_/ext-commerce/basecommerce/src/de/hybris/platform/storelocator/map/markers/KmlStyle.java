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

/**
 * Represents Elements style used in KML document
 * 
 */
public interface KmlStyle extends KmlElement
{
	/**
	 * Returns identifier String that needs to be injected in the placemark.
	 * 
	 * @return String
	 */
	String getPlacemarkInjectionElement();
}
