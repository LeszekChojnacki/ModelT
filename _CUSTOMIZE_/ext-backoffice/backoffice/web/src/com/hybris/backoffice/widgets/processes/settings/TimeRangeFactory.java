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

import java.util.List;



/**
 * Creates time rages {@link TimeRange} based on given input
 */
public interface TimeRangeFactory
{
	/**
	 * Creates ranges from given comma separated values
	 *
	 * @param commaSeparatedRanges e.g. 2m,3h,2w,3w,1d - {@link #createTimeRange(String)}
	 * @return list of time ranges. They are sorted from the smallest one.
	 */
	List<TimeRange> createTimeRanges(String commaSeparatedRanges);

	/**
	 * Creates time range based on given range
	 *
	 * @param range range in following format xm - x minutes, xh - x hours, xd - x days, xw - x weeks
	 * @return TimeRage object
	 */
	TimeRange createTimeRange(String range);
}
