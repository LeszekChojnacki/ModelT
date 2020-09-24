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
package de.hybris.platform.payment.impl;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;


public class PreparePaymentTransactionEntryInterceptor implements PrepareInterceptor
{

	@Override
	public void onPrepare(final Object model, final InterceptorContext ctx) throws InterceptorException
	{
		if (model instanceof PaymentTransactionEntryModel)
		{
			final PaymentTransactionEntryModel transactionEntry = (PaymentTransactionEntryModel) model;
			final PaymentTransactionModel transaction = transactionEntry.getPaymentTransaction();

			if (transaction != null && transaction.getOrder() instanceof OrderModel)
			{
				final OrderModel order = (OrderModel) transaction.getOrder();
				if (ctx.isNew(order) && order.getVersionID() != null && order.getOriginalVersion() != null)
				{
					final String versionID = order.getVersionID();
					transactionEntry.setVersionID(versionID);
				}
			}
		}
	}

}
