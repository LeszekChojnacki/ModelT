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
package de.hybris.platform.adaptivesearchbackoffice.actions.results.discardoverridepromoteditem;

import de.hybris.platform.adaptivesearchbackoffice.actions.results.unpromoteitem.UnpromoteItemAction;
import de.hybris.platform.adaptivesearchbackoffice.widgets.searchresultbrowser.DocumentModel;

import com.hybris.cockpitng.actions.ActionContext;


/**
 * Cockpit action responsible for overriding promoted search result.
 */
public class DiscardOverridePromotedItemAction extends UnpromoteItemAction
{
	@Override
	public boolean canPerform(final ActionContext<DocumentModel> ctx)
	{
		final DocumentModel data = ctx.getData();
		if (data == null || data.getPk() == null)
		{
			return false;
		}

		return data.isPromoted() && data.isFromSearchConfiguration() && data.isOverride();
	}
}
