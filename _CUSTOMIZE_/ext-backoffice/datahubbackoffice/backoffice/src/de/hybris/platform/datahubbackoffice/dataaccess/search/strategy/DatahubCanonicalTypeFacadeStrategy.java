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

package de.hybris.platform.datahubbackoffice.dataaccess.search.strategy;

import de.hybris.platform.datahubbackoffice.dataaccess.metadata.DataHubTypeService;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.cockpitng.dataaccess.context.Context;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacadeStrategy;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;
import com.hybris.datahub.dto.item.ItemData;

public class DatahubCanonicalTypeFacadeStrategy implements TypeFacadeStrategy
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DatahubCanonicalTypeFacadeStrategy.class);

	private DataHubTypeService canonicalTypes;

	@Override
	public boolean canHandle(final String code)
	{
		boolean isExist = false;
		try
		{
			isExist = canonicalTypes.exists(code);
		}
		catch (final Exception e)
		{
			LOGGER.trace(e.getMessage(), e);
		}
		return isExist || StringUtils.equalsIgnoreCase(code, ItemData.class.getName());
	}

	@Override
	public DataType load(final String code) throws TypeNotFoundException
	{
		return load(code, null);
	}

	@Override
	public DataType load(final String typeCode, final Context context) throws TypeNotFoundException
	{
		return canonicalTypes.getType(typeCode);
	}

	@Override
	public String getType(final Object obj)
	{
		return obj instanceof ItemData ? canonicalTypes.deriveTypeCode((ItemData) obj) : "";
	}

	@Override
	public String getAttributeDescription(final String typeCode, final String qualifier)
	{
		return qualifier;
	}

	@Override
	public String getAttributeDescription(final String typeCode, final String qualifier, final Locale locale)
	{
		return qualifier;
	}

	@Required
	public void setCanonicalTypesService(final DataHubTypeService service)
	{
		canonicalTypes = service;
	}
}
