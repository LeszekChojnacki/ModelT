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
package de.hybris.platform.payment.order.strategies.impl;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.order.strategies.ordercloning.impl.DefaultOrderPartOfMembersCloningStrategy;



public class PaymentOrderPartOfMembersCloningStrategy extends DefaultOrderPartOfMembersCloningStrategy
{

	@Override
	public PaymentInfoModel clonePaymentInfoForOrder(final PaymentInfoModel paymentInfo, final OrderModel order)
	{

		final PaymentInfoModel newPaymentInfo = super.clonePaymentInfoForOrder(paymentInfo, order);

		if (newPaymentInfo.getBillingAddress() != null)
		{
			newPaymentInfo.getBillingAddress().setOwner(newPaymentInfo);
			newPaymentInfo.getBillingAddress().setDuplicate(Boolean.TRUE);
			newPaymentInfo.getBillingAddress().setOriginal(paymentInfo.getBillingAddress());
		}

		return newPaymentInfo;
	}

}
