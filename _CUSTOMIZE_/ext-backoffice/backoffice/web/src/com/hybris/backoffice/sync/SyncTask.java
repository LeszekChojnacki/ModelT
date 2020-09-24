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
package com.hybris.backoffice.sync;


import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.catalog.model.SyncItemJobModel;
import de.hybris.platform.catalog.synchronization.SyncConfig;
import de.hybris.platform.core.model.ItemModel;

import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import com.hybris.cockpitng.core.context.impl.DefaultCockpitContext;


/**
 * Defines synchronization task.
 */
public class SyncTask extends DefaultCockpitContext
{
	private final transient List<? extends ItemModel> items;
	private final transient SyncItemJobModel syncItemJob;
	private transient SyncConfig syncConfig;

	/**
	 * Creates items sync task
	 * 
	 * @param items
	 *           - items to be synchronised - see {@link #getItems()} for more details.
	 * @param syncItemJob
	 *           - job which will be used to perform sync.
	 */
	public SyncTask(final List<? extends ItemModel> items, final SyncItemJobModel syncItemJob)
	{
		this.items = items != null ? items : Collections.emptyList();
		this.syncItemJob = syncItemJob;
		this.syncConfig = null;
	}

	/**
	 * Creates full catalog version sync
	 * 
	 * @param syncItemJob
	 *           - job which will be used to perform sync.
	 */
	public SyncTask(final SyncItemJobModel syncItemJob)
	{
		this.items = null;
		this.syncItemJob = syncItemJob;
		this.syncConfig = null;
	}

	public SyncItemJobModel getSyncItemJob()
	{
		return syncItemJob;
	}

	/**
	 * Items to be synchronised. If empty or contains only one catalog version it means that full catalog version sync
	 * should be performed for given {@link #getSyncItemJob()}.
	 *
	 * @return list of items to sync.
	 */
	public List<? extends ItemModel> getItems()
	{
		return items;
	}

	/**
	 * Optional configuration for synchronisation {@link SyncConfig}
	 */
	public SyncConfig getSyncConfig()
	{
		return syncConfig;
	}

	public void setSyncConfig(final SyncConfig syncConfig)
	{
		this.syncConfig = syncConfig;
	}

	/**
	 * Tells if sync is for entire catalog version. Checks if list of {@link #getItems()} is empty or has only one
	 * catalog version.
	 * 
	 * @return true this task should synchronize full catalog version.
	 */
	public boolean isCatalogVersionSync()
	{
		return CollectionUtils.isEmpty(items) || (items.size() == 1 && items.get(0) instanceof CatalogVersionModel);
	}
}
