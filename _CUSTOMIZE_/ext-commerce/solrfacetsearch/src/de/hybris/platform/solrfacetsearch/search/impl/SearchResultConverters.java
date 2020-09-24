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

import java.util.Map;


/**
 * Maps indexed type code with converter mapping
 */
public class SearchResultConverters
{
	private Map<String, ConvertersMapping> converterMapping;

	public void setConverterMapping(final Map<String, ConvertersMapping> converterMapping)
	{
		this.converterMapping = converterMapping;
	}

	/**
	 * Returns converter for type code and type
	 */
	public Converter getConverter(final String indexedTypeCode, final ConverterType converterType)
	{
		final ConvertersMapping convertersMapping = converterMapping.get(indexedTypeCode);
		if (convertersMapping == null)
		{
			return null;
		}
		else
		{
			return convertersMapping.getConverterForType(converterType);
		}
	}

	/**
	 * Returns mapping for type code
	 */
	public ConvertersMapping getConverterMapping(final String indexedTypeCode)
	{
		return converterMapping.get(indexedTypeCode);
	}
}
