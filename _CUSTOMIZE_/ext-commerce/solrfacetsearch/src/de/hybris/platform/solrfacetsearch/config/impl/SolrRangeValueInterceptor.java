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
package de.hybris.platform.solrfacetsearch.config.impl;

import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.ValidateInterceptor;
import de.hybris.platform.solrfacetsearch.config.ValueRangeType;
import de.hybris.platform.solrfacetsearch.config.ValueRanges;
import de.hybris.platform.solrfacetsearch.model.config.SolrValueRangeModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrValueRangeSetModel;

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;


/**
 * Interceptor that checks if ranges of {@link SolrValueRangeModel} are set properly
 */
public class SolrRangeValueInterceptor implements ValidateInterceptor
{

	@Override
	public void onValidate(final Object model, final InterceptorContext ctx) throws InterceptorException
	{
		if (model instanceof SolrValueRangeModel)
		{
			final SolrValueRangeModel range = (SolrValueRangeModel) model;
			if (range.getSolrValueRangeSet() != null)
			{
				final SolrValueRangeSetModel set = range.getSolrValueRangeSet();
				final String type = set.getType();

				if (ValueRangeType.DOUBLE.toString().compareToIgnoreCase(type) == 0)
				{
					parseDouble(range.getFrom(), false);
					parseDouble(range.getTo(), true);
				}
				else if (ValueRangeType.FLOAT.toString().compareToIgnoreCase(type) == 0)
				{
					parseFloat(range.getFrom(), false);
					parseFloat(range.getTo(), true);
				}
				else if (ValueRangeType.INT.toString().compareToIgnoreCase(type) == 0)
				{
					parseInt(range.getFrom(), false);
					parseInt(range.getTo(), true);
				}
				else if (ValueRangeType.DATE.toString().compareToIgnoreCase(type) == 0)
				{
					parseDate(range.getFrom());
					parseDate(range.getTo());
				}
			}
		}
	}

	protected double parseDouble(final String value, final boolean allowEmpty) throws InterceptorException
	{
		try
		{
			if (StringUtils.isBlank(value) && allowEmpty)
			{
				return 0;
			}
			return Double.valueOf(value);
		}
		catch (final NumberFormatException e)
		{
			throw new InterceptorException(String.format("'%s' is not a proper double value ", value), e);
		}
	}

	protected float parseFloat(final String value, final boolean allowEmpty) throws InterceptorException
	{
		try
		{
			if (StringUtils.isBlank(value) && allowEmpty)
			{
				return 0;
			}
			return Float.valueOf(value);
		}
		catch (final NumberFormatException e)
		{
			throw new InterceptorException(String.format("'%s' is not a proper float value ", value), e);
		}
	}

	protected int parseInt(final String value, final boolean allowEmpty) throws InterceptorException
	{
		try
		{
			if (StringUtils.isBlank(value) && allowEmpty)
			{
				return 0;
			}
			return Integer.valueOf(value);
		}
		catch (final NumberFormatException e)
		{
			throw new InterceptorException(String.format("'%s' is not a proper integer value ", value), e);
		}
	}

	protected Date parseDate(final String value) throws InterceptorException
	{
		try
		{
			return ValueRanges.parseDate(value);
		}
		catch (final ParseException e)
		{
			throw new InterceptorException(
					String.format("'%s' is not a proper date value. Accepted format:  %s", value, ValueRanges.DATEFORMAT), e);
		}
	}

}
