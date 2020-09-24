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
package de.hybris.platform.adaptivesearchbackoffice.actions.facets;

import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.CURRENT_OBJECT_KEY;
import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.ACTION_WIDGET_INSTANCE_MANAGER_KEY;

import de.hybris.platform.adaptivesearch.model.AbstractAsFacetConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AsExcludedFacetValueModel;
import de.hybris.platform.adaptivesearch.model.AsPromotedFacetValueModel;
import de.hybris.platform.adaptivesearchbackoffice.editors.facets.FacetValueModel;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.engine.impl.AbstractComponentWidgetAdapterAware;


public class ExcludeFacetValueAction extends AbstractComponentWidgetAdapterAware implements CockpitAction<FacetValueModel, Object>
{
	protected static final String EXCLUDED_OBJECT_EXPRESSION = "currentObject.excludedValues";

	@Resource
	private ModelService modelService;

	@Override
	public ActionResult<Object> perform(final ActionContext<FacetValueModel> ctx)
	{
		final FacetValueModel facetValue = ctx.getData();

		final AbstractAsFacetConfigurationModel facetConfiguration = resolveCurrentObject(ctx);
		final AsExcludedFacetValueModel excludedFacetValue;

		if (facetValue.getModel() instanceof AsExcludedFacetValueModel)
		{
			excludedFacetValue = (AsExcludedFacetValueModel) facetValue.getModel();
		}
		else if (facetValue.getModel() instanceof AsPromotedFacetValueModel)
		{
			excludedFacetValue = modelService.clone(facetValue.getModel(), AsExcludedFacetValueModel.class);
		}
		else
		{
			excludedFacetValue = modelService.create(AsExcludedFacetValueModel.class);
			excludedFacetValue.setFacetConfiguration(facetConfiguration);
			excludedFacetValue.setCatalogVersion(facetConfiguration.getCatalogVersion());
			excludedFacetValue.setValue(facetValue.getData().getValue());
		}

		final List<AsExcludedFacetValueModel> excludedValues = new ArrayList<>(facetConfiguration.getExcludedValues());
		excludedValues.add(excludedFacetValue);

		updateCurrentObject(ctx, excludedValues);

		return new ActionResult<>(ActionResult.SUCCESS);
	}

	protected AbstractAsFacetConfigurationModel resolveCurrentObject(final ActionContext<FacetValueModel> ctx)
	{
		final WidgetInstanceManager widgetInstanceManager = (WidgetInstanceManager) ctx.getParameter(ACTION_WIDGET_INSTANCE_MANAGER_KEY);

		return widgetInstanceManager.getModel().getValue(CURRENT_OBJECT_KEY,
				AbstractAsFacetConfigurationModel.class);
	}

	protected void updateCurrentObject(final ActionContext<FacetValueModel> ctx,
			final List<AsExcludedFacetValueModel> excludedValues)
	{
		final WidgetInstanceManager widgetInstanceManager = (WidgetInstanceManager) ctx.getParameter(ACTION_WIDGET_INSTANCE_MANAGER_KEY);

		widgetInstanceManager.getModel().setValue(EXCLUDED_OBJECT_EXPRESSION, excludedValues);
	}
}
