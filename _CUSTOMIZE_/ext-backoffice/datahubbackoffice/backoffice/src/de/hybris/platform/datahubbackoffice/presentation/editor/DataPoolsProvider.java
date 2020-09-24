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
package de.hybris.platform.datahubbackoffice.presentation.editor;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.util.resource.Labels;

import com.google.common.collect.Lists;
import com.hybris.cockpitng.core.util.Validate;
import com.hybris.datahub.client.DataPoolClient;
import com.hybris.datahub.dto.pool.PoolData;

import de.hybris.platform.datahubbackoffice.exception.NoDataHubInstanceAvailableException;

public class DataPoolsProvider implements DropdownProvider
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DataPoolsProvider.class);

	private static final int MAX_NUMBER_OF_POOLS = 1000;
	private DataPoolClient client;

	@Override
	public List<String> getAllValues()
	{
		Validate.notNull("DataPoolClient cannot be null! ", client);
		final List<String> values = Lists.newArrayList();
		try
		{
			final List<PoolData> list = client.getAllPools(0, MAX_NUMBER_OF_POOLS).getItems();
			for (final PoolData item : list)
			{
				values.add(item.getPoolName());
			}
		}
		catch (final NoDataHubInstanceAvailableException e)
		{
			LOGGER.trace(e.getMessage(), e);
			return Collections.emptyList();
		}
		return values;
	}

	@Override
	public String getName(final Object data)
	{
		final String key = ObjectUtils.toString(data);
		final String ret = Labels.getLabel(String.format("%s.%s", "type", key));
		if (StringUtils.isBlank(ret))
		{
			return key;
		}
		return ret;
	}

	public void setClient(final DataPoolClient client)
	{
		this.client = client;
	}
}
