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
package de.hybris.platform.adaptivesearchbackoffice.editors.facets;

import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.SEARCH_REQUEST_SOCKET;
import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.ZK_LISTBOX_RIGHT_SELECT_ATTRIBUTE;

import de.hybris.platform.adaptivesearch.data.AsFacetData;
import de.hybris.platform.adaptivesearch.data.AsFacetValueData;
import de.hybris.platform.adaptivesearch.data.AsFacetVisibility;
import de.hybris.platform.adaptivesearch.model.AbstractAsFacetConfigurationModel;
import de.hybris.platform.adaptivesearchbackoffice.data.AbstractFacetConfigurationEditorData;
import de.hybris.platform.adaptivesearchbackoffice.data.FacetFiltersRequestData;
import de.hybris.platform.adaptivesearchbackoffice.data.FacetRequestData;
import de.hybris.platform.adaptivesearchbackoffice.editors.EditorRenderer;
import de.hybris.platform.adaptivesearchbackoffice.editors.configurablemultireference.MultiReferenceEditorLogic;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.OpenEvent;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zul.A;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;


public class AsFacetValuesRenderer implements
		EditorRenderer<MultiReferenceEditorLogic<AbstractFacetConfigurationEditorData, AbstractAsFacetConfigurationModel>, AbstractFacetConfigurationEditorData>
{
	protected static final String FACET_VALUES_SCLASS = "yas-facet-values";
	protected static final String FACET_VALUES_TOP_SCLASS = "yas-facet-values-top";
	protected static final String FACET_VALUES_ALL_SCLASS = "yas-facet-values-all";

	protected static final String FACET_VALUE_SCLASS = "yas-facet-value";
	protected static final String FACET_VALUE_NAME_SCLASS = "yas-facet-value-name";
	protected static final String FACET_VALUE_COUNT_SCLASS = "yas-facet-value-count";

	protected static final String ACTIONS_SCLASS = "yas-actions";
	protected static final String ACTION_SCLASS = "yas-action";

	protected static final String STICKY_SCLASS = "yas-sticky";

	protected static final String FACET_VALUE_COUNT_LABEL = "adaptivesearch.facet.value.count";
	protected static final String FACET_VALUE_SHOW_MORE_LABEL = "adaptivesearch.facet.value.showMore";
	protected static final String FACET_VALUE_SHOW_LESS_LABEL = "adaptivesearch.facet.value.showLess";

	@Override
	public boolean isEnabled(final MultiReferenceEditorLogic logic)
	{
		return true;
	}

	@Override
	public boolean canRender(final MultiReferenceEditorLogic logic, final Component parent,
			final AbstractFacetConfigurationEditorData data)
	{
		if (data == null || data.getFacet() == null)
		{
			return false;
		}

		final AsFacetData facet = data.getFacet();
		return CollectionUtils.isNotEmpty(facet.getValues()) || CollectionUtils.isNotEmpty(facet.getSelectedValues());
	}

	@Override
	public void beforeRender(
			final MultiReferenceEditorLogic<AbstractFacetConfigurationEditorData, AbstractAsFacetConfigurationModel> logic,
			final Component parent, final AbstractFacetConfigurationEditorData data)
	{
		final Component item = logic.findEditorItem(parent);

		item.addEventListener(Events.ON_OPEN, event -> {
			final OpenEvent openEvent = (OpenEvent) event;
			sendFacetRequest(logic, data, openEvent.isOpen() ? resolveFacetVisibility(data) : AsFacetVisibility.SHOW);
		});
	}

	@Override
	public void render(final MultiReferenceEditorLogic logic, final Component parent,
			final AbstractFacetConfigurationEditorData data)
	{
		final Div facetValuesDiv = new Div();
		facetValuesDiv.setParent(parent);
		facetValuesDiv.setSclass(FACET_VALUES_SCLASS);

		final AsFacetVisibility facetVisibility = resolveFacetVisibility(data);
		final boolean isFacetValuesOpen = facetVisibility == AsFacetVisibility.SHOW_VALUES;

		logic.setOpen(facetValuesDiv, isFacetValuesOpen);

		renderFacetValuesTop(logic, facetValuesDiv, data);
		renderFacetValuesAll(logic, facetValuesDiv, data);

		facetValuesDiv.addEventListener(Events.ON_OPEN, event -> {
			final OpenEvent openEvent = (OpenEvent) event;
			sendFacetRequest(logic, data, openEvent.isOpen() ? AsFacetVisibility.SHOW_VALUES : AsFacetVisibility.SHOW_TOP_VALUES);
		});
	}

	protected void renderFacetValuesTop(final MultiReferenceEditorLogic logic, final Component facetValues,
			final AbstractFacetConfigurationEditorData data)
	{
		final AsFacetData facet = data.getFacet();

		if (CollectionUtils.isNotEmpty(facet.getTopValues()))
		{
			final Div facetValuesTopDiv = new Div();
			facetValuesTopDiv.setParent(facetValues);
			facetValuesTopDiv.setSclass(FACET_VALUES_TOP_SCLASS);
			facetValuesTopDiv.setVisible(!logic.isOpen(facetValues));

			renderFacetValues(logic, facetValuesTopDiv, data, facet.getTopValues(), facet.getSelectedValues());

			final Div actionsDiv = new Div();
			actionsDiv.setParent(facetValuesTopDiv);
			actionsDiv.setSclass(ACTIONS_SCLASS);

			final Div showMoreActionDiv = new Div();
			showMoreActionDiv.setParent(actionsDiv);
			showMoreActionDiv.setSclass(ACTION_SCLASS);

			final A showMoreAction = new A();
			showMoreAction.setParent(showMoreActionDiv);
			showMoreAction.setLabel(Labels.getLabel(FACET_VALUE_SHOW_MORE_LABEL));

			facetValues.addEventListener(Events.ON_OPEN, event -> facetValuesTopDiv.setVisible(!((OpenEvent) event).isOpen()));
			showMoreAction.addEventListener(Events.ON_CLICK, event -> logic.setOpen(facetValues, true));
		}
	}

	protected void renderFacetValuesAll(final MultiReferenceEditorLogic logic, final Component facetValues,
			final AbstractFacetConfigurationEditorData data)
	{
		final AsFacetData facet = data.getFacet();

		final Div facetValuesAllDiv = new Div();
		facetValuesAllDiv.setParent(facetValues);
		facetValuesAllDiv.setSclass(FACET_VALUES_ALL_SCLASS);
		facetValuesAllDiv.setVisible(logic.isOpen(facetValues));

		renderFacetValues(logic, facetValuesAllDiv, data, facet.getValues(), facet.getSelectedValues());

		if (CollectionUtils.isNotEmpty(facet.getTopValues()))
		{
			final Div actionsDiv = new Div();
			actionsDiv.setParent(facetValuesAllDiv);
			actionsDiv.setSclass(ACTIONS_SCLASS);

			final Div showLessActionDiv = new Div();
			showLessActionDiv.setParent(actionsDiv);
			showLessActionDiv.setSclass(ACTION_SCLASS);

			final A showLessAction = new A();
			showLessAction.setParent(showLessActionDiv);
			showLessAction.setLabel(Labels.getLabel(FACET_VALUE_SHOW_LESS_LABEL));

			facetValues.addEventListener(Events.ON_OPEN, event -> facetValuesAllDiv.setVisible(((OpenEvent) event).isOpen()));
			showLessAction.addEventListener(Events.ON_CLICK, event -> logic.setOpen(facetValues, false));
		}
	}

	protected void renderFacetValues(final MultiReferenceEditorLogic logic, final Component parent,
			final AbstractFacetConfigurationEditorData data, final List<AsFacetValueData> facetValues,
			final List<AsFacetValueData> selectedFacetValues)
	{
		final Listbox listbox = new Listbox();
		listbox.setParent(parent);
		listbox.setCheckmark(true);
		listbox.setModel(createFacetValuesModel(facetValues, selectedFacetValues));
		listbox.setItemRenderer(this::renderFacetValue);
		listbox.setAttribute(ZK_LISTBOX_RIGHT_SELECT_ATTRIBUTE, Boolean.FALSE);

		listbox.addEventListener(Events.ON_SELECT, event -> {
			if (event instanceof SelectEvent)
			{
				final SelectEvent selectEvent = (SelectEvent) event;
				final Set<AsFacetValueData> selectedObjects = selectEvent.getSelectedObjects();

				sendFacetFilterRequest(logic, data,
						selectedObjects.stream().map(AsFacetValueData::getValue).collect(Collectors.toList()));
			}
		});
	}

	protected void renderFacetValue(final Listitem listitem, final Object data, final int index)
	{
		final AsFacetValueData facetValue = (AsFacetValueData) data;
		final ListModel<AsFacetValueData> model = listitem.getListbox().getModel();
		final int stickyValuesSize = ((FacetValuesListModel) model).getStickyValuesSize();

		if (index < stickyValuesSize)
		{
			listitem.setSclass(FACET_VALUE_SCLASS + " " + STICKY_SCLASS);
		}
		else
		{
			listitem.setSclass(FACET_VALUE_SCLASS);
		}

		final Listcell listcell = new Listcell();
		listcell.setParent(listitem);

		final Label nameLabel = new Label();
		nameLabel.setParent(listcell);
		nameLabel.setSclass(FACET_VALUE_NAME_SCLASS);
		nameLabel.setValue(facetValue.getName());

		final Label countLabel = new Label();
		countLabel.setParent(listcell);
		countLabel.setSclass(FACET_VALUE_COUNT_SCLASS);
		countLabel.setValue(Labels.getLabel(FACET_VALUE_COUNT_LABEL, new Object[]
		{ Long.valueOf(facetValue.getCount()) }));
	}

	protected FacetValuesListModel createFacetValuesModel(final List<AsFacetValueData> facetValues,
			final List<AsFacetValueData> selectedFacetValues)
	{
		// sticky facet values are values that are selected but not in the facet values list
		final Map<String, AsFacetValueData> stickyFacetValues = new LinkedHashMap<>();

		for (final AsFacetValueData facetValue : selectedFacetValues)
		{
			stickyFacetValues.put(facetValue.getValue(), facetValue);
		}

		for (final AsFacetValueData facetValue : facetValues)
		{
			stickyFacetValues.remove(facetValue.getValue());
		}

		final List<AsFacetValueData> data = new ArrayList<>();
		data.addAll(stickyFacetValues.values());
		data.addAll(facetValues);

		final List<AsFacetValueData> selectedObjects = data.stream().filter(AsFacetValueData::isSelected)
				.collect(Collectors.toList());

		final FacetValuesListModel model = new FacetValuesListModel(data, stickyFacetValues.size());
		model.setMultiple(true);
		model.setSelection(selectedObjects);
		return model;
	}

	protected AsFacetVisibility resolveFacetVisibility(final AbstractFacetConfigurationEditorData data)
	{
		final AsFacetData facet = data.getFacet();

		if (facet.getVisibility() != AsFacetVisibility.SHOW_VALUES && CollectionUtils.isNotEmpty(facet.getTopValues()))
		{
			return AsFacetVisibility.SHOW_TOP_VALUES;
		}

		return AsFacetVisibility.SHOW_VALUES;
	}

	protected void sendFacetRequest(final MultiReferenceEditorLogic logic, final AbstractFacetConfigurationEditorData data,
			final AsFacetVisibility facetVisibility)
	{
		final FacetRequestData request = new FacetRequestData();
		request.setIndexProperty(data.getIndexProperty());
		request.setFacetVisibility(facetVisibility);

		logic.getWidgetInstanceManager().sendOutput(SEARCH_REQUEST_SOCKET, request);
	}

	protected void sendFacetFilterRequest(final MultiReferenceEditorLogic logic, final AbstractFacetConfigurationEditorData data,
			final List<String> selectedFacetValues)
	{
		final FacetFiltersRequestData request = new FacetFiltersRequestData();
		request.setIndexProperty(data.getIndexProperty());
		request.setValues(selectedFacetValues);

		logic.getWidgetInstanceManager().sendOutput(SEARCH_REQUEST_SOCKET, request);
	}
}
