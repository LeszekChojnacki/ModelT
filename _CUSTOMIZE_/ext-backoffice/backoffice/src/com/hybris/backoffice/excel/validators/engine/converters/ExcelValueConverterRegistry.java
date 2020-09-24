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
package com.hybris.backoffice.excel.validators.engine.converters;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.OrderComparator;

import com.hybris.backoffice.excel.data.ExcelAttribute;
import com.hybris.backoffice.excel.data.ImportParameters;


/**
 * Default registry for excel converters used during validation process.
 */
public class ExcelValueConverterRegistry
{

	private List<ExcelValueConverter> converters;

	/**
	 * Finds converter which can handle given attribute descriptor.
	 * 
	 * @param excelAttribute
	 *           which represents selected attribute.
	 * @param exclude
	 *           list of converters class which shouldn't be taken into account while finding appropriate converter.
	 * @return Optional of {@link ExcelValueConverter}. If none converter can handle given attribute then
	 *         {@link Optional#empty()} will be returned.
	 */
	public <CONVERTER extends ExcelValueConverter> Optional<ExcelValueConverter> getConverter(final ExcelAttribute excelAttribute,
			final ImportParameters importParameters, final Class<CONVERTER>... exclude)
	{
		final Collection<Class<CONVERTER>> excludedConverters = Optional.ofNullable(exclude) //
				.map(Arrays::asList) //
				.orElseGet(Collections::emptyList);
		return getConverters().stream() //
				.filter(converter -> !excludedConverters.contains(converter.getClass())) //
				.filter(converter -> converter.canConvert(excelAttribute, importParameters)) //
				.findFirst();
	}

	/**
	 * Returns list of registered converters.
	 *
	 * @return list of registered converters.
	 */
	public List<ExcelValueConverter> getConverters()
	{
		return converters;
	}

	/**
	 * Sets list of converters for the registry.
	 * 
	 * @param converters
	 *           for the registry.
	 */
	@Required
	public void setConverters(final List<ExcelValueConverter> converters)
	{
		this.converters = converters;
		OrderComparator.sort(converters);
	}
}
