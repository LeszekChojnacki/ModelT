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
package de.hybris.platform.adaptivesearch.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;


/**
 * Interface for a converter that transforms an object of one type into an object of another type.
 *
 * @param <S>
 *           - the type of the source object
 * @param <T>
 *           - the type of the destination object
 * @param <C>
 *           - the type of the context object
 */
public interface ContextAwareConverter<S, T, C>
{
	/**
	 * Converts the source object, creating a new instance of the destination type.
	 *
	 * @param source
	 *           the source object
	 *
	 * @return the converted object
	 */
	T convert(S source, C context);

	/**
	 * Convert Collection of SOURCE instances and return Collection of TARGET instances.
	 *
	 * @param sources
	 *           the source instances
	 *
	 * @return collection of target instances
	 */
	default List<T> convertAll(final Collection<? extends S> sources, final C context)
	{
		if (CollectionUtils.isEmpty(sources))
		{
			return Collections.emptyList();
		}

		final List<T> result = new ArrayList<>(sources.size());

		for (final S source : sources)
		{
			result.add(convert(source, context));
		}

		return result;
	}
}
