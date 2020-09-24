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
package com.hybris.backoffice.solrsearch.populators;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.converters.impl.AbstractPopulatingConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * This bean loads <b>defaultBackofficeIndexedPropertyPopulator</b> (if needed) and add it to bean
 * <b>indexedPropertyConverter</b> collection of populators. It can't be done by only spring configuration because in
 * commerce is extension that extend this in spring configuration, but backoffice do not know about this extension. If
 * we try to do it in bean configuration one extension will overwrite changes in second extension. So it have to work
 * with both scenarios: only backoffice and with whole commerce.
 */
public class BackofficeIndexedPropertyPopulatorLoader implements BeanPostProcessor, ApplicationContextAware, BeanFactoryAware
{
	static final String INDEXED_PROPERTY_CONVERTER_ALIAS = "indexedPropertyConverter";
	static final String BACKOFFICE_INDEXED_PROPERTY_POPULATOR_BEAN_NAME = "defaultBackofficeIndexedPropertyPopulator";

	private BeanFactory beanFactory;
	private ApplicationContext applicationContext;

	@Override
	public void setBeanFactory(final BeanFactory beanFactory) throws BeansException
	{
		this.beanFactory = beanFactory;
	}

	@Override
	public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException
	{
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException
	{
		if (bean instanceof AbstractPopulatingConverter && isBeanAliasesByIndexedPropertyConverter(beanName))
		{
			final BackofficeIndexedPropertyPopulator populator = (BackofficeIndexedPropertyPopulator) applicationContext
					.getBean(BACKOFFICE_INDEXED_PROPERTY_POPULATOR_BEAN_NAME);

			final AbstractPopulatingConverter populatingConverter = (AbstractPopulatingConverter) bean;

			final List<Populator> newList = new ArrayList<>(populatingConverter.getPopulators());
			newList.add(populator);
			populatingConverter.setPopulators(newList);
		}
		return bean;
	}

	private boolean isBeanAliasesByIndexedPropertyConverter(final String beanName)
	{
		return Arrays.asList(beanFactory.getAliases(beanName)).contains(INDEXED_PROPERTY_CONVERTER_ALIAS);
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException
	{
		this.applicationContext = applicationContext;
	}
}
