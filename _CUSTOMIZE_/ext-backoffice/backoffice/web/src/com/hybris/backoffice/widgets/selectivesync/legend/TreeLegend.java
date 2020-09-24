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
package com.hybris.backoffice.widgets.selectivesync.legend;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Span;

import com.hybris.backoffice.widgets.selectivesync.renderer.SelectiveSyncRenderer;


/** Legend view for {@link SelectiveSyncRenderer}. */
public class TreeLegend extends Div
{
	private static final String LABEL_ATTRIBUTE = "edit.catalogsyncjob.syncattributedescriptorconfigcollectioneditor.legend.attribute";
	private static final String LABEL_ALL_CHECKBOX = "edit.catalogsyncjob.syncattributedescriptorconfigcollectioneditor.legend.all";
	private static final String LABEL_PART_CHECKBOX = "edit.catalogsyncjob.syncattributedescriptorconfigcollectioneditor.legend.part";
	private static final String LABEL_NONE_CHECKBOX = "edit.catalogsyncjob.syncattributedescriptorconfigcollectioneditor.legend.none";

	private static final String SCLASS_ATTRIBUTE_ICON = "ye-selsync-tree-attribute-icon";
	private static final String SCLASS_ALL_SELECTED = "ye-selsync-all-selected";
	private static final String SCLASS_PARTIALLY_SELECTED = "ye-selsync-partially-selected";
	private static final String SCLASS_NONE_SELECTED = "ye-selsync-none-selected";

	private static final String SCLASS_LEGEND = "ye-selsync-legend";
	private static final String SCLASS_LEGEND_ITEM = "ye-selsync-legend-item";
	private static final String SCLASS_LEGEND_ITEM_LABEL = "ye-selsync-legend-item-label";


	public TreeLegend()
	{
		super.setSclass(SCLASS_LEGEND);
		super.appendChild(createAttributeItem());
		super.appendChild(createLegendCheckboxItem(LABEL_ALL_CHECKBOX, SCLASS_ALL_SELECTED));
		super.appendChild(createLegendCheckboxItem(LABEL_PART_CHECKBOX, SCLASS_PARTIALLY_SELECTED));
		super.appendChild(createLegendCheckboxItem(LABEL_NONE_CHECKBOX, SCLASS_NONE_SELECTED));

	}

	protected final Component createAttributeItem()
	{
		final Div legendItemContainer = new Div();
		legendItemContainer.setSclass(SCLASS_LEGEND_ITEM);
		legendItemContainer.appendChild(createAttributeIcon());
		legendItemContainer.appendChild(createLabel(LABEL_ATTRIBUTE));
		return legendItemContainer;
	}

	protected final Span createAttributeIcon()
	{
		final Span icon = new Span();
		icon.setSclass(SCLASS_ATTRIBUTE_ICON);
		return icon;
	}

	protected final Label createLabel(final String labelKey)
	{
		final Label legendLabel = new Label(Labels.getLabel(labelKey));
		legendLabel.setTooltiptext(Labels.getLabel(labelKey));
		legendLabel.setSclass(SCLASS_LEGEND_ITEM_LABEL);
		return legendLabel;
	}

	protected final Component createLegendCheckboxItem(final String labelKey, final String sclass)
	{
		final Div legendItemContainer = new Div();
		legendItemContainer.setSclass(SCLASS_LEGEND_ITEM);
		legendItemContainer.appendChild(createCheckbox(sclass));
		legendItemContainer.appendChild(createLabel(labelKey));
		return legendItemContainer;
	}

	protected final Checkbox createCheckbox(final String sclass)
	{
		final Checkbox legendCheckbox = new Checkbox();
		legendCheckbox.setSclass(sclass);
		legendCheckbox.setDisabled(true);
		legendCheckbox.setChecked(true);
		return legendCheckbox;
	}
}
