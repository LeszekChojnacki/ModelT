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

import org.springframework.beans.factory.annotation.Required;

import com.hybris.cockpitng.dataaccess.context.Context;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacadeStrategy;
import com.hybris.cockpitng.dataaccess.facades.object.exceptions.ObjectSavingException;
import com.hybris.datahub.client.DataPoolClient;
import com.hybris.datahub.dto.pool.PoolData;

public class PoolObjectFacadeStrategy implements ObjectFacadeStrategy
{
	private static final String POOL_CREATION_ERROR = "datahub.pool.creation.error";
	private DataPoolClient poolClient;

	@Override
	public <T> boolean canHandle(final T objectOrCode)
	{
		return objectOrCode != null && objectOrCode instanceof PoolData
				|| PoolTypeFacadeStrategy.DATAHUB_POOL_TYPECODE.equals(objectOrCode);
	}

	@Override
	public PoolData load(final String uniqueId, final Context context)
	{
		throw new UnsupportedOperationException("Pool Details not supported");
	}

	@Override
	public <T> void delete(final T obj, final Context context)
	{
		throw new UnsupportedOperationException("Method not implemented yet");
	}

	@Override
	public PoolData create(final String uniqueId, final Context context)
	{
		return new PoolData();
	}

	@Override
	public <T> T save(final T obj, final Context context) throws ObjectSavingException
	{
		final PoolData spec = (PoolData) obj;
		final PoolData created = createPool(spec);
		return (T) created;
	}

	private PoolData createPool(final PoolData spec) throws ObjectSavingException
	{
		final PoolData created;
		try
		{
			created = poolClient.createDataPool(spec);
		}
		catch (final Exception e)
		{
			throw new ObjectSavingException(POOL_CREATION_ERROR, e.getMessage(), e);
		}
		if (created == null)
		{
			throw new ObjectSavingException(POOL_CREATION_ERROR, null);
		}
		return created;
	}

	@Override
	public <T> T reload(final T instance, final Context context)
	{
		return instance;
	}

	@Required
	public void setPoolClient(final DataPoolClient cl)
	{
		poolClient = cl;
	}
}
