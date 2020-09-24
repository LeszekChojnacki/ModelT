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


import de.hybris.platform.spring.ctx.TenantIgnoreXmlWebApplicationContext;

import java.io.IOException;
import java.io.ObjectOutputStream;

import org.apache.commons.io.output.NullOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;


public class SessionBeansGuardPostProcessor implements BeanPostProcessor, ApplicationContextAware
{
	private static final Logger LOG = LoggerFactory.getLogger(SessionBeansGuardPostProcessor.class);

	private ConfigurableListableBeanFactory applicationContext;

	@Override
	public Object postProcessBeforeInitialization(final Object bean, final String beanName)
	{
		if (applicationContext == null)
		{
			return bean;
		}

		try
		{
			if (applicationContext.containsBeanDefinition(beanName))
			{
				final String scope = applicationContext.getBeanDefinition(beanName).getScope();

				if (WebApplicationContext.SCOPE_SESSION.equalsIgnoreCase(scope) && !canSerializeBean(bean, beanName))
				{
					LOG.warn(
							"A session scoped bean '{}' is not serializable. This may lead to errors in cluster environment. Please make your session bean serializable.",
							beanName);
				}
			}
		}
		catch (final NoSuchBeanDefinitionException noBeanException)
		{
			LOG.debug(noBeanException.getMessage(), noBeanException);
		}

		return bean;
	}

	protected boolean canSerializeBean(final Object bean, final String beanName)
	{
		try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new NullOutputStream()))
		{
			objectOutputStream.writeObject(bean);
			objectOutputStream.flush();
		}
		catch (final IOException e)
		{
			LOG.debug("A session scoped bean '" + beanName + "' is not serializable", e);
			return false;
		}
		return true;
	}

	@Override
	public Object postProcessAfterInitialization(final Object bean, final String beanName)
	{
		return bean;
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext)
	{
		if (applicationContext instanceof ConfigurableListableBeanFactory)
		{
			this.applicationContext = (ConfigurableListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
		}
		else if (applicationContext instanceof TenantIgnoreXmlWebApplicationContext)
		{
			this.applicationContext = ((TenantIgnoreXmlWebApplicationContext) applicationContext).getBeanFactory();
		}
		else
		{
			LOG.warn("Could not wire application context: {}, expected an instance of {}", applicationContext,
					ConfigurableListableBeanFactory.class.getSimpleName());
		}
	}
}
