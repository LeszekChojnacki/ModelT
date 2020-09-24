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
package com.hybris.backoffice.spring;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;


/**
 * Abstract parser of a single tag that defines multiple beans
 */
public abstract class AbstractMultipleBeanParser extends AbstractBeanDefinitionParser
{

	protected void registerBeanDefinition(final String beanName, final BeanDefinition beanDefinition,
			final ParserContext parserContext)
	{
		parserContext.getRegistry().registerBeanDefinition(beanName, beanDefinition);

		if (this.shouldFireEvents())
		{
			final BeanComponentDefinition componentDefinition = new BeanComponentDefinition(beanDefinition, beanName);
			this.postProcessComponentDefinition(componentDefinition);
			parserContext.registerComponent(componentDefinition);
		}
	}

}
