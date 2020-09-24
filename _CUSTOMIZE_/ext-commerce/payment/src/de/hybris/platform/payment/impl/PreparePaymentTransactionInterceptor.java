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
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * Adds a 'generated' code, if there is not a defined one
 * 
 */
public class PreparePaymentTransactionInterceptor implements PrepareInterceptor
{
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(PreparePaymentTransactionInterceptor.class.getName());
	private KeyGenerator keyGenerator;

	@Override
	public void onPrepare(final Object model, final InterceptorContext ctx) throws InterceptorException
	{
		if (model instanceof PaymentTransactionModel)
		{
			final PaymentTransactionModel authModel = (PaymentTransactionModel) model;
			if (authModel.getCode() == null)
			{
				authModel.setCode((String) keyGenerator.generate());
			}

			//fill in versionID if related order is only a order version
			if (authModel.getOrder() instanceof OrderModel)
			{
				final OrderModel order = (OrderModel) authModel.getOrder();
				if (ctx.isNew(order) && order.getVersionID() != null && order.getOriginalVersion() != null)
				{
					authModel.setVersionID(order.getVersionID());
				}
			}
		}
	}

	@Required
	public void setKeyGenerator(final KeyGenerator keyGenerator)
	{
		this.keyGenerator = keyGenerator;
	}
}