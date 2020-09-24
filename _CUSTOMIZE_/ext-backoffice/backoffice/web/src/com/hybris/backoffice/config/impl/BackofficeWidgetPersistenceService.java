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

import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.servicelayer.media.MediaService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Sets;
import com.hybris.cockpitng.core.Widget;
import com.hybris.cockpitng.core.persistence.impl.jaxb.WidgetConnection;
import com.hybris.cockpitng.core.persistence.impl.jaxb.WidgetExtension;
import com.hybris.cockpitng.core.persistence.impl.jaxb.Widgets;
import com.hybris.cockpitng.core.util.ClassLoaderUtils;
import com.hybris.cockpitng.core.util.impl.WidgetTreeUtils;
import com.hybris.cockpitng.modules.persistence.impl.XmlModuleAwarePersistenceService;



/**
 * Backoffice extension of the {@link XmlModuleAwarePersistenceService}. The implementation uses {@link MediaModel} to
 * persist the xml content.
 */
public class BackofficeWidgetPersistenceService extends XmlModuleAwarePersistenceService
{
	public static final String ERROR_PROCESSING_WIDGETS_CONFIG = "Error occurred while processing widgets configuration";
	protected static final String WIDGET_CONFIG_MEDIA = "cockpitng-widgets-config";
	protected static final String TEXT_XML_MIME_TYPE = "text/xml";
	/**
	 * Backoffice URL as root module does not have an url - it's items have null url.
	 */
	protected static final String BACKOFFICE_URL = null;
	private static final Logger LOG = LoggerFactory.getLogger(BackofficeWidgetPersistenceService.class);
	private MediaService mediaService;
	private BackofficeConfigurationMediaHelper backofficeConfigurationMediaHelper;


	@Override
	public Widget loadWidgetTree(final String widgetId)
	{
		final MediaModel widgetsConfig;
		try
		{
			requestWriteLock();
			widgetsConfig = getOrCreateWidgetsConfigMedia();
			requestReadLock();
		}
		catch (final Exception e)
		{
			LOG.error(ERROR_PROCESSING_WIDGETS_CONFIG, e);
			return null;
		}
		finally
		{
			releaseWriteLock();
		}

		try (final InputStream streamFromMedia = getMediaService().getStreamFromMedia(widgetsConfig))
		{
			return loadWidgetTree(widgetId, streamFromMedia);
		}
		catch (final Exception e)
		{
			LOG.error(ERROR_PROCESSING_WIDGETS_CONFIG, e);
			return null;
		}
		finally
		{
			releaseReadLock();
		}
	}

	@Override
	public void storeWidgetTree(final Widget widget)
	{
		if (isStoringEnabled())
		{
			try
			{
				requestWriteLock();

				storeWidgetTreeInternal(widget);
			}
			catch (final Exception ex)
			{
				LOG.error(ERROR_PROCESSING_WIDGETS_CONFIG, ex);
			}
			finally
			{
				releaseWriteLock();
			}
		}
	}

	protected void storeWidgetTreeInternal(final Widget widget)
	{
		final Widget widgetToStore = isWidgetsConfigStoredInMedia() ? widget : WidgetTreeUtils.getRootWidget(widget);

		final MediaModel widgetsConfig = getOrCreateWidgetsConfigMedia();

		try (InputStream inputStream = getMediaService().getStreamFromMedia(widgetsConfig);
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream())
		{
			final Widgets widgets = loadWidgets(inputStream);
			storeWidgetTree(widgetToStore, widgets, outputStream);
			getMediaService().setDataForMedia(widgetsConfig, outputStream.toByteArray());
		}
		catch (final IOException e)
		{
			LOG.error("Cannot store widget's tree", e);
		}
	}

	@Override
	public void deleteWidgetTree(final Widget widget)
	{
		if (isStoringEnabled())
		{
			try
			{
				requestWriteLock();

				deleteWidgetTreeInternal(widget);
			}
			catch (final Exception ex)
			{
				LOG.error(ERROR_PROCESSING_WIDGETS_CONFIG, ex);
			}
			finally
			{
				releaseWriteLock();
			}
		}
	}

	protected void deleteWidgetTreeInternal(final Widget widget)
	{
		final MediaModel widgetsConfigMedia = getOrCreateWidgetsConfigMedia();

		try (InputStream inputStream = getMediaService().getStreamFromMedia(widgetsConfigMedia);
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream())
		{
			final Widgets widgets = loadWidgets(inputStream);
			deleteWidgetTreeRecursive(widgets, widget);
			deleteOrphanedConnections(widgets);
			storeWidgets(widgets, outputStream);
			getMediaService().setDataForMedia(widgetsConfigMedia, outputStream.toByteArray());
		}
		catch (final IOException e)
		{
			LOG.error("Cannot delete widget's tree", e);
		}
	}

