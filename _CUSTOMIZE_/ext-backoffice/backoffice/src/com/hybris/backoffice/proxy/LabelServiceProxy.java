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
package com.hybris.backoffice.proxy;


import java.util.Locale;

/**
 * Service proxies {@link com.hybris.cockpitng.labels.LabelService} for retrieving Object's label and description.
 */
public interface LabelServiceProxy
{

	/**
	 * Returns label for a given Object.
	 *
	 * @param object
	 * @return label for a given object
	 */
	String getObjectLabel(Object object, Locale locale);

	/**
	 * Returns text description for a given Object.
	 *
	 * @param object
	 * @return description for a given object
	 */
	String getObjectDescription(Object object);
}
