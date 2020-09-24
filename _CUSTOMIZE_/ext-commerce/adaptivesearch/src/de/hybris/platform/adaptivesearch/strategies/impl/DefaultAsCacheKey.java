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
package de.hybris.platform.adaptivesearch.strategies.impl;

import de.hybris.platform.adaptivesearch.strategies.AsCacheKey;
import de.hybris.platform.adaptivesearch.strategies.AsCacheScope;

import java.io.Serializable;
import java.util.Arrays;

import org.apache.commons.lang3.builder.EqualsBuilder;


/**
 * Default implementation of {@link AsCacheKey}.
 */
public class DefaultAsCacheKey implements AsCacheKey
{
	private final AsCacheScope scope;
	private final Serializable[] keyFragments;

	/**
	 * Default constructor.
	 *
	 * @param scope
	 *           - the cache key scope
	 * @param keyFragments
	 *           - the fragments of the cache key
	 */
	public DefaultAsCacheKey(final AsCacheScope scope, final Serializable... keyFragments)
	{
		this.scope = scope;
		this.keyFragments = keyFragments;
	}

	@Override
	public AsCacheScope getScope()
	{
		return scope;
	}

	@Override
	public String toString()
	{
		return "DefaultAsCacheKey [scope=" + scope + ", keyFragments=" + keyFragments + "]";
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}

		if (obj == null || this.getClass() != obj.getClass())
		{
			return false;
		}

		final DefaultAsCacheKey that = (DefaultAsCacheKey) obj;
		return new EqualsBuilder().append(this.scope, that.scope).append(this.keyFragments, that.keyFragments).isEquals();
	}

	@Override
	public int hashCode()
	{
		final int keyFragmentsResult = keyFragments != null ? Arrays.hashCode(keyFragments) : 0;

		return 31 * (scope != null ? scope.ordinal() : 0) + keyFragmentsResult;
	}
}
