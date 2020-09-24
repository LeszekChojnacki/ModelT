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
package de.hybris.platform.solrfacetsearch.provider.impl;

import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.indexer.IndexerBatchContext;
import de.hybris.platform.solrfacetsearch.provider.ValueFilter;

import java.text.Format;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;


/**
 * Convert An Object or collection of Objects to a formatted String or collection of Strings based on the Bean ID from
 * the the format parameter which should be an instance of java.text.format
 */
public class FormatValueFilter implements ValueFilter, BeanFactoryAware
{
	public static final String FORMAT_PARAM = "format";
	public static final String FORMAT_PARAM_DEFAULT_VALUE = null;

	private BeanFactory beanFactory;

	@Override
	public void setBeanFactory(final BeanFactory beanFactory)
	{
		this.beanFactory = beanFactory;
	}

	protected Format getValueFormatter(final String formatterBeanName)
	{
		return beanFactory.getBean(formatterBeanName, Format.class);
	}

	/**
	 * @param batchContext
	 *           - The Batch context
	 * @param indexedProperty
	 *           - The indexed properties that should contains the Bean Id of the formatter bean
	 * @param value
	 *           - The Object or collection of Objects to be formatted
	 * @return formatted Object or collection of formatted Object
	 */
	@Override
	public Object doFilter(final IndexerBatchContext batchContext, final IndexedProperty indexedProperty, final Object value)
	{
		if (value == null)
		{
			return null;
		}

		if (value instanceof Collection)
		{
			final Collection<Object> values = (Collection<Object>) value;

			if (values.isEmpty())
			{
				return values;
			}

			final List<Object> resultValues = new ArrayList<>();
			for (final Object singleValue : values)
			{
				resultValues.add(formatValue(batchContext, indexedProperty, singleValue));
			}

			return resultValues;
		}
		else
		{
			return formatValue(batchContext, indexedProperty, value);
		}
	}

	/**
	 * Convert An Object to a formatted String based on the Bean ID from the the format parameter whcih should be an
	 * instance of java.text.format
	 *
	 * @param batchContext
	 *           - The Batch context
	 * @param indexedProperty
	 *           - The indexed properties that should contains the Bean Id of the formatter bean
	 * @param value
	 *           - The Object to be formatted
	 * @return String Object with the formatted value
	 */
	protected Object formatValue(final IndexerBatchContext batchContext, final IndexedProperty indexedProperty, final Object value)
	{
		if (value != null)
		{
			final String formatterBeanName = ValueProviderParameterUtils.getString(indexedProperty, FORMAT_PARAM,
					FORMAT_PARAM_DEFAULT_VALUE);

			if (StringUtils.isNotEmpty(formatterBeanName))
			{
				final Format formatter = getValueFormatter(formatterBeanName);

				return formatter.format(value);
			}
		}

		return value;
	}
}
