/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.datahubbackoffice.service.cockpitng;

import de.hybris.platform.datahubbackoffice.ItemDataConstants;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;

import com.google.common.collect.Lists;
import com.hybris.cockpitng.dataaccess.facades.type.DataAttribute;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacade;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;
import com.hybris.datahub.dto.item.AttributeValueData;
import com.hybris.datahub.dto.item.ItemAttributeData;
import com.hybris.datahub.dto.item.ItemData;

public class ItemDataPropertyAccessor implements PropertyAccessor
{
	private static final Logger LOG = LoggerFactory.getLogger(ItemDataPropertyAccessor.class);

	private TypeFacade typeFacade;

	@Override
	public Class[] getSpecificTargetClasses()
	{
		return new Class[] {ItemData.class};
	}

	@Override
	public boolean canRead(final EvaluationContext context, final Object target, final String name)
	{
		return true;
	}

	@Override
	public TypedValue read(final EvaluationContext context, final Object target, final String name)
	{
		Object attributeValue;

		final DataType dataType = getDataType(target);
		final ItemData itemData = (ItemData) target;

		final ItemAttributeData attributeData = itemData.getItemAttributeData(name);
		if (attributeData != null)
		{
			attributeValue = getValueForAttributeData(name, dataType, attributeData);
		}
		else
		{
			if (ItemDataConstants.ID.equals(name))
			{
				attributeValue = itemData.getId();
			}
			else if (ItemDataConstants.INTEGRATION_KEY.equals(name))
			{
				attributeValue = itemData.getIntegrationKey();
			}
			else
			{
				throw new IllegalStateException(name + " is not a supported attribute");
			}
		}
		return new TypedValue(attributeValue);
	}

	private static Object getValueForAttributeData(final String name, final DataType dataType, final ItemAttributeData attributeData)
	{
		final DataAttribute attribute = dataType.getAttribute(name);
		if (CollectionUtils.isEmpty(attributeData.getAttributeValues()))
		{
			return null;
		}

		if (attribute.isLocalized())
		{
			final Map<Locale, Object> localizedAttrValue = new HashMap<>();
			for (final AttributeValueData valueData : attributeData.getAttributeValues())
			{
				if (valueData.getIsoCode() != null)
				{
					localizedAttrValue.put(new Locale(valueData.getIsoCode()), valueData.getValue().iterator().next());
				}
			}
			return localizedAttrValue;
		}
		else if (attribute.getAttributeType().equals(DataAttribute.AttributeType.SET))
		{
			return attributeData.getAttributeValues().iterator().next().getValue();
		}

		final Collection<?> value = attributeData.getAttributeValues().iterator().next().getValue();
		return value.isEmpty() ? null : value.iterator().next();
	}

	@Override
	public boolean canWrite(final EvaluationContext context, final Object target, final String name)
	{
		return true;
	}

	@Override
	public void write(final EvaluationContext context, final Object target, final String name, final Object newValue)
	{

		final ItemData itemData = (ItemData) target;
		final ItemAttributeData itemAttributeData = itemData.getItemAttributeData(name);

		if (itemAttributeData != null)
		{
			if (itemAttributeData.getAttributeValues() != null)
			{
				itemAttributeData.getAttributeValues().clear();
			}
			itemAttributeData.setAttributeValues(Lists.newArrayList(new AttributeValueData(Lists.newArrayList(newValue))));
		}
	}

	protected DataType getDataType(final Object target)
	{
		DataType ret = null;
		final String currentType = typeFacade.getType(target);

		try
		{
			ret = typeFacade.load(currentType);
		}
		catch (final TypeNotFoundException e)
		{
			LOG.warn(String.format("Type %s was not found!", currentType), e);
		}
		return ret;
	}

	public void setTypeFacade(final TypeFacade typeFacade)
	{
		this.typeFacade = typeFacade;
	}
}
