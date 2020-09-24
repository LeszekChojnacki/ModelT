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
package de.hybris.platform.adaptivesearch.strategies.impl;

import de.hybris.platform.adaptivesearch.model.AbstractAsConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchProfileModel;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.core.model.type.CollectionTypeModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.core.model.type.TypeModel;
import de.hybris.platform.servicelayer.model.visitor.ItemVisitor;
import de.hybris.platform.servicelayer.type.TypeService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Required;


public class DefaultAsItemVisitor implements ItemVisitor<ItemModel>
{
	private TypeService typeService;

	@Override
	public List<ItemModel> visit(final ItemModel source, final List<ItemModel> path, final Map<String, Object> context)
	{
		final List<ItemModel> items = new ArrayList();

		final ComposedTypeModel composedType = typeService.getComposedTypeForCode(source.getItemtype());
		final Set<AttributeDescriptorModel> attributeDescriptors = typeService.getAttributeDescriptorsForType(composedType);

		for (final AttributeDescriptorModel attributeDescriptor : attributeDescriptors)
		{
			final TypeModel attributeType = attributeDescriptor.getAttributeType();

			if (isValidType(attributeType))
			{
				final Object value = source.getProperty(attributeDescriptor.getQualifier());

				if (value instanceof Collection)
				{
					items.addAll((Collection<ItemModel>) value);
				}
				else if (value != null)
				{
					items.add((ItemModel) value);
				}
			}
		}

		return items;
	}

	protected boolean isValidType(final TypeModel type)
	{
		if (type instanceof ComposedTypeModel)
		{
			return isValidTypeCode(((ComposedTypeModel) type).getCode());
		}
		else if (type instanceof CollectionTypeModel)
		{
			return isValidTypeCode(((CollectionTypeModel) type).getElementType().getCode());
		}

		return false;
	}

	protected boolean isValidTypeCode(final String typeCode)
	{
		return typeService.isAssignableFrom(AbstractAsSearchProfileModel._TYPECODE, typeCode)
				|| typeService.isAssignableFrom(AbstractAsConfigurationModel._TYPECODE, typeCode);
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
