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

import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.ACTION_WIDGET_INSTANCE_MANAGER_KEY;
import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.CURRENT_OBJECT_KEY;

import de.hybris.platform.adaptivesearch.data.AsFacetData;
import de.hybris.platform.adaptivesearch.data.AsFacetValueData;
import de.hybris.platform.adaptivesearch.model.AbstractAsFacetConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AbstractAsFacetValueConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AsExcludedFacetValueModel;
import de.hybris.platform.adaptivesearch.model.AsPromotedFacetValueModel;
import de.hybris.platform.adaptivesearchbackoffice.components.ActionsMenu;
import de.hybris.platform.adaptivesearchbackoffice.data.AbstractFacetConfigurationEditorData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;

import com.hybris.cockpitng.components.Widgetslot;
import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.AbstractPanel;
import com.hybris.cockpitng.data.TypeAwareSelectionContext;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.util.WidgetTreeUIUtils;
import com.hybris.cockpitng.widgets.editorarea.renderer.impl.AbstractEditorAreaPanelRenderer;


public class AsFacetValuesPanelRenderer extends AbstractEditorAreaPanelRenderer<AbstractAsFacetConfigurationModel>
{
	protected static final String FACET_VALUE_LABEL = "adaptivesearch.facet.value.label";

	protected static final String FACET_VALUES_PANEL_SCLASS = "yas-facet-values-panel";
	protected static final String STICKY_SCLASS = "yas-sticky";

	protected static final String FACET_VALUE_SCLASS = "yas-facet-value";
	protected static final String FACET_VALUE_NAME_SCLASS = "yas-facet-value-name";
	protected static final String FACET_VALUE_COUNT_SCLASS = "yas-facet-value-count";

	protected static final String ACTIONS_SCLASS = "yas-actions";

	protected static final String FACET_VALUE_COUNT_LABEL = "adaptivesearch.facet.value.count";

	protected static final String ACTIONS_CONTEXT = "as-facet-values-panel-actions";

	protected static final String LAST_INPUT_KEY = "lastInput";
	protected static final String EDITOR_DATA_KEY = "editorData";

	protected static final String PROMOTED_OBJECT_EXPRESSION = "currentObject.promotedValues";
	protected static final String EXCLUDED_OBJECT_EXPRESSION = "currentObject.excludedValues";

	@Override
	public void render(final Component component, final AbstractPanel abstractPanel,
			final AbstractAsFacetConfigurationModel facetConfiguration, final DataType dataType,
			final WidgetInstanceManager widgetInstanceManager)
	{
		final List<AsFacetValueData> facetValues = resolveFacetValues(widgetInstanceManager.getWidgetslot(), 0);

		if (CollectionUtils.isEmpty(facetValues))
		{
			return;
		}

		final Div panel = new Div();
		panel.setParent(component);
		panel.setSclass(FACET_VALUES_PANEL_SCLASS);

		final Label label = new Label(Labels.getLabel(FACET_VALUE_LABEL));
		label.setParent(panel);

		final Listbox listbox = new Listbox();
		listbox.setParent(panel);
		listbox.setItemRenderer((item, data, index) -> renderItem(item, (FacetValueModel) data, widgetInstanceManager));
		listbox.setModel(createListModel(widgetInstanceManager, facetValues));

		widgetInstanceManager.getModel().addObserver(PROMOTED_OBJECT_EXPRESSION, () -> updateList(listbox, widgetInstanceManager));
		widgetInstanceManager.getModel().addObserver(EXCLUDED_OBJECT_EXPRESSION, () -> updateList(listbox, widgetInstanceManager));
	}

	protected void updateList(final Listbox listbox, final WidgetInstanceManager widgetInstanceManager)
	{
		final List<AsFacetValueData> facetValues = resolveFacetValues(widgetInstanceManager.getWidgetslot(), 0);
		listbox.setModel(createListModel(widgetInstanceManager, facetValues));
	}

	protected void renderItem(final Listitem item, final FacetValueModel model, final WidgetInstanceManager widgetInstanceManager)
	{
		final AsFacetValueData data = model.getData();
		final String displayedName = StringUtils.isBlank(data.getName()) ? ("[" + data.getValue() + "]") : data.getName();
		final String displayedValue = "[" + data.getValue() + "]";

		item.setValue(model);
		item.setSclass(FACET_VALUE_SCLASS);

		if (model.isSticky())
		{
			item.setSclass(STICKY_SCLASS);
		}

		final Listcell listcell = new Listcell();
		listcell.setParent(item);
		listcell.setTooltiptext(displayedValue);

		final Label nameLabel = new Label();
		nameLabel.setParent(listcell);
		nameLabel.setSclass(FACET_VALUE_NAME_SCLASS);
		nameLabel.setValue(displayedName);

		final Label countLabel = new Label();
		countLabel.setParent(listcell);
		countLabel.setSclass(FACET_VALUE_COUNT_SCLASS);
		countLabel.setValue(Labels.getLabel(FACET_VALUE_COUNT_LABEL, new Object[]
		{ data.getCount() }));

		final Listcell action = new Listcell();
		action.setSclass(ACTIONS_SCLASS);
		action.setParent(item);

		renderActions(action, model, widgetInstanceManager);
	}

	protected void renderActions(final Component parent, final FacetValueModel data,
			final WidgetInstanceManager widgetInstanceManager)
	{
		final ActionsMenu actionsMenu = new ActionsMenu();
		actionsMenu.setInputValue(data);
		actionsMenu.setConfig(ACTIONS_CONTEXT);
		actionsMenu.setWidgetInstanceManager(widgetInstanceManager);
		actionsMenu.setAttribute(ACTION_WIDGET_INSTANCE_MANAGER_KEY, widgetInstanceManager);
		actionsMenu.initialize();
		actionsMenu.setParent(parent);
	}

