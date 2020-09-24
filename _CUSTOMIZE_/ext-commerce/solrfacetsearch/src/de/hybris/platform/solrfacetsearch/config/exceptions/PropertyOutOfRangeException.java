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
package de.hybris.platform.solrfacetsearch.config.exceptions;

import de.hybris.platform.solrfacetsearch.provider.impl.AbstractPropertyFieldValueProvider;


/**
 * Thrown by
 * {@link AbstractPropertyFieldValueProvider#getRangeName(de.hybris.platform.solrfacetsearch.config.IndexedProperty, Object)}
 * whenever the actual value of the property is not
 * included in any of the related value range set defined in this indexed property definition
 */
public class PropertyOutOfRangeException extends FieldValueProviderException
{

	/**
	 * Initializes the exception
	 * 
	 * @param message
	 *           - error message
	 */
	public PropertyOutOfRangeException(final String message)
	{
		super(message);
	}

}
