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
package com.hybris.backoffice.widgets.selectivesync.renderer;

import de.hybris.platform.catalog.model.SyncAttributeDescriptorConfigModel;
import de.hybris.platform.catalog.model.synchronization.CatalogVersionSyncJobModel;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;

import com.hybris.cockpitng.components.Widgetslot;
import com.hybris.cockpitng.core.Widget;
import com.hybris.cockpitng.core.WidgetDefinition;
import com.hybris.cockpitng.core.WidgetSocket;
import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.AbstractPanel;
import com.hybris.cockpitng.core.ui.WidgetInstance;
import com.hybris.cockpitng.core.util.impl.WidgetSocketUtils;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.widgets.editorarea.renderer.impl.AbstractEditorAreaPanelRenderer;


/**
 * Custom renderer for {@link CatalogVersionSyncJobModel} with summary view of attributes included for synchronization.
 */
public class SelectiveSyncAttributesOverviewRenderer extends AbstractEditorAreaPanelRenderer<CatalogVersionSyncJobModel>
{
	protected static final String LABEL_CONFIGURATION = "edit.catalogsyncjob.overviewrenderer.header";
	protected static final String LABEL_PROPERTIES_INCLUDED = "edit.catalogsyncjob.overviewrenderer.included";
	protected static final String LABEL_PROPERTIES_EXCLUDED = "edit.catalogsyncjob.overviewrenderer.excluded";
	protected static final String LABEL_PROPERTIES_TYPES = "edit.catalogsyncjob.overviewrenderer.types";
	protected static final String LABEL_PROPERTIES_ATTRIBUTES = "edit.catalogsyncjob.overviewrenderer.attributes";
	protected static final String LABEL_EDIT_CONFIGURATION = "edit.catalogsyncjob.overviewrenderer.edit";

	protected static final String SOCKET_OUTPUT_SEL_SYNC_OBJECT = "selectivesyncobject";

	private static final String SCLASS_MAIN = "yw-editorarea-box-main";
	private static final String SCLASS_COL = "yw-editorarea-box-col";
	private static final String SCLASS_HEAD = "yw-editorarea-box-head";
	private static final String SCLASS_ROW = "yw-editorarea-box-row";
	private static final String SCLASS_EDIT_LINK = "yw-editorarea-editlink";


	@Override
	public void render(final Component parent, final AbstractPanel configuration, final CatalogVersionSyncJobModel data,
			final DataType dataType, final WidgetInstanceManager widgetInstanceManager)
	{
		parent.appendChild(new Label(Labels.getLabel(LABEL_CONFIGURATION)));
		parent.appendChild(createTable(calculateSummary(data.getSyncAttributeConfigurations())));
		if (isCurrentObjectSocketAvailable(widgetInstanceManager))
		{
			parent.appendChild(createButton(data, widgetInstanceManager));
		}
	}

	protected SyncAttributesSummary calculateSummary(
			final Collection<SyncAttributeDescriptorConfigModel> syncAttributeConfigurations)
	{
		final SyncAttributesSummary summary = new SyncAttributesSummary();
		summary.includedAttributes = calculateIncludedAttributes(syncAttributeConfigurations);
		summary.excludedAttributes = calculateExcludedAttributes(syncAttributeConfigurations);
		summary.includedTypes = calculateIncludedTypes(syncAttributeConfigurations);
		summary.excludedTypes = calculateExcludedTypes(syncAttributeConfigurations);
		return summary;
	}

	protected int calculateIncludedAttributes(final Collection<SyncAttributeDescriptorConfigModel> syncAttributeConfigurations)
	{
		return (int) syncAttributeConfigurations.stream().filter(SyncAttributeDescriptorConfigModel::getIncludedInSync).count();
	}

	protected int calculateExcludedAttributes(final Collection<SyncAttributeDescriptorConfigModel> syncAttributeConfigurations)
	{
		return (int) syncAttributeConfigurations.stream().filter(c -> !c.getIncludedInSync()).count();
	}

