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
package com.hybris.backoffice.excel.translators;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.OrderComparator;

import com.hybris.backoffice.excel.data.ExcelAttribute;


/**
 * Default registry for excel attribute translator strategies.
 */
public class ExcelAttributeTranslatorRegistry
{

	private List<ExcelAttributeTranslator<ExcelAttribute>> translators;

	/**
	 * Finds translator which can handle given excel attribute.
	 * 
	 * @param excelAttribute
	 *           which represents selected excel attribute.
	 * @return Optional of {@link ExcelAttributeTranslator}. If none translator can handle given attribute descriptor
	 *         then {@link Optional#empty()} will be returned.
	 */
	public Optional<ExcelAttributeTranslator<ExcelAttribute>> findTranslator(final ExcelAttribute excelAttribute)
	{
		return translators.stream().filter(translator -> translator.canHandle(excelAttribute)).findFirst();
	}

	/**
	 * Finds translator which can handle given excel attribute.
	 *
	 * @param excelAttribute
	 *           which represents selected excel attribute.
	 * @param exclude
	 *           translators classes which should not be taken into account
	 * @return Optional of {@link ExcelAttributeTranslator<ExcelAttribute>}. If none translator can handle given
	 *         attribute descriptor then {@link Optional#empty()} will be returned.
	 */
	public Optional<ExcelAttributeTranslator<ExcelAttribute>> findTranslator(final ExcelAttribute excelAttribute,
			final Class<? extends ExcelAttributeTranslator>... exclude)
	{
		final Collection<Class<? extends ExcelAttributeTranslator>> excludedTranslators = Optional.ofNullable(exclude) //
				.map(Arrays::asList) //
				.orElseGet(Collections::emptyList);
		return translators.stream() //
				.filter(translator -> !excludedTranslators.contains(getTranslatorClass(translator))) //
				.filter(translator -> translator.canHandle(excelAttribute)) //
				.findFirst();
	}

	protected Class<? extends ExcelAttributeTranslator> getTranslatorClass(final ExcelAttributeTranslator<ExcelAttribute> translator)
	{
		return translator.getClass();
	}

	/**
	 * Post construct method which sorts translators by its order. The method shouldn't be invoked manually.
	 */
	@PostConstruct
	public void init()
	{
		OrderComparator.sort(getTranslators());
	}

	/**
	 * Indicates whether there is at least one translator which can handle given attribute descriptor.
	 * 
	 * @param excelAttribute
	 *           which represents selected attribute.
	 * @return true whether at least one translator can handle given attribute descriptor.
	 */
	public boolean canHandle(final ExcelAttribute excelAttribute)
	{
		return translators.stream().anyMatch(translator -> translator.canHandle(excelAttribute));
	}

	/**
	 * Returns list of registered attribute translators.
	 *
	 * @return list of registered translators.
	 */
	public List<ExcelAttributeTranslator<ExcelAttribute>> getTranslators()
	{
		return translators;
	}

	/**
	 * Sets list of attribute translators for the registry.
	 * 
	 * @param translators
	 *           for the registry.
	 */
	@Required
	public void setTranslators(final List<ExcelAttributeTranslator<ExcelAttribute>> translators)
	{
		this.translators = translators;
	}
}
