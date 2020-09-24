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
package com.hybris.backoffice.excel.util;

import java.util.Date;

import org.apache.commons.lang3.tuple.Pair;


/**
 * Utility for excel date operations.
 */
public interface ExcelDateUtils
{
	/**
	 * Extracts from given value start and end date. Expected format is "start to end"
	 * 
	 * @param cellValue
	 *           value from which date range will be extracted.
	 * @return pair of start and end date.
	 */
	Pair<String, String> extractDateRange(String cellValue);

	/**
	 * Returns date range patter used in excel column header e.g. [dd.MM.yyyy HH:mm:ss to dd.MM.yyyy HH:mm:ss]
	 * 
	 * @return string with date range pattern.
	 */
	String getDateRangePattern();

	/**
	 * Returns date range param key which will be {@link #getDateRangePattern()} without [ and ] around the range.
	 * 
	 * @return date range param key.
	 */
	String getDateRangeParamKey();

	/**
	 * Exports date range
	 *
	 * @param start
	 *           start date
	 * @param end
	 *           end date
	 * @return string representation of date range which follows pattern from {@link #getDateRangePattern()}
	 */
	String exportDateRange(Date start, Date end);

	/**
	 * Return date and time format e.g. dd.MM.yyyy HH:mm:ss.
	 *
	 * @return dateTime format.
	 */
	String getDateTimeFormat();

	/**
	 * Exports given date in excel date timezone using {@link #getDateTimeFormat()}
	 *
	 * @param date
	 *           date to be exported
	 * @return date string representation in excel date timezone {@link #getExportTimeZone()}.
	 */
	String exportDate(Date date);

	/**
	 * Converts date in excel date timezone into system date zone date string.
	 *
	 * @param date
	 *           date in excel date timezone {@link #getExportTimeZone()}
	 * @return date string in system timezone.
	 */
	String importDate(String date);

	/**
	 * Converts given date in excel date timezone into system date in system timezone.
	 *
	 * @param date
	 *           date in excel date timezone {@link #getExportTimeZone()}
	 * @return date in system timezone.
	 */
	Date convertToImportedDate(String date);

	/**
	 * Timezone in which dates are exported into excel e.g. UTC
	 * 
	 * @return excel timezone
	 */
	String getExportTimeZone();
}
