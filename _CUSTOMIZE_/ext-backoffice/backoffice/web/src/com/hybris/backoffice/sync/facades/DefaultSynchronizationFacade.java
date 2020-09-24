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


import de.hybris.platform.catalog.CatalogTypeService;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.enums.SyncItemStatus;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.catalog.model.ItemSyncTimestampModel;
import de.hybris.platform.catalog.model.SyncItemCronJobModel;
import de.hybris.platform.catalog.model.SyncItemJobModel;
import de.hybris.platform.catalog.model.synchronization.CatalogVersionSyncCronJobModel;
import de.hybris.platform.catalog.model.synchronization.CatalogVersionSyncScheduleMediaModel;
import de.hybris.platform.catalog.synchronization.CatalogSynchronizationService;
import de.hybris.platform.catalog.synchronization.SyncConfig;
import de.hybris.platform.catalog.synchronization.SyncItemInfo;
import de.hybris.platform.catalog.synchronization.SyncResult;
import de.hybris.platform.catalog.synchronization.SynchronizationStatusService;
import de.hybris.platform.catalog.synchronization.strategy.SyncJobApplicableTypesStrategy;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.cronjob.enums.ErrorMode;
import de.hybris.platform.cronjob.enums.JobLogLevel;
import de.hybris.platform.servicelayer.cronjob.CronJobService;
import de.hybris.platform.servicelayer.interceptor.impl.InterceptorExecutionPolicy;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.model.collector.RelatedItemsCollector;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.servicelayer.util.ServicesUtil;
import de.hybris.platform.tx.Transaction;
import de.hybris.platform.tx.TransactionBody;
import de.hybris.platform.util.Config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.hybris.backoffice.sync.PartialSyncInfo;
import com.hybris.backoffice.sync.SyncTask;


/**
 * Default implementation using the {@link CatalogSynchronizationService}. The synchronizations are done asynchronously
 * using a background process (cron job).
 */
public class DefaultSynchronizationFacade implements SynchronizationFacade
{
	protected static final String SYNC_CONFIG_CREATE_SAVED_VALUES = "backoffice.sync.config.createSavedValues";
	protected static final String SYNC_CONFIG_FORCE_UPDATE = "backoffice.sync.config.forceUpdate";
	protected static final String SYNC_CONFIG_SYNCHRONOUS = "backoffice.sync.config.synchronous";
	protected static final String SYNC_CONFIG_LOG_TO_FILE = "backoffice.sync.config.logToFile";
	protected static final String SYNC_CONFIG_LOG_TO_DATABASE = "backoffice.sync.config.logToDatabase";
	protected static final String SYNC_CONFIG_KEEP_CRON_JOB = "backoffice.sync.config.keepCronJob";
	protected static final String SYNC_CONFIG_LOG_LEVEL_DATABASE = "backoffice.sync.config.logLevelDatabase";
	protected static final String SYNC_CONFIG_LOG_LEVEL_FILE = "backoffice.sync.config.logLevelFile";
	protected static final String SYNC_CONFIG_ERROR_MODE = "backoffice.sync.config.errorMode";
	protected static final String BACKOFFICE_SYNC_CONFIG_ABORT_ON_COLLIDING = "backoffice.sync.config.abortOnColliding";
	private static final Logger LOG = LoggerFactory.getLogger(DefaultSynchronizationFacade.class);
	private CatalogSynchronizationService catalogSynchronizationService;
	private SynchronizationStatusService synchronizationStatusService;
	private SyncJobApplicableTypesStrategy syncJobApplicableTypesStrategy;
	private CatalogTypeService catalogTypeService;
	private RelatedItemsCollector relatedItemsCollector;
	private CronJobService cronJobService;
	private ModelService modelService;
	private MediaService mediaService;
	private SessionService sessionService;
	private CatalogVersionService catalogVersionService;
	private UserService userService;

