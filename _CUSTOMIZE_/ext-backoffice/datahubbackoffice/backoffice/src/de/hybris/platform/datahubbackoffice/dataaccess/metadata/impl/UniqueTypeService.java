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

import de.hybris.platform.datahubbackoffice.dataaccess.metadata.DataHubTypeService;

import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;

import com.hybris.cockpitng.dataaccess.facades.type.DataAttribute;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.datahub.dto.item.ItemData;

/**
 * This service ensures uniqueness of the DataHub types within a backoffice application.
 */
public class UniqueTypeService implements DataHubTypeService
{
	private static final String DATAHUB_TYPECODE_PREFIX = "Datahub_";

	private final DataHubTypeService delegate;

	/**
	 * Default constructor.
	 * @param service The DataHubTypeService
	 */
	public UniqueTypeService(final DataHubTypeService service)
	{
		delegate = service;
	}

	@Override
	public boolean exists(final String code)
	{
		return delegate.exists(toDataHubTypeCode(code));
	}

	@Override
	public Collection<String> getAllTypeCodes()
	{
		return delegate.getAllTypeCodes()
					   .stream()
					   .map(UniqueTypeService::toGloballyUniqueTypeCode)
					   .collect(Collectors.toList());
	}

	@Override
	public DataType getType(final String code)
	{
		final DataType type = delegate.getType(toDataHubTypeCode(code));
		final DataType.Builder builder = new DataType.Builder(code).searchable(true)
																   .clazz(ItemData.class)
																   .label(Locale.ENGLISH, code);
		for (final DataAttribute attr : type.getAttributes())
		{
			builder.attribute(attr);
		}
		return builder.build();
	}

	@Override
	public String deriveTypeCode(final ItemData item)
	{
		return toGloballyUniqueTypeCode(delegate.deriveTypeCode(item));
	}

	private static String toGloballyUniqueTypeCode(final String code)
	{
		return DATAHUB_TYPECODE_PREFIX + code;
	}

	private static String toDataHubTypeCode(final String unique)
	{
		return unique.startsWith(DATAHUB_TYPECODE_PREFIX) ? unique.substring(DATAHUB_TYPECODE_PREFIX.length()) : unique;
	}
}
