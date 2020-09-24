/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 *
 */

package de.hybris.platform.warehousingbackoffice.actions.stockadjustment;

import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.warehousing.enums.AsnStatus;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.engine.impl.AbstractComponentWidgetAdapterAware;


/**
 * Access popup to create stock level adjustment(s)
 */
public class StockAdjustmentAction extends AbstractComponentWidgetAdapterAware
		implements CockpitAction<StockLevelModel, StockLevelModel>
{
	protected static final String SOCKET_OUT_CONTEXT = "stockAdjustmentContext";

	@Override
	public ActionResult<StockLevelModel> perform(ActionContext<StockLevelModel> actionContext)
	{
		sendOutput(SOCKET_OUT_CONTEXT, actionContext.getData());
		final ActionResult<StockLevelModel> actionResult = new ActionResult<>(ActionResult.SUCCESS);
		return actionResult;
	}

	@Override
	public boolean canPerform(ActionContext<StockLevelModel> ctx)
	{
		final StockLevelModel stockLevel = ctx.getData();
		boolean physicallyInStock = true;
		if (stockLevel != null && stockLevel.getAsnEntry() != null)
		{
			physicallyInStock = AsnStatus.RECEIVED.equals(stockLevel.getAsnEntry().getAsn().getStatus());
		}
		return stockLevel != null && stockLevel.getWarehouse() != null && !(stockLevel.getWarehouse().isExternal())
				&& physicallyInStock;
	}

	@Override
	public boolean needsConfirmation(ActionContext<StockLevelModel> ctx)
	{
		return false;
	}

	@Override
	public String getConfirmationMessage(ActionContext<StockLevelModel> ctx)
	{
		return null;
	}
}
