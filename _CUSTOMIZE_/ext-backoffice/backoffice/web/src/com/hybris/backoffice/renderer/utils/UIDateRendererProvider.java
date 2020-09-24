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
package com.hybris.backoffice.renderer.utils;

import java.text.DateFormat;
import java.util.Date;

import org.zkoss.util.Locales;
import org.zkoss.util.resource.Labels;

import com.hybris.backoffice.dates.DayUtils;


public class UIDateRendererProvider
{

	protected static final String LABEL_LATERDATE = "com.hybris.backoffice.dates.laterdate";
	protected static final String LABEL_TODAY = "com.hybris.backoffice.dates.today";
	protected static final String LABEL_UNKNOWN = "com.hybris.backoffice.dates.unknown";
	protected static final String LABEL_YESTERDAY = "com.hybris.backoffice.dates.yesterday";

	/**
	 * @param now
	 * @param referenceDate
	 * @return formatted value for Label. If <code>referenceDate</code> is today in reference to <code>now</code>, then
	 *         the returned value is "Today - {time}", when is yesterday then returned value is "Yesterday - {time}",
	 *         otherwise is {date} - {time}. "Today" and "Yesterday" labels can be localized. If one of the inputs is null
	 *         then "Unknown" is returned.
	 */
	public String getFormattedDateLabel(final Date now, final Date referenceDate)
	{
		if (now == null || referenceDate == null)
		{
			return Labels.getLabel(LABEL_UNKNOWN);
		}

		final DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT, Locales.getCurrent());
		final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locales.getCurrent());
		String labelKey = LABEL_LATERDATE;
		if (DayUtils.isYesterday(now, referenceDate))
		{
			labelKey = LABEL_YESTERDAY;
		}
		else if (DayUtils.isToday(now, referenceDate))
		{
			labelKey = LABEL_TODAY;
		}

		return getLabel(labelKey, timeFormat.format(referenceDate), dateFormat.format(referenceDate));
	}

	protected String getLabel(final String labelKey, final String timeFormat, final String dateFormat)
	{
		return Labels.getLabel(labelKey, new String[]
		{ timeFormat, dateFormat });
	}

}
