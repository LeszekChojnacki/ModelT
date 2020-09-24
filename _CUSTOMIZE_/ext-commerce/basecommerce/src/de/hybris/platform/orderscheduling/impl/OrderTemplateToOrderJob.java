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
import de.hybris.platform.orderscheduling.model.OrderTemplateToOrderCronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;


/**
 *
 */
public class OrderTemplateToOrderJob extends AbstractJobPerformable<OrderTemplateToOrderCronJobModel>
{
	private OrderUtility orderUtility;

	/**
	 * @param orderUtility
	 *           the orderUtility to set
	 */
	public void setOrderUtility(final OrderUtility orderUtility)
	{
		this.orderUtility = orderUtility;
	}

	@Override
	public PerformResult perform(final OrderTemplateToOrderCronJobModel cronJob)
	{

		orderUtility.createOrderFromOrderTemplate(cronJob.getOrderTemplate());

		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}

}