	@Override
	public Optional<String> performSynchronization(final SyncTask syncTask)
	{
		if (syncTask == null)
		{
			return Optional.empty();
		}

		return syncTask.isCatalogVersionSync() ? performCatalogSync(syncTask) : preformItemsSync(syncTask);
	}

	protected Optional<String> performCatalogSync(final SyncTask syncTask)
	{
		return executeSync(syncTask, (syncJob, syncConfig) -> catalogSynchronizationService.synchronize(syncJob, syncConfig));
	}

	protected Optional<String> preformItemsSync(final SyncTask syncTask)
	{
		final List<ItemModel> catalogVersionAwareItems = getCatalogVersionAwareItems(syncTask.getItems());
		final List<ItemModel> itemsToSync = collectRelatedItems(catalogVersionAwareItems, syncTask.getParameters());

		return executeSync(syncTask,
				(syncJob, syncConfig) -> catalogSynchronizationService.performSynchronization(itemsToSync, syncJob, syncConfig));
	}

	protected Optional<String> executeSync(final SyncTask syncTask,
			final BiFunction<SyncItemJobModel, SyncConfig, SyncResult> syncRunner)
	{
		final SyncItemJobModel sync = syncTask.getSyncItemJob();
		final SyncConfig syncConfig = prepareSyncConfig(syncTask.getSyncConfig(), sync);
		final SyncResult syncResult = syncRunner.apply(sync, syncConfig);
		if (syncResult != null)
		{
			final SyncItemCronJobModel cronJob = syncResult.getCronJob();
			return cronJob != null ? Optional.ofNullable(cronJob.getCode()) : Optional.empty();
		}
		return Optional.empty();
	}

