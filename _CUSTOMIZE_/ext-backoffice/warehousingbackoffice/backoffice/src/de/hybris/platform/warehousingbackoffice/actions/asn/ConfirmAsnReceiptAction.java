/*
 * [y] hybris Platform
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */

package de.hybris.platform.warehousingbackoffice.actions.asn;

import de.hybris.platform.warehousing.asn.service.AsnService;
import de.hybris.platform.warehousing.enums.AsnStatus;
import de.hybris.platform.warehousing.model.AdvancedShippingNoticeModel;
import de.hybris.warehousingbackoffice.constants.WarehousingBackofficeConstants;

import javax.annotation.Resource;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.lang.Strings;

import static java.util.Objects.nonNull;


/**
 * Action used to confirm receipt of the {@link AdvancedShippingNoticeModel}
 */
public class ConfirmAsnReceiptAction implements CockpitAction<AdvancedShippingNoticeModel, AdvancedShippingNoticeModel>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfirmAsnReceiptAction.class);
	private static final String SUCCESS_MESSAGE = "warehousingbackoffice.confirm.asn.receipt.success";
	private static final String FAILURE_MESSAGE = "warehousingbackoffice.confirm.asn.receipt.failure";

	@Resource
	private AsnService asnService;
	@Resource
	private NotificationService notificationService;

	@Override
	public ActionResult<AdvancedShippingNoticeModel> perform(final ActionContext<AdvancedShippingNoticeModel> actionContext)
	{
		final AdvancedShippingNoticeModel asn = actionContext.getData();

		ActionResult<AdvancedShippingNoticeModel> result;
		try
		{
			getAsnService().confirmAsnReceipt(asn.getInternalId());
			getNotificationService()
					.notifyUser(Strings.EMPTY, WarehousingBackofficeConstants.NOTIFICATION_TYPE, NotificationEvent.Level.SUCCESS,
							actionContext.getLabel(SUCCESS_MESSAGE));
			result = new ActionResult<>(ActionResult.SUCCESS);
		}
		catch (final IllegalArgumentException e) //NOSONAR
		{
			LOGGER.info(
					String.format("Unable to Confirm Receipt of ASN: [%s] with status: [%s]", asn.getInternalId(), asn.getStatus()));
			getNotificationService()
					.notifyUser(Strings.EMPTY, WarehousingBackofficeConstants.NOTIFICATION_TYPE, NotificationEvent.Level.FAILURE,
							actionContext.getLabel(FAILURE_MESSAGE));
			result = new ActionResult<>(ActionResult.ERROR);
		}

		result.getStatusFlags().add(ActionResult.StatusFlag.OBJECT_PERSISTED);
		return result;
	}

	@Override
	public boolean canPerform(final ActionContext<AdvancedShippingNoticeModel> actionContext)
	{
		final AdvancedShippingNoticeModel asn = actionContext.getData();

		return nonNull(asn) && AsnStatus.CREATED.equals(asn.getStatus());
	}

	@Override
	public boolean needsConfirmation(final ActionContext<AdvancedShippingNoticeModel> actionContext)
	{
		return false;
	}

	@Override
	public String getConfirmationMessage(final ActionContext<AdvancedShippingNoticeModel> actionContext)
	{
		return null;
	}

	protected AsnService getAsnService()
	{
		return asnService;
	}

	protected NotificationService getNotificationService()
	{
		return notificationService;
	}
}
