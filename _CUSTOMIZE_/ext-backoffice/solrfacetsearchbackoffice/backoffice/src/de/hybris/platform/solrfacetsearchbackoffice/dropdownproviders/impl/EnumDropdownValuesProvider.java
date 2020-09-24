/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.solrfacetsearchbackoffice.dropdownproviders.impl;

import de.hybris.platform.platformbackoffice.editors.yenum.PlatformEnumValueResolver;
import de.hybris.platform.solrfacetsearchbackoffice.dropdownproviders.DropdownValuesProvider;
import de.hybris.platform.solrfacetsearchbackoffice.editor.ConfigurableDropdownEditor;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Lists;


public class EnumDropdownValuesProvider implements DropdownValuesProvider
{
	private PlatformEnumValueResolver platformEnumValueResolver;

	@Override
	public List<Object> getValues(final String dropDownValueClassTypes)
	{
		return getValues(dropDownValueClassTypes, null);
	}

	@Override
	public List<Object> getValues(final String dropDownValueClassTypes, final Map<String, String> options)
	{
		final List<Object> values = Lists.newArrayList();

		final List<Object> enumValues = platformEnumValueResolver.getAllValues(dropDownValueClassTypes, null);
		for (final Object value : enumValues)
		{
			final String name = ((Enum) value).name();
			if (!MapUtils.isEmpty(options) && options.get(ConfigurableDropdownEditor.TO_LOWER_CASE_OPTION) != null
					&& Boolean.parseBoolean(options.get(ConfigurableDropdownEditor.TO_LOWER_CASE_OPTION)))
			{
				values.add(name.toLowerCase(Locale.ROOT));
			}
			else
			{
				values.add(name);
			}

		}
		return values;
	}

	public PlatformEnumValueResolver getPlatformEnumValueResolver()
	{
		return platformEnumValueResolver;
	}

	@Required
	public void setPlatformEnumValueResolver(final PlatformEnumValueResolver platformEnumValueResolver)
	{
		this.platformEnumValueResolver = platformEnumValueResolver;
	}
}
