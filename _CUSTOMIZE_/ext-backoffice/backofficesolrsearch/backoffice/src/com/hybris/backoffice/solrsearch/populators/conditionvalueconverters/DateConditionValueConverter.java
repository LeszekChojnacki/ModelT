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

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.function.Function;

import com.hybris.backoffice.solrsearch.constants.BackofficesolrsearchConstants;


public class DateConditionValueConverter implements Function<Date, String>
{

	private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter
			.ofPattern(BackofficesolrsearchConstants.SOLR_DATE_FORMAT);

	@Override
	public String apply(final Date date)
	{
		return date != null ? dateTimeFormatter.format(date.toInstant().atZone(ZoneOffset.UTC)) : "";
	}

}
