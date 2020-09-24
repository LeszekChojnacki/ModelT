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
package de.hybris.platform.orderscheduling.impl;

import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.order.InvalidCartException;
import de.hybris.platform.orderscheduling.OrderUtility;
import de.hybris.platform.orderscheduling.ScheduleOrderService;
import de.hybris.platform.orderscheduling.model.CartToOrderCronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;

import org.apache.log4j.Logger;


/**
 * This job performs the cart to order conversion using the {@link OrderUtility} interface.
 */
public class CartToOrderJob extends AbstractJobPerformable<CartToOrderCronJobModel>
{
	private OrderUtility orderUtility;

	@Override
	public PerformResult perform(final CartToOrderCronJobModel cronJob)
	{

		try
		{
			orderUtility.createOrderFromCart(cronJob.getCart(), cronJob.getDeliveryAddress(), cronJob.getPaymentAddress(),
					cronJob.getPaymentInfo());
		}
		catch (final InvalidCartException e)
		{
			Logger.getLogger(ScheduleOrderService.class).error(e.getMessage());
			Logger.getLogger(ScheduleOrderService.class).error(e.getStackTrace());

			if (Logger.getLogger(ScheduleOrderService.class).isDebugEnabled())
			{
				Logger.getLogger(ScheduleOrderService.class).debug(e);
			}
			return new PerformResult(CronJobResult.FAILURE, CronJobStatus.FINISHED);

		}
		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}

	/**
	 * @param orderUtility
	 *           the orderUtility to set
	 */
	public void setOrderUtility(final OrderUtility orderUtility)
	{
		this.orderUtility = orderUtility;
	}

}
