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
package de.hybris.platform.promotions.util;

/**
 * Helper generic pair type.
 * 
 * 
 * @version v1
 */
public class Pair<K, V>
{
	private K key;
	private V value;

	public Pair(final K key, final V value)
	{
		this.key = key;
		this.value = value;
	}

	public K getKey()
	{
		return key;
	}

	public void setKey(final K key)
	{
		this.key = key;
	}

	public V getValue()
	{
		return value;
	}

	public void setValue(final V value)
	{
		this.value = value;
	}
}