	@Override
	public void resetToDefaults()
	{
		try
		{
			requestWriteLock();
			super.resetToDefaults();

			MediaModel media = getBackofficeConfigurationMediaHelper().findWidgetsConfigMedia(WIDGET_CONFIG_MEDIA);
			if (media == null)
			{
				try
				{
					media = getBackofficeConfigurationMediaHelper().createWidgetsConfigMedia(WIDGET_CONFIG_MEDIA, TEXT_XML_MIME_TYPE);
				}
				catch (final Exception e)
				{
					LOG.error(e.getLocalizedMessage(), e);
					media = new MediaModel();
				}
			}
			getMediaService().removeDataFromMediaQuietly(media);
			putDefaultWidgetsConfig(media);
		}
		finally
		{
			releaseWriteLock();
		}
	}

	@Override
	protected boolean isLocalWidgetsFileExisting()
	{
		return isWidgetsConfigStoredInMedia();
	}

	protected boolean isWidgetsConfigStoredInMedia()
	{
		final MediaModel widgetsConfig = getBackofficeConfigurationMediaHelper().findWidgetsConfigMedia(WIDGET_CONFIG_MEDIA);
		return widgetsConfig != null && getMediaService().hasData(widgetsConfig);
	}

	protected MediaModel getOrCreateWidgetsConfigMedia()
	{
		return getBackofficeConfigurationMediaHelper().findOrCreateWidgetsConfigMedia(WIDGET_CONFIG_MEDIA, TEXT_XML_MIME_TYPE,
				this::putDefaultWidgetsConfig);
	}

	protected void putDefaultWidgetsConfig(final MediaModel mediaModel)
	{
		try (final InputStream resourceAsStream = getDefaultWidgetsConfigInputStream();
				final ByteArrayOutputStream outputStream = new ByteArrayOutputStream())
		{
			final Widgets widgets = loadWidgets(resourceAsStream);
			applyImports(getImportRootFile(), widgets, Sets.newHashSet());

			applyExtensions(widgets);
			sortWidgetConnections(widgets);

			widgets.getImports().clear();
			widgets.getWidgetConnectionRemove().clear();
			storeWidgets(widgets, outputStream);
			getMediaService().setDataForMedia(mediaModel, outputStream.toByteArray());
		}
		catch (final IOException e)
		{
			LOG.error("Cannot store default widgets config in media", e);
		}
	}

	/**
	 * @param root
	 *           {@link Widgets} component holding all the items to be applied
	 * @return list of {@link WidgetExtension} objects sorted in order of dependencies defined in extensioninfo.xml
	 */
	@Override
	protected List<WidgetExtension> extractWidgetExtensions(final Widgets root)
	{
		return sortByCockpitModulesLoadOrder(super.extractWidgetExtensions(root), WidgetExtension::getContextId);
	}

	/**
	 * @param root
	 *           {@link Widgets} component holding all the connections
	 */
	protected void sortWidgetConnections(final Widgets root)
	{
		final List<WidgetConnection> connections = sortByCockpitModulesLoadOrder(super.extractWidgetConnections(root),
				WidgetConnection::getModuleUrl);
		root.getWidgetConnection().clear();
		root.getWidgetConnection().addAll(connections);
	}

	/**
	 * Sorts given items by loading order of cockpit modules - using moduleUrlExtractor to item's module url.
	 *
	 * @param items
	 *           items to be sorted
	 * @param moduleUrlExtractor
	 *           extracts url of module from which item comes. If item is from backoffice (the root) it will be null.
	 * @param <T>
	 *           type of items.
	 * @return sorted list of items by load modules load order.
	 */
	protected <T> List<T> sortByCockpitModulesLoadOrder(final List<T> items, final Function<T, String> moduleUrlExtractor)
	{
		final Map<String, List<T>> orderedModulesToItems = new LinkedHashMap<>();

		orderedModulesToItems.put(BACKOFFICE_URL, new ArrayList<>());
		getCockpitModuleConnector().getCockpitModuleUrls().forEach(url -> orderedModulesToItems.put(url, new ArrayList<>()));

		items.forEach(extension -> {

			final List<T> bucket = orderedModulesToItems.get(moduleUrlExtractor.apply(extension));
			if (bucket != null)
			{
				bucket.add(extension);
			}
		});

		final List<T> sortedExtensions = new ArrayList<>();
		orderedModulesToItems.forEach((key, value) -> sortedExtensions.addAll(value));
		return sortedExtensions;
	}

	protected InputStream getDefaultWidgetsConfigInputStream()
	{
		return ClassLoaderUtils.getCurrentClassLoader(this.getClass()).getResourceAsStream(getDefaultWidgetConfig());
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

	protected BackofficeConfigurationMediaHelper getBackofficeConfigurationMediaHelper()
	{
		return backofficeConfigurationMediaHelper;
	}

	@Required
	public void setBackofficeConfigurationMediaHelper(final DefaultBackofficeConfigurationMediaHelper backofficeConfigurationMediaHelper)
	{
		this.backofficeConfigurationMediaHelper = backofficeConfigurationMediaHelper;
	}
}

