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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;


public class DefaultExcelDateUtils implements ExcelDateUtils
{
	private static final Pattern PATTERN_DATE_RANGE = Pattern.compile("(.*)\\s+to\\s+(.*)");
	private static final String FORMAT_DATE_RANGE_PARAM_KEY = "%s to %s";
	private static final String FORMAT_DATE_RANGE_EXPORT_VALUE = "[" + FORMAT_DATE_RANGE_PARAM_KEY + "]";
	private String exportTimeZone = "UTC";
	private String dateTimeFormat = "dd.MM.yyyy HH:mm:ss";


	@Override
	public Pair<String, String> extractDateRange(final String cellValue)
	{
		final Matcher matcher = PATTERN_DATE_RANGE.matcher(cellValue);
		if (matcher.matches() && matcher.groupCount() == 2)
		{
			return new ImmutablePair<>(matcher.group(1).trim(), matcher.group(2).trim());
		}
		return null;
	}

	@Override
	public String getDateRangePattern()
	{
		return String.format(FORMAT_DATE_RANGE_EXPORT_VALUE, getDateTimeFormat(), getDateTimeFormat());
	}

	@Override
	public String getDateRangeParamKey()
	{
		return String.format(FORMAT_DATE_RANGE_PARAM_KEY, getDateTimeFormat(), getDateTimeFormat());
	}

	@Override
	public String exportDateRange(final Date start, final Date end)
	{
		return String.format(FORMAT_DATE_RANGE_EXPORT_VALUE, exportDate(start), exportDate(end));
	}

	@Override
	public String getDateTimeFormat()
	{
		return dateTimeFormat;
	}

	@Override
	public String exportDate(final Date date)
	{
		final DateTimeFormatter format = DateTimeFormatter.ofPattern(dateTimeFormat);
		final ZonedDateTime exportDateTimeInUTC = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of(exportTimeZone));
		return format.format(exportDateTimeInUTC);
	}

	@Override
	public String importDate(final String date)
	{
		if(StringUtils.isNotBlank(date))
		{
			final DateTimeFormatter format = DateTimeFormatter.ofPattern(dateTimeFormat);

			final LocalDateTime importedDateTime = LocalDateTime.parse(StringUtils.trim(date), format);
			final ZonedDateTime dateFromExcelInUtc = ZonedDateTime
					.ofInstant(importedDateTime.atZone(ZoneId.of(exportTimeZone)).toInstant(), ZoneId.systemDefault());
			return format.format(dateFromExcelInUtc);
		}
		return StringUtils.EMPTY;
	}

	@Override
	public Date convertToImportedDate(final String date)
	{
		final DateTimeFormatter format = DateTimeFormatter.ofPattern(dateTimeFormat);

		final LocalDateTime importedDateTime = LocalDateTime.parse(StringUtils.trim(date), format);
		final ZonedDateTime dateFromExcelInUtc = ZonedDateTime
				.ofInstant(importedDateTime.atZone(ZoneId.of(exportTimeZone)).toInstant(), ZoneId.systemDefault());
		return Date.from(dateFromExcelInUtc.toInstant());
	}

	@Override
	public String getExportTimeZone()
	{
		return exportTimeZone;
	}

	public void setExportTimeZone(final String exportTimeZone)
	{
		this.exportTimeZone = exportTimeZone;
	}

	public void setDateTimeFormat(final String dateTimeFormat)
	{
		this.dateTimeFormat = dateTimeFormat;
	}
}
