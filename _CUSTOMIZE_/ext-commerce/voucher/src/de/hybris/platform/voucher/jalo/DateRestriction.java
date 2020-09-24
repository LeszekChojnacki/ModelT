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
package de.hybris.platform.voucher.jalo;

import de.hybris.platform.jalo.Item;
import de.hybris.platform.jalo.JaloBusinessException;
import de.hybris.platform.jalo.JaloInvalidParameterException;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.order.AbstractOrder;
import de.hybris.platform.jalo.product.Product;
import de.hybris.platform.jalo.type.ComposedType;
import de.hybris.platform.util.localization.Localization;
import de.hybris.platform.voucher.jalo.util.DateTimeUtils;

import java.text.DateFormat;
import java.util.Date;

import org.apache.log4j.Logger;


/**
 * This restriction restricts vouchers to start/end date.
 *
 */
public class DateRestriction extends GeneratedDateRestriction //NOSONAR
{
	// Log4J implementation - edit log4j.properties to configurate your own LOG channel
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(DateRestriction.class.getName());

	/*
	 * create the item you can delete this method if you don't want to intercept the creation of this item
	 */
	@Override
	protected Item createItem(final SessionContext ctx, final ComposedType type, final ItemAttributeMap allAttributes)
			throws JaloBusinessException
	{
		if (!allAttributes.containsKey(DateRestriction.STARTDATE))
		{
			throw new JaloBusinessException("missing " + DateRestriction.STARTDATE + " - cannot create item DateRestriction!");
		}
		else
		{
			if (!(allAttributes.get(DateRestriction.STARTDATE) instanceof Date))
			{
				throw new JaloBusinessException(DateRestriction.STARTDATE + " is NULL or not a Date");
			}
		}
		if (!allAttributes.containsKey(DateRestriction.ENDDATE))
		{
			throw new JaloBusinessException("missing " + DateRestriction.ENDDATE + " - cannot create item DateRestriction!");
		}
		else
		{
			if (!(allAttributes.get(DateRestriction.ENDDATE) instanceof Date))
			{
				throw new JaloBusinessException(DateRestriction.ENDDATE + " is NULL or not a Date");
			}
		}
		//JIRA COM-1884
		final Date startDate = (Date) allAttributes.get(DateRestriction.STARTDATE);
		final Date endDate = (Date) allAttributes.get(DateRestriction.ENDDATE);
		validateStartEndDate(startDate, endDate);
		return super.createItem(ctx, type, allAttributes);
	}

	@Override
	public void setStartDate(final Date value)
	{
		super.setStartDate(value);
		validateStartEndDate(value, getEndDate());
	}

	@Override
	public void setEndDate(final Date value)
	{
		super.setEndDate(value);
		validateStartEndDate(getStartDate(), value);
	}

	protected void validateStartEndDate(final Date startDate, final Date endDate)
	{
		if (startDate != null && endDate != null && endDate.before(startDate))
		{
			final String l13nMessage = Localization.getLocalizedString("type.daterestriction.validation.endstartdate");
			throw new JaloInvalidParameterException(l13nMessage, 0);
		}

	}

	@Override
	protected String[] getMessageAttributeValues()
	{
		final DateFormat dateFormat = DateTimeUtils.getDateFormat(DateFormat.SHORT);
		final String startdatestring = getStartDate() != null ? dateFormat.format(getStartDate()) : "n/a";
		final String enddatestring = getEndDate() != null ? dateFormat.format(getEndDate()) : "n/a";
		return new String[]
		{ Localization.getLocalizedString("type.restriction.positive." + isPositiveAsPrimitive()), startdatestring, enddatestring };
	}

	/**
	 * Returns <tt>true</tt> if the specified abstract order fulfills this restriction. More formally, returns <tt>true</tt>
	 * if the point of time when this check is executed is within the valid period specified by this restriction (start
	 * date, end date and positive).
	 *
	 * @param anOrder
	 *           the abstract order to check whether it fullfills this restriction.
	 * @return <tt>true</tt> if the specified abstract order fulfills this restriction, <tt>false</tt> else.
	 * @see Restriction#isFulfilledInternal(AbstractOrder)
	 */
	@Override
	protected boolean isFulfilledInternal(final AbstractOrder anOrder)
	{
		boolean start;
		boolean end;
		final Date currentDate = new Date();
		final Date startDate = getStartDate();
		final Date endDate = getEndDate();
		start = (startDate == null) || (startDate.before(currentDate) == isPositiveAsPrimitive());
		end = (endDate == null) || (endDate.after(currentDate) == isPositiveAsPrimitive());
		return start && end;
	}

	/**
	 * Returns <tt>true</tt> if the specified product fulfills this restriction.
	 *
	 * @param aProduct
	 *           the product to check whether it fullfills this restriction.
	 * @return <tt>true</tt> if the specified product fulfills this restriction, <tt>false</tt> else.
	 * @see Restriction#isFulfilledInternal(Product)
	 */
	@Override
	protected boolean isFulfilledInternal(final Product aProduct) //NOSONAR
	{
		return true;
	}
}
