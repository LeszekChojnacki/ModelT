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
package de.hybris.platform.warehousingbackoffice.actions.reallocate;


import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.engine.impl.AbstractComponentWidgetAdapterAware;

import de.hybris.platform.basecommerce.enums.ConsignmentStatus;
import de.hybris.platform.commerceservices.model.PickUpDeliveryModeModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;

import javax.annotation.Resource;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;


/**
 * Action to open a reallocate popup in order to manually or automatically reallocate all or partial part of consignment
 * entries using the UI.
 */
public class ReallocateAction extends AbstractComponentWidgetAdapterAware
		implements CockpitAction<ConsignmentModel, ConsignmentModel>
{

	protected static final String CAPTURE_PAYMENT_ON_CONSIGNMENT = "warehousing.capturepaymentonconsignment";
	protected static final String SOCKET_OUT_CONTEXT = "reallocateContext";

	@Resource
	private List<ConsignmentStatus> reallocableConsignmentStatuses;
	@Resource
	private ConfigurationService configurationService;

	@Override
	public boolean canPerform(final ActionContext<ConsignmentModel> actionContext)
	{
		final Object data = actionContext.getData();
		ConsignmentModel consignment;
		boolean decision = false;
		boolean captureOnConsignmentReallocationAllowed = true;

		if (data instanceof ConsignmentModel)
		{
			consignment = (ConsignmentModel) data;
			if (!CollectionUtils.isEmpty(consignment.getConsignmentEntries()) && !(consignment
					.getDeliveryMode() instanceof PickUpDeliveryModeModel) && (consignment.getFulfillmentSystemConfig() == null))
			{
				decision = consignment.getConsignmentEntries().stream()
						.anyMatch(consignmentEntry -> consignmentEntry.getQuantityPending() > 0);
			}

			if (getConfigurationService().getConfiguration().getBoolean(CAPTURE_PAYMENT_ON_CONSIGNMENT, Boolean.FALSE))
			{
				captureOnConsignmentReallocationAllowed = getReallocableConsignmentStatuses().contains(consignment.getStatus());
			}
		}

		return decision && captureOnConsignmentReallocationAllowed;
	}

	@Override
	public String getConfirmationMessage(final ActionContext<ConsignmentModel> actionContext)
	{
		return null;
	}

	@Override
	public boolean needsConfirmation(final ActionContext<ConsignmentModel> actionContext)
	{
		return false;
	}

	@Override
	public ActionResult<ConsignmentModel> perform(final ActionContext<ConsignmentModel> actionContext)
	{
		sendOutput(SOCKET_OUT_CONTEXT, actionContext.getData());
		final ActionResult<ConsignmentModel> actionResult = new ActionResult<>(ActionResult.SUCCESS);
		return actionResult;
	}

	protected ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	protected List<ConsignmentStatus> getReallocableConsignmentStatuses()
	{
		return this.reallocableConsignmentStatuses;
	}
}
