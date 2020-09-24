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

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import com.hybris.cockpitng.dataaccess.context.Context;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacadeStrategy;
import com.hybris.datahub.dto.metadata.TargetSystemData;


public class TargetSystemObjectFacadeStrategy implements ObjectFacadeStrategy
{
	@Override
	public <T> boolean canHandle(final T object)
	{
		return StringUtils.equals(ObjectUtils.toString(object), "Datahub_TargetSystem") || object instanceof TargetSystemData;
	}

	@Override
	public <T> T load(final String id, final Context ctx)
	{
		return null;
	}

	@Override
	public <T> void delete(final T object, final Context ctx)
	{
		// not implemented
	}

	@Override
	public <T> T create(final String typeId, final Context ctx)
	{
		return null;
	}

	@Override
	public <T> T save(final T object, final Context ctx)
	{
		return null;
	}

	@Override
	public <T> T reload(final T object, final Context ctx)
	{
		return null;
	}
}
