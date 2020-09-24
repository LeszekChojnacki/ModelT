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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Sets;
import com.hybris.cockpitng.core.config.ConfigContext;
import com.hybris.cockpitng.core.config.impl.DefaultConfigContext;
import com.hybris.cockpitng.core.config.impl.DefaultListViewConfigFallbackStrategy;
import com.hybris.cockpitng.core.config.impl.jaxb.listview.ListColumn;
import com.hybris.cockpitng.core.config.impl.jaxb.listview.ListView;


public class DefaultBackofficeListViewConfigFallbackStrategy extends DefaultListViewConfigFallbackStrategy
{

	private static final Logger LOG = LoggerFactory.getLogger(DefaultBackofficeListViewConfigFallbackStrategy.class);

	private TypeService typeService;

	@Override
	public ListView loadFallbackConfiguration(final ConfigContext context, final Class<ListView> configurationType)
	{
		final ListView configuration = super.loadFallbackConfiguration(context, configurationType);
		final String typeCode = context.getAttribute(DefaultConfigContext.CONTEXT_TYPE);
		if (StringUtils.isNotBlank(typeCode))
		{
			try
			{
				final TypeModel typeForCode = getTypeService().getTypeForCode(typeCode);
				if (typeForCode instanceof ViewTypeModel)
				{
					final Collection<String> qualifiers = Sets.newTreeSet();
					qualifiers.addAll(((ViewTypeModel) typeForCode).getColumns().stream()
							.map(ViewAttributeDescriptorModel::getQualifier).collect(Collectors.toList()));
					if (!qualifiers.isEmpty())
					{
						final Iterator<ListColumn> it = configuration.getColumn().iterator();
						while (it.hasNext())
						{
							final ListColumn next = it.next();
							if (!qualifiers.contains(next.getQualifier()))
							{
								it.remove();
							}
						}
					}
				}
			}
			catch (final UnknownIdentifierException uie)
			{
				LOG.debug(uie.getMessage(), uie);
			}
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
