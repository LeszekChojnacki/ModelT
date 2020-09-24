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

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import com.hybris.cockpitng.dataaccess.context.Context;
import com.hybris.cockpitng.dataaccess.facades.type.DataAttribute;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.DataType.Type;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacadeStrategy;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;
import com.hybris.datahub.dto.metadata.CanonicalAttributeData;

public class CanonicalAttributeDefinitionTypeFacadeStrategy implements TypeFacadeStrategy
{
	private static final String DATAHUB_CANONICAL_ATTRIBUTE_DEFINITION_TYPECODE = "Datahub_AttributeDefinition";
	private static final String NAME_ATTR = "name";
	private static final String ITEM_TYPE_ATTR = "itemType";
	private static final String IS_SECURED_ATTR = "isSecured";

	private static final DataType STRING_DATA_TYPE = new DataType.Builder("java.lang.String").clazz(String.class)
																							 .type(Type.ATOMIC)
																							 .build();
	private static final DataType BOOLEAN_DATA_TYPE = new DataType.Builder("java.lang.Boolean").clazz(Boolean.class)
																							   .type(Type.ATOMIC)
																							   .build();

	@Override
	public boolean canHandle(final String code)
	{
		return StringUtils.equals(code, DATAHUB_CANONICAL_ATTRIBUTE_DEFINITION_TYPECODE)
				|| StringUtils.equals(code, CanonicalAttributeData.class.getName());
	}

	@Override
	public DataType load(final String typeCode) throws TypeNotFoundException
	{
		final DataType.Builder dataTypeBuilder = new DataType.Builder(DATAHUB_CANONICAL_ATTRIBUTE_DEFINITION_TYPECODE);

		dataTypeBuilder.clazz(CanonicalAttributeData.class)
					   .searchable(true)
					   .label(Locale.ENGLISH, "Canonical Attribute Definition");

		final DataAttribute.Builder nameAttrBuilder = new DataAttribute.Builder(NAME_ATTR);

		nameAttrBuilder.label(Locale.ENGLISH, NAME_ATTR)
					   .valueType(STRING_DATA_TYPE)
					   .searchable(true)
					   .writable(false);

		final DataAttribute.Builder itemTypeBuilder = new DataAttribute.Builder(ITEM_TYPE_ATTR);

		itemTypeBuilder.label(Locale.ENGLISH, ITEM_TYPE_ATTR)
					   .valueType(STRING_DATA_TYPE)
					   .searchable(true).writable(false);

		final DataAttribute.Builder isSecuredAttrBuilder = new DataAttribute.Builder(IS_SECURED_ATTR);

		isSecuredAttrBuilder.label(Locale.ENGLISH, IS_SECURED_ATTR)
							.valueType(BOOLEAN_DATA_TYPE)
							.searchable(true)
							.writable(false);

		dataTypeBuilder.attribute(nameAttrBuilder.build())
					   .attribute(itemTypeBuilder.build())
					   .attribute(isSecuredAttrBuilder.build());

		return dataTypeBuilder.build();
	}

	@Override
	public DataType load(final String typeCode, final Context context) throws TypeNotFoundException
	{
		return load(typeCode);
	}

	@Override
	public String getType(final Object entity)
	{
		String ret = StringUtils.EMPTY;
		if (entity instanceof CanonicalAttributeData)
		{
			ret = DATAHUB_CANONICAL_ATTRIBUTE_DEFINITION_TYPECODE;
		}
		return ret;
	}

	@Override
	public String getAttributeDescription(final String s, final String qualifier)
	{
		return qualifier;
	}

	@Override
	public String getAttributeDescription(final String s, final String qualifier, final Locale locale)
	{
		return qualifier;
	}
}
