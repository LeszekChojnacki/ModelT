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
package de.hybris.platform.adaptivesearchbackoffice.actions.sorts;

import static de.hybris.platform.adaptivesearch.model.AbstractAsConfigurableSearchConfigurationModel.EXCLUDEDSORTS;
import static de.hybris.platform.adaptivesearch.model.AbstractAsConfigurableSearchConfigurationModel.PROMOTEDSORTS;
import static de.hybris.platform.adaptivesearch.model.AbstractAsConfigurableSearchConfigurationModel.SORTS;

import de.hybris.platform.adaptivesearch.model.AbstractAsConfigurableSearchConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AbstractAsSortConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AsPromotedSortModel;
import de.hybris.platform.adaptivesearch.model.AsSortModel;
import de.hybris.platform.adaptivesearch.services.AsConfigurationService;
import de.hybris.platform.adaptivesearchbackoffice.actions.configurablemultireferenceeditor.ReferenceActionUtils;
import de.hybris.platform.adaptivesearchbackoffice.data.AbstractEditorData;

import javax.annotation.Resource;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.engine.impl.AbstractComponentWidgetAdapterAware;


public class ExcludeSortAction extends AbstractComponentWidgetAdapterAware implements CockpitAction<AbstractEditorData, Object>
{
	@Resource
	private AsConfigurationService asConfigurationService;

	@Override
	public ActionResult<Object> perform(final ActionContext<AbstractEditorData> ctx)
	{
		final AbstractEditorData data = ctx.getData();
		final AbstractAsSortConfigurationModel sort = (AbstractAsSortConfigurationModel) data.getModel();
		final AbstractAsConfigurableSearchConfigurationModel searchConfiguration = ReferenceActionUtils.resolveCurrentObject(ctx,
				AbstractAsConfigurableSearchConfigurationModel.class);

		if (sort instanceof AsPromotedSortModel)
		{
			asConfigurationService.moveConfiguration(searchConfiguration, PROMOTEDSORTS, EXCLUDEDSORTS, sort.getUid());
		}
		else if (sort instanceof AsSortModel)
		{
			asConfigurationService.moveConfiguration(searchConfiguration, SORTS, EXCLUDEDSORTS, sort.getUid());
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
		if (data == null)
		{
			return false;
		}

		final Object model = data.getModel();
		if (!(model instanceof AsPromotedSortModel || model instanceof AsSortModel))
		{
			return false;
		}

		return data.isFromSearchConfiguration();
	}
}
