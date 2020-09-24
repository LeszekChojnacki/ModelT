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

import de.hybris.platform.datahubbackoffice.exception.NoDataHubInstanceAvailableException;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.util.resource.Labels;

import com.google.common.collect.Lists;
import com.hybris.cockpitng.core.util.Validate;
import com.hybris.datahub.client.RawItemClassClient;
import com.hybris.datahub.dto.metadata.ItemTypeData;

public class RawTypesProvider implements DropdownProvider
{
	private static final Logger LOGGER = LoggerFactory.getLogger(RawTypesProvider.class);

	private RawItemClassClient client;

	@Override
	public List<String> getAllValues()
	{
		Validate.notNull("RawItemClassClient cannot be null! ", client);
		final List<String> typeCodes = Lists.newArrayList();
		try
		{
			final List<ItemTypeData> rawItemTypes = client.getItemTypes();
			for (final ItemTypeData rawItemType : rawItemTypes)
			{
				typeCodes.add(rawItemType.getName());
			}
		}
		catch (final NoDataHubInstanceAvailableException e)
		{
			LOGGER.trace(e.getMessage(), e);
			return Collections.emptyList();
		}
		return typeCodes;
	}

	@Override
	public String getName(final Object data)
	{

		final String key = ObjectUtils.toString(data);
		String ret = Labels.getLabel(String.format("%s.%s", "type", key));
		if (StringUtils.isBlank(ret))
		{
			ret = key;
		}
		return ret;
	}

	public void setClient(final RawItemClassClient client)
	{
		this.client = client;
	}
}
