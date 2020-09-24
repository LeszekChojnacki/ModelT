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

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang.ArrayUtils.isEmpty;
import static org.springframework.util.ClassUtils.getAllInterfacesForClass;
import static org.springframework.util.ReflectionUtils.invokeMethod;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;


/**
 * The bean post processor for {@link Prototyped} annotated methods
 */
public class PrototypedBeanPostProcessor implements BeanFactoryAware, BeanPostProcessor, Ordered
{
	private BeanFactory beanFactory;
	private final Map<String, Class> prototypedAwareBeans = newHashMap();
	private final Map<String, Set<Method>> prototypedMethodsMap = newHashMap();

	@Override
	public void setBeanFactory(final BeanFactory beanFactory) throws BeansException
	{
		this.beanFactory = beanFactory;
	}

	@Override
	public Object postProcessBeforeInitialization(final Object bean, final String beanName)
	{
		final Class<?> beanClass = bean.getClass();
		final Method[] methods = beanClass.getMethods();
		final Set<Method> prototypedMethods = newHashSet();
		for (final Method method : methods)
		{
			if (method.isAnnotationPresent(Prototyped.class))
			{
				prototypedMethods.add(method);
			}
		}
		if (isNotEmpty(prototypedMethods))
		{
			prototypedAwareBeans.putIfAbsent(beanName, beanClass);
			prototypedMethodsMap.put(beanName, prototypedMethods);
		}
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(final Object bean, final String beanName)
	{
		final Class origBeanClass = prototypedAwareBeans.get(beanName);
		if (nonNull(origBeanClass))
		{
			final Set<Method> prototypedMethods = prototypedMethodsMap.get(beanName);
			final Map<String, String> methodsMap = prototypedMethods.stream().collect(
					Collectors.toMap(Method::getName, m -> m.getAnnotation(Prototyped.class).beanName()));
			return Proxy.newProxyInstance(getClass().getClassLoader(), getAllInterfacesForClass(origBeanClass), (proxy, method1,
					args) -> getPrototypedBean(bean, method1, methodsMap, args));
		}
		return bean;
	}

	private Object getPrototypedBean(final Object bean, final Method proxyMethod, final Map<String, String> prototypedBeans,
			final Object... args)
	{
		final String prototypedBeanName = prototypedBeans.get(proxyMethod.getName());
		if (nonNull(prototypedBeanName) && isEmpty(args))
		{
			return beanFactory.getBean(prototypedBeanName);
		}
		return invokeMethod(proxyMethod, bean, args);
	}

	@Override
	public int getOrder()
	{
		return LOWEST_PRECEDENCE;
	}
}
