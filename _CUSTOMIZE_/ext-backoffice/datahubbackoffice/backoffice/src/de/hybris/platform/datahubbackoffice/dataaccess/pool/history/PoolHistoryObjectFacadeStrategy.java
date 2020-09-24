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

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import com.hybris.cockpitng.dataaccess.context.Context;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacadeStrategy;
import com.hybris.datahub.dto.event.PoolActionData;

public class PoolHistoryObjectFacadeStrategy implements ObjectFacadeStrategy
{
	private static final String DATAHUB_POOL_HISTORY_TYPECODE = "Datahub_DataHubPoolAction";

	@Override
	public <T> boolean canHandle(final T objectOrCode)
	{
		return StringUtils.equals(ObjectUtils.toString(objectOrCode), DATAHUB_POOL_HISTORY_TYPECODE)
				|| objectOrCode instanceof PoolActionData;
	}

	@Override
	public PoolActionData load(final String uniqueId, final Context context)
	{
		return null;
	}

	@Override
	public <T> void delete(final T instance, final Context context)
	{
		throw new UnsupportedOperationException("Not implemented yet!");
	}

	@Override
	public PoolActionData create(final String uniqueId, final Context context)
	{
		return null;
	}

	@Override
	public <T> T save(final T instance, final Context context)
	{
		return null;
	}

	@Override
	public <T> T reload(final T instance, final Context context)
	{
		return instance;
	}
}
