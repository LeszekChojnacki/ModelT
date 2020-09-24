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

import de.hybris.platform.catalog.enums.SyncItemStatus;
import de.hybris.platform.catalog.model.SyncItemJobModel;
import de.hybris.platform.core.model.ItemModel;

import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * A pojo which contains partial sync information for given item
 */
public class PartialSyncInfo
{
	private final List<ItemModel> syncItems;
	private final SyncItemStatus targetStatus;
	private final Map<SyncItemJobModel, Boolean> inboundSyncStatus;
	private final Map<SyncItemJobModel, Boolean> outboundSyncStatus;

	/**
	 * @param syncItems sync items - item which should be synchronized and it's related items
	 * @param targetStatus chosen target status
	 * @param inboundSyncStatus map of inbound sync jobs and if sync status matches target status
	 * @param outboundSyncStatus map of outbound sync job and if sync status matches target status
	 */
	public PartialSyncInfo(final List<ItemModel> syncItems, final SyncItemStatus targetStatus,
			final Map<SyncItemJobModel, Boolean> inboundSyncStatus, final Map<SyncItemJobModel, Boolean> outboundSyncStatus)
	{
		this.syncItems = syncItems;
		this.targetStatus = targetStatus;
		this.inboundSyncStatus = inboundSyncStatus != null ? inboundSyncStatus : Collections.emptyMap();
		this.outboundSyncStatus = outboundSyncStatus != null ? outboundSyncStatus : Collections.emptyMap();
	}

	public List<ItemModel> getSyncItems()
	{
		return syncItems;
	}

	public SyncItemStatus getTargetStatus()
	{
		return targetStatus;
	}

	public Map<SyncItemJobModel, Boolean> getInboundSyncStatus()
	{
		return inboundSyncStatus;
	}

	public Map<SyncItemJobModel, Boolean> getOutboundSyncStatus()
	{
		return outboundSyncStatus;
	}
}
