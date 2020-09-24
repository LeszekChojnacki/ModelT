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

import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider;
import de.hybris.platform.solrfacetsearch.provider.FieldValue;
import de.hybris.platform.solrfacetsearch.provider.FieldValueProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;


/**
 * Parses spring expression value set for indexed property and produces collection of field values
 */
public class SpELValueProvider implements FieldValueProvider, ApplicationContextAware
{
	private ExpressionParser parser;
	private ApplicationContext applicationContext;
	private FieldNameProvider fieldNameProvider;
	private CommonI18NService commonI18NService;

	@Override
	public Collection<FieldValue> getFieldValues(final IndexConfig indexConfig, final IndexedProperty indexedProperty,
			final Object model) throws FieldValueProviderException
	{
		final String exprValue = getSpringExpression(indexedProperty);

		final Expression parsedExpression = parser.parseExpression(exprValue);
		final StandardEvaluationContext context = new StandardEvaluationContext(model);
		context.setBeanResolver(new BeanFactoryResolver(applicationContext));
		context.setVariable("item", model);

		final List<FieldValue> resolvedFieldValues = new ArrayList<>();

		if (indexedProperty.isLocalized())
		{
			for (final LanguageModel language : indexConfig.getLanguages())
			{
				final Locale locale = commonI18NService.getLocaleForLanguage(language);
				context.setVariable("lang", locale);

				final Object value = parsedExpression.getValue(context);
				resolvedFieldValues.addAll(resolve(indexedProperty, value, language.getIsocode()));
			}
		}
		else if (indexedProperty.isCurrency())
		{
			for (final CurrencyModel currency : indexConfig.getCurrencies())
			{
				final CurrencyModel sessionCurrency = commonI18NService.getCurrentCurrency();
				try
				{
					commonI18NService.setCurrentCurrency(currency);
					context.setVariable("currency", currency);
					final Object value = parsedExpression.getValue(context);
					resolvedFieldValues.addAll(resolve(indexedProperty, value, currency.getIsocode()));
				}
				finally
				{
					commonI18NService.setCurrentCurrency(sessionCurrency);
				}
			}
		}
		else
		{
			final Object value = parsedExpression.getValue(context);
			resolvedFieldValues.addAll(resolve(indexedProperty, value, null));
		}

		return resolvedFieldValues;
	}

	protected Collection resolve(final IndexedProperty indexedProperty, final Object value, final String qualifier)
	{
		final Collection<String> fieldNames = fieldNameProvider.getFieldNames(indexedProperty, qualifier);

		if (value instanceof Collection)
		{
			return resolveValuesForCollection((Collection) value, fieldNames);

		}
		else if (value == null)
		{
			return Collections.emptyList();
		}
		else
		{
			return getFieldValuesForFieldNames(fieldNames, value);
		}
	}

	protected Collection<FieldValue> resolveValuesForCollection(final Collection value, final Collection<String> fieldNames)
	{
		final Collection<Collection<FieldValue>> fieldValues = Collections2.transform(value,
				new Function<Object, Collection<FieldValue>>()
				{
					@Override
					public Collection<FieldValue> apply(final Object applyTo)
					{
						return getFieldValuesForFieldNames(fieldNames, applyTo);
					}
				});

		return Lists.newArrayList(Iterables.concat(fieldValues));
	}

	protected Collection<FieldValue> getFieldValuesForFieldNames(final Collection<String> fieldNames, final Object o)
	{
		return Collections2.transform(fieldNames, new Function<String, FieldValue>()
		{
			@Override
			public FieldValue apply(final String fieldName)
			{
				return new FieldValue(fieldName, String.valueOf(o));
			}
		});
	}

	protected String getSpringExpression(final IndexedProperty indexedProperty)
	{
		String exprValue = indexedProperty.getValueProviderParameter();
		if (exprValue == null)
		{
			exprValue = indexedProperty.getName();
		}
		return exprValue;
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext)
	{
		this.applicationContext = applicationContext;
	}

	@Required
	public void setFieldNameProvider(final FieldNameProvider fieldNameProvider)
	{
		this.fieldNameProvider = fieldNameProvider;
	}

	@Required
	public void setParser(final ExpressionParser parser)
	{
		this.parser = parser;
	}

	@Required
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}
}
