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
package de.hybris.platform.datahubbackoffice.dataaccess.metadata.impl;

import de.hybris.platform.datahubbackoffice.ItemDataConstants;
import de.hybris.platform.datahubbackoffice.dataaccess.metadata.DataHubTypeService;
import de.hybris.platform.datahubbackoffice.service.datahub.DataHubServer;
import de.hybris.platform.datahubbackoffice.service.datahub.DataHubServerContextService;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Lists;
import com.hybris.cockpitng.dataaccess.facades.type.DataAttribute;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.datahub.client.CanonicalItemClassClient;
import com.hybris.datahub.dto.item.ItemData;
import com.hybris.datahub.dto.metadata.CanonicalAttributeData;
import com.hybris.datahub.dto.metadata.CanonicalItemTypeData;
import com.hybris.datahub.dto.pool.PoolData;

/**
 * A simple implementation of the type service, which retrieves all data type related information from a DataHub via a
 * REST API.
 */
public class CanonicalTypeService implements DataHubTypeService
{
	private static final DataType POOL_TYPE = new DataType.Builder("Datahub_Pool").clazz(PoolData.class).build();

	private CanonicalItemClassClient client;

	private DataHubServerContextService dataHubServerContextService;

	@Override
	public boolean exists(final String code)
	{
		return getAllTypeCodes().contains(code);
	}

	@Override
	public Collection<String> getAllTypeCodes()
	{
		final List<String> typeCodes = Lists.newArrayList();
		final DataHubServer dataHubServer = dataHubServerContextService.getContextDataHubServer();
		if (dataHubServer != null)
		{
			typeCodes.addAll(client.getItemTypes()
								   .stream()
								   .map(CanonicalItemTypeData::getName)
								   .collect(Collectors.toList()));
		}
		return typeCodes;
	}

	@Override
	public DataType getType(final String code)
	{
		final DataType.Builder typeBuilder = typeBuilder(code);
		typeBuilder.attribute(plainAttribute(ItemDataConstants.ID, DataType.LONG));
		typeBuilder.attribute(plainAttribute(ItemDataConstants.INTEGRATION_KEY, DataType.STRING));
		typeBuilder.attribute(searchableAttribute("pool", POOL_TYPE));
		typeBuilder.attribute(searchableAttribute("status", DataType.STRING));
		addSchemalessAttributes(typeBuilder, code);
		return typeBuilder.build();
	}

	@Override
	public String deriveTypeCode(final ItemData item)
	{
		return item.getType();
	}

	private void addSchemalessAttributes(final DataType.Builder builder, final String type)
	{
		for (final CanonicalAttributeData attributeData : client.getAttributes(type))
		{
			builder.attribute(plainAttribute(attributeData.getName(), DataType.STRING,
					BooleanUtils.isTrue(attributeData.getIsLocalizable())));
		}
	}

	private static DataAttribute plainAttribute(final String attrName, final DataType type)
	{
		return plainAttribute(attrName, type, false);
	}

	private static DataAttribute plainAttribute(final String attrName, final DataType type, final boolean localized)
	{
		return attributeBuilder(attrName, type).writable(false).searchable(false).localized(localized).build();
	}

	private static DataAttribute searchableAttribute(final String attrName, final DataType type)
	{
		return attributeBuilder(attrName, type).writable(false).searchable(false).build();
	}

	private static DataAttribute.Builder attributeBuilder(final String attrName, final DataType type)
	{
		return new DataAttribute.Builder(attrName).label(Locale.ENGLISH, attrName).valueType(type);
	}

	private static DataType.Builder typeBuilder(final String type)
	{
		return new DataType.Builder(type).searchable(true).clazz(ItemData.class).label(Locale.ENGLISH, type);
	}

	public void setDataHubServerContextService(final DataHubServerContextService dataHubServerContextService)
	{
		this.dataHubServerContextService = dataHubServerContextService;
	}

	/**
	 * Injects a REST client for this service.
	 *
	 * @param cl a client to use with this service.
	 */
	@Required
	public void setClient(final CanonicalItemClassClient cl)
	{
		client = cl;
	}
}
