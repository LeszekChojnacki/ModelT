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
package com.hybris.backoffice.sync.renderers;

import de.hybris.platform.core.model.ItemModel;

import java.util.HashMap;
import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Span;
import org.zkoss.zul.impl.XulElement;

import com.hybris.backoffice.sync.facades.SynchronizationFacade;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.lazyloading.DefaultLazyTaskResult;
import com.hybris.cockpitng.lazyloading.LazyTaskResult;
import com.hybris.cockpitng.util.UITools;
import com.hybris.cockpitng.widgets.common.AbstractLazyRenderer;
import com.hybris.cockpitng.widgets.common.WidgetComponentRenderer;


public class SyncStatusCellRenderer extends AbstractLazyRenderer<Component, Object, ItemModel, Optional<Boolean>>
{
	private static final Logger LOG = LoggerFactory.getLogger(SyncStatusCellRenderer.class);
	protected SynchronizationFacade synchronizationFacade;
	protected WidgetComponentRenderer<XulElement, Object, ItemModel> partialSyncInfoRenderer;
	protected Boolean lazyRender = Boolean.FALSE;

	@Override
	public void render(final Component parent, final Object configuration, final ItemModel data, final DataType dataType,
			final WidgetInstanceManager widgetInstanceManager)
	{
		if (BooleanUtils.isTrue(lazyRender))
		{
			super.render(parent, configuration, data, dataType, widgetInstanceManager);
		}
		else
		{
			try
			{
				final Optional<Boolean> synced = loadData(configuration, data, dataType);
				renderAfterLoad(parent, configuration, data, dataType, widgetInstanceManager, DefaultLazyTaskResult.success(synced));
			}
			catch (final Exception e)
			{
				renderAfterLoad(parent, configuration, data, dataType, widgetInstanceManager, DefaultLazyTaskResult.failure());
				LOG.error("Unable to calculate sync status", e);
			}
		}

		fireComponentRendered(parent, configuration, data);
	}

	@Override
	protected void renderBeforeLoad(final Component parent, final Object configuration, final ItemModel data,
			final DataType dataType, final WidgetInstanceManager widgetInstanceManager)
	{
		final Span icon = new Span();
		UITools.modifySClass(icon, SyncRenderConstants.YW_IMAGE_ATTRIBUTE_SYNC_STATUS_LOADING, true);
		parent.appendChild(icon);
		fireComponentRendered(icon, parent, configuration, data);
	}

	@Override
	protected Optional<Boolean> loadData(final Object configuration, final ItemModel data, final DataType dataType)
	{
		return synchronizationFacade.isInSync(data, new HashMap<>());
	}

	@Override
	protected void renderAfterLoad(final Component parent, final Object configuration, final ItemModel data,
			final DataType dataType, final WidgetInstanceManager wim, final LazyTaskResult<Optional<Boolean>> lazyLoadedData)
	{
		parent.getChildren().clear();
		final Span icon = new Span();
		if (!lazyLoadedData.isSuccess())
		{
			UITools.modifySClass(icon, SyncRenderConstants.YW_IMAGE_ATTRIBUTE_SYNC_STATUS_ERROR, true);
			icon.setTooltiptext(Labels.getLabel(SyncRenderConstants.LABEL_SYNC_ERROR_TOOLTIP));
		}
		else if (lazyLoadedData.get().isPresent())
		{
			if (lazyLoadedData.get().get().booleanValue())
			{
				UITools.modifySClass(icon, SyncRenderConstants.YW_IMAGE_ATTRIBUTE_SYNC_STATUS_IN_SYNC, true);
			}
			else
			{
				UITools.modifySClass(icon, SyncRenderConstants.YW_IMAGE_ATTRIBUTE_SYNC_STATUS_OUT_OF_SYNC, true);
			}
			partialSyncInfoRenderer.render(icon, configuration, data, dataType, wim);
		}
		else
		{
			UITools.modifySClass(icon, SyncRenderConstants.YW_IMAGE_ATTRIBUTE_SYNC_STATUS_UNDEFINED, true);
			icon.setTooltiptext(Labels.getLabel(SyncRenderConstants.LABEL_SYNC_UNDEFINED_TOOLTIP));
		}
		parent.appendChild(icon);
		fireComponentRendered(icon, parent, configuration, data);
	}

	@Required
	public void setSynchronizationFacade(final SynchronizationFacade synchronizationFacade)
	{
		this.synchronizationFacade = synchronizationFacade;
	}

	public SynchronizationFacade getSynchronizationFacade()
	{
		return synchronizationFacade;
	}

	@Required
	public void setPartialSyncInfoRenderer(final WidgetComponentRenderer<XulElement, Object, ItemModel> partialSyncInfoRenderer)
	{
		this.partialSyncInfoRenderer = partialSyncInfoRenderer;
	}

	public WidgetComponentRenderer<XulElement, Object, ItemModel> getPartialSyncInfoRenderer()
	{
		return partialSyncInfoRenderer;
	}

	public void setLazyRender(final Boolean lazyRender)
	{
		this.lazyRender = lazyRender;
	}

	public Boolean getLazyRender()
	{
		return lazyRender;
	}
}
