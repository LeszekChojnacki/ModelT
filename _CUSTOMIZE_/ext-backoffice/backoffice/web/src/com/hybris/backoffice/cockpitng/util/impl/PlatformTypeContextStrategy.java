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
package com.hybris.backoffice.cockpitng.util.impl;

import de.hybris.platform.core.model.type.AtomicTypeModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.core.model.type.TypeModel;
import de.hybris.platform.core.model.type.ViewTypeModel;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.type.TypeService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.cockpitng.core.config.CockpitConfigurationContextStrategy;
import com.hybris.cockpitng.core.util.ClassLoaderUtils;


/**
 * Implementation of {@link CockpitConfigurationContextStrategy} that deals with hybris platform type hierarchy for the
 * type configuration context dimension.
 */
public class PlatformTypeContextStrategy implements CockpitConfigurationContextStrategy
{

	private static final Logger LOG = LoggerFactory.getLogger(PlatformTypeContextStrategy.class);

	private TypeService typeService;

	@Override
	public List<String> getParentContexts(final String context)
	{
		try
		{
			final TypeModel typeModel = typeService.getTypeForCode(context);
			if (typeModel instanceof AtomicTypeModel)
			{
				final TypeModel superType = ((AtomicTypeModel) typeModel).getSuperType();
				return superType != null ? Collections.singletonList(superType.getCode()) : Collections.emptyList();
			}
			else if (typeModel instanceof ComposedTypeModel)
			{
				if (((ComposedTypeModel) typeModel).getSuperType() == null || typeModel instanceof ViewTypeModel)
				{
					return Collections.emptyList();
				}
				else
				{
					return Collections.singletonList(((ComposedTypeModel) typeModel).getSuperType().getCode());
				}
			}
			return getParentInterfacesAndClasses(context);
		}
		catch (final UnknownIdentifierException e)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug(e.getMessage(), e);
			}
			return getParentInterfacesAndClasses(context);
		}
		catch (final Exception e)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug(e.getMessage(), e);
			}
			return Collections.emptyList();
		}
	}

	private List<String> getParentInterfacesAndClasses(final String context)
	{
		try
		{
			if (StringUtils.isBlank(context))
			{
				return Collections.emptyList();
			}

			final Class<?> currentClass = ClassLoaderUtils.getCurrentClassLoader(this.getClass()).loadClass(context);
			final List<String> parentInterfacesAndClasses = new ArrayList<>();
			if (currentClass.getSuperclass() != null)
			{
				parentInterfacesAndClasses.add(currentClass.getSuperclass().getName());
			}
			if (currentClass.getInterfaces() != null && currentClass.getInterfaces().length > 0)
			{
				for (final Class currInterface : currentClass.getInterfaces())
				{
					parentInterfacesAndClasses.add(currInterface.getName());
				}
			}
			if (parentInterfacesAndClasses.contains(Object.class.getName()))
			{
				parentInterfacesAndClasses.add(StringUtils.EMPTY);
			}
			return parentInterfacesAndClasses;
		}
		catch (final ClassNotFoundException e)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug(e.getMessage(), e);
			}
			return Collections.emptyList();
		}
	}

	@Override
	public boolean valueMatches(final String contextValue, final String value)
	{
		return StringUtils.equalsIgnoreCase(contextValue, value);
	}


	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}

}
