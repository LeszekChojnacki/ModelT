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
package de.hybris.platform.ruleengine.cronjob;

import static com.google.common.collect.ImmutableMap.of;

import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.jobs.maintenance.MaintenanceCleanupStrategy;
import de.hybris.platform.ruleengine.model.DroolsKIEModuleMediaModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.internal.model.MaintenanceCleanupJobModel;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Required;


/**
 * A Cleanup Job Strategy to search and clean up old serialized KieModules.
 */
public class DefaultKieModuleCleanupStrategy implements MaintenanceCleanupStrategy<DroolsKIEModuleMediaModel, CronJobModel>
{
	public static final String LAST_VERSIONS_OF_KIE_MODULES_TO_KEEP = "kieModuleCleanupStrategy.lastVersionsOfKieModulesToKeep";

	private static final int DEFAULT_LAST_VERSIONS_OF_KIE_MODULES_TO_KEEP = 3;
	private static final String FIND_OLD_KIE_MODULES = "select {old_kie_module_medias." + DroolsKIEModuleMediaModel.PK
			+ "} from {"
			+ DroolsKIEModuleMediaModel._TYPECODE + " as old_kie_module_medias} where {old_kie_module_medias."
			+ DroolsKIEModuleMediaModel.PK
			+ "} not in ({{select {recent_kie_module_medias." + DroolsKIEModuleMediaModel.PK + "} from {"
			+ DroolsKIEModuleMediaModel._TYPECODE
			+ " as recent_kie_module_medias} where ({{select count(*) from {" + DroolsKIEModuleMediaModel._TYPECODE
			+ " as counted_kie_module_medias} where {recent_kie_module_medias."
			+ DroolsKIEModuleMediaModel.KIEMODULENAME + "}={counted_kie_module_medias." + DroolsKIEModuleMediaModel.KIEMODULENAME
			+ "} and {counted_kie_module_medias."
			+ DroolsKIEModuleMediaModel.PK + "} >= {recent_kie_module_medias." + DroolsKIEModuleMediaModel.PK + "}}}) <= ?last }})";


	private ModelService modelService;
	private MediaService mediaService;
	private ConfigurationService configurationService;

	@Override
	public FlexibleSearchQuery createFetchQuery(final CronJobModel cronJob)
	{
		if (cronJob.getJob() instanceof MaintenanceCleanupJobModel)
		{
			final MaintenanceCleanupJobModel job = (MaintenanceCleanupJobModel) cronJob.getJob();
			final Map<String, Object> queryParams = of("last", getNumberOfLastVersionsOfKieModuleToKeep(job));
			// the request assumes PKs are ordered according to the record insertions order
			return new FlexibleSearchQuery(FIND_OLD_KIE_MODULES, queryParams);
		}
		throw new IllegalStateException("The job was not a MaintenanceCleanupJob");
	}

	protected int getNumberOfLastVersionsOfKieModuleToKeep(final MaintenanceCleanupJobModel maintenanceCleanupJob)
	{
		return maintenanceCleanupJob.getThreshold() != null ? maintenanceCleanupJob.getThreshold() : getConfigurationService()
				.getConfiguration().getInt(
						LAST_VERSIONS_OF_KIE_MODULES_TO_KEEP, DEFAULT_LAST_VERSIONS_OF_KIE_MODULES_TO_KEEP);
	}

	@Override
	public void process(final List<DroolsKIEModuleMediaModel> elements)
	{
		elements.forEach(kieModule -> getMediaService().removeDataFromMediaQuietly(kieModule));
		getModelService().removeAll(elements);
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	protected MediaService getMediaService()
	{
		return mediaService;
	}

	@Required
	public void setMediaService(final MediaService mediaService)
	{
		this.mediaService = mediaService;
	}

	protected ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	@Required
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}
}
