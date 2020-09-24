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
package de.hybris.platform.voucher.jalo.util;

import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.SessionContext;

import java.text.DateFormat;
import java.util.Calendar;

import com.google.common.base.Preconditions;

/**
 * Contains utility methods to work with dates.
 */
public class DateTimeUtils
{
    private DateTimeUtils ()
    {
    }

    /**
     * Returns DateFormat with current TimeZone and Locale.
     */
	public static DateFormat getDateFormat(final int dateStyle)
    {
        final SessionContext ctx = JaloSession.getCurrentSession().getSessionContext();
        Preconditions.checkArgument(ctx != null);
        final DateFormat df = DateFormat.getDateInstance(dateStyle, ctx.getLocale());
        df.setCalendar(Calendar.getInstance(ctx.getTimeZone(), ctx.getLocale()));
        return df;
    }
}
