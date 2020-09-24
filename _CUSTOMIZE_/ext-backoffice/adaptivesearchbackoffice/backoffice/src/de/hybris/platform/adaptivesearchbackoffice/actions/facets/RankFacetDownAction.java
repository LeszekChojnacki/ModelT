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
/**
 *
 */
package de.hybris.platform.adaptivesearchbackoffice.actions.facets;

import static de.hybris.platform.adaptivesearch.model.AbstractAsConfigurableSearchConfigurationModel.EXCLUDEDFACETS;
import static de.hybris.platform.adaptivesearch.model.AbstractAsConfigurableSearchConfigurationModel.FACETS;
import static de.hybris.platform.adaptivesearch.model.AbstractAsConfigurableSearchConfigurationModel.PROMOTEDFACETS;

import de.hybris.platform.adaptivesearch.model.AbstractAsConfigurableSearchConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AbstractAsFacetConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AsExcludedFacetModel;
import de.hybris.platform.adaptivesearch.model.AsFacetModel;
import de.hybris.platform.adaptivesearch.model.AsPromotedFacetModel;
import de.hybris.platform.adaptivesearch.services.AsConfigurationService;
import de.hybris.platform.adaptivesearchbackoffice.actions.configurablemultireferenceeditor.ReferenceActionUtils;
import de.hybris.platform.adaptivesearchbackoffice.data.AbstractEditorData;

import javax.annotation.Resource;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.engine.impl.AbstractComponentWidgetAdapterAware;


public class RankFacetDownAction extends AbstractComponentWidgetAdapterAware implements CockpitAction<AbstractEditorData, Object>
{
	@Resource
	private AsConfigurationService asConfigurationService;

	@Override
	public ActionResult<Object> perform(final ActionContext<AbstractEditorData> ctx)
	{
		final AbstractEditorData data = ctx.getData();
		final AbstractAsFacetConfigurationModel facet = (AbstractAsFacetConfigurationModel) data.getModel();
		final AbstractAsConfigurableSearchConfigurationModel searchConfiguration = ReferenceActionUtils.resolveCurrentObject(ctx,
				AbstractAsConfigurableSearchConfigurationModel.class);

		if (facet instanceof AsPromotedFacetModel)
		{
			asConfigurationService.rerankConfiguration(searchConfiguration, PROMOTEDFACETS, facet.getUid(), 1);
		}
		else if (facet instanceof AsFacetModel)
		{
			asConfigurationService.rerankConfiguration(searchConfiguration, FACETS, facet.getUid(), 1);
		}
		else if (facet instanceof AsExcludedFacetModel)
		{
			asConfigurationService.rerankConfiguration(searchConfiguration, EXCLUDEDFACETS, facet.getUid(), 1);
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
		if (data == null || !data.isRankDownAllowed())
		{
			return false;
		}

		final Object model = data.getModel();
		if (!(model instanceof AsPromotedFacetModel || model instanceof AsFacetModel || model instanceof AsExcludedFacetModel))
		{
			return false;
		}

		return data.isFromSearchConfiguration();
	}
}
