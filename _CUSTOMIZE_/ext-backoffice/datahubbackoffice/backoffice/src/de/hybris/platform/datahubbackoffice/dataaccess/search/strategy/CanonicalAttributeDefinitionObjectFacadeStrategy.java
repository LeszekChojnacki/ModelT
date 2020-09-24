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

import org.apache.commons.lang3.StringUtils;

import com.hybris.cockpitng.dataaccess.context.Context;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacadeStrategy;
import com.hybris.datahub.dto.metadata.CanonicalAttributeData;

public class CanonicalAttributeDefinitionObjectFacadeStrategy implements ObjectFacadeStrategy
{
	private static final String DATAHUB_CANONICAL_ATTRIBUTE_DEFINITION_TYPECODE = "Datahub_AttributeDefinition";

	@Override
	public <T> boolean canHandle(final T objectOrCode)
	{
		return (objectOrCode instanceof String)
				&& StringUtils.equals((String) objectOrCode, DATAHUB_CANONICAL_ATTRIBUTE_DEFINITION_TYPECODE)
				|| objectOrCode instanceof CanonicalAttributeData;
	}

	@Override
	public <T> T load(final String s, final Context context)
	{
		return null;
	}

	@Override
	public <T> void delete(final T t, final Context context)
	{
		throw new UnsupportedOperationException("Not implemented yet!");
	}

	@Override
	public <T> T create(final String s, final Context context)
	{
		return null;
	}

	@Override
	public <T> T save(final T t, final Context context)
	{
		return null;
	}

	@Override
	public <T> T reload(final T t, final Context context)
	{
		return t;
	}
}
