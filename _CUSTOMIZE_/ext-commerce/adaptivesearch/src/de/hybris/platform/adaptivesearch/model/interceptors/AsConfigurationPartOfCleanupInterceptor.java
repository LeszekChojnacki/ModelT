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
package de.hybris.platform.adaptivesearch.model.interceptors;

import de.hybris.platform.adaptivesearch.model.AbstractAsConfigurationModel;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.PersistenceOperation;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;
import de.hybris.platform.servicelayer.internal.model.impl.DefaultModelService;
import de.hybris.platform.servicelayer.type.TypeService;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;


public class AsConfigurationPartOfCleanupInterceptor extends AbstractAsInterceptor
		implements PrepareInterceptor<AbstractAsConfigurationModel>
{
	private TypeService typeService;

	@Override
	public void onPrepare(final AbstractAsConfigurationModel configuration, final InterceptorContext context)
	{
		if (shouldProcessConfiguration(configuration, context))
		{
			final Set<String> partOfAttributes = getWritablePartOfAttributes(configuration, context);
			for (final String partOfAttribute : partOfAttributes)
			{
				processPartOfAttribute(configuration, partOfAttribute, context);
			}
		}
	}

	protected boolean shouldProcessConfiguration(final AbstractAsConfigurationModel configuration,
			final InterceptorContext context)
	{
		return context.isModified(configuration) && !context.isNew(configuration) && !context.isRemoved(configuration);
	}

	protected Set<String> getWritablePartOfAttributes(final AbstractAsConfigurationModel configuration,
			final InterceptorContext context)
	{
		final String itemType = configuration.getItemModelContext().getItemType();
		return ((DefaultModelService) context.getModelService()).getConverterRegistry().getModelConverterBySourceType(itemType)
				.getWritablePartOfAttributes(typeService);
	}

	protected void processPartOfAttribute(final AbstractAsConfigurationModel configuration, final String partOfAttribute,
			final InterceptorContext context)
	{
		final Map<String, Set<Locale>> dirtyAttributes = context.getDirtyAttributes(configuration);

		if (dirtyAttributes.containsKey(partOfAttribute))
		{
			final Set<Locale> locales = dirtyAttributes.get(partOfAttribute);
			if (locales == null)
			{
				//for un-localized
				final Object currentValue = getAttributeValue(configuration, partOfAttribute);
				final Object originalValue = getOriginalValue(configuration, partOfAttribute);
				removeItems(configuration, currentValue, originalValue, context);
			}
			else
			{
				//for localized
				for (final Locale locale : locales)
				{
					final Object currentValue = getAttributeValue(configuration, partOfAttribute, locale);
					final Object originalValue = getOriginalValue(configuration, partOfAttribute, locale);
					removeItems(configuration, currentValue, originalValue, context);
				}
			}
		}
	}

	protected void removeItems(final AbstractAsConfigurationModel configuration, final Object currentValue,
			final Object originalValue, final InterceptorContext context)
	{
		if (!Objects.equals(originalValue, currentValue))
		{
			final Collection<ItemModel> items = collectItemsToRemove(originalValue, currentValue);

			for (final ItemModel item : items)
			{
				if (!context.contains(item, PersistenceOperation.DELETE))
				{
					context.getModelService().remove(item);
				}
			}
		}
	}

	protected Collection<ItemModel> collectItemsToRemove(final Object originalValue, final Object currentValue)
	{
		if (originalValue instanceof Collection)
		{
			final Collection originalCollection = (Collection) originalValue;
			if (CollectionUtils.isNotEmpty(originalCollection))
			{
				Collection currentCollection = (Collection) currentValue;
				if (currentCollection == null)
				{
					currentCollection = Collections.emptyList();
				}

				return CollectionUtils.subtract(originalCollection, currentCollection);
			}
			else
			{
				return Collections.emptyList();
			}
		}
		else if (originalValue instanceof ItemModel)
		{
			return Collections.singletonList((ItemModel) originalValue);
		}
		else
		{
			throw new IllegalArgumentException("Not supported original value " + originalValue);
		}
	}

	protected Object getOriginalValue(final AbstractAsConfigurationModel configuration, final String attribute)
	{
		return configuration.getItemModelContext().getOriginalValue(attribute);
	}

	protected Object getOriginalValue(final AbstractAsConfigurationModel configuration, final String attribute,
			final Locale locale)
	{
		return configuration.getItemModelContext().getOriginalValue(attribute, locale);
	}

	protected Object getAttributeValue(final AbstractAsConfigurationModel configuration, final String attribute)
	{
		return getModelService().getAttributeValue(configuration, attribute);
	}

	protected Object getAttributeValue(final AbstractAsConfigurationModel configuration, final String attribute,
			final Locale locale)
	{
		return getModelService().getAttributeValue(configuration, attribute, locale);
	}

	public TypeService getTypeService()
	{
		return typeService;
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}
}
