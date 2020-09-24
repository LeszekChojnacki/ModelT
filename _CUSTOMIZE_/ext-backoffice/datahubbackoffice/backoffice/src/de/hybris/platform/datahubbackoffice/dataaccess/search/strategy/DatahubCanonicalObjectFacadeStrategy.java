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

import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.cockpitng.dataaccess.context.Context;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacadeStrategy;
import com.hybris.cockpitng.dataaccess.facades.object.exceptions.ObjectNotFoundException;
import com.hybris.datahub.client.DataHubClientException;
import com.hybris.datahub.client.DataItemClient;
import com.hybris.datahub.dto.item.ItemData;

public class DatahubCanonicalObjectFacadeStrategy implements ObjectFacadeStrategy
{
	private DataItemClient dataItemClient;

	@Override
	public boolean canHandle(final Object objectOrCOde)
	{
		return (objectOrCOde instanceof String) && StringUtils.contains((String) objectOrCOde, "Canonical")
				|| objectOrCOde instanceof ItemData;
	}

	@Override
	public ItemData load(final String uniqueId, final Context context) throws ObjectNotFoundException
	{

		return load(Long.valueOf(uniqueId));
	}

	public ItemData load(final Long itemId) throws ObjectNotFoundException
	{
		return loadItemById(itemId);
	}

	@Override
	public void delete(final Object t, final Context context)
	{
		final ItemData instance = (ItemData) t;

		dataItemClient.deleteItem("GLOBAL", instance.getType(),
				Collections.singletonMap("identifier", (String) instance.getAttributeValue("identifier")));
	}

	@Override
	public ItemData create(final String uniqueId, final Context context)
	{

		return null;
	}

	@Override
	public Object save(final Object instance, final Context context)
	{
		return null;
	}

	@Override
	public Object reload(final Object instance, final Context context) throws ObjectNotFoundException
	{
		return instance != null ? loadItemById(((ItemData) instance).getId()) : null;
	}

	private ItemData loadItemById(final Long itemId) throws ObjectNotFoundException
	{
		try
		{
			return dataItemClient.getCanonicalItem(itemId);
		}
		catch (final DataHubClientException e)
		{
			throw new ObjectNotFoundException(e.getMessage(), e);
		}
	}

	@Required
	public void setDataItemClient(final DataItemClient client)
	{
		dataItemClient = client;
	}
}
