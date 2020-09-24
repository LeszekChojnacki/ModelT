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

import de.hybris.platform.catalog.enums.SyncItemStatus;
import de.hybris.platform.catalog.model.SyncItemJobModel;
import de.hybris.platform.core.model.ItemModel;

import java.util.HashMap;
import java.util.Optional;

import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.OpenEvent;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Popup;
import org.zkoss.zul.Span;
import org.zkoss.zul.impl.XulElement;

import com.hybris.backoffice.sync.PartialSyncInfo;
import com.hybris.backoffice.sync.facades.SynchronizationFacade;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.labels.LabelService;
import com.hybris.cockpitng.widgets.common.WidgetComponentRenderer;


/**
 * Renderer which adds a tooltip to given component with partial sync info.
 */
public class PartialSyncInfoTooltipRenderer implements WidgetComponentRenderer<XulElement, Object, ItemModel>
{

	public static final String SCLASS_PARTIAL_SYNC_INFO_JOB = "yw-partial-sync-info-syncJob";
	public static final String SCLASS_PARTIAL_SYNC_INFO_JOB_NAME = "yw-partial-sync-info-syncJob-name";
	public static final String SCLASS_PARTIAL_SYNC_INFO = "yw-partial-sync-info";
	public static final String LABEL_PARTIAL_SYNC_INFO_UNAVAILABLE = "partial.sync.info.unavailable";
	private SynchronizationFacade synchronizationFacade;
	private LabelService labelService;

	@Override
	public void render(final XulElement component, final Object configuration, final ItemModel itemModel, final DataType dataType,
			final WidgetInstanceManager widgetInstanceManager)
	{
		final Popup toolTip = new Popup();
		component.appendChild(toolTip);
		component.setTooltip(toolTip);

		toolTip.addEventListener(Events.ON_OPEN, new EventListener<OpenEvent>()
		{
			@Override
			public void onEvent(final OpenEvent event)
			{
				if (event.isOpen())
				{
					toolTip.removeEventListener(Events.ON_OPEN, this);
					final Optional<PartialSyncInfo> partialSync = synchronizationFacade.getPartialSyncStatusInfo(itemModel,
							SyncItemStatus.IN_SYNC, new HashMap<>());
					if (partialSync.isPresent())
					{
						toolTip.appendChild(PartialSyncInfoTooltipRenderer.this.createPartialSyncInfo(partialSync.get()));
					}
					else
					{
						toolTip.appendChild(new Label(Labels.getLabel(LABEL_PARTIAL_SYNC_INFO_UNAVAILABLE)));
					}
				}
			}
		});
	}

	protected Component createPartialSyncInfo(final PartialSyncInfo partialSync)
	{
		final Div container = new Div();
		container.setSclass(SCLASS_PARTIAL_SYNC_INFO);
		partialSync.getInboundSyncStatus().entrySet().stream().map(entry -> createJobSyncInfo(entry.getKey(), entry.getValue()))
				.forEach(container::appendChild);

		partialSync.getOutboundSyncStatus().entrySet().stream().map(entry -> createJobSyncInfo(entry.getKey(), entry.getValue()))
				.forEach(container::appendChild);
		return container;
	}

	protected Component createJobSyncInfo(final SyncItemJobModel syncJob, final Boolean inSync)
	{
		final Label jobName = new Label(labelService.getObjectLabel(syncJob));
		jobName.setSclass(SCLASS_PARTIAL_SYNC_INFO_JOB_NAME);

		final Span statusIcon = new Span();
		statusIcon.setSclass(BooleanUtils.isTrue(inSync) ? SyncRenderConstants.YW_IMAGE_ATTRIBUTE_SYNC_STATUS_IN_SYNC
				: SyncRenderConstants.YW_IMAGE_ATTRIBUTE_SYNC_STATUS_OUT_OF_SYNC);

		final Div container = new Div();
		container.setSclass(SCLASS_PARTIAL_SYNC_INFO_JOB);
		container.appendChild(jobName);
		container.appendChild(statusIcon);
		return container;
	}

	@Required
	public void setSynchronizationFacade(final SynchronizationFacade synchronizationFacade)
	{
		this.synchronizationFacade = synchronizationFacade;
	}

	@Required
	public void setLabelService(final LabelService labelService)
	{
		this.labelService = labelService;
	}
}
