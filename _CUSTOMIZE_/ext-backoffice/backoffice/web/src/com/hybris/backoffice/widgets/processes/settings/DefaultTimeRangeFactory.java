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

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.util.resource.Labels;


public class DefaultTimeRangeFactory implements TimeRangeFactory
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultTimeRangeFactory.class);
	public static final Pattern PATTERN = Pattern.compile("^(?<number>\\d+)(?<type>[mhdw])$");
	public static final String GROUP_NUMBER = "number";
	public static final String GROUP_TYPE = "type";
	public static final String MINUTE = "m";
	public static final String HOUR = "h";
	public static final String DAY = "d";
	public static final String WEEK = "w";
	public static final String LABEL_PROCESSES_RANGE_HOUR = "time.range.hour";
	public static final String LABEL_PROCESSES_RANGE_DAY = "time.range.day";
	public static final String LABEL_PROCESSES_RANGE_WEEK = "time.range.week";
	public static final String LABEL_PROCESSES_RANGE_MINUTE = "time.range.minute";
	public static final String PLURAL_SUFFIX = "s";
	public static final String RANGES_SEPARATOR = ",";


	@Override
	public List<TimeRange> createTimeRanges(final String commaSeparatedRanges)
	{
		final List<TimeRange> timeRanges = new ArrayList<>();
		if (StringUtils.isNotBlank(commaSeparatedRanges))
		{
			final String[] ranges = commaSeparatedRanges.split(RANGES_SEPARATOR);
			for (final String range : ranges)
			{
				final TimeRange timeRange = createTimeRange(range);
				if (timeRange != null)
				{
					timeRanges.add(timeRange);
				}
				else
				{
					LOG.warn("Cannot create time range for given range {}", range);
				}
			}
			timeRanges.sort(Comparator.comparing(TimeRange::getDuration));
		}

		return timeRanges;
	}


	@Override
	public TimeRange createTimeRange(final String range)
	{
		if (StringUtils.isNotBlank(range))
		{
			final Matcher matcher = PATTERN.matcher(range.trim().toLowerCase());
			if (matcher.find())
			{
				final String number = matcher.group(GROUP_NUMBER);
				final String type = matcher.group(GROUP_TYPE);
				try
				{
					return createTimeRange(Long.parseLong(number), type);
				}
				catch (final NumberFormatException ne)
				{
					LOG.warn("Can not parse long from {}", number);
				}
			}
		}
		return null;
	}

	protected TimeRange createTimeRange(final long number, final String type)
	{

		if (StringUtils.isNotBlank(type) && number > 0)
		{
			final ChronoUnit unit;
			final String labelKey;

			switch (type)
			{
				case MINUTE:
					unit = ChronoUnit.MINUTES;
					labelKey = LABEL_PROCESSES_RANGE_MINUTE;
					break;
				case HOUR:
					unit = ChronoUnit.HOURS;
					labelKey = LABEL_PROCESSES_RANGE_HOUR;
					break;
				case DAY:
					unit = ChronoUnit.DAYS;
					labelKey = LABEL_PROCESSES_RANGE_DAY;
					break;
				case WEEK:
					unit = ChronoUnit.WEEKS;
					labelKey = LABEL_PROCESSES_RANGE_WEEK;
					break;
				default:
					unit = null;
					labelKey = null;
			}

			if (unit != null)
			{
				return new TimeRange(unit, number, getLabel(number, labelKey));
			}
		}

		return null;
	}

	protected String getLabel(final long number, final String labelKey)
	{
		final String labelKeyModified = number > 1 ? labelKey.concat(PLURAL_SUFFIX) : labelKey;
		return Labels.getLabel(labelKeyModified, new Object[] { Long.valueOf(number) });
	}

}
