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
package de.hybris.platform.datahubbackoffice.dataaccess.targetsystem;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.zkoss.util.resource.Labels;

import com.hybris.cockpitng.dataaccess.context.Context;
import com.hybris.cockpitng.dataaccess.facades.type.DataAttribute;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacadeStrategy;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;
import com.hybris.datahub.dto.metadata.TargetSystemData;

public class TargetSystemTypeFacadeStrategy implements TypeFacadeStrategy
{
	private static final String DATAHUB_TARGETSYSTEM_TYPECODE = "Datahub_TargetSystem";
	private static final String TARGET_SYSTEM_NAME_ATTR = "targetSystemName";

	@Override
	public boolean canHandle(final String typeCode)
	{
		return StringUtils.equals(typeCode, DATAHUB_TARGETSYSTEM_TYPECODE)
				|| StringUtils.equals(typeCode, TargetSystemData.class.getName());
	}

	@Override
	public DataType load(final String qualifier) throws TypeNotFoundException
	{
		final DataType.Builder dataTypeBuilder = new DataType.Builder(DATAHUB_TARGETSYSTEM_TYPECODE);

		dataTypeBuilder.clazz(TargetSystemData.class).searchable(true).label(Locale.ENGLISH, "Target System");

		final DataAttribute.Builder nameAttrBuilder = new DataAttribute.Builder(TARGET_SYSTEM_NAME_ATTR);
		nameAttrBuilder.searchable(true).writable(true).label(Locale.ENGLISH, Labels.getLabel("datahub.attribute.targetSystem"))
				.valueType(DataType.STRING);

		return dataTypeBuilder.attribute(nameAttrBuilder.build()).build();
	}

	@Override
	public DataType load(final String qualifier, final Context ctx) throws TypeNotFoundException
	{
		return load(qualifier);
	}

	@Override
	public String getType(final Object object)
	{
		String ret = StringUtils.EMPTY;
		if (object instanceof TargetSystemData)
		{
			ret = DATAHUB_TARGETSYSTEM_TYPECODE;
		}

		return ret;
	}

	@Override
	public String getAttributeDescription(final String type, final String attribute)
	{
		return null;
	}

	@Override
	public String getAttributeDescription(final String type, final String attribute, final Locale locale)
	{
		return null;
	}
}
