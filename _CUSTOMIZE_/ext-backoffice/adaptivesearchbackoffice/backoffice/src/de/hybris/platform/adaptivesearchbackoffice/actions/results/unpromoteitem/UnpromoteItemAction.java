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
package de.hybris.platform.adaptivesearchbackoffice.actions.results.unpromoteitem;

import de.hybris.platform.adaptivesearch.model.AbstractAsConfigurableSearchConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AsPromotedItemModel;
import de.hybris.platform.adaptivesearchbackoffice.actions.results.AbstractResultAction;
import de.hybris.platform.adaptivesearchbackoffice.data.NavigationContextData;
import de.hybris.platform.adaptivesearchbackoffice.data.SearchContextData;
import de.hybris.platform.adaptivesearchbackoffice.facades.AsSearchConfigurationFacade;
import de.hybris.platform.adaptivesearchbackoffice.widgets.searchresultbrowser.DocumentModel;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;


/**
 * Action responsible for unpromoting a search result.
 */
public class UnpromoteItemAction extends AbstractResultAction
{
	@Resource
	protected AsSearchConfigurationFacade asSearchConfigurationFacade;

	@Resource
	protected ModelService modelService;

	@Override
	public ActionResult<Object> perform(final ActionContext<DocumentModel> ctx)
	{
		if (!isValidContextParams(ctx))
		{
			return new ActionResult<>(ActionResult.ERROR);
		}

		final NavigationContextData navigationContext = getNavigationContext(ctx);
		final SearchContextData searchContext = getSearchContext(ctx);

		final AbstractAsConfigurableSearchConfigurationModel searchConfiguration = asSearchConfigurationFacade
				.getOrCreateSearchConfiguration(navigationContext, searchContext);
		final List<AsPromotedItemModel> newPromotedItems = new ArrayList<>();

		if (CollectionUtils.isNotEmpty(searchConfiguration.getPromotedItems()))
		{
			newPromotedItems.addAll(searchConfiguration.getPromotedItems());
		}

		final boolean removed = newPromotedItems
				.removeIf(promotedItem -> Objects.equals(ctx.getData().getPk(), promotedItem.getItem().getPk()));
		if (!removed)
		{
			return new ActionResult<>(ActionResult.ERROR, ctx.getData());
		}

		searchConfiguration.setPromotedItems(newPromotedItems);
		modelService.save(searchConfiguration);

		refreshSearchResults(ctx);

		return new ActionResult<>(ActionResult.SUCCESS);
	}

	@Override
	public boolean canPerform(final ActionContext<DocumentModel> ctx)
	{
		final DocumentModel data = ctx.getData();
		if (data == null || data.getPk() == null)
		{
			return false;
		}

		return data.isPromoted() && data.isFromSearchConfiguration() && !data.isOverride();
	}
}
