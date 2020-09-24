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

import de.hybris.platform.core.model.type.AttributeDescriptorModel;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.OrderComparator;


/**
 * Default registry for excel translator strategies.
 */
public class ExcelTranslatorRegistry
{

	private List<ExcelValueTranslator<Object>> translators;

	/**
	 * Finds translator which can handle given attribute descriptor.
	 * 
	 * @param attributeDescriptor
	 *           which represents selected attribute.
	 * @return Optional of {@link ExcelValueTranslator}. If none translator can handle given attribute descriptor then
	 *         {@link Optional#empty()} will be returned.
	 */
	public Optional<ExcelValueTranslator<Object>> getTranslator(final AttributeDescriptorModel attributeDescriptor)
	{
		return getTranslators().stream().filter(translator -> translator.canHandle(attributeDescriptor)).findFirst();
	}

	/**
	 * Finds translator which can handle given attribute descriptor.
	 *
	 * @param attributeDescriptor
	 *           which represents selected attribute.
	 * @param exclude
	 *           translators classes which should not be taken into account
	 * @return Optional of {@link ExcelValueTranslator<Object>}. If none translator can handle given attribute descriptor
	 *         then {@link Optional#empty()} will be returned.
	 */
	public Optional<ExcelValueTranslator<Object>> getTranslator(final AttributeDescriptorModel attributeDescriptor,
			final Class<? extends ExcelValueTranslator>... exclude)
	{
		final Collection<Class<? extends ExcelValueTranslator>> excludedTranslators = Optional.ofNullable(exclude) //
				.map(Arrays::asList) //
				.orElseGet(Collections::emptyList);
		return getTranslators().stream() //
				.filter(translator -> !excludedTranslators.contains(getTranslatorClass(translator))) //
				.filter(translator -> translator.canHandle(attributeDescriptor)).findFirst();
	}

	protected Class<? extends ExcelValueTranslator> getTranslatorClass(final ExcelValueTranslator<Object> translator)
	{
		return translator.getClass();
	}

	/**
	 * Post construct method which sorts translators by its order. The method shouldn't be invoked manually.
	 *
	 * @deprecated since 1811 use method {@link #getTranslators()} which return sorted translators
	 */
	@Deprecated
	@PostConstruct
	public void init()
	{
	}

	/**
	 * Indicates whether there is at least one translator which can handle given attribute descriptor.
	 * 
	 * @param attributeDescriptor
	 *           which represents selected attribute.
	 * @return boolean whether at least one translator can handler given attribute descriptor.
	 */
	public boolean canHandle(final AttributeDescriptorModel attributeDescriptor)
	{
		return getTranslators().stream().anyMatch(translator -> translator.canHandle(attributeDescriptor));
	}

	/**
	 * Returns list of registered translators.
	 *
	 * @return list of registered translators.
	 */
	public List<ExcelValueTranslator<Object>> getTranslators()
	{
		OrderComparator.sort(translators);
		return translators;
	}

	/**
	 * Sets list of translators for the registry.
	 * 
	 * @param translators
	 *           for the registry.
	 */
	@Required
	public void setTranslators(final List<ExcelValueTranslator<Object>> translators)
	{
		this.translators = translators;
	}
}
