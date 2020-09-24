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
package com.hybris.backoffice.sync.facades;

import de.hybris.platform.catalog.enums.SyncItemStatus;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.catalog.model.SyncItemJobModel;
import de.hybris.platform.catalog.model.synchronization.CatalogVersionSyncCronJobModel;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.cronjob.model.CronJobModel;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.hybris.backoffice.sync.PartialSyncInfo;
import com.hybris.backoffice.sync.SyncTask;


/**
 * Facade which allows to perform synchronization related operations.
 */
public interface SynchronizationFacade
{
	/**
	 * Performs synchronization based on given sync task.
	 *
	 * @param syncTask
	 *           - defines sync jobs and items to sync.
	 * @return executed sync cron job code {@link CronJobModel#getCode()} if sync has been started.
	 */
	Optional<String> performSynchronization(SyncTask syncTask);

	/**
	 * Gets inbound sync jobs for given catalog version.
	 *
	 * @param catalogVersion
	 *           - catalog version for which outbound synchronizations will be returned. To obtain catalog version please
	 *           use {@link #getSyncCatalogVersion(Collection)}
	 * @return list of sync jobs
	 */
	List<SyncItemJobModel> getInboundSynchronizations(CatalogVersionModel catalogVersion);

	/**
	 * Gets outbound sync jobs for given catalog version.
	 *
	 * @param catalogVersion
	 *           - catalog version for which outbound synchronizations will be returned. To obtain catalog version please
	 *           use {@link #getSyncCatalogVersion(Collection)}
	 * @return list of sync jobs
	 */
	List<SyncItemJobModel> getOutboundSynchronizations(CatalogVersionModel catalogVersion);

	/**
	 * Returns catalog version for given items if they are from the same catalog version or are CatalogVersion itself.
	 *
	 * @param items
	 *           items for which catalog version will be extracted.
	 * @return catalog version for given items.
	 */
	Optional<CatalogVersionModel> getSyncCatalogVersion(final Collection<ItemModel> items);

	/**
	 * Checks if all {@link ItemModel}s are sync according to source and target from {@link SyncItemJobModel}.
	 *
	 * @param itemModels
	 *           defines items for which sync status is checked
	 * @param jobModel
	 *           defines sync item job model from which source and target is taken to perform status check
	 * @param ctxMap
	 *           defines additional context
	 * @return true if all itemModels are in sync
	 */
	Optional<Boolean> isInSync(List<ItemModel> itemModels, SyncItemJobModel jobModel, Map<String, Object> ctxMap);

	/**
	 * Checks {@link ItemModel}'s status in all available synchronizations.
	 *
	 * @param itemModel
	 *           defines item for which sync status is checked
	 * @param ctxMap
	 *           defines additional context
	 * @return true if all itemModel are in sync, Optional.empty if sync status is not available e.g. item is not catalog version
	 *         aware
	 */
	Optional<Boolean> isInSync(ItemModel itemModel, Map<String, Object> ctxMap);


	/**
	 * Checks if {@link SyncItemJobModel} is applicable for {@link ItemModel}s
	 * @param jobModel define sync job which will be checked if is applicable for items
	 * @param items defines items for which sync job is checked
	 * @return true if sync job is applicable for items, false if it is not applicable
	 */
	default boolean isApplicableForItems(final SyncItemJobModel jobModel, final Collection<ItemModel> items)
	{
		return true;
	}

	/**
	 * Checks {@link ItemModel}'s status in all available synchronizations.
	 *
	 * @param itemModel
	 *           defines item for which sync status is checked
	 * @param status
	 *           defines target status for partial sync info {@link PartialSyncInfo}
	 * @param ctxMap
	 *           defines additional context
	 * @return partial sync information if item is catalog version aware.
	 */
	Optional<PartialSyncInfo> getPartialSyncStatusInfo(ItemModel itemModel, final SyncItemStatus status, Map<String, Object> ctxMap);

	/**
	 * Finds a counterpart of given item in respective catalog version defined by the syncItemJob.
	 *
	 * @param item
	 *           item of which counterpart will be found.
	 * @param syncItemJob
	 *           synchronization job which defines source and target catalog version.
	 * @return counterpart item if found.
	 */
	Optional<ItemModel> findSyncCounterpart(ItemModel item, SyncItemJobModel syncItemJob);

	/**
	 * Extracts from given list items which are CatalogVersion
	 *
	 * @param items
	 *           list of items
	 * @return list of catalog version items
	 */
	List<CatalogVersionModel> getItemsCatalogVersions(final List<? extends ItemModel> items);

	/**
	 * Extract catalog version aware items
	 * {@link de.hybris.platform.catalog.CatalogTypeService#isCatalogVersionAwareModel(ItemModel)}
	 *
	 * @param items
	 *           list of items
	 * @return list of catalog version aware items
	 */
	List<ItemModel> getCatalogVersionAwareItems(final List<? extends ItemModel> items);

	/**
	 * Tells if given item is being synchronized.
	 *
	 * @param item
	 *           item to check
	 * @return true if item is being synchronized.
	 */
	boolean isSyncInProgress(ItemModel item);

	/**
	 * Runs given sync cron job again omitting items already processed in the previous run.
	 *
	 * @param cronJob
	 *           sync cron job to rerun.
	 */
	void reRunCronJob(final CatalogVersionSyncCronJobModel cronJob);

	/**
	 * Tells whether current user can perform given sync.
	 * @param sync synchronization job.
	 * @return true if user can synchronize.
	 */
	boolean canSync(SyncItemJobModel sync);

}
