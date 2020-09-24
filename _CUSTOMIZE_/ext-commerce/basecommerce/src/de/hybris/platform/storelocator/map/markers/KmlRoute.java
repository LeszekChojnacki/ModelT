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
 * Route representation of the KML document
 * 
 */
public interface KmlRoute extends KmlElement
{
	/**
	 * Returns route's style
	 * 
	 * @return {@link KmlStyle}
	 */
	KmlStyle getRouteStyle();
}
