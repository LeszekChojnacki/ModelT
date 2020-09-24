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
package com.hybris.backoffice.widgets.processes.settings;

import java.time.Duration;
import java.time.temporal.ChronoUnit;


/**
 * Represents time range
 */
public class TimeRange
{
	private final String label;
	private final ChronoUnit unit;
	private final long numberOfUnits;

	/**
	 * @param numberOfUnits
	 *           numberOfUnits of given units
	 * @param unit
	 *           unit
	 * @param label
	 *           label to display
	 */
	public TimeRange(final ChronoUnit unit, final long numberOfUnits, final String label)
	{
		this.label = label;
		this.numberOfUnits = numberOfUnits;
		this.unit = unit;
	}

	public String getLabel()
	{
		return label;
	}

	public Duration getDuration()
	{
		return Duration.ofMillis(unit.getDuration().toMillis() * numberOfUnits);
	}

	public ChronoUnit getUnit()
	{
		return unit;
	}

	public long getNumberOfUnits()
	{
		return numberOfUnits;
	}

	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || this.getClass() != o.getClass())
		{
			return false;
		}

		final TimeRange timeRange = (TimeRange) o;

		if (numberOfUnits != timeRange.numberOfUnits)
		{
			return false;
		}
		return unit == timeRange.unit;
	}

	@Override
	public int hashCode()
	{
		int result = unit.hashCode();
		result = 31 * result + (int) (numberOfUnits ^ (numberOfUnits >>> 32));
		return result;
	}
}
