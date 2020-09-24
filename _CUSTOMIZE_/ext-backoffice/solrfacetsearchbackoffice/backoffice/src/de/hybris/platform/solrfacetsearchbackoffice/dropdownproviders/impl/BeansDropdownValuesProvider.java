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
package de.hybris.platform.solrfacetsearchbackoffice.dropdownproviders.impl;

import de.hybris.platform.solrfacetsearchbackoffice.dropdownproviders.DropdownValuesProvider;
import de.hybris.platform.solrfacetsearchbackoffice.editor.EditorRuntimeException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.common.collect.Lists;


/**
 * Implementation of {@link DropdownValuesProvider}. Provides all beans for given type.
 */
public class BeansDropdownValuesProvider implements DropdownValuesProvider, ApplicationContextAware
{
	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext)
	{
		this.applicationContext = applicationContext;
	}

	@Override
	public List<Object> getValues(final String dropDownValueClassTypes)
	{
		return getValues(dropDownValueClassTypes, null);
	}

	@Override
	public List<Object> getValues(final String dropDownValueClassTypes, final Map<String, String> options)
	{
		final List<Object> data = Lists.newArrayList();

		final List<String> splitClassTypes = Arrays.asList(dropDownValueClassTypes.split(","));

		try
		{
			for (final String className : splitClassTypes)
			{
				final Class classType = Class.forName(className);

				for (final String beanName : BeanFactoryUtils.beanNamesForTypeIncludingAncestors(applicationContext, classType))
				{
					data.add(resolveProviderName(beanName));
				}
			}

			return data;
		}
		catch (final ClassNotFoundException e)
		{
			throw new EditorRuntimeException(e);
		}
	}

	protected String resolveProviderName(final String beanName)
	{
		final String[] beanAliases = applicationContext.getAliases(beanName);

		if (beanAliases.length > 0)
		{
			return beanAliases[0];
		}
		else
		{
			return beanName;
		}
	}
}
