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
package com.hybris.backoffice.config.impl;

import com.hybris.backoffice.daos.BackofficeConfigurationDao;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.Registry;
import de.hybris.platform.core.Tenant;
import de.hybris.platform.core.model.media.MediaFolderModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.jalo.media.MediaManager;
import de.hybris.platform.media.storage.MediaStorageConfigService;
import de.hybris.platform.search.restriction.SearchRestrictionService;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.impex.ImpExResource;
import de.hybris.platform.servicelayer.impex.ImportConfig;
import de.hybris.platform.servicelayer.impex.ImportService;
import de.hybris.platform.servicelayer.impex.impl.ClasspathImpExResource;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.util.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;
import java.util.function.Consumer;


public class DefaultBackofficeConfigurationMediaHelper implements BackofficeConfigurationMediaHelper
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultBackofficeConfigurationMediaHelper.class);

	protected static final String PROPERTY_BACKOFFICE_CONFIGURATION_SECURE_MEDIA_FOLDER = "backofficeconfiguration";
	private static final Consumer<MediaModel> NOOP = o -> {
		/* do nothing */};
	private static final String DEFAULT_MEDIA_CATALOG_ID = "_boconfig";
	private static final String DEFAULT_MEDIA_CATALOG_VERSION = "hidden";

	private SessionService sessionService;
	private MediaService mediaService;
	private ModelService modelService;
	private SearchRestrictionService searchRestrictionService;
	private BackofficeConfigurationDao configurationDao;
	private MediaStorageConfigService mediaStorageConfigService;
	private UserService userService;
	private CatalogVersionService catalogVersionService;
	private String mediaCatalogId = DEFAULT_MEDIA_CATALOG_ID;
	private String mediaCatalogVersion = DEFAULT_MEDIA_CATALOG_VERSION;
	private ImportService importService;

	protected void handleBackofficeConfigurationMedia()
	{
		if (isSystemInitialized())
		{
			try
			{
				final boolean impexNeeded = checkIfImpexNeeded();

				if (impexNeeded)
				{
					final ImpExResource impExResource = new ClasspathImpExResource("/impex/essentialdataBackofficeMediaCatalog.impex", "UTF-8");
					final ImportConfig importConfig = new ImportConfig();
					importConfig.setScript(impExResource);
					getImportService().importData(importConfig);
				}
			}
			catch (final RuntimeException e)
			{
				throw new IllegalStateException("Check backoffice configuration media. " +
						"Try to manually remove all media of codes: 'cockpitng-config', 'cockpitng-widgets-config'", e);
			}
		}
	}

	protected boolean isSystemInitialized()
	{
		final Tenant tenant = Registry.getCurrentTenantNoFallback();
		return (tenant != null && Utilities.isSystemInitialized(tenant.getDataSource()));
	}

	protected boolean checkIfImpexNeeded()
	{
		return !catalogVersionExists();
	}

	protected boolean catalogVersionExists()
	{
		try
		{
			getCatalogVersionService().getCatalogVersion(getMediaCatalogId(), getMediaCatalogVersion());
			return true;
		}
		catch (final UnknownIdentifierException e)
		{
			return false;
		}
	}

	@Override
	public MediaModel findOrCreateWidgetsConfigMedia(final String mediaCode, final String mediaMime,
			final Consumer<MediaModel> newMediaInitializer)
	{
		MediaModel widgetsConfigMedia = findWidgetsConfigMedia(mediaCode);
		if (widgetsConfigMedia == null)
		{
			widgetsConfigMedia = createWidgetsConfigMedia(mediaCode, mediaMime);
			newMediaInitializer.accept(widgetsConfigMedia);

		}

		if (widgetsConfigMedia != null)
		{
			assureSecureFolderAssignment(widgetsConfigMedia);
		}

		return widgetsConfigMedia;
	}

	@Override
	public MediaModel findOrCreateWidgetsConfigMedia(final String mediaCode, final String mediaMime)
	{
		return findOrCreateWidgetsConfigMedia(mediaCode, mediaMime, NOOP);
	}

	@Override
	public MediaModel findWidgetsConfigMedia(final String mediaCode)
	{
		MediaModel mediaModel = null;
		try
		{
			mediaModel = findMediaByCode(mediaCode);
		}
		catch (final AmbiguousIdentifierException ambiguousIdentifierException)
		{
			removeAmbiguousConfiguration(mediaCode);
		}
		catch (final UnknownIdentifierException ignore)
		{
			// ignore this exception
		}
		return mediaModel;
	}

	protected MediaModel findMediaByCode(final String mediaCode)
	{
		return getSessionService().executeInLocalView(new SessionExecutionBody()
		{
			@Override
			public Object execute()
			{
				try
				{
					getSearchRestrictionService().disableSearchRestrictions();
					final CatalogVersionModel backofficeMediaCatalog = findBackofficeMediaCatalog();
					return getMediaService().getMedia(backofficeMediaCatalog, mediaCode);

				}
				finally
				{
					getSearchRestrictionService().enableSearchRestrictions();
				}
			}
		}, getUserService().getAdminUser());
	}

	@Override
	public MediaModel createWidgetsConfigMedia(final String mediaCode, final String mediaMime)
	{
		final CatalogVersionModel boMediaActiveCatalogVersion = findBackofficeMediaCatalog();

		MediaModel media = modelService.create(MediaModel.class);
		media.setCatalogVersion(boMediaActiveCatalogVersion);
		media.setCode(mediaCode);
		media.setMime(mediaMime);
		media.setFolder(getSecureFolder());
		try
		{
			modelService.save(media);
			LOG.info("{} media created", mediaCode);
		}
		catch (final ModelSavingException ex)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Configuration media was not saved", ex);
			}

			LOG.warn("Cannot create {} media, probably it was created on other node", mediaCode);

			Registry.getCurrentTenantNoFallback().getCache().clear();//clear cache
			media = findMediaByCode(mediaCode);
		}
		return media;
	}

	protected CatalogVersionModel findBackofficeMediaCatalog()
	{
		return getSessionService().executeInLocalView(new SessionExecutionBody()
		{
			@Override
			public Object execute()
			{
				return getCatalogVersionService().getCatalogVersion(getMediaCatalogId(), getMediaCatalogVersion());
			}
		}, getUserService().getAdminUser());
	}

	protected void removeAmbiguousConfiguration(final String mediaCode)
	{
		final Collection<MediaModel> existingConfiguration = configurationDao.findMedias(mediaCode);
		modelService.removeAll(existingConfiguration);
		LOG.warn("Multiple medias for {} existed. Ambiguous configuration has been removed.", mediaCode);
	}

	@Override
	public MediaFolderModel getSecureFolder()
	{
		try
		{
			return loadSecureFolder();
		}
		catch (final UnknownIdentifierException aie)
		{
			return createSecureFolder();
		}
	}

	protected MediaFolderModel loadSecureFolder()
	{
		return getMediaService().getFolder(PROPERTY_BACKOFFICE_CONFIGURATION_SECURE_MEDIA_FOLDER);
	}

	protected MediaFolderModel createSecureFolder()
	{
		final MediaFolderModel folder = getModelService().create(MediaFolderModel.class);
		folder.setPath(PROPERTY_BACKOFFICE_CONFIGURATION_SECURE_MEDIA_FOLDER);
		folder.setQualifier(PROPERTY_BACKOFFICE_CONFIGURATION_SECURE_MEDIA_FOLDER);
		try
		{
			getModelService().save(folder);
			return folder;
		}
		catch (final ModelSavingException modelSavingException)
		{
			LOG.info(String.format("Cannot create %s media folder, probably it was created on other node; re-trying read.",
					PROPERTY_BACKOFFICE_CONFIGURATION_SECURE_MEDIA_FOLDER), modelSavingException);
			try
			{
				return loadSecureFolder();
			}
			catch (final UnknownIdentifierException | AmbiguousIdentifierException ex)
			{
				throw new IllegalStateException("Cannot create media folder, probably it was created on other node.", ex);
			}
		}
	}


	/**
	 * The method assures secure media folder assignment of the given media. The following scenarios are covered:
	 * <ul>
	 * <li>if the media is not assigned to a folder or it is assigned to the root folder it will be assigned to the standard
	 * one and persisted if the {@code persistChanged} argument is set to true</li>
	 * <li>if the media is assigned to a folder which is secure (see
	 * {@link de.hybris.platform.media.storage.MediaStorageConfigService}) nothing will be done</li>
	 * <li>if the media is assigned to a folder which is not secure (see
	 * {@link de.hybris.platform.media.storage.MediaStorageConfigService}) {@link IllegalStateException} will be thrown</li>
	 * </ul>
	 * In case of a race condition, where two subsequent saves happen and the method fails to save the folder, it re-tries
	 * the fetch and return before it finally fails.
	 *
	 * @param mediaModel
	 *           media to checked for secure folder assignment
	 * @throws IllegalStateException
	 *            if the media is assigned to a non-secure folder
	 * @throws IllegalArgumentException
	 *            if the passed media is {@code null}
	 * @see de.hybris.platform.media.storage.MediaStorageConfigService
	 * @see #assureSecureFolderAssignment(MediaModel)
	 * @see MediaManager#ROOT_FOLDER_QUALIFIER
	 */
	protected void assureSecureFolderAssignment(final MediaModel mediaModel)
	{
		if (mediaModel == null)
		{
			throw new IllegalArgumentException("Cannot check secure folder assignment for null media");
		}
		final MediaFolderModel folder = mediaModel.getFolder();
		if (folder == null || MediaManager.ROOT_FOLDER_QUALIFIER.equals(folder.getQualifier()))
		{
			final MediaFolderModel secureFolder = getSecureFolder();
			mediaModel.setFolder(secureFolder);

			if (LOG.isInfoEnabled())
			{
				LOG.info(String.format("Assigned media '%s' to secure folder: '%s'.", mediaModel.getCode(),
						secureFolder.getQualifier()));
			}

			try
			{
				getModelService().save(mediaModel);
			}
			catch (final ModelSavingException mse)
			{
				getModelService().refresh(mediaModel);
				final MediaFolderModel refreshedMediaFolder = mediaModel.getFolder();
				if (refreshedMediaFolder == null)
				{
					throw mse;
				}
				else
				{
					failOnInsecureFolderAssignment(refreshedMediaFolder.getQualifier());
				}
			}
		}
		else
		{
			failOnInsecureFolderAssignment(folder.getQualifier());
		}
	}

	protected void failOnInsecureFolderAssignment(final String folderQualifier)
	{
		final Collection<String> securedFolders = getMediaStorageConfigService().getSecuredFolders();
		if (securedFolders == null || !securedFolders.contains(folderQualifier))
		{
			throw new IllegalStateException(
					String.format("Given media is assigned to '%s' folder which is not secured.", folderQualifier));
		}
	}

	protected SessionService getSessionService()
	{
		return sessionService;
	}

	@Required
	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
	}

	protected SearchRestrictionService getSearchRestrictionService()
	{
		return searchRestrictionService;
	}

	@Required
	public void setSearchRestrictionService(final SearchRestrictionService searchRestrictionService)
	{
		this.searchRestrictionService = searchRestrictionService;
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

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	protected BackofficeConfigurationDao getConfigurationDao()
	{
		return configurationDao;
	}

	@Required
	public void setConfigurationDao(final BackofficeConfigurationDao configurationDao)
	{
		this.configurationDao = configurationDao;
	}

	public MediaStorageConfigService getMediaStorageConfigService()
	{
		return mediaStorageConfigService;
	}

	@Required
	public void setMediaStorageConfigService(final MediaStorageConfigService mediaStorageConfigService)
	{
		this.mediaStorageConfigService = mediaStorageConfigService;
	}

	protected UserService getUserService()
	{
		return userService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	protected CatalogVersionService getCatalogVersionService()
	{
		return catalogVersionService;
	}

	@Required
	public void setCatalogVersionService(final CatalogVersionService catalogVersionService)
	{
		this.catalogVersionService = catalogVersionService;
	}

	public String getMediaCatalogId()
	{
		return mediaCatalogId;
	}

	public void setMediaCatalogId(final String mediaCatalogId)
	{
		this.mediaCatalogId = mediaCatalogId;
	}

	public String getMediaCatalogVersion()
	{
		return mediaCatalogVersion;
	}

	public void setMediaCatalogVersion(final String mediaCatalogVersion)
	{
		this.mediaCatalogVersion = mediaCatalogVersion;
	}

	protected ImportService getImportService()
	{
		return importService;
	}

	@Required
	public void setImportService(final ImportService importService)
	{
		this.importService = importService;
	}
}
