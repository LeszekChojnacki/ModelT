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
package de.hybris.platform.warehousingbackoffice.actions.util;

import de.hybris.platform.basecommerce.enums.ConsignmentStatus;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.warehousing.process.BusinessProcessException;
import de.hybris.platform.workflow.exceptions.WorkflowActionDecideException;
import de.hybris.warehousingbackoffice.constants.WarehousingBackofficeConstants;

import javax.annotation.Resource;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import org.zkoss.lang.Strings;


/**
 * Abstract class which provides handy methods for workflow interactions
 */
public abstract class AbstractConsignmentWorkflow
{
	protected static final int RETRIES = 500;

	@Resource
	private ModelService modelService;
	@Resource
	private NotificationService notificationService;

	/**
	 * Get the result of a shipping/pickup action for a given {@link ActionContext} which contains a {@link ConsignmentModel}
	 *
	 * @param actionContext
	 * 		the {@link ActionContext} containing the {@link ConsignmentModel}
	 * @param successMessage
	 * 		the code of the success message to be displayed
	 * @param failedMessage
	 * 		the code of the failure message to be displayed
	 * @param expectedStatus
	 * 		the expected consignment status after the action
	 * @return the {@link ActionResult}
	 */
	protected ActionResult<ConsignmentModel> getConsignmentActionResult(ActionContext<ConsignmentModel> actionContext,
			final String successMessage, final String failedMessage, final ConsignmentStatus expectedStatus)
	{
		final ConsignmentModel consignment = actionContext.getData();
		String result;

		try
		{
			int count = 0;
			while (!expectedStatus.equals(getUpdatedConsignmentStatus(consignment)) && count < RETRIES)
			{
				count++;
			}

			getNotificationService()
					.notifyUser(Strings.EMPTY, WarehousingBackofficeConstants.NOTIFICATION_TYPE, NotificationEvent.Level.SUCCESS,
							actionContext.getLabel(successMessage));
			result = ActionResult.SUCCESS;
		}
		catch (final BusinessProcessException | WorkflowActionDecideException e) //NOSONAR
		{
			getNotificationService()
					.notifyUser(Strings.EMPTY, WarehousingBackofficeConstants.NOTIFICATION_TYPE, NotificationEvent.Level.FAILURE,
							actionContext.getLabel(failedMessage));
			result = ActionResult.ERROR;
		}

		final ActionResult<ConsignmentModel> actionResult = new ActionResult<>(result);
		actionResult.getStatusFlags().add(ActionResult.StatusFlag.OBJECT_PERSISTED);
		return actionResult;
	}

	/**
	 * Returns the updated {@link ConsignmentStatus} of the given {@link ConsignmentModel} as a result of workflow event
	 * triggered by this action
	 *
	 * @param consignmentModel
	 * 		the consignment to be updated
	 * @return updated status of the given consignment
	 */
	protected ConsignmentStatus getUpdatedConsignmentStatus(final ConsignmentModel consignmentModel)
	{
		return ((ConsignmentModel) getModelService().get(consignmentModel.getPk())).getStatus();
	}

	/**
	 * Determines whether a given {@link ConsignmentModel} is linked to an external fulfillment configuration
	 *
	 * @param consignmentModel
	 * 		the consignment to be verified
	 * @return flag to determine whether the fulfillment config is external or internal
	 */
	protected boolean isFulfillmentExternal(final ConsignmentModel consignmentModel)
	{
		return consignmentModel.getFulfillmentSystemConfig() != null;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	protected NotificationService getNotificationService()
	{
		return notificationService;
	}
}
