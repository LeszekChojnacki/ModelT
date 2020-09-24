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
package com.hybris.backoffice.cockpitng.dataaccess.facades.object;


import de.hybris.platform.servicelayer.model.AbstractItemModel;
import de.hybris.platform.servicelayer.model.ItemModelContext;
import de.hybris.platform.servicelayer.model.ItemModelContextImpl;
import de.hybris.platform.servicelayer.model.ModelContextUtils;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hybris.cockpitng.dataaccess.context.Context;
import com.hybris.cockpitng.dataaccess.facades.object.impl.PermissionAwareObjectFacade;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;


/**
 * This implementation of {@link PermissionAwareObjectFacade} uses the {@link ItemModelContext} to identify which
 * properties were modified. The lookup includes localized fields.
 */
public class DefaultPlatformPermissionAwareObjectFacade extends PermissionAwareObjectFacade
{
	public static final Logger LOG = LoggerFactory.getLogger(DefaultPlatformPermissionAwareObjectFacade.class);

	@Override
	protected <T> Set<String> getModifiedProperties(final T objectToSave, final Context ctx)
	{
		if (objectToSave instanceof AbstractItemModel)
		{
			final ItemModelContext context = ModelContextUtils.getItemModelContext(((AbstractItemModel) objectToSave));
			final Set<String> modifiedProperties = new HashSet<>(context.getDirtyAttributes());
			final Map<Locale, Set<String>> dirtyLocalizedAttributes = context.getDirtyLocalizedAttributes();
			if (MapUtils.isNotEmpty(dirtyLocalizedAttributes))
			{
				dirtyLocalizedAttributes.entrySet().forEach(e -> modifiedProperties.addAll(e.getValue()));
			}
			final DataType dataType = loadDataType((AbstractItemModel) objectToSave);
			if (dataType != null)
			{
				return modifiedProperties.stream().filter(attr -> dataType.getAttribute(attr) != null)
						.filter(attr -> !hasDefaultValueSet(dataType, attr, context)).collect(Collectors.toSet());
			}
		}
		else
		{
			LOG.warn("Cannot check modified properties for non AbstractItemModel object: {}", objectToSave);
		}
		return super.getModifiedProperties(objectToSave, ctx);
	}

	/**
	 * Returns true if newly created instance has property set with default value. This means that such property was not
	 * changed by the user, but it was set automatically.
	 *
	 * @param dataType
	 *           type to check
	 * @param attribute
	 *           name of attribute
	 * @param context
	 *           with additional information about the instance
	 * @return true if default value is set, false otherwise
	 */
	protected boolean hasDefaultValueSet(final DataType dataType, final String attribute, final ItemModelContext context)
	{
		if (context.isNew() && context instanceof ItemModelContextImpl)
		{
			final Object value = ((ItemModelContextImpl) context).getPropertyValue(attribute);
			return Objects.equals(value, dataType.getAttribute(attribute).getDefaultValue()) && value != null;
		}
		return false;
	}

	protected DataType loadDataType(final AbstractItemModel objectToSave)
	{
		final String typeCode = typeFacade.getType(objectToSave);
		try
		{
			return typeFacade.load(typeCode);
		}
		catch (final TypeNotFoundException e)
		{
			final String message = String.format("Cannot find type %s.", typeCode);
			if (LOG.isDebugEnabled())
			{
				LOG.error(message, e);
			}
			else
			{
				LOG.error(message);
			}
		}
		return null;
	}
}
