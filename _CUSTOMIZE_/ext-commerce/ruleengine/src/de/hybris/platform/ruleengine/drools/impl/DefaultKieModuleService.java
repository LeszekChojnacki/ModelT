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
package de.hybris.platform.ruleengine.drools.impl;

import de.hybris.platform.core.model.media.MediaFolderModel;
import de.hybris.platform.ruleengine.dao.DroolsKIEModuleMediaDao;
import de.hybris.platform.ruleengine.drools.KieModuleService;
import de.hybris.platform.ruleengine.model.DroolsKIEModuleMediaModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.drools.compiler.compiler.io.File;
import org.drools.compiler.compiler.io.Folder;
import org.drools.compiler.compiler.io.Resource;
import org.drools.compiler.compiler.io.memory.MemoryFileSystem;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.compiler.kie.builder.impl.MemoryKieModule;
import org.drools.compiler.kproject.ReleaseIdImpl;
import org.drools.compiler.kproject.models.KieModuleModelImpl;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.model.KieModuleModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;

import static de.hybris.platform.ruleengine.constants.RuleEngineConstants.KIE_MODULE_MEDIA_FOLDER_QUALIFIER;
import static de.hybris.platform.ruleengine.constants.RuleEngineConstants.KIE_MODULE_MEDIA_FOLDER_QUALIFIER_DEFAULT_VALUE;
import static de.hybris.platform.ruleengine.util.JarValidator.validateZipSlipSecure;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;
import static java.util.Optional.empty;


/**
 * Default implementation of {@link KieModuleService}
 */
