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

import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.cronjob.model.TriggerModel;
import de.hybris.platform.orderscheduling.ScheduleOrderService;
import de.hybris.platform.orderscheduling.model.CartToOrderCronJobModel;
import de.hybris.platform.orderscheduling.model.OrderScheduleCronJobModel;
import de.hybris.platform.orderscheduling.model.OrderTemplateToOrderCronJobModel;
import de.hybris.platform.servicelayer.cronjob.CronJobService;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.List;

import javax.annotation.Resource;


/**
 * The Class DefaultScheduleOrderServiceImpl. This is a preliminary release of a new functionality. It is incomplete and
 * subject to change in future versions. Use at your own risk.
 */
public class DefaultScheduleOrderServiceImpl implements ScheduleOrderService
{
	@Resource
	private ModelService modelService;

	@Resource
	private CronJobService cronJobService;

	@Override
	public CartToOrderCronJobModel createOrderFromCartCronJob(final CartModel cart, final AddressModel deliveryAddress,
			final AddressModel paymentAddress, final PaymentInfoModel paymentInfo, final List<TriggerModel> triggers)
	{
		final CartToOrderCronJobModel cartToOrderCronJob = modelService.create(CartToOrderCronJobModel.class);
		cartToOrderCronJob.setCart(cart);
		cartToOrderCronJob.setDeliveryAddress(deliveryAddress);
		cartToOrderCronJob.setPaymentAddress(paymentAddress);
		cartToOrderCronJob.setPaymentInfo(paymentInfo);
		setCronJobToTrigger(cartToOrderCronJob, triggers);
		cartToOrderCronJob.setJob(cronJobService.getJob("cartToOrderJob"));

		modelService.save(cartToOrderCronJob);

		return cartToOrderCronJob;
	}

	@Override
	public OrderTemplateToOrderCronJobModel createOrderFromOrderTemplateCronJob(final OrderModel template,
			final List<TriggerModel> triggers)
	{
		final OrderTemplateToOrderCronJobModel orderTemplateCronJob = modelService.create(OrderTemplateToOrderCronJobModel.class);
		orderTemplateCronJob.setOrderTemplate(template);
		setCronJobToTrigger(orderTemplateCronJob, triggers);

		orderTemplateCronJob.setJob(cronJobService.getJob("orderTemplateToOrderJob"));
		modelService.save(orderTemplateCronJob);

		return orderTemplateCronJob;
	}


	@Override
	public OrderScheduleCronJobModel createScheduledOrderCronJob(final OrderModel order, final List<TriggerModel> triggers)
	{
		final OrderScheduleCronJobModel orderScheduleJob = modelService.create(OrderScheduleCronJobModel.class);
		orderScheduleJob.setOrder(order);
		setCronJobToTrigger(orderScheduleJob, triggers);
		orderScheduleJob.setJob(cronJobService.getJob("orderScheduleJob"));

		modelService.save(orderScheduleJob);

		return orderScheduleJob;
	}


	protected void setCronJobToTrigger(final CronJobModel cronJob, final List<TriggerModel> triggers)
	{
		for (final TriggerModel trigger : triggers)
		{
			trigger.setCronJob(cronJob);
		}
		cronJob.setTriggers(triggers);
	}

	/**
	 * @param modelService
	 *           the modelService to set
	 */
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	/**
	 * @return the modelService
	 */
	public ModelService getModelService()
	{
		return modelService;
	}



	/**
	 * @param cronJobService
	 *           the cronJobService to set
	 */
	public void setCronJobService(final CronJobService cronJobService)
	{
		this.cronJobService = cronJobService;
	}



	/**
	 * @return the cronJobService
	 */
	public CronJobService getCronJobService()
	{
		return cronJobService;
	}




}
