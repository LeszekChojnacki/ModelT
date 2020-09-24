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
package com.hybris.backoffice.solrsearch.setup.impl;

import de.hybris.platform.core.initialization.SystemSetup;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.cronjob.CronJobService;
import de.hybris.platform.servicelayer.exceptions.SystemException;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.impex.ImportConfig;
import de.hybris.platform.servicelayer.impex.ImportResult;
import de.hybris.platform.servicelayer.impex.ImportService;
import de.hybris.platform.servicelayer.model.ModelService;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hybris.backoffice.solrsearch.constants.BackofficesolrsearchConstants;
import com.hybris.backoffice.solrsearch.setup.BackofficeSolrSearchImpexImportSystemSetup;
import com.hybris.backoffice.solrsearch.setup.BackofficeSolrSearchSystemSetupConfig;


@SystemSetup(extension = BackofficesolrsearchConstants.EXTENSIONNAME)
public class DefaultBackofficeSolrSearchImpexImportSystemSetup implements BackofficeSolrSearchImpexImportSystemSetup
{

	private static final Logger LOG = LoggerFactory.getLogger(DefaultBackofficeSolrSearchImpexImportSystemSetup.class);

	private static final String RESOURCE_NOT_FOUND = "Resource {} not found";
	private static final String ERROR_IMPORTING_FILE = "Error importing {} file";
	private static final String FILE_IMPORTED = "Impex file {} imported";
	private static final String DOT = ".";
	private static final String BAD_URI = "Bad URI";
	private static final String IMPEX_EXTENSION = "impex";
	private static final String INDEX_UPDATING_CRONJOB_NAME = "update-backofficeIndex-CronJob";
	private static final String PROPERTY_CRONJOB_NODE_GROUP = "backofficesearch.cronjob.nodegroup";

	private final ImportService importService;
	private final CommonI18NService commonI18NService;
	private final ModelService modelService;
	private final CronJobService cronJobService;
	private final ConfigurationService configurationService;
	private final BackofficeSolrSearchSystemSetupConfig config;
	private final FileBasedImpExResourceFactory fileBasedImpExResourceFactory;


	public DefaultBackofficeSolrSearchImpexImportSystemSetup(final ImportService importService,
			final CommonI18NService commonI18NService, final ModelService modelService, final CronJobService cronJobService,
			final ConfigurationService configurationService,
			final BackofficeSolrSearchSystemSetupConfig backofficeSolrSearchSystemSetupConfig,
			final FileBasedImpExResourceFactory fileBasedImpExResourceFactory)
	{
		this.importService = importService;
		this.commonI18NService = commonI18NService;
		this.modelService = modelService;
		this.cronJobService = cronJobService;
		this.configurationService = configurationService;
		this.config = backofficeSolrSearchSystemSetupConfig;
		this.fileBasedImpExResourceFactory = fileBasedImpExResourceFactory;
	}

	/**
	 * Imports configured (project.properties) impex files, and localized impex files during system initialization and
	 * system update
	 */
	@Override
	@SystemSetup(type = SystemSetup.Type.PROJECT, process = SystemSetup.Process.ALL)
	public void importImpex()
	{
		final Collection<String> nonLocalizedRoots = getConfig().getNonLocalizedRootNames();
		nonLocalizedRoots.forEach(this::importImpexFileWithLogging);
		final Collection<String> roots = getConfig().getLocalizedRootNames();
		roots.forEach(this::importLocalizedImpexFiles);
		adjustIndexUpdatingCronjob();
	}

	protected void importImpexFileWithLogging(final String filePath)
	{
		tryToImportImpexFile(filePath).ifPresent(ir -> {
			if (ir.isError())
			{
				LOG.error(ERROR_IMPORTING_FILE, filePath);
				return;
			}
			LOG.info(FILE_IMPORTED, filePath);
		});
	}

	protected Optional<ImportResult> tryToImportImpexFile(final String filePath)
	{
		final ImportConfig importConfig = new ImportConfig();
		final URL resource = getClass().getResource(filePath);
		if (resource == null)
		{
			LOG.debug(RESOURCE_NOT_FOUND, filePath);
			return Optional.empty();
		}
		final File importedFile;
		try
		{
			importedFile = new File(resource.toURI());
		}
		catch (final URISyntaxException e)
		{
			LOG.error(BAD_URI, e);
			return Optional.empty();
		}
		importConfig.setScript(getFileBasedImpExResourceFactory().createFileBasedImpExResource(importedFile,
				getConfig().getFileEncoding()));
		return Optional.ofNullable(getImportService().importData(importConfig));
	}

	protected void importLocalizedImpexFiles(final String rootPath)
	{
		final List<LanguageModel> allLanguages = getCommonI18NService().getAllLanguages();

		for (final LanguageModel language : allLanguages)
		{
			final String filePath = resolveLocalizedFilePath(rootPath, language);
			importImpexFileWithLogging(filePath);
		}
	}

	protected String resolveLocalizedFilePath(final String root, final LanguageModel languageModel)
	{
		final StringBuilder sb = new StringBuilder(root);
		sb.append(getConfig().getRootNameLanguageSeparator()).append(languageModel.getIsocode()).append(DOT)
				.append(IMPEX_EXTENSION);
		return sb.toString();
	}

	protected void adjustIndexUpdatingCronjob()
	{
		try
		{
			final CronJobModel cronJob = cronJobService.getCronJob(INDEX_UPDATING_CRONJOB_NAME);
			final String nodeGroup = configurationService.getConfiguration().getString(PROPERTY_CRONJOB_NODE_GROUP,
					StringUtils.EMPTY);
			if (cronJob != null && StringUtils.isNotBlank(nodeGroup))
			{
				cronJob.setNodeGroup(nodeGroup);
				modelService.save(cronJob);
			}
		}
		catch (final SystemException e)
		{
			LOG.warn("Error adjusting update-backofficeIndex-CronJob", e);
		}
	}

	protected ImportService getImportService()
	{
		return importService;
	}

	protected CommonI18NService getCommonI18NService()
	{
		return commonI18NService;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	protected CronJobService getCronJobService()
	{
		return cronJobService;
	}

	protected ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	protected BackofficeSolrSearchSystemSetupConfig getConfig()
	{
		return config;
	}

	protected FileBasedImpExResourceFactory getFileBasedImpExResourceFactory()
	{
		return fileBasedImpExResourceFactory;
	}

}
