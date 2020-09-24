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
package de.hybris.platform.warehousingbackoffice.actions.ship;

import de.hybris.platform.basecommerce.enums.ConsignmentStatus;
import de.hybris.platform.commerceservices.model.PickUpDeliveryModeModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.warehousing.process.BusinessProcessException;
import de.hybris.platform.warehousing.shipping.service.WarehousingShippingService;
import de.hybris.platform.warehousingbackoffice.actions.util.AbstractConsignmentWorkflow;
import de.hybris.platform.workflow.exceptions.WorkflowActionDecideException;
import de.hybris.warehousingbackoffice.constants.WarehousingBackofficeConstants;

import javax.annotation.Resource;

import java.util.Objects;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import org.zkoss.lang.Strings;


/**
 * This action confirms shipping by creating shipped entries for the requested consignment entries.
 */
public class ConfirmShippedConsignmentAction extends AbstractConsignmentWorkflow
		implements CockpitAction<ConsignmentModel, ConsignmentModel>
{
	public static final String SUCCESS_MESSAGE = "ship.action.message.success";
	public static final String FAILED_MESSAGE = "ship.action.message.failed";

	@Resource
	private WarehousingShippingService warehousingShippingService;
	@Resource
	private NotificationService notificationService;

	@Override
	public boolean canPerform(final ActionContext<ConsignmentModel> actionContext)
	{
		final Object data = actionContext.getData();

		ConsignmentModel consignment = null;
		if (data instanceof ConsignmentModel)
		{
			consignment = (ConsignmentModel) data;
		}

		// A consignment is not shippable when it is a pickup order or when there is no quantity pending
		// or the consignmentStatus or the orderstatus is not correct
		// or when the consignment is delegated to an external system
		if (Objects.isNull(consignment) || (consignment.getDeliveryMode() instanceof PickUpDeliveryModeModel)
				|| isFulfillmentExternal(consignment) || !getWarehousingShippingService().isConsignmentConfirmable(consignment))
		{
			return false;
		}
		return true;
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
		try
		{
			getWarehousingShippingService().confirmShipConsignment(actionContext.getData());
		}
		catch (final BusinessProcessException | WorkflowActionDecideException e) //NOSONAR
		{
			getNotificationService()
					.notifyUser(Strings.EMPTY, WarehousingBackofficeConstants.NOTIFICATION_TYPE, NotificationEvent.Level.FAILURE,
							actionContext.getLabel(FAILED_MESSAGE));
			final ActionResult<ConsignmentModel> actionResult = new ActionResult<>(ActionResult.ERROR);
			actionResult.getStatusFlags().add(ActionResult.StatusFlag.OBJECT_PERSISTED);
			return actionResult;
		}
		return getConsignmentActionResult(actionContext, SUCCESS_MESSAGE, FAILED_MESSAGE, ConsignmentStatus.SHIPPED);
	}

	protected WarehousingShippingService getWarehousingShippingService()
	{
		return warehousingShippingService;
	}

}
