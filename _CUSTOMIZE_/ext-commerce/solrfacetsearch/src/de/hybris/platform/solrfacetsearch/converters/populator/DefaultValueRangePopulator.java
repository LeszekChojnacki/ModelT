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
package de.hybris.platform.solrfacetsearch.converters.populator;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.solrfacetsearch.config.ValueRange;
import de.hybris.platform.solrfacetsearch.config.ValueRangeType;
import de.hybris.platform.solrfacetsearch.config.ValueRanges;
import de.hybris.platform.solrfacetsearch.jalo.config.SolrValueRangeSet;
import de.hybris.platform.solrfacetsearch.model.config.SolrValueRangeModel;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;


/**
 * Populates Name and From and To values of a {@link ValueRange}. The type of the From and To values is determined by
 * the {@link SolrValueRangeSet#getType()} value.
 */
public class DefaultValueRangePopulator implements Populator<SolrValueRangeModel, ValueRange>
{

	private static final Logger LOG = Logger.getLogger(DefaultValueRangePopulator.class);

	@Override
	public void populate(final SolrValueRangeModel source, final ValueRange target)
	{
		target.setFrom(populateComparableFromString(source.getSolrValueRangeSet().getType(), source.getFrom()));
		target.setName(source.getName());
		target.setTo(populateComparableFromString(source.getSolrValueRangeSet().getType(), source.getTo()));
	}

	protected Comparable populateComparableFromString(final String type, final String value)
	{
		if (StringUtils.isBlank(value))
		{
			return value;
		}

		switch (ValueRangeType.valueOf(type.toUpperCase(Locale.ROOT)))
		{
			case STRING:
				return value.toLowerCase(Locale.ROOT).intern();
			case DOUBLE:
				return Double.valueOf(value);
			case INT:
				return Integer.valueOf(value);
			case DATE:
				return parseDate(value);
			default:
				return value;
		}
	}

	/**
	 * Tries to parse string date to Date object. In case of parsing exception a new Date(0); is returned.
	 */
	protected Comparable parseDate(final String value)
	{
		try
		{
			return ValueRanges.parseDate(value);
		}
		catch (final ParseException e)
		{
			LOG.error(String.format("'%s' is not a proper date value. Accepted format : %s", value, ValueRanges.DATEFORMAT));
			return new Date(0);
		}
	}

}