	@Override
	public List<CatalogVersionModel> getItemsCatalogVersions(final List<? extends ItemModel> items)
	{
		if (CollectionUtils.isNotEmpty(items))
		{
			return items.stream().filter(CatalogVersionModel.class::isInstance).map(CatalogVersionModel.class::cast)
					.collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	@Override
	public List<ItemModel> getCatalogVersionAwareItems(final List<? extends ItemModel> items)
	{
		if (CollectionUtils.isNotEmpty(items))
		{
			return items.stream().filter(item -> catalogTypeService.isCatalogVersionAwareModel(item)).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	protected List<SyncItemJobModel> getSynchronizations(final CatalogVersionModel syncCatalogVersion)
	{
		final List<SyncItemJobModel> allJobs = new ArrayList<>();
		if (syncCatalogVersion != null)
		{
			allJobs.addAll(getInboundSynchronizations(syncCatalogVersion));
			allJobs.addAll(getOutboundSynchronizations(syncCatalogVersion));
		}
		return allJobs;
	}

	@Override
	public List<SyncItemJobModel> getInboundSynchronizations(final CatalogVersionModel catalogVersion)
	{
		return Collections.unmodifiableList(catalogVersion.getIncomingSynchronizations());
	}

	@Override
	public List<SyncItemJobModel> getOutboundSynchronizations(final CatalogVersionModel catalogVersion)
	{
		return Collections.unmodifiableList(catalogVersion.getSynchronizations());
	}

	@Override
	public Optional<CatalogVersionModel> getSyncCatalogVersion(final Collection<ItemModel> items)
	{
		if (CollectionUtils.isNotEmpty(items))
		{
			if (items.stream().allMatch(CatalogVersionModel.class::isInstance))
			{
				if (items.stream().distinct().count() == 1L)
				{
					return Optional.of((CatalogVersionModel) items.iterator().next());
				}
			}
			else if (items.stream().allMatch(catalogTypeService::isCatalogVersionAwareModel))
			{

				final List<CatalogVersionModel> distinctCatalogVersions = items.stream().map(item -> {
					try
					{
						return catalogTypeService.getCatalogVersionForCatalogVersionAwareModel(item);
					}
					catch (final IllegalStateException e)
					{
						LOG.debug("Cannot get catalog version for given item: " + item, e);
						return null;
					}
				}).filter(item -> !Objects.isNull(item)).distinct().collect(Collectors.toList());

				if (distinctCatalogVersions.size() == 1)
				{
					return Optional.of(distinctCatalogVersions.get(0));
				}
			}

		}
		return Optional.empty();
	}

	@Override
	public Optional<Boolean> isInSync(final List<ItemModel> itemModels, final SyncItemJobModel jobModel,
			final Map<String, Object> ctxMap)
	{
		if (CollectionUtils.isNotEmpty(itemModels) && itemModels.stream().noneMatch(CatalogVersionModel.class::isInstance))
		{
			final List<ItemModel> relatedItems = collectRelatedItems(itemModels, ctxMap);
			final List<Optional<Boolean>> inSync = relatedItems.stream().map(relatedItem -> isInSync(relatedItem, Collections.singleton(jobModel))).collect(Collectors.toList());
			return inSync.stream().allMatch(Optional::isPresent) ? inSync.stream().map(sync -> sync.orElse(false)).reduce(Boolean::logicalAnd) : Optional.empty();
		}
		return Optional.empty();
	}

	@Override
	public Optional<Boolean> isInSync(final ItemModel itemModel, final Map<String, Object> ctxMap)
	{
		ServicesUtil.validateParameterNotNull(itemModel, "item cannot be null");
		if (modelService.isNew(itemModel) || itemModel instanceof CatalogVersionModel)
		{
			return Optional.empty();
		}

		final List<ItemModel> relatedItems = collectRelatedItems(Collections.singletonList(itemModel), ctxMap);
		final List<Optional<Boolean>> inSync = relatedItems.stream().map(this::isInSync).collect(Collectors.toList());
		return inSync.stream().allMatch(Optional::isPresent) ? inSync.stream().map(sync -> sync.orElse(false)).reduce(Boolean::logicalAnd) : Optional.empty();
	}

	protected Optional<Boolean> isInSync(final ItemModel syncItem)
	{
		final Optional<CatalogVersionModel> catalogVersion = getSyncCatalogVersion(Collections.singleton(syncItem));
		return catalogVersion.map(this::getSynchronizations).flatMap(jobs -> isInSync(syncItem, jobs));
	}

	protected Optional<Boolean> isInSync(final ItemModel syncItem, final Collection<SyncItemJobModel> syncJobs)
	{
		final List<SyncItemStatus> syncStatuses = synchronizationStatusService.getSyncInfo(syncItem, new ArrayList<>(syncJobs)).stream()
				.map(SyncItemInfo::getSyncStatus).collect(Collectors.toList());

		final boolean isInSync = syncStatuses.stream().allMatch(
				syncStatus -> (SyncItemStatus.IN_SYNC.equals(syncStatus) || SyncItemStatus.NOT_APPLICABLE.equals(syncStatus)));

		return Optional.of(isInSync);
	}

	private Optional<Boolean> matchesSyncStatus(final List<ItemModel> syncItems, final List<SyncItemJobModel> allJobs,
			final SyncItemStatus inSync)
	{
		try
		{
			return Optional.of(synchronizationStatusService.matchesSyncStatus(syncItems, allJobs, inSync));
		}
		catch (final RuntimeException ex)
		{
			LOG.debug("Cannot check sync status:", ex);
			return Optional.empty();
		}
	}

	@Override
	public boolean isApplicableForItems(final SyncItemJobModel jobModel, final Collection<ItemModel> items)
	{
		return items.stream().filter(item -> !(item instanceof CatalogVersionModel))
				.allMatch(item -> getSyncJobApplicableTypesStrategy().checkIfApplicable(item, jobModel));
	}

	@Override
	public boolean isSyncInProgress(final ItemModel item)
	{
		if (modelService.isNew(item))
		{
			return false;
		}

		final Optional<CatalogVersionModel> cv = getSyncCatalogVersion(Lists.newArrayList(item));
		return cv.isPresent() && getSynchronizations(cv.get()).stream().anyMatch(catalogSynchronizationService::isInProgress);
	}

	@Override
	public void reRunCronJob(final CatalogVersionSyncCronJobModel cronJob)
	{
		final CatalogVersionSyncCronJobModel syncCronJobModel = copyCronJobSafely(() -> createCronJobForRerun(cronJob));
		if (syncCronJobModel != null)
		{
			cronJobService.performCronJob(syncCronJobModel);
		}
	}

	protected CatalogVersionSyncCronJobModel copyCronJobSafely(final Supplier<CatalogVersionSyncCronJobModel> copySupplier)
	{
		return executeInTransaction(() -> executeWithoutTypeInterceptors(copySupplier));
	}

	protected <T> T executeInTransaction(final Supplier<T> toExecute)
	{
		ServicesUtil.validateParameterNotNull(toExecute, "cronJobForRerun cannot be null");
		try
		{
			return (T) Transaction.current().execute(new TransactionBody()
			{
				@Override
				public T execute() throws Exception
				{
					return toExecute.get();
				}
			});
		}
		catch (final Exception e)
		{
			LOG.error("Cannot prepareStream cron job for re-run!", e);
			return null;
		}
	}

	protected <T> T executeWithoutTypeInterceptors(final Supplier<T> toExecute)
	{
		ServicesUtil.validateParameterNotNull(toExecute, "cronJobForRerun cannot be null");

		final Map<String, Object> params = ImmutableMap.of(InterceptorExecutionPolicy.DISABLED_INTERCEPTOR_TYPES,
				ImmutableSet.of(InterceptorExecutionPolicy.InterceptorType.VALIDATE));

		return sessionService.executeInLocalViewWithParams(params, new SessionExecutionBody()
		{
			@Override
			public T execute()
			{
				return toExecute.get();
			}
		});
	}

	protected CatalogVersionSyncCronJobModel createCronJobForRerun(final CatalogVersionSyncCronJobModel cronJobForRerun)
	{
		final CatalogVersionSyncCronJobModel newCronJob = modelService.create(cronJobForRerun.getItemtype());
		copyCronJobData(newCronJob, cronJobForRerun);
		copyScheduledMedia(newCronJob, cronJobForRerun);
		modelService.save(newCronJob);
		return newCronJob;
	}

	protected void copyCronJobData(final CatalogVersionSyncCronJobModel newCronJob,
			final CatalogVersionSyncCronJobModel cronJobForRerun)
	{
		newCronJob.setJob(cronJobForRerun.getJob());
		newCronJob.setForceUpdate(cronJobForRerun.getForceUpdate());
		newCronJob.setCreateSavedValues(cronJobForRerun.getCreateSavedValues());
		newCronJob.setLogToDatabase(cronJobForRerun.getLogToDatabase());
		newCronJob.setLogToFile(cronJobForRerun.getLogToFile());
		newCronJob.setLogLevelDatabase(cronJobForRerun.getLogLevelDatabase());
		newCronJob.setLogLevelFile(cronJobForRerun.getLogLevelFile());
		newCronJob.setErrorMode(cronJobForRerun.getErrorMode());
		newCronJob.setFullSync(cronJobForRerun.getFullSync());
	}

	protected void copyScheduledMedia(final CatalogVersionSyncCronJobModel newCronJob,
			final CatalogVersionSyncCronJobModel cronJobForRerun)
	{
		final CatalogVersionSyncScheduleMediaModel firstScheduledMedia = CollectionUtils
				.isNotEmpty(cronJobForRerun.getScheduleMedias()) ? cronJobForRerun.getScheduleMedias().get(0) : null;

		if (firstScheduledMedia != null)
		{
			final CatalogVersionSyncScheduleMediaModel newScheduledMedia = modelService
					.create(CatalogVersionSyncScheduleMediaModel.class);
			newScheduledMedia
					.setCode(String.format("sync_schedule_%s_%s", firstScheduledMedia.getPk().toString(), UUID.randomUUID()));
			newScheduledMedia.setCronjob(newCronJob);
			newScheduledMedia.setCode(RandomStringUtils.randomAlphanumeric(10)); // just to satisfy temporary model service
			newCronJob.setScheduleMedias(Lists.newArrayList(newScheduledMedia));

			modelService.save(newScheduledMedia);
			mediaService.copyData(firstScheduledMedia, newScheduledMedia);
		}
	}

	@Override
	public Optional<PartialSyncInfo> getPartialSyncStatusInfo(final ItemModel itemModel, final SyncItemStatus status,
			final Map<String, Object> ctxMap)
	{
		ServicesUtil.validateParameterNotNull(itemModel, "item cannot be null");
		ServicesUtil.validateParameterNotNull(status, "status cannot be null");

		final List<ItemModel> syncItems = collectRelatedItems(Lists.newArrayList(itemModel), ctxMap);
		final Optional<CatalogVersionModel> syncCatalogVersion = getSyncCatalogVersion(syncItems);
		if (syncCatalogVersion.isPresent())
		{

			final Collector<SyncItemJobModel, ?, Map<SyncItemJobModel, Boolean>> syncStatusCollector = Collectors//
					.toMap(syncJob -> syncJob,
							syncJob -> matchesSyncStatus(syncItems, Lists.newArrayList(syncJob), status).orElse(Boolean.FALSE));

			final Map<SyncItemJobModel, Boolean> inbound = getInboundSynchronizations(syncCatalogVersion.get()).stream()
					.filter(syncJob -> isApplicableForItems(syncJob, Collections.singleton(itemModel))).collect(syncStatusCollector);

			final Map<SyncItemJobModel, Boolean> outbound = getOutboundSynchronizations(syncCatalogVersion.get()).stream()
					.filter(syncJob -> isApplicableForItems(syncJob, Collections.singleton(itemModel))).collect(syncStatusCollector);

			return Optional.of(new PartialSyncInfo(syncItems, status, inbound, outbound));
		}
		return Optional.empty();
	}

	@Override
	public Optional<ItemModel> findSyncCounterpart(final ItemModel item, final SyncItemJobModel syncItemJob)
	{
		if (!modelService.isRemoved(item) && syncItemJob != null)
		{
			final CatalogVersionModel catalogVersion = catalogTypeService.getCatalogVersionForCatalogVersionAwareModel(item);
			final boolean fromSource = ObjectUtils.equals(syncItemJob.getSourceVersion(), catalogVersion);
			if (fromSource)
			{
				final ItemSyncTimestampModel timestamp = catalogSynchronizationService
						.getSynchronizationSourceTimestampFor(syncItemJob, item);
				return timestamp != null ? Optional.ofNullable(timestamp.getTargetItem()) : Optional.empty();
			}
			else
			{
				final ItemSyncTimestampModel timestamp = catalogSynchronizationService
						.getSynchronizationTargetTimestampFor(syncItemJob, item);
				return timestamp != null ? Optional.ofNullable(timestamp.getSourceItem()) : Optional.empty();
			}
		}
		return Optional.empty();
	}

	protected List<ItemModel> collectRelatedItems(final List<ItemModel> items, final Map<String, Object> ctxMap)
	{
		ServicesUtil.validateParameterNotNull(items, "items cannot be null");

		return items.stream().map(item -> relatedItemsCollector.collect(item, getCtxWithMaxRecursionDepth(ctxMap)))
				.flatMap(Collection::stream).collect(Collectors.toList());
	}

	@Override
	public boolean canSync(final SyncItemJobModel sync)
	{
		final UserModel currentUser = userService.getCurrentUser();

		if (userService.isAdmin(currentUser))
		{
			return true;
		}
		else if (CollectionUtils.isNotEmpty(sync.getSyncPrincipals()))
		{
			if (sync.getSyncPrincipals().contains(currentUser)
					|| CollectionUtils.containsAny(sync.getSyncPrincipals(), currentUser.getAllGroups()))
			{
				return true;
			}
			else if (BooleanUtils.isTrue(sync.getSyncPrincipalsOnly()))
			{
				return false;
			}
		}

		return catalogVersionService.canWrite(sync.getTargetVersion(), currentUser)
				&& catalogVersionService.canRead(sync.getSourceVersion(), currentUser);
	}

	protected Map<String, Object> getCtxWithMaxRecursionDepth(final Map<String, Object> ctx)
	{
		if (ctx.containsKey(RelatedItemsCollector.MAX_RECURSION_DEPTH))
		{
			return ctx;
		}
		else
		{
			final int maxDepth = Config.getInt("backoffice.sync.related.items.collector.maxRecursionDepth", 10);
			final Map<String, Object> newCtx = new HashMap<>(ctx);
			newCtx.put(RelatedItemsCollector.MAX_RECURSION_DEPTH, Integer.valueOf(maxDepth));
			return newCtx;
		}
	}

	/**
	 * Prepares sync config. If given sync config is null then then it will be created using
	 * {@link #createSyncConfigWithDefaults(SyncItemJobModel)}
	 *
	 * @param syncConfig
	 *           sync config from sync task {@link SyncTask#getSyncConfig()};
	 * @param syncJob
	 *           sync job to be executed.
	 * @return sync config
	 */
	protected SyncConfig prepareSyncConfig(final SyncConfig syncConfig, final SyncItemJobModel syncJob)
	{
		return syncConfig != null ? syncConfig : createSyncConfigWithDefaults(syncJob);
	}

	/**
	 * Creates sync config based on project properties and syncJob settings. If one of the settings is not defined in
	 * project properties then default from given syncJob is taken
	 *
	 * @param syncJob
	 *           sync job to be executed.
	 * @return sync config with default values
	 */
	protected SyncConfig createSyncConfigWithDefaults(final SyncItemJobModel syncJob)
	{
		final SyncConfig syncConfig = new SyncConfig();
		syncConfig.setCreateSavedValues(getBooleanFromSystemConfig(SYNC_CONFIG_CREATE_SAVED_VALUES, Boolean.FALSE));
		syncConfig.setForceUpdate(getBooleanFromSystemConfig(SYNC_CONFIG_FORCE_UPDATE, Boolean.TRUE));
		syncConfig.setSynchronous(getBooleanFromSystemConfig(SYNC_CONFIG_SYNCHRONOUS, Boolean.FALSE));
		syncConfig.setAbortWhenCollidingSyncIsRunning(
				getBooleanFromSystemConfig(BACKOFFICE_SYNC_CONFIG_ABORT_ON_COLLIDING, Boolean.TRUE).booleanValue());

		syncConfig.setLogToFile(getBooleanFromSystemConfig(SYNC_CONFIG_LOG_TO_FILE, syncJob.getLogToFile()));
		syncConfig.setLogToDatabase(getBooleanFromSystemConfig(SYNC_CONFIG_LOG_TO_DATABASE, syncJob.getLogToDatabase()));
		syncConfig.setKeepCronJob(getBooleanFromSystemConfig(SYNC_CONFIG_KEEP_CRON_JOB, syncJob.getRemoveOnExit()));

		final JobLogLevel syncConfigLogLevelDatabase = getEnumValueFromSystemConfig(JobLogLevel.class,
				SYNC_CONFIG_LOG_LEVEL_DATABASE, syncJob.getLogLevelDatabase());
		syncConfig.setLogLevelDatabase(syncConfigLogLevelDatabase != null ? syncConfigLogLevelDatabase : JobLogLevel.WARNING);
		final JobLogLevel syncConfigLogLevelFile = getEnumValueFromSystemConfig(JobLogLevel.class, SYNC_CONFIG_LOG_LEVEL_FILE,
				syncJob.getLogLevelFile());
		syncConfig.setLogLevelFile(syncConfigLogLevelFile != null ? syncConfigLogLevelFile : JobLogLevel.INFO);
		final ErrorMode syncConfigErrorMode = getEnumValueFromSystemConfig(ErrorMode.class, SYNC_CONFIG_ERROR_MODE,
				syncJob.getErrorMode());
		syncConfig.setErrorMode(syncConfigErrorMode != null ? syncConfigErrorMode : ErrorMode.FAIL);
		return syncConfig;
	}

	protected Boolean getBooleanFromSystemConfig(final String property, final Boolean defaultValue)
	{
		return Boolean.valueOf(Config.getBoolean(property, defaultValue.booleanValue()));
	}

	protected <T extends Enum> T getEnumValueFromSystemConfig(final Class<T> enumType, final String property, final T defaultValue)
	{
		try
		{
			final String value = getConfigValue(property, "");
			if (StringUtils.isNotEmpty(value))
			{
				return (T) Enum.valueOf(enumType, value);
			}
		}
		catch (final IllegalArgumentException ie)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Cannot get enum value from config", ie);
			}
			else
			{
				LOG.warn("Cannot get enum value {} from config, default value {} will be used", property, defaultValue);
			}
		}
		return defaultValue;
	}

	protected String getConfigValue(final String property, final String defaultValue)
	{
		return Config.getString(property, defaultValue);
	}

	public CatalogSynchronizationService getCatalogSynchronizationService()
	{
		return catalogSynchronizationService;
	}

	@Required
	public void setCatalogSynchronizationService(final CatalogSynchronizationService catalogSynchronizationService)
	{
		this.catalogSynchronizationService = catalogSynchronizationService;
	}

	public SynchronizationStatusService getSynchronizationStatusService()
	{
		return synchronizationStatusService;
	}

	@Required
	public void setSynchronizationStatusService(final SynchronizationStatusService synchronizationStatusService)
	{
		this.synchronizationStatusService = synchronizationStatusService;
	}

	protected SyncJobApplicableTypesStrategy getSyncJobApplicableTypesStrategy()
	{
		return syncJobApplicableTypesStrategy;
	}

	@Required
	public void setSyncJobApplicableTypesStrategy(final SyncJobApplicableTypesStrategy syncJobApplicableTypesStrategy)
	{
		this.syncJobApplicableTypesStrategy = syncJobApplicableTypesStrategy;
	}

	public CatalogTypeService getCatalogTypeService()
	{
		return catalogTypeService;
	}

	@Required
	public void setCatalogTypeService(final CatalogTypeService catalogTypeService)
	{
		this.catalogTypeService = catalogTypeService;
	}

	public RelatedItemsCollector getRelatedItemsCollector()
	{
		return relatedItemsCollector;
	}

	@Required
	public void setRelatedItemsCollector(final RelatedItemsCollector relatedItemsCollector)
	{
		this.relatedItemsCollector = relatedItemsCollector;
	}

	public CronJobService getCronJobService()
	{
		return cronJobService;
	}

	@Required
	public void setCronJobService(final CronJobService cronJobService)
	{
		this.cronJobService = cronJobService;
	}

	public ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	public MediaService getMediaService()
	{
		return mediaService;
	}

	@Required
	public void setMediaService(final MediaService mediaService)
	{
		this.mediaService = mediaService;
	}

	public SessionService getSessionService()
	{
		return sessionService;
	}

	@Required
	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
	}

	public CatalogVersionService getCatalogVersionService()
	{
		return catalogVersionService;
	}

	@Required
	public void setCatalogVersionService(final CatalogVersionService catalogVersionService)
	{
		this.catalogVersionService = catalogVersionService;
	}

	public UserService getUserService()
	{
		return userService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}
}
