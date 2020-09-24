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
package com.hybris.backoffice.widgets.syncpopup;


import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.catalog.model.SyncItemJobModel;
import de.hybris.platform.core.model.ItemModel;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.hybris.backoffice.sync.facades.SynchronizationFacade;
import com.hybris.cockpitng.util.BackofficeSpringUtil;


/**
 * Loads sync item jobs for given items.
 */
public class SyncJobsLoader
{
	private final List<ItemModel> selectedItems;
	private SynchronizationFacade synchronizationFacade;
	private List<SyncItemJobModel> pullJobs;
	private List<SyncItemJobModel> pushJobs;
	private SyncJobsLoadingStatus loadingStatus;

	public SyncJobsLoader(final List<ItemModel> selectedItems)
	{
		this.selectedItems = selectedItems;
	}

	public List<SyncItemJobModel> getPullJobs()
	{
		return getLoadingStatus().isOK() ? pullJobs : Collections.emptyList();
	}

	public List<SyncItemJobModel> getPushJobs()
	{
		return getLoadingStatus().isOK() ? pushJobs : Collections.emptyList();
	}

	public SyncJobsLoadingStatus getLoadingStatus()
	{
		if (loadingStatus == null)
		{
			loadingStatus = loadData();
		}
		return loadingStatus;
	}

	protected SyncJobsLoadingStatus loadData()
	{
		if (CollectionUtils.isEmpty(selectedItems))
		{
			return SyncJobsLoadingStatus.EMPTY_SELECTION;
		}

		final Optional<CatalogVersionModel> syncCatalogVersion = getSynchronizationFacade().getSyncCatalogVersion(selectedItems);
		if (syncCatalogVersion.isPresent())
		{
			final List<SyncItemJobModel> allPushJobs = getSynchronizationFacade()
					.getOutboundSynchronizations(syncCatalogVersion.get());
			final List<SyncItemJobModel> allPullJobs = getSynchronizationFacade()
					.getInboundSynchronizations(syncCatalogVersion.get());

			pushJobs = filterApplicableJobs(allPushJobs);
			pullJobs = filterApplicableJobs(allPullJobs);

			if (CollectionUtils.isNotEmpty(pushJobs) || CollectionUtils.isNotEmpty(pullJobs))
			{

				pushJobs = filterAccessibleJobs(pushJobs);
				pullJobs = filterAccessibleJobs(pullJobs);
				if (CollectionUtils.isNotEmpty(pushJobs) || CollectionUtils.isNotEmpty(pullJobs))
				{
					return SyncJobsLoadingStatus.OK;
				}
				else
				{
					return SyncJobsLoadingStatus.MISSING_PERMISSIONS;
				}
			}
			else
			{
				return SyncJobsLoadingStatus.NO_SYNC_JOBS;
			}
		}
		return findLoadingFailureRootCause(syncCatalogVersion.isPresent());
	}

	protected List<SyncItemJobModel> filterAccessibleJobs(final List<SyncItemJobModel> jobs)
	{
		return CollectionUtils.isNotEmpty(jobs)
				? jobs.stream().filter(getSynchronizationFacade()::canSync).collect(Collectors.toList())
				: Collections.emptyList();
	}

	protected List<SyncItemJobModel> filterApplicableJobs(final List<SyncItemJobModel> jobs)
	{
		return CollectionUtils.isNotEmpty(jobs) ? jobs.stream()
				.filter(job -> getSynchronizationFacade().isApplicableForItems(job, selectedItems)).collect(Collectors.toList())
				: Collections.emptyList();
	}

	protected SyncJobsLoadingStatus findLoadingFailureRootCause(final boolean catalogPresent)
	{
		final List<ItemModel> catalogVersionAwareItems = getSynchronizationFacade().getCatalogVersionAwareItems(selectedItems);

		if (catalogVersionAwareItems.size() != selectedItems.size())
		{
			final List<CatalogVersionModel> catalogVersions = getSynchronizationFacade().getItemsCatalogVersions(selectedItems);
			if (CollectionUtils.isNotEmpty(catalogVersions))
			{
				if (catalogVersions.size() != selectedItems.size() && CollectionUtils.isNotEmpty(catalogVersionAwareItems))
				{
					return SyncJobsLoadingStatus.MIXED_ITEMS;
				}
				else if (catalogVersions.size() > 1)
				{
					return SyncJobsLoadingStatus.MULTIPLE_CATALOG_VERSIONS;
				}
			}
			else
			{
				return SyncJobsLoadingStatus.ITEMS_CONTAIN_CV_UNAWARE;
			}
		}

		return catalogPresent ? SyncJobsLoadingStatus.NO_SYNC_JOBS : SyncJobsLoadingStatus.DIFFERENT_CATALOG_VERSIONS;
	}


	protected SynchronizationFacade getSynchronizationFacade()
	{
		if (synchronizationFacade == null)
		{
			synchronizationFacade = BackofficeSpringUtil.getBean("synchronizationFacade");
		}
		return synchronizationFacade;
	}

	/**
	 * Defines loading status and label message key for given status.
	 */
	public enum SyncJobsLoadingStatus
	{
		/**
		 * Jobs is loaded successfully.
		 */
		OK(StringUtils.EMPTY),
		/**
		 * Given items contain more than one catalog versions.
		 */
		MULTIPLE_CATALOG_VERSIONS("sync.jobs.loading.status.too.many.cvs"),
		/**
		 * Given items contain CatalogVersion items and items of other types.
		 */
		MIXED_ITEMS("sync.jobs.loading.status.items.mixed.with.cv"),
		/**
		 * Given items contain catalog version unaware items.
		 */
		ITEMS_CONTAIN_CV_UNAWARE("sync.jobs.loading.status.items.not.cv.aware"),
		/**
		 * Given items are in different catalog versions.
		 */
		DIFFERENT_CATALOG_VERSIONS("sync.jobs.loading.status.different.catalog.versions"),
		/**
		 * List of items is empty
		 */
		EMPTY_SELECTION("sync.jobs.loading.status.empty.selection"),
		/**
		 * No sync jobs are found for given items.
		 */
		NO_SYNC_JOBS("sync.jobs.loading.status.no.jobs"),
		/**
		 * User doesn't have permissions to perform sync.
		 */
		MISSING_PERMISSIONS("sync.jobs.loading.status.missing.permissions");

		private final String violationMsgKey;

		SyncJobsLoadingStatus(final String msgKey)
		{
			violationMsgKey = msgKey;
		}

		/**
		 * @return status message label key.
		 */
		public String getMsgLabelKey()
		{
			return violationMsgKey;
		}

		public boolean isOK()
		{
			return this == OK;
		}
	}
}
