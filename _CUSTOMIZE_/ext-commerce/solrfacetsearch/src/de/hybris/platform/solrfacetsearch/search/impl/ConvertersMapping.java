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
package de.hybris.platform.solrfacetsearch.search.impl;

import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.solrfacetsearch.enums.ConverterType;

import java.io.Serializable;
import java.util.Map;


/**
 * Maps converter type with converter
 */
public class ConvertersMapping implements Serializable
{
	private Map<ConverterType, Converter> converters;

	public void setConverters(final Map<ConverterType, Converter> converters)
	{
		this.converters = converters;
	}

	/**
	 * Returns converter for specific type
	 */
	public Converter getConverterForType(final ConverterType converterType)
	{
		return converters.get(converterType);
	}
}
