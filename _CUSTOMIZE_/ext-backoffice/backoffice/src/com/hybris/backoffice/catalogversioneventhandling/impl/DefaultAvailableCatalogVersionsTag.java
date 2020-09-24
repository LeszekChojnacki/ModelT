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
package com.hybris.backoffice.catalogversioneventhandling.impl;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import com.hybris.backoffice.catalogversioneventhandling.AvailableCatalogVersionsTag;


/**
 * Service responsible for tracking any changes (creation or removal) in catalog versions
 */
public class DefaultAvailableCatalogVersionsTag implements AvailableCatalogVersionsTag
{
	private final AtomicReference<UUID> tag = new AtomicReference<>();

	@Override
	public UUID getTag()
	{
		return tag.get();
	}

	@Override
	public void refresh()
	{
		tag.set(UUID.randomUUID());
	}
}
