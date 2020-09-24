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

import de.hybris.platform.converters.Converters;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.solrfacetsearch.config.ValueRange;
import de.hybris.platform.solrfacetsearch.config.ValueRangeSet;
import de.hybris.platform.solrfacetsearch.config.ValueRangeType;
import de.hybris.platform.solrfacetsearch.model.config.SolrValueRangeModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrValueRangeSetModel;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;


public class DefaultValueRangeSetPopulator implements Populator<SolrValueRangeSetModel, ValueRangeSet>
{
	private static final String DEFAULT_QUALIFIER = "default";
	private Converter<SolrValueRangeModel, ValueRange> valueRangeConverter;
	private String defaultQualifier;

	@Override
	public void populate(final SolrValueRangeSetModel source, final ValueRangeSet target)
	{
		if (source.getQualifier() != null)
		{
			target.setQualifier(source.getQualifier());
		}
		else
		{
			target.setQualifier(getDefaultQualifier());
		}
		target.setValueRanges(Converters.convertAll(source.getSolrValueRanges(), valueRangeConverter));
		target.setType(ValueRangeType.valueOf(source.getType().toUpperCase(Locale.ROOT)));
	}

	public void setValueRangeConverter(final Converter<SolrValueRangeModel, ValueRange> valueRangeConverter)
	{
		this.valueRangeConverter = valueRangeConverter;
	}

	protected String getDefaultQualifier()
	{
		if (StringUtils.isEmpty(defaultQualifier))
		{
			return DEFAULT_QUALIFIER;
		}
		return defaultQualifier;
	}

	/**
	 * @param defaultQualifier
	 *           the defaultQualifier to set
	 */
	public void setDefaultQualifier(final String defaultQualifier)
	{
		this.defaultQualifier = defaultQualifier;
	}

}
