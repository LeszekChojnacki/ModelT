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
package de.hybris.platform.datahubbackoffice.dataaccess.pool.history;

import com.hybris.cockpitng.dataaccess.context.Context;
import com.hybris.cockpitng.dataaccess.facades.type.DataAttribute;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacadeStrategy;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;
import com.hybris.datahub.dto.event.PoolActionData;
import com.hybris.datahub.dto.pool.PoolData;

import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;

public class PoolHistoryTypeFacadeStrategy implements TypeFacadeStrategy
{
	private static final String DATAHUB_POOLHISTORY_TYPECODE = "Datahub_DataHubPoolAction";

	private static final String ACTION_ID_ATTR = "actionId";
	private static final String TYPE_ATTR = "type";
	private static final String START_TIME_ATTR = "startTime";

	private static final DataType stringDataType = new DataType.Builder("java.lang.String").clazz(String.class).build();
	private static final DataType longDataType = new DataType.Builder("java.lang.Long").clazz(Long.class).build();
	private static final DataType dataDataType = new DataType.Builder("java.util.Date").clazz(Date.class).build();

	@Override
	public boolean canHandle(final String code)
	{
		return StringUtils.equals(code, DATAHUB_POOLHISTORY_TYPECODE)
				|| StringUtils.equals(code, PoolActionData.class.getName());
	}

	@Override
	public DataType load(final String typeCode) throws TypeNotFoundException
	{

		final DataType.Builder dataTypeBuilder = new DataType.Builder(DATAHUB_POOLHISTORY_TYPECODE);

		dataTypeBuilder.clazz(PoolActionData.class).searchable(true).
				label(Locale.ENGLISH, "PoolHistory");


		final DataAttribute.Builder actionIdAttrBuilder = new DataAttribute.Builder(ACTION_ID_ATTR);

		actionIdAttrBuilder.label(Locale.ENGLISH, ACTION_ID_ATTR)
						   .searchable(true)
						   .writable(false)
						   .valueType(longDataType);


		final DataAttribute.Builder typeAttrBuilder = new DataAttribute.Builder(TYPE_ATTR);

		typeAttrBuilder.label(Locale.ENGLISH, TYPE_ATTR)
					   .searchable(true)
					   .writable(false)
					   .valueType(stringDataType);

		final DataAttribute.Builder startTimeAttrBuilder = new DataAttribute.Builder(START_TIME_ATTR);

		startTimeAttrBuilder.label(Locale.ENGLISH, START_TIME_ATTR)
							.searchable(true)
							.writable(false)
							.valueType(dataDataType);

		final DataType.Builder poolType = new DataType.Builder("Datahub_Pool").clazz(PoolData.class);
		final DataAttribute.Builder attributePool = new DataAttribute.Builder("pool");
		attributePool.label(Locale.ENGLISH, "Pool")
					 .writable(false)
					 .searchable(true)
					 .valueType(poolType.build());

		dataTypeBuilder.attribute(attributePool.build());

		dataTypeBuilder.attribute(actionIdAttrBuilder.build()).
				attribute(typeAttrBuilder.build()).attribute(startTimeAttrBuilder.build());

		return dataTypeBuilder.build();
	}

	@Override
	public DataType load(final String code, Context context) throws TypeNotFoundException
	{
		return load(code);
	}

	@Override
	public String getType(final Object entity)
	{

		String ret = StringUtils.EMPTY;
		if (entity instanceof PoolActionData)
		{
			ret = DATAHUB_POOLHISTORY_TYPECODE;
		}

		return ret;
	}

	@Override
	public String getAttributeDescription(String typeCode, String qualifier)
	{
		return qualifier;
	}

	@Override
	public String getAttributeDescription(String typeCode, String qualifier, Locale locale)
	{
		return qualifier;
	}
}
