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
package de.hybris.platform.ruleengine.infrastructure;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang.ArrayUtils.isNotEmpty;
import static org.springframework.util.ReflectionUtils.invokeMethod;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.util.ClassUtils;

import com.google.common.collect.Maps;


/**
 * The bean post processor for {@link GetRuleEngineGlobalByName} annotated methods
 */
public class PostRuleEngineInitializeBeanPostProcessor implements BeanFactoryAware, BeanPostProcessor, Ordered
{
	private BeanFactory beanFactory;
	private final Map<String, Class> ruleGlobalsAwareBeans = Maps.newHashMap();
	private final Map<String, Method> ruleGlobalsRetrievalMethods = Maps.newHashMap();

	@Override
	public void setBeanFactory(final BeanFactory beanFactory) throws BeansException
	{
		this.beanFactory = beanFactory;
	}

	@Override
	public Object postProcessBeforeInitialization(final Object bean, final String beanName)
	{
		final Class<?> beanClass = bean.getClass();
		final Method[] methods = beanClass.getDeclaredMethods();
		for (final Method method : methods)
		{
			if (method.isAnnotationPresent(GetRuleEngineGlobalByName.class))
			{
				ruleGlobalsAwareBeans.put(beanName, beanClass);
				ruleGlobalsRetrievalMethods.put(beanName, method);
				break;
			}
		}
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(final Object bean, final String beanName)
	{
		final Class origBeanClass = ruleGlobalsAwareBeans.get(beanName);
		if (nonNull(origBeanClass))
		{
			final Method ruleGlobalsRetrievalMethod = ruleGlobalsRetrievalMethods.get(beanName);
			return Proxy.newProxyInstance(getClass().getClassLoader(), ClassUtils.getAllInterfacesForClass(origBeanClass), (proxy,
					method1, args) -> getRuleGlobalBean(bean, method1, ruleGlobalsRetrievalMethod.getName(), args));
		}
		return bean;
	}

	private Object getRuleGlobalBean(final Object bean, final Method proxyMethod, final String ruleGlobalsGetMethodName,
			final Object... args)
	{
		if (proxyMethod.getName().equals(ruleGlobalsGetMethodName) && hasOneStringArg(args))
		{
			return beanFactory.getBean((String) args[0]);
		}
		return invokeMethod(proxyMethod, bean, args);
	}

	protected boolean hasOneStringArg(final Object... args)
	{
		return isNotEmpty(args) && args.length == 1 && args[0] instanceof String;
	}

	@Override
	public int getOrder()
	{
		return LOWEST_PRECEDENCE;
	}
}
