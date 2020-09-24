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
package com.hybris.backoffice.dates;

import java.util.Date;

import org.apache.commons.lang.time.DateUtils;


public class DayUtils
{

	private DayUtils()
	{

	}

	public static boolean isToday(final Date now, final Date referenceDate)
	{
		return DateUtils.isSameDay(referenceDate, now);
	}

	public static boolean isYesterday(final Date now, final Date referenceDate)
	{
		return DateUtils.isSameDay(DateUtils.addDays(referenceDate, 1), now);
	}

}
