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
package de.hybris.platform.adaptivesearchbackoffice.actions.results.rankitemfirst;

import de.hybris.platform.adaptivesearch.model.AbstractAsConfigurableSearchConfigurationModel;
import de.hybris.platform.adaptivesearch.services.AsConfigurationService;
import de.hybris.platform.adaptivesearchbackoffice.actions.results.AbstractResultAction;
import de.hybris.platform.adaptivesearchbackoffice.data.NavigationContextData;
import de.hybris.platform.adaptivesearchbackoffice.data.SearchContextData;
import de.hybris.platform.adaptivesearchbackoffice.facades.AsSearchConfigurationFacade;
import de.hybris.platform.adaptivesearchbackoffice.widgets.searchresultbrowser.DocumentModel;

import javax.annotation.Resource;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;


/**
 * Action that moves a promoted item to the top of the list
 */
public class RankItemFirstAction extends AbstractResultAction
{

	@Resource
	protected AsSearchConfigurationFacade asSearchConfigurationFacade;

	@Resource
	protected AsConfigurationService asConfigurationService;

	@Override
	public ActionResult<Object> perform(final ActionContext<DocumentModel> ctx)
	{
		final DocumentModel data = ctx.getData();

		if (!isValidContextParams(ctx))
		{
			return new ActionResult<>(ActionResult.ERROR);
		}

		final NavigationContextData navigationContext = getNavigationContext(ctx);
		final SearchContextData searchContext = getSearchContext(ctx);

		final AbstractAsConfigurableSearchConfigurationModel searchConfiguration = asSearchConfigurationFacade
				.getOrCreateSearchConfiguration(navigationContext, searchContext);

		asConfigurationService.rankAfterConfiguration(searchConfiguration,
				AbstractAsConfigurableSearchConfigurationModel.PROMOTEDITEMS, null, data.getPromotedItemUid());

		refreshSearchResults(ctx);

		return new ActionResult<>(ActionResult.SUCCESS);
	}

	@Override
	public boolean canPerform(final ActionContext<DocumentModel> ctx)
	{
		final DocumentModel data = ctx.getData();
		if (data == null || !data.isPromoted() || !data.isShowOnTop() || data.getPromotedItemUid() == null)
		{
			return false;
		}

		final NavigationContextData navigationContext = getNavigationContext(ctx);
		final SearchContextData searchContext = getSearchContext(ctx);

		final AbstractAsConfigurableSearchConfigurationModel searchConfiguration = asSearchConfigurationFacade
				.getOrCreateSearchConfiguration(navigationContext, searchContext);


		// if the promoted items list is not empty, and the object is not already the last one
		return searchConfiguration.getPromotedItems() != null && !searchConfiguration.getPromotedItems().isEmpty()
				&& !data.getPromotedItemUid().equals(searchConfiguration.getPromotedItems().get(0).getUid())
				&& data.isFromSearchConfiguration();

	}

}
