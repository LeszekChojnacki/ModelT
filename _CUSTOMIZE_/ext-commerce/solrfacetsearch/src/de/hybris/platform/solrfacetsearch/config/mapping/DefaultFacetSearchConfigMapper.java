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
package de.hybris.platform.solrfacetsearch.config.mapping;

import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;

import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import ma.glasnost.orika.Converter;
import ma.glasnost.orika.Filter;
import ma.glasnost.orika.Mapper;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.ConfigurableMapper;


/**
 * Implementation of {@link ma.glasnost.orika.impl.ConfigurableMapper} used for cloning FacetSearchConfig. <br/>
 * It automatically discovers and registers managed beans of type {@link ma.glasnost.orika.Mapper},
 * {@link ma.glasnost.orika.Converter} or {@link ma.glasnost.orika.Filter} annotated with
 * {@link FacetSearchConfigMapping}.
 */
public class DefaultFacetSearchConfigMapper extends ConfigurableMapper implements ApplicationContextAware
{
	protected MapperFactory mapperFactory;
	protected ApplicationContext applicationContext;

	DefaultFacetSearchConfigMapper()
	{
		super(false);
	}

	@Override
	protected void configure(final MapperFactory mapperFactory)
	{
		this.mapperFactory = mapperFactory;
		addAllSpringBeans();
	}

	/**
	 * Registers all managed beans of type {@link ma.glasnost.orika.Mapper}, {@link ma.glasnost.orika.Converter} or
	 * {@link ma.glasnost.orika.Filter} and annotated with {@link FacetSearchConfigMapping}.
	 */
	protected void addAllSpringBeans()
	{
		final Map<String, Object> beans = applicationContext.getBeansWithAnnotation(FacetSearchConfigMapping.class);

		for (final Object bean : beans.values())
		{
			if (bean instanceof Converter)
			{
				addConverter((Converter) bean);
			}
			else if (bean instanceof Mapper)
			{
				addMapper((Mapper) bean);
			}
			else if (bean instanceof Filter)
			{
				addFilter((Filter) bean);
			}
		}
	}

	/**
	 * Registers a {@link ma.glasnost.orika.Converter}.
	 *
	 * @param converter
	 *           The converter.
	 */
	public void addConverter(final Converter<?, ?> converter)
	{
		mapperFactory.getConverterFactory().registerConverter(converter);
	}

	/**
	 * Registers a {@link ma.glasnost.orika.Mapper}.
	 *
	 * @param mapper
	 *           The mapper.
	 */
	public void addMapper(final Mapper<?, ?> mapper)
	{
		mapperFactory.classMap(mapper.getAType(), mapper.getBType()).byDefault().customize((Mapper) mapper).register();
	}

	/**
	 * Registers a {@link ma.glasnost.orika.Filter}.
	 *
	 * @param filter
	 *           The filter.
	 */
	public void addFilter(final Filter<?, ?> filter)
	{
		mapperFactory.registerFilter(filter);
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext)
	{
		this.applicationContext = applicationContext;
		init();

		/*
		 * Workaround for an ugly bug in Orika:
		 *
		 * classPool is not thread-safe in ma.glasnost.orika.impl.generator.JavassistCompilerStrategy.assureTypeIsAccessible()
		 *
		 * Race condition occurs when multiple indexing cronjobs start at the same time (i.e. when platform starts).
		 *
		 * There should be a synchronized lock on assureTypeIsAccessible method or classPool object.
		 *
		 * As a workaround we can make sure that platform's class loader (PlatformInPlaceClassLoader) is registered by Orika at
		 * the very beginning by calling this:
		 */
		map(new FacetSearchConfig(), FacetSearchConfig.class);
	}

}
