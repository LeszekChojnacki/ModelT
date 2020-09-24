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

import de.hybris.platform.adaptivesearch.converters.AsItemConfigurationReverseConverterContext;
import de.hybris.platform.adaptivesearch.data.AbstractAsFacetConfiguration;
import de.hybris.platform.adaptivesearch.data.AsExcludedFacet;
import de.hybris.platform.adaptivesearch.data.AsFacet;
import de.hybris.platform.adaptivesearch.data.AsPromotedFacet;
import de.hybris.platform.adaptivesearch.model.AbstractAsConfigurableSearchConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AsExcludedFacetModel;
import de.hybris.platform.adaptivesearch.model.AsFacetModel;
import de.hybris.platform.adaptivesearch.model.AsPromotedFacetModel;
import de.hybris.platform.adaptivesearch.util.ContextAwareConverter;
import de.hybris.platform.adaptivesearchbackoffice.actions.configurablemultireferenceeditor.ReferenceActionUtils;
import de.hybris.platform.adaptivesearchbackoffice.data.AbstractEditorData;
import de.hybris.platform.adaptivesearchbackoffice.data.AbstractFacetConfigurationEditorData;
import de.hybris.platform.servicelayer.model.ModelService;

import javax.annotation.Resource;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.engine.impl.AbstractComponentWidgetAdapterAware;


public class OverrideFacetAction extends AbstractComponentWidgetAdapterAware implements CockpitAction<AbstractEditorData, Object>
{
	@Resource
	private ModelService modelService;

	@Resource
	private ContextAwareConverter<AsPromotedFacet, AsPromotedFacetModel, AsItemConfigurationReverseConverterContext> asPromotedFacetReverseConverter;

	@Resource
	private ContextAwareConverter<AsFacet, AsFacetModel, AsItemConfigurationReverseConverterContext> asFacetReverseConverter;

	@Resource
	private ContextAwareConverter<AsExcludedFacet, AsExcludedFacetModel, AsItemConfigurationReverseConverterContext> asExcludedFacetReverseConverter;

	@Override
	public ActionResult<Object> perform(final ActionContext<AbstractEditorData> ctx)
	{
		final AbstractEditorData data = ctx.getData();
		final AbstractAsFacetConfiguration facetConfiguration = ((AbstractFacetConfigurationEditorData) data)
				.getFacetConfiguration();
		final AbstractAsConfigurableSearchConfigurationModel searchConfiguration = ReferenceActionUtils.resolveCurrentObject(ctx,
				AbstractAsConfigurableSearchConfigurationModel.class);

		final AsItemConfigurationReverseConverterContext context = new AsItemConfigurationReverseConverterContext();
		context.setCatalogVersion(searchConfiguration.getCatalogVersion());
		context.setParentConfiguration(searchConfiguration);

		if (facetConfiguration instanceof AsPromotedFacet)
		{
			final AsPromotedFacetModel promotedFacet = asPromotedFacetReverseConverter.convert((AsPromotedFacet) facetConfiguration,
					context);
			modelService.save(promotedFacet);
		}
		else if (facetConfiguration instanceof AsFacet)
		{
			final AsFacetModel facet = asFacetReverseConverter.convert((AsFacet) facetConfiguration, context);
			modelService.save(facet);
		}
		else if (facetConfiguration instanceof AsExcludedFacet)
		{
			final AsExcludedFacetModel excludedFacet = asExcludedFacetReverseConverter.convert((AsExcludedFacet) facetConfiguration,
					context);
			modelService.save(excludedFacet);
		}
		else
		{
			return new ActionResult<>(ActionResult.ERROR);
		}

		ReferenceActionUtils.refreshCurrentObject(ctx);

		return new ActionResult<>(ActionResult.SUCCESS);
	}

	@Override
	public boolean canPerform(final ActionContext<AbstractEditorData> ctx)
	{
		final AbstractEditorData data = ctx.getData();
		if (data == null || data.isFromSearchConfiguration())
		{
			return false;
		}

		return data instanceof AbstractFacetConfigurationEditorData
				&& ((AbstractFacetConfigurationEditorData) data).getFacetConfiguration() != null;
	}
}
