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
package de.hybris.platform.ordercancel.impl.denialstrategies;

import de.hybris.platform.basecommerce.enums.ConsignmentStatus;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.security.PrincipalModel;
import de.hybris.platform.ordercancel.OrderCancelDenialReason;
import de.hybris.platform.ordercancel.OrderCancelDenialStrategy;
import de.hybris.platform.ordercancel.model.OrderCancelConfigModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Required;

import static de.hybris.platform.warehousing.constants.WarehousingConstants.CAPTURE_PAYMENT_ON_CONSIGNMENT_PROPERTY_NAME;


/**
 * This strategy forbids order cancellation if if the system captures payment on consignment level
 * and the payment has been captured for all the consignments
 * and the order doesn't have any unallocated items
 */
public class ConsignmentPaymentCapturedDenialStrategy extends AbstractCancelDenialStrategy implements OrderCancelDenialStrategy
{
	private Collection<ConsignmentStatus> notCancellableConsignmentStatuses;
	private ConfigurationService configurationService;

	@Override
	public OrderCancelDenialReason getCancelDenialReason(final OrderCancelConfigModel configuration, final OrderModel order,
			final PrincipalModel requestor, final boolean partialCancel, final boolean partialEntryCancel)
	{
		OrderCancelDenialReason result = null;
		if ((getConfigurationService().getConfiguration().getBoolean(CAPTURE_PAYMENT_ON_CONSIGNMENT_PROPERTY_NAME, false)))
		{
			final boolean hasUnallocatedItems =
					order.getEntries().stream().mapToLong(entry -> ((OrderEntryModel) entry).getQuantityUnallocated()).sum() > 0;

			if (!hasUnallocatedItems && order.getConsignments().stream()
					.allMatch(consignmentModel -> getNotCancellableConsignmentStatuses().contains(consignmentModel.getStatus())))
			{
				result = getReason();
			}
		}

		return result;
	}

	protected Collection<ConsignmentStatus> getNotCancellableConsignmentStatuses()
	{
		return notCancellableConsignmentStatuses;
	}

	@Required
	public void setNotCancellableConsignmentStatuses(final Collection<ConsignmentStatus> notCancellableConsignmentStatuses)
	{
		this.notCancellableConsignmentStatuses = notCancellableConsignmentStatuses;
	}

	protected ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	@Required
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}
}
