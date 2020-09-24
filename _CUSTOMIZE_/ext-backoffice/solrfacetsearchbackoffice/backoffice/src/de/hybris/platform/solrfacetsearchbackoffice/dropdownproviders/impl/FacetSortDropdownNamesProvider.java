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

import de.hybris.platform.solrfacetsearchbackoffice.dropdownproviders.DropdownNamesProvider;

import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.zkoss.util.resource.Labels;


public class FacetSortDropdownNamesProvider implements DropdownNamesProvider
{

	private static final String KEY_PREFIX = "hmc.solrfacetsearch.sortprovider.";

	@Override
	public String getName(final Object data)
	{
		return getName(data, null);
	}

	@Override
	public String getName(final Object data, final Map<String, String> options)
	{
		if (data == null)
		{
			return "";
		}

		String nameLabel = Labels.getLabel(KEY_PREFIX + data.toString().toLowerCase(Locale.ROOT));
		if (StringUtils.isBlank(nameLabel))
		{
			nameLabel = data.toString();
		}

		return nameLabel;
	}
}
