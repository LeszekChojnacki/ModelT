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
package de.hybris.platform.datahubbackoffice.dataaccess.rawdata;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;

import com.hybris.cockpitng.dataaccess.context.Context;
import com.hybris.cockpitng.dataaccess.facades.type.DataAttribute;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacadeStrategy;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;
import com.hybris.datahub.client.RawItemClassClient;
import com.hybris.datahub.dto.item.ItemData;
import com.hybris.datahub.dto.metadata.ItemTypeData;
import com.hybris.datahub.dto.metadata.RawAttributeData;
import com.hybris.datahub.dto.pool.PoolData;

public class RawItemTypeFacadeStrategy implements TypeFacadeStrategy
{
	private static final String DATAHUB_TYPECODE_PREFIX = "Datahub_";
	private static final String RAW_ITEM_TYPE_CODE = "Datahub_RawItem";

	private static final DataType STRING_DATA_TYPE = new DataType.Builder(String.class.getName()).clazz(String.class).build();
	private final RawItemClassClient client = new RawItemClassClient("");
	private final Map<String, DataType> typeCache = new ConcurrentHashMap<>();

	@Override
	public boolean canHandle(final String s)
	{
		return StringUtils.contains(s, "Raw") || StringUtils.equalsIgnoreCase(s, ItemData.class.getName());
	}

	@Override
	public DataType load(final String s) throws TypeNotFoundException
	{
		return load(s, null);
	}

	@Override
	public DataType load(final String s, final Context context) throws TypeNotFoundException
	{
		if (typeCache.containsKey(s))
		{
			return typeCache.get(s);
		}

		final DataType.Builder dataTypeBuilder = new DataType.Builder(RAW_ITEM_TYPE_CODE).label(Locale.ENGLISH, "Raw Items")
				.searchable(true);

		final List<ItemTypeData> types = client.getItemTypes();

		for (final ItemTypeData type : types)
		{
			final DataType.Builder subTypeBuilder = new DataType.Builder(
					String.format("%s%s", DATAHUB_TYPECODE_PREFIX, type.getName()));
			subTypeBuilder.searchable(true).clazz(ItemData.class).supertype(RAW_ITEM_TYPE_CODE).label(Locale.ENGLISH,
					type.getName());

			final Collection<RawAttributeData> attributes = client.getAttributes(type.getName());

			for (final Object item : attributes)
			{
				final Map<String, String> attributeMap = (Map<String, String>) item;
				final String attrName = attributeMap.get("name");

				final DataAttribute.Builder schemaLessAttribute = new DataAttribute.Builder(attrName);

				schemaLessAttribute.searchable(true).writable(false).description(Locale.ENGLISH, attributeMap.get("name"))
						.label(Locale.ENGLISH, attributeMap.get("name")).valueType(STRING_DATA_TYPE);

				subTypeBuilder.attribute(schemaLessAttribute.build());
			}

			final DataType.Builder poolType = new DataType.Builder("Datahub_Pool").clazz(PoolData.class);
			final DataAttribute.Builder attributePool = new DataAttribute.Builder("pool");
			attributePool.label(Locale.ENGLISH, "Pool").writable(false).searchable(true).valueType(poolType.build());

			subTypeBuilder.attribute(attributePool.build());

			final DataAttribute.Builder attributeStatus = new DataAttribute.Builder("status");
			attributeStatus.label(Locale.ENGLISH, "Status").writable(false).searchable(true).

					valueType(STRING_DATA_TYPE);
			subTypeBuilder.attribute(attributeStatus.build());

			final DataAttribute.Builder localeAttr = new DataAttribute.Builder("isoCode");
			localeAttr.label(Locale.ENGLISH, "isoCode").searchable(true).writable(false).valueType(STRING_DATA_TYPE);
			subTypeBuilder.attribute(localeAttr.build());

			final String datahubTypeCode = String.format("%s%s", DATAHUB_TYPECODE_PREFIX, type.getName());
			typeCache.put(datahubTypeCode, subTypeBuilder.build());
			dataTypeBuilder.subtype(datahubTypeCode);
		}

		typeCache.put(RAW_ITEM_TYPE_CODE, dataTypeBuilder.build());
		return typeCache.get(s);
	}

	@Override
	public String getType(final Object o)
	{
		String ret = StringUtils.EMPTY;
		if (o instanceof ItemData)
		{

			final ItemData itemData = (ItemData) o;
			ret = String.format("%s%s", DATAHUB_TYPECODE_PREFIX, itemData.getType());
		}

		return ret;
	}

	@Override
	public String getAttributeDescription(final String s, final String s2)
	{
		return s2;
	}

	@Override
	public String getAttributeDescription(final String s, final String s2, final Locale locale)
	{
		return s2;
	}
}
