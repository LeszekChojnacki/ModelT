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

import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.zkoss.util.resource.Labels;

import com.google.common.collect.Lists;
import com.hybris.cockpitng.core.util.Validate;
import com.hybris.datahub.client.CanonicalItemClassClient;
import com.hybris.datahub.dto.metadata.CanonicalItemTypeData;

public class CanonicalItemTypesProvider implements DropdownProvider
{
	private CanonicalItemClassClient client;

	@Override
	public List<String> getAllValues()
	{
		Validate.notNull("CanonicalItemClassItem cannot be null! ", client);
		final List<String> typeCodes = Lists.newArrayList();
		final List<CanonicalItemTypeData> canonicalItemTypes = client.getItemTypes();
		for (final CanonicalItemTypeData canonicalItem : canonicalItemTypes)
		{
			typeCodes.add(canonicalItem.getName());
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

	public void setClient(final CanonicalItemClassClient client)
	{
		this.client = client;
	}
}
