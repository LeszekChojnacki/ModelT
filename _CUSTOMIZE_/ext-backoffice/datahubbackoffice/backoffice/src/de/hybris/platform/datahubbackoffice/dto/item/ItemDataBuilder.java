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
package de.hybris.platform.datahubbackoffice.dto.item;

import com.hybris.datahub.dto.item.ItemData;

public class ItemDataBuilder
{
	private Long itemId;

	public static ItemDataBuilder itemData()
	{
		return new ItemDataBuilder();
	}

	public ItemDataBuilder withId(final Long id)
	{
		itemId = id;
		return this;
	}

	public ItemData build()
	{
		final ItemData item = new ItemData();
		item.setId(itemId);
		return item;
	}
}
