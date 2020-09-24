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
package de.hybris.platform.datahubbackoffice.dataaccess.publication.error;

import de.hybris.platform.datahubbackoffice.dataaccess.publication.PublicationConstants;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;

import com.hybris.cockpitng.dataaccess.context.Context;
import com.hybris.cockpitng.dataaccess.facades.type.DataAttribute;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacadeStrategy;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;
import com.hybris.datahub.dto.item.ErrorData;
import com.hybris.datahub.dto.publication.CanonicalItemPublicationStatusData;

public class PublicationErrorTypeFacadeStrategy implements TypeFacadeStrategy
{
	private static final String TYPE_CODE = PublicationConstants.PUBLICATION_ERROR_TYPE_CODE;

	@Override
	public boolean canHandle(final String typeCode)
	{
		return StringUtils.equals(typeCode, TYPE_CODE) || StringUtils.equals(typeCode, ErrorData.class.getName());
	}

	@Override
	public DataType load(final String qualifier) throws TypeNotFoundException
	{
		final DataType.Builder dataTypeBuilder = new DataType.Builder((TYPE_CODE));

		dataTypeBuilder.clazz(CanonicalItemPublicationStatusData.class).searchable(true).label(Locale.ENGLISH,
				"Publication Errors");

		final DataAttribute.Builder integrationKeyAttr = new DataAttribute.Builder("integrationKey");
		integrationKeyAttr.label(Locale.ENGLISH, "integrationKey")
						  .searchable(false)
						  .writable(false)
						  .valueType(DataType.STRING);

		final DataAttribute.Builder typeAttr = new DataAttribute.Builder("type");
		typeAttr.label(Locale.ENGLISH, "type")
				.searchable(false)
				.writable(false)
				.valueType(DataType.STRING);

		final DataAttribute.Builder canonicalItemIdAttr = new DataAttribute.Builder("canonicalItemId");
		canonicalItemIdAttr.label(Locale.ENGLISH, "canonicalItemId")
						   .searchable(false)
						   .writable(false)
						   .valueType(DataType.LONG);

		final DataAttribute.Builder errorCodeAttr = new DataAttribute.Builder("code");
		errorCodeAttr.label(Locale.ENGLISH, "code")
					 .searchable(false)
					 .writable(false)
					 .valueType(DataType.STRING);

		final DataAttribute.Builder errorMessageAttr = new DataAttribute.Builder("message");
		errorMessageAttr.label(Locale.ENGLISH, "message")
						.searchable(false)
						.writable(false)
						.valueType(DataType.STRING);

		dataTypeBuilder.attribute(integrationKeyAttr.build())
					   .attribute(typeAttr.build())
					   .attribute(canonicalItemIdAttr.build())
					   .attribute(errorCodeAttr.build())
					   .attribute(errorMessageAttr.build());

		return dataTypeBuilder.build();
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
		if (object instanceof ErrorData)
		{
			ret = TYPE_CODE;
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
