/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.datahubbackoffice.dataaccess;

import com.hybris.cockpitng.dataaccess.facades.type.DataType;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A cache for data access <code>DataType</code>s.
 * The type names can be predefined before the actual <code>DataType</code>s placed into the cache
 */
public class PredefinableTypeCache
{
	private final Map<String, DataType> cache = new HashMap<>();

	/**
	 * Determines whether this cache was defined or contains already some elements
	 *
	 * @return <code>true</code>, if there is not type known by this cache; <code>false</code>, if types were defined or already
	 * added.
	 */
	public boolean isEmpty()
	{
		return cache.isEmpty();
	}

	/**
	 * Adds a data type to this cache.
	 *
	 * @param type type to add
	 * @return the cache with the type added
	 */
	public PredefinableTypeCache add(final DataType type)
	{
		return addType(type.getCode(), type);
	}

	protected PredefinableTypeCache addType(final String code, final DataType value)
	{
		cache.put(code, value);
		return this;
	}

	/**
	 * Retrieves item from the cache.
	 *
	 * @param code code of the type to retrieve
	 * @return the item type corresponding to the code or <code>null</code>, if the type was never added to this cache.
	 */
	public DataType get(final String code)
	{
		return cache.get(code);
	}

	/**
	 * Determines whether the type code has be defined in this cache.
	 *
	 * @param code a type code to check
	 * @return <code>true</code>, if the code has be defined or the data type was added; <code>false</code>, otherwise.
	 */
	public boolean isDefined(final String code)
	{
		return cache.containsKey(code);
	}

	/**
	 * Defines type(s) that may be added to the cache.
	 *
	 * @param codes type codes to define in this cache
	 * @return this cache with the type codes defined
	 */
	public PredefinableTypeCache define(final String... codes)
	{
		return define(Arrays.asList(codes));
	}

	/**
	 * Defines types that may be added to the cache.
	 *
	 * @param codes type codes to define in this cache
	 * @return this cache with the type codes defined
	 */
	public PredefinableTypeCache define(final Collection<String> codes)
	{
		if (codes != null)
		{
			for (final String code : codes)
			{
				defineType(code);
			}
		}
		return this;
	}

	protected void defineType(final String code)
	{
		if (!isDefined(code))
		{
			addType(code, null);
		}
	}

	public Collection<String> definedCodes()
	{
		return cache.keySet();
	}
}