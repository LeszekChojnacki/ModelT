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

import de.hybris.platform.adaptivesearch.converters.AsItemConfigurationReverseConverterContext;
import de.hybris.platform.adaptivesearch.data.AbstractAsSortConfiguration;
import de.hybris.platform.adaptivesearch.data.AsExcludedSort;
import de.hybris.platform.adaptivesearch.data.AsPromotedSort;
import de.hybris.platform.adaptivesearch.data.AsSort;
import de.hybris.platform.adaptivesearch.model.AbstractAsConfigurableSearchConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AsExcludedSortModel;
import de.hybris.platform.adaptivesearch.model.AsPromotedSortModel;
import de.hybris.platform.adaptivesearch.model.AsSortModel;
import de.hybris.platform.adaptivesearch.util.ContextAwareConverter;
import de.hybris.platform.adaptivesearchbackoffice.actions.configurablemultireferenceeditor.ReferenceActionUtils;
import de.hybris.platform.adaptivesearchbackoffice.data.AbstractEditorData;
import de.hybris.platform.adaptivesearchbackoffice.data.AbstractSortConfigurationEditorData;
import de.hybris.platform.servicelayer.model.ModelService;

import javax.annotation.Resource;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.engine.impl.AbstractComponentWidgetAdapterAware;


public class OverrideSortAction extends AbstractComponentWidgetAdapterAware implements CockpitAction<AbstractEditorData, Object>
{
	@Resource
	private ModelService modelService;

	@Resource
	private ContextAwareConverter<AsPromotedSort, AsPromotedSortModel, AsItemConfigurationReverseConverterContext> asPromotedSortReverseConverter;

	@Resource
	private ContextAwareConverter<AsSort, AsSortModel, AsItemConfigurationReverseConverterContext> asSortReverseConverter;

	@Resource
	private ContextAwareConverter<AsExcludedSort, AsExcludedSortModel, AsItemConfigurationReverseConverterContext> asExcludedSortReverseConverter;

	@Override
	public ActionResult<Object> perform(final ActionContext<AbstractEditorData> ctx)
	{
		final AbstractEditorData data = ctx.getData();
		final AbstractAsSortConfiguration sortConfiguration = ((AbstractSortConfigurationEditorData) data).getSortConfiguration();
		final AbstractAsConfigurableSearchConfigurationModel searchConfiguration = ReferenceActionUtils.resolveCurrentObject(ctx,
				AbstractAsConfigurableSearchConfigurationModel.class);

		final AsItemConfigurationReverseConverterContext context = new AsItemConfigurationReverseConverterContext();
		context.setCatalogVersion(searchConfiguration.getCatalogVersion());
		context.setParentConfiguration(searchConfiguration);

		if (sortConfiguration instanceof AsPromotedSort)
		{
			final AsPromotedSortModel promotedSort = asPromotedSortReverseConverter.convert((AsPromotedSort) sortConfiguration,
					context);
			modelService.save(promotedSort);
		}
		else if (sortConfiguration instanceof AsSort)
		{
			final AsSortModel sort = asSortReverseConverter.convert((AsSort) sortConfiguration, context);
			modelService.save(sort);
		}
		else if (sortConfiguration instanceof AsExcludedSort)
		{
			final AsExcludedSortModel excludedSort = asExcludedSortReverseConverter.convert((AsExcludedSort) sortConfiguration,
					context);
			modelService.save(excludedSort);
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

		return data instanceof AbstractSortConfigurationEditorData
				&& ((AbstractSortConfigurationEditorData) data).getSortConfiguration() != null;
	}
}
