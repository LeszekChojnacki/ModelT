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
package com.hybris.backoffice.cockpitng.json.impl;

import de.hybris.platform.servicelayer.dto.converter.Converter;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.cockpitng.json.MutableConverterRegistry;



/**
 * Class defining implicitly which converter should be used for specific conversion
 */
public class ConverterRegisterExtension<S, D>
{

	private MutableConverterRegistry converterRegistry;

	private Class<? extends S> source;

	private Class<? extends D> destination;

	private Converter<S, D> converter;


	@PostConstruct
	public void extendRegistry()
	{
		getConverterRegistry().addConverter(getConverter(), getSource(), getDestination());
	}

	protected MutableConverterRegistry getConverterRegistry()
	{
		return converterRegistry;
	}

	@Required
	public void setConverterRegistry(final MutableConverterRegistry converterRegistry)
	{
		this.converterRegistry = converterRegistry;
	}

	protected Class<? extends S> getSource()
	{
		return source;
	}

	@Required
	public void setSource(final Class<? extends S> source)
	{
		this.source = source;
	}

	protected Class<? extends D> getDestination()
	{
		return destination;
	}

	@Required
	public void setDestination(final Class<? extends D> destination)
	{
		this.destination = destination;
	}

	protected Converter<S, D> getConverter()
	{
		return converter;
	}

	@Required
	public void setConverter(final Converter<S, D> converter)
	{
		this.converter = converter;
	}
}
