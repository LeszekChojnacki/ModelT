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
package de.hybris.platform.adaptivesearchbackoffice.actions.results.excludeitem;

import de.hybris.platform.adaptivesearch.model.AbstractAsConfigurableSearchConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AsExcludedItemModel;
import de.hybris.platform.adaptivesearchbackoffice.actions.results.AbstractResultAction;
import de.hybris.platform.adaptivesearchbackoffice.data.NavigationContextData;
import de.hybris.platform.adaptivesearchbackoffice.data.SearchContextData;
import de.hybris.platform.adaptivesearchbackoffice.facades.AsSearchConfigurationFacade;
import de.hybris.platform.adaptivesearchbackoffice.widgets.searchresultbrowser.DocumentModel;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;


/**
 * Cockpit action responsible for excluding a search result.
 */
public class ExcludeItemAction extends AbstractResultAction
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

		excludeResult(ctx);
		refreshSearchResults(ctx);

		return new ActionResult<>(ActionResult.SUCCESS);
	}

	protected void excludeResult(final ActionContext<DocumentModel> ctx)
	{
		final NavigationContextData navigationContext = getNavigationContext(ctx);
		final SearchContextData searchContext = getSearchContext(ctx);

		final AbstractAsConfigurableSearchConfigurationModel searchConfiguration = asSearchConfigurationFacade
				.getOrCreateSearchConfiguration(navigationContext, searchContext);
		final List<AsExcludedItemModel> newExcludedItems = new ArrayList<>();

		if (CollectionUtils.isNotEmpty(searchConfiguration.getExcludedItems()))
		{
			newExcludedItems.addAll(searchConfiguration.getExcludedItems());

			// YTODO check if it already exists in the list, show a warning message in that case
		}

		final ItemModel item = modelService.get(ctx.getData().getPk());

		final AsExcludedItemModel newExcludedItem = new AsExcludedItemModel();
		newExcludedItem.setSearchConfiguration(searchConfiguration);
		newExcludedItem.setCatalogVersion(searchConfiguration.getCatalogVersion());
		newExcludedItem.setItem(item);

		newExcludedItems.add(newExcludedItem);

		searchConfiguration.setExcludedItems(newExcludedItems);
		modelService.save(searchConfiguration);
	}

	@Override
	public boolean canPerform(final ActionContext<DocumentModel> ctx)
	{
		final DocumentModel data = ctx.getData();
		if (data == null || data.getPk() == null)
		{
			return false;
		}

		return !data.isPromoted() && !data.isFromSearchConfiguration();
	}
}
