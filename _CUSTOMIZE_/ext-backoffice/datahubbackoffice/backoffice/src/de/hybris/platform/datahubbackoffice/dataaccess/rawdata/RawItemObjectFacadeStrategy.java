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

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import com.hybris.cockpitng.dataaccess.context.Context;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacadeStrategy;
import com.hybris.datahub.dto.item.ItemData;

public class RawItemObjectFacadeStrategy implements ObjectFacadeStrategy
{
	private static final String DATAHUB_RAWITEM = "Datahub_RawItem";

	@Override
	public <T> boolean canHandle(final T objectOrCode)
	{
		return StringUtils.equals(ObjectUtils.toString(objectOrCode), DATAHUB_RAWITEM)
				|| (objectOrCode instanceof ItemData) && ((ItemData) objectOrCode).getType().contains("Raw");
	}

	@Override
	public <T> T load(final String s, final Context context)
	{
		return null;
	}

	@Override
	public <T> void delete(final T t, final Context context)
	{
		// not needed
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
