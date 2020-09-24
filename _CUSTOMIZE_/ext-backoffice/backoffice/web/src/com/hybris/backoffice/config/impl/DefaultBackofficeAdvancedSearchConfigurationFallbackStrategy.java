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

import de.hybris.platform.core.model.type.TypeModel;
import de.hybris.platform.core.model.type.ViewAttributeDescriptorModel;
import de.hybris.platform.core.model.type.ViewTypeModel;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.type.TypeService;

import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Sets;
import com.hybris.cockpitng.core.config.ConfigContext;
import com.hybris.cockpitng.core.config.impl.DefaultAdvancedSearchConfigurationFallbackStrategy;
import com.hybris.cockpitng.core.config.impl.DefaultConfigContext;
import com.hybris.cockpitng.core.config.impl.jaxb.hybris.advancedsearch.AdvancedSearch;
import com.hybris.cockpitng.core.config.impl.jaxb.hybris.advancedsearch.FieldType;


public class DefaultBackofficeAdvancedSearchConfigurationFallbackStrategy
		extends DefaultAdvancedSearchConfigurationFallbackStrategy
{

	private static final Logger LOG = LoggerFactory.getLogger(DefaultBackofficeAdvancedSearchConfigurationFallbackStrategy.class);

	private TypeService typeService;

	@Override
	public AdvancedSearch loadFallbackConfiguration(final ConfigContext context, final Class<AdvancedSearch> configurationType)
	{
		final AdvancedSearch configuration = super.loadFallbackConfiguration(context, configurationType);
		final String typeCode = context.getAttribute(DefaultConfigContext.CONTEXT_TYPE);
		try
		{
			final TypeModel typeForCode = getTypeService().getTypeForCode(typeCode);
			if (typeForCode instanceof ViewTypeModel)
			{
				if (!configuration.getFieldList().getField().isEmpty())
				{
					configuration.setDisableAutoSearch(Boolean.TRUE);
					configuration.setDisableSimpleSearch(Boolean.TRUE);
				}

				configuration.getFieldList().setDisableAttributesComparator(Boolean.TRUE);
				configuration.getFieldList().setIncludeSubtypes(Boolean.FALSE);
				configuration.getFieldList().setIncludeSubtypes(Boolean.FALSE);

				final Collection<String> qualifiers = Sets.newTreeSet();
				qualifiers.addAll(((ViewTypeModel) typeForCode).getColumns().stream().map(ViewAttributeDescriptorModel::getQualifier)
						.collect(Collectors.toList()));
				if (!qualifiers.isEmpty())
				{
					final Iterator<FieldType> it = configuration.getFieldList().getField().iterator();
					while (it.hasNext())
					{
						final FieldType next = it.next();
						if (qualifiers.contains(next.getName()))
						{
							it.remove();
						}
						else
						{
							next.setMandatory(Boolean.TRUE);
						}
					}
				}
			}
		}
		catch (final UnknownIdentifierException uie)
		{
			LOG.debug(uie.getMessage(), uie);
		}
		return configuration;
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
