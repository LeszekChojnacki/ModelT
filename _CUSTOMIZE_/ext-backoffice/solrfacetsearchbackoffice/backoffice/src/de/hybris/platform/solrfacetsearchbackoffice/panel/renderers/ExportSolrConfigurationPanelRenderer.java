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
/**
 *
 */
package de.hybris.platform.solrfacetsearchbackoffice.panel.renderers;

import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfigService;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.config.exceptions.FacetConfigServiceException;
import de.hybris.platform.solrfacetsearch.model.SolrIndexModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrFacetSearchConfigModel;
import de.hybris.platform.solrfacetsearch.solr.Index;
import de.hybris.platform.solrfacetsearch.solr.SolrIndexService;
import de.hybris.platform.solrfacetsearch.solr.SolrSearchProvider;
import de.hybris.platform.solrfacetsearch.solr.SolrSearchProviderFactory;
import de.hybris.platform.solrfacetsearch.solr.exceptions.SolrServiceException;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEventTypes;
import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.AbstractPanel;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.widgets.editorarea.renderer.impl.AbstractEditorAreaPanelRenderer;


/**
 * Renders button to export Solr configuration (synonyms and stop words)
 */
public class ExportSolrConfigurationPanelRenderer extends AbstractEditorAreaPanelRenderer<Object>
{
	private static final Logger LOG = Logger.getLogger(ExportSolrConfigurationPanelRenderer.class);

	protected static final String CSS_EXPORT_BUTTON = "export-solr-configuration-btn";
	protected static final String EXPORT_BUTTON = "export.solrconfiguration.button";

	protected static final String NOTIFICATION_EVENT_EXPORT_SOLR_CONFIGURATION = "ExportSolrConfiguration";
	private FacetSearchConfigService facetSearchConfigService;
	private SolrSearchProviderFactory solrSearchProviderFactory;
	private SolrIndexService solrIndexService;
	private NotificationService notificationService;

	@Override
	public void render(final Component parent, final AbstractPanel panel, final Object data, final DataType type,
			final WidgetInstanceManager widgetInstanceManager)
	{
		final Button exportButton = new Button(Labels.getLabel(EXPORT_BUTTON));
		exportButton.setSclass(CSS_EXPORT_BUTTON);
		parent.appendChild(exportButton);

		exportButton.addEventListener(Events.ON_CLICK, event -> {
			if (data instanceof SolrFacetSearchConfigModel)
			{
				final SolrFacetSearchConfigModel solrFacetSearchConfig = (SolrFacetSearchConfigModel) data;

				try
				{
					exportConfiguration(solrFacetSearchConfig.getName());
				}
				catch (final SolrServiceException | FacetConfigServiceException e)
				{
					LOG.error("Failed to export configuration", e);
					notificationService.notifyUser(widgetInstanceManager, NotificationEventTypes.EVENT_TYPE_GENERAL,
							NotificationEvent.Level.FAILURE, e);
					return;
				}

				notificationService.notifyUser(widgetInstanceManager, NOTIFICATION_EVENT_EXPORT_SOLR_CONFIGURATION,
						NotificationEvent.Level.SUCCESS, solrFacetSearchConfig.getName());
			}
		});
	}

	protected void exportConfiguration(final String configurationName) throws FacetConfigServiceException, SolrServiceException
	{
		final FacetSearchConfig facetSearchConfig = facetSearchConfigService.getConfiguration(configurationName);
		final IndexConfig indexConfig = facetSearchConfig.getIndexConfig();

		for (final IndexedType indexedType : indexConfig.getIndexedTypes().values())
		{
			final SolrIndexModel activeIndex = solrIndexService.getActiveIndex(facetSearchConfig.getName(),
					indexedType.getIdentifier());
			final SolrSearchProvider solrSearchProvider = solrSearchProviderFactory.getSearchProvider(facetSearchConfig,
					indexedType);
			final Index index = solrSearchProvider.resolveIndex(facetSearchConfig, indexedType, activeIndex.getQualifier());
			solrSearchProvider.exportConfig(index);
		}
	}

	public FacetSearchConfigService getFacetSearchConfigService()
	{
		return facetSearchConfigService;
	}

	@Required
	public void setFacetSearchConfigService(final FacetSearchConfigService facetSearchConfigService)
	{
		this.facetSearchConfigService = facetSearchConfigService;
	}

	public SolrSearchProviderFactory getSolrSearchProviderFactory()
	{
		return solrSearchProviderFactory;
	}

	@Required
	public void setSolrSearchProviderFactory(final SolrSearchProviderFactory solrSearchProviderFactory)
	{
		this.solrSearchProviderFactory = solrSearchProviderFactory;
	}

	public SolrIndexService getSolrIndexService()
	{
		return solrIndexService;
	}

	@Required
	public void setSolrIndexService(final SolrIndexService solrIndexService)
	{
		this.solrIndexService = solrIndexService;
	}

	public NotificationService getNotificationService()
	{
		return notificationService;
	}

	@Required
	public void setNotificationService(NotificationService notificationService)
	{
		this.notificationService = notificationService;
	}
}
