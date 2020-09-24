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
package de.hybris.platform.datahubbackoffice.dataaccess.pool;

import com.hybris.cockpitng.dataaccess.context.Context;
import com.hybris.cockpitng.dataaccess.facades.type.DataAttribute;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacadeStrategy;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;
import com.hybris.datahub.dto.pool.PoolData;

import java.util.Locale;

import org.zkoss.util.resource.Labels;

public class PoolTypeFacadeStrategy implements TypeFacadeStrategy
{
	public static final String DATAHUB_POOL_TYPECODE = "Datahub_Pool";

	private static final String TYPE_ATTR = "type";
	private static final String POOL_ID_ATTR = "poolId";
	private static final String POOL_NAME_ATTR = "poolName";
	private static final String DELETABLE_ATTR = "deletable";
	private static final String POOL_COMPOSITION_STRATEGY_ATTR = "compositionStrategy";
	private static final String POOL_PUBLICATION_STRATEGY_ATTR = "publicationStrategy";

	@Override
	public boolean canHandle(final String code)
	{
		return DATAHUB_POOL_TYPECODE.equals(code) || PoolData.class.getName().equals(code);
	}

	@Override
	public DataType load(final String code) throws TypeNotFoundException
	{
		final DataType.Builder dataTypeBuilder = new DataType.Builder(DATAHUB_POOL_TYPECODE)
				.clazz(PoolData.class)
				.searchable(true)
				.label(Locale.ENGLISH, Labels.getLabel("datahub.pool.type"));

		final DataAttribute.Builder poolIdAttrBuilder = new DataAttribute.Builder(POOL_ID_ATTR);
		poolIdAttrBuilder.searchable(true).writable(false).valueType(DataType.LONG);

		final DataAttribute.Builder typeAttrBuilder = new DataAttribute.Builder(TYPE_ATTR);
		typeAttrBuilder.searchable(true).writable(false).valueType(DataType.STRING);

		final DataAttribute.Builder nameAttrBuilder = new DataAttribute.Builder(POOL_NAME_ATTR);
		nameAttrBuilder.searchable(true)
					   .writable(true)
					   .label(Locale.ENGLISH, Labels.getLabel("datahub.attribute.pool.poolName"))
					   .valueType(DataType.STRING);

		final DataAttribute.Builder deletableAttrBuilder = new DataAttribute.Builder(DELETABLE_ATTR);
		deletableAttrBuilder.searchable(true)
							.writable(true)
							.label(Locale.ENGLISH, Labels.getLabel("datahub.attribute.pool.deletable"))
							.valueType(DataType.BOOLEAN);

		final DataAttribute.Builder compStrategyAttrBuilder = new DataAttribute.Builder(POOL_COMPOSITION_STRATEGY_ATTR);
		compStrategyAttrBuilder.searchable(true)
							   .writable(false)
							   .valueType(DataType.STRING);

		final DataAttribute.Builder publStrategyAttrBuilder = new DataAttribute.Builder(POOL_PUBLICATION_STRATEGY_ATTR);
		publStrategyAttrBuilder.searchable(true)
							   .writable(false)
							   .valueType(DataType.STRING);

		dataTypeBuilder
				.attribute(poolIdAttrBuilder.build())
				.attribute(typeAttrBuilder.build())
				.attribute(nameAttrBuilder.build())
				.attribute(deletableAttrBuilder.build())
				.attribute(compStrategyAttrBuilder.build())
				.attribute(publStrategyAttrBuilder.build());

		return dataTypeBuilder.build();
	}

	@Override
	public DataType load(final String code, final Context context) throws TypeNotFoundException
	{
		return load(code);
	}

	@Override
	public String getType(final Object entity)
	{
		assert entity != null : "do not expect to be called with null";
		if (entity instanceof PoolData)
		{
			return DATAHUB_POOL_TYPECODE;
		}
		throw new IllegalArgumentException("Cannot derive type code for " + entity);
	}

	@Override
	public String getAttributeDescription(final String s, final String s2)
	{
		return null;
	}

	@Override
	public String getAttributeDescription(final String s, final String s2, final Locale locale)
	{
		return null;
	}
}
