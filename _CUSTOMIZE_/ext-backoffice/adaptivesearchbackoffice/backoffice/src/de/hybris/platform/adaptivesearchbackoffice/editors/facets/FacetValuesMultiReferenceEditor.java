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

import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.EDITOR_WIDGET_INSTANCE_MANAGER_KEY;

import de.hybris.platform.adaptivesearch.data.AsFacetData;
import de.hybris.platform.adaptivesearch.data.AsFacetValueData;
import de.hybris.platform.adaptivesearch.model.AbstractAsFacetValueConfigurationModel;
import de.hybris.platform.adaptivesearchbackoffice.data.AbstractFacetConfigurationEditorData;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;

import com.hybris.cockpitng.components.Widgetslot;
import com.hybris.cockpitng.data.TypeAwareSelectionContext;
import com.hybris.cockpitng.editor.defaultmultireferenceeditor.DefaultMultiReferenceEditor;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.util.WidgetTreeUIUtils;


public class FacetValuesMultiReferenceEditor<T> extends DefaultMultiReferenceEditor<T>
{
	protected static final String LAST_INPUT_KEY = "lastInput";
	protected static final String EDITOR_DATA_KEY = "editorData";

	@Override
	public String getStringRepresentationOfObject(final T obj)
	{
		if (obj instanceof AbstractAsFacetValueConfigurationModel)
		{
			final AbstractAsFacetValueConfigurationModel facetValueConfiguration = (AbstractAsFacetValueConfigurationModel) obj;

			final WidgetInstanceManager widgetInstanceManager = (WidgetInstanceManager) getEditorContext()
					.getParameter(EDITOR_WIDGET_INSTANCE_MANAGER_KEY);
			final List<AsFacetValueData> facetValues = resolveFacetValues(widgetInstanceManager.getWidgetslot(), 0);

			if (CollectionUtils.isNotEmpty(facetValues))
			{
				final Optional<AsFacetValueData> optionalFacetValue = facetValues.stream()
						.filter(facetValueData -> facetValueData.getValue().equals(facetValueConfiguration.getValue())).findFirst();

				if (optionalFacetValue.isPresent())
				{
					final AsFacetValueData facetValue = optionalFacetValue.get();
					return facetValue.getName() + " (" + facetValue.getCount() + ")";
				}
			}
		}

		return super.getStringRepresentationOfObject(obj);
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