	protected ListModel createListModel(final WidgetInstanceManager widgetInstanceManager,
			final List<AsFacetValueData> facetValues)
	{

		final AbstractAsFacetConfigurationModel facetConfiguration = widgetInstanceManager.getModel()
				.getValue(CURRENT_OBJECT_KEY, AbstractAsFacetConfigurationModel.class);

		final LinkedHashMap<String, AbstractAsFacetValueConfigurationModel> stickyValuesMapping = createStickyValuesMapping(
				facetConfiguration);
		final LinkedHashMap<String, AsFacetValueData> valuesMapping = createValuesMapping(facetValues);

		final List<AsPromotedFacetValueModel> promotedValues = facetConfiguration.getPromotedValues();
		for (final AsPromotedFacetValueModel promotedValue : promotedValues)
		{
			valuesMapping.remove(promotedValue.getValue());
			stickyValuesMapping.remove(promotedValue.getValue());
		}

		final List<AsExcludedFacetValueModel> excludedValues = facetConfiguration.getExcludedValues();
		for (final AsExcludedFacetValueModel excludedValue : excludedValues)
		{
			valuesMapping.remove(excludedValue.getValue());
			stickyValuesMapping.remove(excludedValue.getValue());
		}

		final ArrayList<FacetValueModel> values = new ArrayList<>();

		for (final AbstractAsFacetValueConfigurationModel facetValueConfiguration : stickyValuesMapping.values())
		{
			final FacetValueModel value;

			final AsFacetValueData facetValue = valuesMapping.remove(facetValueConfiguration.getValue());
			if (facetValue != null)
			{
				value = convertFacetValue(facetValue);
			}
			else
			{
				value = convertFacetValueConfiguration(facetValueConfiguration);
			}

			value.setSticky(true);
			value.setModel(facetValueConfiguration);

			values.add(value);
		}

		for (final AsFacetValueData facetValue : valuesMapping.values())
		{
			values.add(convertFacetValue(facetValue));
		}

		return new ListModelList<>(values, true);
	}

	protected LinkedHashMap<String, AbstractAsFacetValueConfigurationModel> createStickyValuesMapping(
			final AbstractAsFacetConfigurationModel facetConfiguration)
	{
		final LinkedHashMap<String, AbstractAsFacetValueConfigurationModel> stickyValuesMapping = new LinkedHashMap<>();

		final List<AsPromotedFacetValueModel> originalPromotedValues = facetConfiguration.getItemModelContext()
				.getOriginalValue(AbstractAsFacetConfigurationModel.PROMOTEDVALUES);

		if (CollectionUtils.isNotEmpty(originalPromotedValues))
		{
			for (final AbstractAsFacetValueConfigurationModel promotedValue : originalPromotedValues)
			{
				stickyValuesMapping.put(promotedValue.getValue(), promotedValue);
			}
		}

		final List<AsExcludedFacetValueModel> originalExcludedValues = facetConfiguration.getItemModelContext()
				.getOriginalValue(AbstractAsFacetConfigurationModel.EXCLUDEDVALUES);

		if (CollectionUtils.isNotEmpty(originalExcludedValues))
		{
			for (final AbstractAsFacetValueConfigurationModel excludedValue : originalExcludedValues)
			{
				stickyValuesMapping.put(excludedValue.getValue(), excludedValue);
			}
		}

		return stickyValuesMapping;
	}

	protected LinkedHashMap<String, AsFacetValueData> createValuesMapping(final List<AsFacetValueData> facetValues)
	{
		final LinkedHashMap<String, AsFacetValueData> valuesMapping = new LinkedHashMap<>();

		if (CollectionUtils.isNotEmpty(facetValues))
		{
			for (final AsFacetValueData facetValue : facetValues)
			{
				valuesMapping.put(facetValue.getValue(), facetValue);
			}
		}

		return valuesMapping;
	}

	protected FacetValueModel convertFacetValue(final AsFacetValueData facetValue)
	{
		final FacetValueModel target = new FacetValueModel();
		target.setData(facetValue);
		return target;
	}

	protected FacetValueModel convertFacetValueConfiguration(final AbstractAsFacetValueConfigurationModel facetValueConfiguration)
	{
		final AsFacetValueData facetValue = new AsFacetValueData();
		facetValue.setValue(facetValueConfiguration.getValue());

		final FacetValueModel target = new FacetValueModel();
		target.setData(facetValue);
		return target;
	}

	protected List<AsFacetValueData> resolveFacetValues(final Widgetslot widgetslot, final int iterationNumber)
	{
		if (iterationNumber >= 3)
		{
			return Collections.emptyList();
		}

		final Widgetslot parentWidgetslot = WidgetTreeUIUtils.getParentWidgetslot(widgetslot);

		final TypeAwareSelectionContext selectionContext = parentWidgetslot.getViewModel().getValue(LAST_INPUT_KEY,
				TypeAwareSelectionContext.class);
		if (selectionContext == null)
		{
			return resolveFacetValues(parentWidgetslot, iterationNumber + 1);
		}

		final Object editorData = selectionContext.getParameters().get(EDITOR_DATA_KEY);

		if (editorData instanceof AbstractFacetConfigurationEditorData)
		{
			final AsFacetData facet = ((AbstractFacetConfigurationEditorData) editorData).getFacet();
			return CollectionUtils.isNotEmpty(facet.getAllValues()) ? facet.getAllValues() : facet.getValues();
		}
		else
		{
			return resolveFacetValues(parentWidgetslot, iterationNumber + 1);
		}
	}
}
