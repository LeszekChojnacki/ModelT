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
package de.hybris.platform.adaptivesearchbackoffice.actions.boostitems;

import de.hybris.platform.adaptivesearch.converters.AsItemConfigurationReverseConverterContext;
import de.hybris.platform.adaptivesearch.data.AbstractAsBoostItemConfiguration;
import de.hybris.platform.adaptivesearch.data.AsExcludedItem;
import de.hybris.platform.adaptivesearch.data.AsPromotedItem;
import de.hybris.platform.adaptivesearch.model.AbstractAsConfigurableSearchConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AsExcludedItemModel;
import de.hybris.platform.adaptivesearch.model.AsPromotedItemModel;
import de.hybris.platform.adaptivesearch.util.ContextAwareConverter;
import de.hybris.platform.adaptivesearchbackoffice.actions.configurablemultireferenceeditor.ReferenceActionUtils;
import de.hybris.platform.adaptivesearchbackoffice.data.AbstractBoostItemConfigurationEditorData;
import de.hybris.platform.adaptivesearchbackoffice.data.AbstractEditorData;
import de.hybris.platform.servicelayer.model.ModelService;

import javax.annotation.Resource;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.engine.impl.AbstractComponentWidgetAdapterAware;


public class OverrideBoostItemAction extends AbstractComponentWidgetAdapterAware
		implements CockpitAction<AbstractEditorData, Object>
{
	@Resource
	private ModelService modelService;

	@Resource
	private ContextAwareConverter<AsPromotedItem, AsPromotedItemModel, AsItemConfigurationReverseConverterContext> asPromotedItemReverseConverter;

	@Resource
	private ContextAwareConverter<AsExcludedItem, AsExcludedItemModel, AsItemConfigurationReverseConverterContext> asExcludedItemReverseConverter;

	@Override
	public ActionResult<Object> perform(final ActionContext<AbstractEditorData> ctx)
	{
		final AbstractEditorData data = ctx.getData();
		final AbstractAsBoostItemConfiguration boostItemConfiguration = ((AbstractBoostItemConfigurationEditorData) data)
				.getBoostItemConfiguration();
		final AbstractAsConfigurableSearchConfigurationModel searchConfiguration = ReferenceActionUtils.resolveCurrentObject(ctx,
				AbstractAsConfigurableSearchConfigurationModel.class);

		final AsItemConfigurationReverseConverterContext context = new AsItemConfigurationReverseConverterContext();
		context.setCatalogVersion(searchConfiguration.getCatalogVersion());
		context.setParentConfiguration(searchConfiguration);

		if (boostItemConfiguration instanceof AsPromotedItem)
		{
			final AsPromotedItemModel promotedItem = asPromotedItemReverseConverter.convert((AsPromotedItem) boostItemConfiguration,
					context);
			modelService.save(promotedItem);
		}
		else if (boostItemConfiguration instanceof AsExcludedItem)
		{
			final AsExcludedItemModel excludedItem = asExcludedItemReverseConverter.convert((AsExcludedItem) boostItemConfiguration,
					context);
			modelService.save(excludedItem);
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

		return data instanceof AbstractBoostItemConfigurationEditorData
				&& ((AbstractBoostItemConfigurationEditorData) data).getBoostItemConfiguration() != null;
	}
}