public class DefaultKieModuleService implements KieModuleService
{
	private static final String META_INF_KMODULE_XML = "META-INF/kmodule.xml";

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultKieModuleService.class);

	private DroolsKIEModuleMediaDao droolsKIEModuleMediaDao;
	private ModelService modelService;
	private MediaService mediaService;
	private ConfigurationService configurationService;
	private boolean useCMC = true;

	@Override
	public void saveKieModule(final String kieModuleName, final String releaseId, final KieModule kieModule)
	{

		if (!isUseCMC())
		{
			LOGGER.debug("centralized module compilation disabled, skipping saveKieModule()");
			return;
		}
		validateParameterNotNull(kieModuleName, "kieModuleName can't be null");
		validateParameterNotNull(releaseId, "releaseId can't be null");
		validateParameterNotNull(kieModule, "kieModule can't be null");

		final Optional<DroolsKIEModuleMediaModel> droolsKIEModuleMediaOptional = getKieModuleMedia(kieModuleName, releaseId);
		if (!droolsKIEModuleMediaOptional.isPresent())
		{
			LOGGER.debug("KieModule for name '{}' and releaseId '{}' not found and is valid to be stored", kieModuleName, releaseId);
			final DroolsKIEModuleMediaModel kieModuleMedia = createKieModuleMedia(kieModuleName, releaseId);
			getModelService().save(kieModuleMedia);
			getMediaService()
					.setStreamForMedia(kieModuleMedia, new ByteArrayInputStream(((InternalKieModule) kieModule).getBytes()));

			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("save KieModule with name '{}' and releaseId '{}'", kieModuleName, releaseId);
				if (kieModule instanceof MemoryKieModule)
				{
					debugLogFSFolder(((MemoryKieModule) kieModule).getMemoryFileSystem().getRootFolder(), "");
				}
				debugGenerateKjar(kieModuleName, releaseId, kieModule);
			}
		}
		else
		{
			LOGGER.debug("Stored instance of KieModule for name '{}' and releaseId '{}' already exists", kieModuleName, releaseId);
		}
	}

	protected DroolsKIEModuleMediaModel createKieModuleMedia(final String kieModuleName, final String releaseId)
	{
		final DroolsKIEModuleMediaModel droolsKIEModuleMedia = modelService.create(DroolsKIEModuleMediaModel.class);
		droolsKIEModuleMedia.setCode(generateMediaCode(kieModuleName, releaseId));
		droolsKIEModuleMedia.setFolder(getMediaFolder());
		droolsKIEModuleMedia.setKieModuleName(kieModuleName);
		droolsKIEModuleMedia.setReleaseId(releaseId);
		return droolsKIEModuleMedia;
	}

	protected MediaFolderModel getMediaFolder()
	{
		final String mediaFolderQualifier = getConfigurationService().getConfiguration().getString(
				KIE_MODULE_MEDIA_FOLDER_QUALIFIER, KIE_MODULE_MEDIA_FOLDER_QUALIFIER_DEFAULT_VALUE);

		return getMediaService().getFolder(mediaFolderQualifier);
	}

	protected void debugGenerateKjar(final String kieModuleName, final String releaseId, final KieModule kieModule)
	{
		if (!LOGGER.isDebugEnabled())
		{
			return;
		}
		try
		{
			FileUtils
					.writeByteArrayToFile(
							new java.io.File(String.format("%s_%s.jar", kieModuleName,
									releaseId.replaceAll(":", "-").replaceAll("\\.", "_"))), ((InternalKieModule) kieModule).getBytes());
		}
		catch (final IOException e)
		{
			LOGGER.error("error during debugGenerateKjar()", e);
		}
	}

	protected void debugLogFSFolder(final Folder folder, final String tab)
	{
		if (!LOGGER.isDebugEnabled())
		{
			return;
		}
		LOGGER.debug("{}{}", tab, folder.getName());
		for (final Resource r : folder.getMembers())
		{
			if (r instanceof File)
			{
				try
				{
					LOGGER.debug("{}{} {} bytes", tab, ((File) r).getName(), IOUtils.toByteArray(((File) r).getContents()).length);
				}
				catch (final IOException e)
				{
					LOGGER.error("error during debugLogFSFolder()", e);
				}
			}
		}
		for (final Resource r : folder.getMembers())
		{
			if (r instanceof Folder)
			{
				debugLogFSFolder((Folder) r, tab + "    ");
			}
		}
	}

	protected String generateMediaCode(final String kieModuleName, final String releaseId)
	{
		return String.format("%s:%s", kieModuleName, releaseId);
	}

	@Override
	public Optional<KieModule> loadKieModule(final String kieModuleName, final String releaseId)
	{
		if (!isUseCMC())
		{
			LOGGER.debug("centralized module compilation disabled, skipping loadKieModule()");
			return empty();
		}
		validateParameterNotNull(kieModuleName, "kieModuleName can't be null");
		validateParameterNotNull(releaseId, "releaseId can't be null");

		final Optional<DroolsKIEModuleMediaModel> droolsKIEModuleMediaOptional = getKieModuleMedia(kieModuleName, releaseId);
		if (droolsKIEModuleMediaOptional.isPresent())
		{
			try
			{
				validateZipSlipSecure(getMediaService().getStreamFromMedia(droolsKIEModuleMediaOptional.get()));

				final MemoryFileSystem memoryFileSystem = MemoryFileSystem.readFromJar(
						getMediaService().getStreamFromMedia(droolsKIEModuleMediaOptional.get()));

				final File kModuleFile = memoryFileSystem.getFile(META_INF_KMODULE_XML);
				if (!kModuleFile.exists())
				{
					LOGGER.warn("{} is absent for kjar of the KieModule (kieModuleName = '{}', releaseId = '{}')",
							META_INF_KMODULE_XML, kieModuleName, releaseId);
					return empty();
				}
				final KieModuleModel kieModuleModel = KieModuleModelImpl.fromXML(kModuleFile.getContents());
				final KieModule newKieModule = new MemoryKieModule(new ReleaseIdImpl(releaseId), kieModuleModel, memoryFileSystem);
				memoryFileSystem.mark();
				return Optional.of(newKieModule);
			}
			catch (final Exception e)
			{
				LOGGER.error("Can't read serialized KieModule (kieModuleName = '{}', releaseId = '{}')", kieModuleName, releaseId);
				LOGGER.error("exception caught.", e);
				return empty();
			}
		}
		return empty();
	}

	protected Optional<DroolsKIEModuleMediaModel> getKieModuleMedia(final String kieModuleName, final String releaseId)
	{
		return getDroolsKIEModuleMediaDao().findKIEModuleMedia(kieModuleName, releaseId);
	}

	protected DroolsKIEModuleMediaDao getDroolsKIEModuleMediaDao()
	{
		return droolsKIEModuleMediaDao;
	}

	@Required
	public void setDroolsKIEModuleMediaDao(final DroolsKIEModuleMediaDao droolsKIEModuleMediaDao)
	{
		this.droolsKIEModuleMediaDao = droolsKIEModuleMediaDao;
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

	protected boolean isUseCMC()
	{
		return useCMC;
	}

	@Required
	public void setUseCMC(final boolean useCMC)
	{
		this.useCMC = useCMC;
	}
}
