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
package com.hybris.backoffice.config.impl;

import de.hybris.platform.core.model.ItemModel;

import java.util.Set;
import java.util.stream.Collectors;

import com.hybris.cockpitng.config.jaxb.wizard.Flow;
import com.hybris.cockpitng.config.jaxb.wizard.PropertyListType;
import com.hybris.cockpitng.config.jaxb.wizard.PropertyType;
import com.hybris.cockpitng.core.config.impl.adapters.flow.ConfigurableFlowConfigurationProcessor;
import com.hybris.cockpitng.dataaccess.facades.type.DataAttribute;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;


public class DefaultPlatformConfigurableFlowConfigurationProcessor extends ConfigurableFlowConfigurationProcessor
{

	@Override
	protected Set<PropertyType> retrieveMissingProperties(final Flow flowConfiguration, final String typeCode,
			final PropertyListType propertyListType)
	{
		final Set<PropertyType> missingProperties = super.retrieveMissingProperties(flowConfiguration, typeCode, propertyListType);
		return filterFields(typeCode, missingProperties);
	}

	protected Set<PropertyType> filterFields(final String typeCode, final Set<PropertyType> missingProperties)
	{
		final DataType type = loadDataType(typeCode);
		return missingProperties.stream().filter(prop -> {
			final String qualifier = prop.getQualifier();
			final DataAttribute attribute = type.getAttribute(qualifier);
			return attribute == null
					|| !(ItemModel.OWNER.equals(qualifier) && ItemModel._TYPECODE.equals(attribute.getValueType().getCode()));
		}).collect(Collectors.toSet());
	}

	protected DataType loadDataType(final String typeCode)
	{
		final DataType type;
		try
		{
			type = getTypeFacade().load(typeCode);
		}
		catch (final TypeNotFoundException e)
		{
			throw new IllegalStateException(e);
		}
		return type;
	}
}
