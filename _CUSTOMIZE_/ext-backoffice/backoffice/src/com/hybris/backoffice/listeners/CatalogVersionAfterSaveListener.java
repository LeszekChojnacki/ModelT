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
package com.hybris.backoffice.listeners;

import de.hybris.platform.tx.AfterSaveEvent;
import de.hybris.platform.tx.AfterSaveListener;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.ApplicationUtils;
import com.hybris.backoffice.catalogversioneventhandling.AvailableCatalogVersionsTag;


/**
 * Listener which handle creation or removal catalog version events
 */
public class CatalogVersionAfterSaveListener implements AfterSaveListener
{

	private static final int CATALOG_VERSION_DEPLOYMENT_CODE = 601;
	private AvailableCatalogVersionsTag availableCatalogVersionsTag;


	@Override
	public void afterSave(final Collection<AfterSaveEvent> collection)
	{
		if (shouldPerform())
		{
			collection.stream().filter(this::isCatalogVersionRelatedEvent).findAny().ifPresent(afterSaveEvent -> handleEvent());
		}
	}

	protected boolean shouldPerform()
	{
		return ApplicationUtils.isPlatformReady();
	}

	protected boolean isCatalogVersionRelatedEvent(final AfterSaveEvent event)
	{
		return CATALOG_VERSION_DEPLOYMENT_CODE == event.getPk().getTypeCode()
				&& (AfterSaveEvent.CREATE == event.getType() || AfterSaveEvent.REMOVE == event.getType());
	}

	protected void handleEvent()
	{
		getAvailableCatalogVersionsTag().refresh();
	}

	protected AvailableCatalogVersionsTag getAvailableCatalogVersionsTag()
	{
		return availableCatalogVersionsTag;
	}

	@Required
	public void setAvailableCatalogVersionsTag(final AvailableCatalogVersionsTag availableCatalogVersionsTag)
	{
		this.availableCatalogVersionsTag = availableCatalogVersionsTag;
	}

}
