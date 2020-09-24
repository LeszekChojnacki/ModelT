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
package de.hybris.platform.adaptivesearchbackoffice.actions.results;

import de.hybris.platform.adaptivesearchbackoffice.data.NavigationContextData;
import de.hybris.platform.adaptivesearchbackoffice.data.SearchContextData;
import de.hybris.platform.adaptivesearchbackoffice.widgets.searchresultbrowser.DocumentModel;
import de.hybris.platform.adaptivesearchbackoffice.widgets.searchresultbrowser.SearchResultBrowserViewModel;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.CockpitAction;


public abstract class AbstractResultAction implements CockpitAction<DocumentModel, Object>
{
	protected static final String VIEW_MODEL_PARAM = "viewModel";

	protected SearchResultBrowserViewModel getViewModel(final ActionContext<DocumentModel> ctx)
	{
		final Object viewModel = ctx.getParameter(VIEW_MODEL_PARAM);

		if (viewModel instanceof SearchResultBrowserViewModel)
		{
			return (SearchResultBrowserViewModel) viewModel;
		}

		return null;
	}

	protected NavigationContextData getNavigationContext(final ActionContext<DocumentModel> ctx)
	{
		final SearchResultBrowserViewModel viewModel = getViewModel(ctx);

		if (viewModel != null && viewModel.getSearchResult() != null && viewModel.getSearchResult().getNavigationContext() != null)
		{
			return viewModel.getSearchResult().getNavigationContext();
		}

		return null;
	}

	protected SearchContextData getSearchContext(final ActionContext<DocumentModel> ctx)
	{
		final SearchResultBrowserViewModel viewModel = getViewModel(ctx);

		if (viewModel != null && viewModel.getSearchResult() != null && viewModel.getSearchResult().getSearchContext() != null)
		{
			return viewModel.getSearchResult().getSearchContext();
		}

		return null;
	}

	protected boolean isValidContextParams(final ActionContext<DocumentModel> ctx)
	{
		final SearchResultBrowserViewModel viewModel = getViewModel(ctx);

		return ctx.getData() != null && viewModel != null && viewModel.getNavigationContext() != null
				&& viewModel.getSearchContext() != null;
	}

	protected void refreshSearchResults(final ActionContext<DocumentModel> ctx)
	{
		final SearchResultBrowserViewModel viewModel = getViewModel(ctx);

		if (viewModel != null)
		{
			viewModel.refreshSearchResults();
		}
	}
}
