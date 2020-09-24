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
import de.hybris.platform.orderscheduling.OrderUtility;
import de.hybris.platform.orderscheduling.model.OrderScheduleCronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;


/**
 *
 */
public class OrderScheduleJob extends AbstractJobPerformable<OrderScheduleCronJobModel>
{
	private OrderUtility orderUtility;


	@Override
	public PerformResult perform(final OrderScheduleCronJobModel cronJob)
	{
		orderUtility.runScheduledOrder(cronJob.getOrder());
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
