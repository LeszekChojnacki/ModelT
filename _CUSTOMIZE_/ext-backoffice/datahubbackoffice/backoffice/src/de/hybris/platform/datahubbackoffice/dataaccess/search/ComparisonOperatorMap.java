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
package de.hybris.platform.datahubbackoffice.dataaccess.search;

import com.hybris.cockpitng.search.data.ValueComparisonOperator;
import com.hybris.datahub.dto.filter.ComparisonOperator;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public enum ComparisonOperatorMap
{
	GREATER(ValueComparisonOperator.GREATER, ComparisonOperator.gt),
	LESSER(ValueComparisonOperator.LESS, ComparisonOperator.lt),
	GREATER_OR_EQUAL(ValueComparisonOperator.GREATER_OR_EQUAL, ComparisonOperator.gte),
	LESSER_OR_EQUAL(ValueComparisonOperator.LESS_OR_EQUAL, ComparisonOperator.lte);

	private static final Map<ValueComparisonOperator, ComparisonOperator> comparatorMap = Collections.unmodifiableMap(initializeMapping());

	private ValueComparisonOperator cockpitComparator;
	private ComparisonOperator luceneComparator;

	ComparisonOperatorMap(final ValueComparisonOperator cockpitComparator, final ComparisonOperator luceneComparator)
	{
		this.cockpitComparator = cockpitComparator;
		this.luceneComparator = luceneComparator;
	}

	public static ComparisonOperator getLuceneComparator(final ValueComparisonOperator cockpitComparator)
	{
		return comparatorMap.get(cockpitComparator);
	}

	private static Map<ValueComparisonOperator, ComparisonOperator> initializeMapping()
	{
		final Map<ValueComparisonOperator, ComparisonOperator> comparatorMap = new EnumMap<>(ValueComparisonOperator.class);
		for (final ComparisonOperatorMap value : ComparisonOperatorMap.values())
		{
			comparatorMap.put(value.getCockpitComparator(), value.getLuceneComparator());
		}
		return comparatorMap;
	}

	public ValueComparisonOperator getCockpitComparator()
	{
		return cockpitComparator;
	}

	public ComparisonOperator getLuceneComparator()
	{
		return luceneComparator;
	}
}
