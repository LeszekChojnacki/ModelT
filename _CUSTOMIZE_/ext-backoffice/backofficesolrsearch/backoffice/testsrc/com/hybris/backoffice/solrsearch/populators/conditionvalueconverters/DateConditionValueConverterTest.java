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
package com.hybris.backoffice.solrsearch.populators.conditionvalueconverters;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;

import com.hybris.backoffice.solrsearch.constants.BackofficesolrsearchConstants;


public class DateConditionValueConverterTest
{
	private final DateConditionValueConverter converter = new DateConditionValueConverter();

	@Test
	public void shouldConvertDateToSolrDateFormat()
	{
		//given
		final Calendar calendar = Calendar.getInstance();
		calendar.set(2000, Calendar.MARCH, 2, 10, 11, 12);

		for (final String timeZoneID : TimeZone.getAvailableIDs())
		{
			calendar.setTimeZone(TimeZone.getTimeZone(timeZoneID));
			final Date dateToTest = calendar.getTime();

			//when
			final String converted = converter.apply(dateToTest);

			//then
			assertThat(converted).isEqualTo(getExpectedDate(dateToTest, timeZoneID));
		}
	}

	private String getExpectedDate(final Date dateToTest, final String timeZoneID)
	{
		final Calendar calendar = Calendar.getInstance();
		calendar.setTimeZone(TimeZone.getTimeZone(timeZoneID));
		final String DATE_FORMAT = BackofficesolrsearchConstants.SOLR_DATE_FORMAT;
		final DateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

		return formatter.format(dateToTest);
	}
}