	protected int calculateIncludedTypes(final Collection<SyncAttributeDescriptorConfigModel> syncAttributeConfigurations)
	{
		return syncAttributeConfigurations.stream().filter(SyncAttributeDescriptorConfigModel::getIncludedInSync)
				.map(c -> c.getAttributeDescriptor().getEnclosingType()).collect(Collectors.toSet()).size();
	}

	protected int calculateExcludedTypes(final Collection<SyncAttributeDescriptorConfigModel> syncAttributeConfigurations)
	{
		return syncAttributeConfigurations.stream().map(c -> c.getAttributeDescriptor().getEnclosingType())
				.collect(Collectors.toSet()).size() - calculateIncludedTypes(syncAttributeConfigurations);
	}

	protected Component createTable(final SyncAttributesSummary summary)
	{
		final Div main = new Div();
		main.setSclass(SCLASS_MAIN);

		main.appendChild(createColumn(LABEL_PROPERTIES_INCLUDED, summary.includedAttributes, summary.includedTypes));
		main.appendChild(createColumn(LABEL_PROPERTIES_EXCLUDED, summary.excludedAttributes, summary.excludedTypes));

		return main;
	}

	protected Component createColumn(final String headerKey, final int attributes, final int types)
	{
		final Div div = new Div();
		div.setSclass(SCLASS_COL);

		final Label header = new Label(Labels.getLabel(headerKey));
		header.setSclass(SCLASS_HEAD);

		div.appendChild(header);
		div.appendChild(createLabel(LABEL_PROPERTIES_TYPES, types));
		div.appendChild(createLabel(LABEL_PROPERTIES_ATTRIBUTES, attributes));

		return div;
	}

	protected Label createLabel(final String labelKey, final Object... values)
	{
		final Label label = new Label(Labels.getLabel(labelKey, values));
		label.setSclass(SCLASS_ROW);
		return label;
	}

	protected Button createButton(final CatalogVersionSyncJobModel data, final WidgetInstanceManager widgetInstanceManager)
	{
		final Button button = new Button(Labels.getLabel(LABEL_EDIT_CONFIGURATION));
		button.setSclass(SCLASS_EDIT_LINK);
		button.addEventListener(Events.ON_CLICK, event -> widgetInstanceManager.sendOutput(SOCKET_OUTPUT_SEL_SYNC_OBJECT, data));
		return button;
	}

	protected boolean isCurrentObjectSocketAvailable(final WidgetInstanceManager widgetInstanceManager)
	{
		return getAllOutputs(widgetInstanceManager).stream().anyMatch(o -> SOCKET_OUTPUT_SEL_SYNC_OBJECT.equals(o.getId()));
	}

	protected List<WidgetSocket> getAllOutputs(final WidgetInstanceManager widgetInstanceManager)
	{
		final Optional<Widget> widget = getWidget(widgetInstanceManager);
		final Optional<WidgetDefinition> widgetDefinition = widget.isPresent()
				? getWidgetDefinition(widgetInstanceManager, widget.get())
				: Optional.empty();
		return widget.isPresent() && widgetDefinition.isPresent()
				? WidgetSocketUtils.getAllOutputs(widget.get(), widgetDefinition.get())
				: Collections.emptyList();
	}

	private static Optional<Widget> getWidget(final WidgetInstanceManager widgetInstanceManager)
	{
		return Optional.ofNullable(widgetInstanceManager) //
				.map(WidgetInstanceManager::getWidgetslot) //
				.map(Widgetslot::getWidgetInstance) //
				.map(WidgetInstance::getWidget);
	}

	private static Optional<WidgetDefinition> getWidgetDefinition(final WidgetInstanceManager widgetInstanceManager,
			final Widget widget)
	{
		return Optional.ofNullable(widgetInstanceManager) //
				.map(WidgetInstanceManager::getWidgetslot) //
				.map(slot -> slot.getWidgetDefinition(widget));
	}

	protected static class SyncAttributesSummary
	{
		protected int excludedAttributes;
		protected int excludedTypes;
		protected int includedAttributes;
		protected int includedTypes;
	}

}
