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

import com.google.common.collect.Lists;

public class DefaultDropdownProvider implements DropdownProvider
{
	@Override
	public List<String> getAllValues()
	{
		return Lists.newArrayList("i18n.dynamic1", "i18n.dynamic2", "i18n.dynamic3");
	}

	@Override
	public String getName(final Object data)
	{
		return "#" + data;
	}
}
