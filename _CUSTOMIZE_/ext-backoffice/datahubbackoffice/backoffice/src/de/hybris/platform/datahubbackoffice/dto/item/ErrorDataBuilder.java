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

import com.hybris.datahub.dto.item.ErrorData;
import com.hybris.datahub.dto.item.ItemData;

public class ErrorDataBuilder
{
	private Long itemId;

	public static ErrorDataBuilder errorData()
	{
		return new ErrorDataBuilder();
	}

	public ErrorDataBuilder forItem(final ItemData item)
	{
		return forItemId(item.getId());
	}

	public ErrorDataBuilder forItemId(final Long id)
	{
		itemId = id;
		return this;
	}

	public ErrorData build()
	{
		final ErrorData error = new ErrorData();
		error.setCanonicalItemId(itemId);
		return error;
	}
}
