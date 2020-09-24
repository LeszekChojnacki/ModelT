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
package com.hybris.pcmbackoffice.widgets.charts.facetchart;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;

import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.search.data.facet.FacetData;


public class FacetChartFacetChooserRenderer
{
	private static final Logger LOG = LoggerFactory.getLogger(FacetChartFacetChooserRenderer.class);

	private static final String LABEL_FACET_CHOOSER = "solrchart.facetchooserrenderer.label";
	private static final String FACET_SELECTED_INDEX = "selectedFacetIndex";

	private WidgetInstanceManager widgetInstanceManager;
	private Consumer<String> facetChange;
	private List<FacetData> availableFacets;

	public void render(final Div parent, final WidgetInstanceManager widgetInstanceManager, final Consumer<String> facetChange,
			final List<FacetData> availableFacets)
	{
		this.facetChange = facetChange;
		this.widgetInstanceManager = widgetInstanceManager;
		this.availableFacets = availableFacets;

		if (availableFacets.size() < 2)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("No facet is set, add some facet to configuration");
			}
			return;
		}

		parent.getChildren().clear();

		createLabel(parent);
		createCombobox(parent);
	}

	private void createLabel(final Div parent)
	{
		final Label label = new Label(widgetInstanceManager.getLabel(LABEL_FACET_CHOOSER));
		label.setParent(parent);
	}

	private void createCombobox(final Div parent)
	{
		final Combobox combobox = new Combobox();
		availableFacets.forEach(facet -> {
			final Comboitem comboitem = new Comboitem(facet.getDisplayName());
			comboitem.setValue(facet.getName());
			combobox.getChildren().add(comboitem);
		});

		combobox.setSelectedIndex(getSelectedIndex());
		combobox.setReadonly(true);
		combobox.addEventListener(Events.ON_SELECT, (EventListener<SelectEvent>) this::onSelectFacet);
		combobox.setParent(parent);
	}

	void onSelectFacet(final SelectEvent<Comboitem, String> event)
	{
		final Set<Comboitem> selectedItems = event.getSelectedItems();
		if (selectedItems.isEmpty())
		{
			return;
		}
		final Comboitem selectedItem = selectedItems.iterator().next();
		storeSelectedIndex(selectedItem.getIndex());
		facetChange.accept(selectedItem.getValue());
	}

	private Integer getSelectedIndex()
	{
		Integer selectedFacetIndex = widgetInstanceManager.getModel().getValue(FACET_SELECTED_INDEX, Integer.class);
		if (selectedFacetIndex == null)
		{
			selectedFacetIndex = 0;
		}
		return selectedFacetIndex;
	}

	private void storeSelectedIndex(final Integer selectedItem)
	{
		widgetInstanceManager.getModel().setValue(FACET_SELECTED_INDEX, selectedItem);
	}
}
