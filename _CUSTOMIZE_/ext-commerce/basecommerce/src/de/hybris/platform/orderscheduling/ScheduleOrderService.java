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
package de.hybris.platform.orderscheduling;

import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.cronjob.model.TriggerModel;
import de.hybris.platform.orderscheduling.model.CartToOrderCronJobModel;
import de.hybris.platform.orderscheduling.model.OrderScheduleCronJobModel;
import de.hybris.platform.orderscheduling.model.OrderTemplateToOrderCronJobModel;

import java.util.List;


/**
 * This is a preliminary release of a new functionality. It is incomplete and subject to change in future versions. Use
 * at your own risk. The Interface ScheduleOrderService.
 */
public interface ScheduleOrderService
{



	/**
	 * Creates the order from order template cron job.
	 *
	 * @param template
	 * 		the template
	 * @param trigger
	 * 		the trigger
	 * @return instance of {@link OrderTemplateToOrderCronJobModel} 
	 */
	OrderTemplateToOrderCronJobModel createOrderFromOrderTemplateCronJob(OrderModel template, List<TriggerModel> trigger);



	/**
	 * Creates the order from cart cron job.
	 *
	 * @param cart
	 * 		the cart
	 * @param deliveryAddress
	 * 		instance of {@link AddressModel} corresponding to delivery address
	 * @param paymentAddress
	 * 		instance of {@link AddressModel} corresponding to payment address
	 * @param paymentInfo
	 * 		instance of {@link PaymentInfoModel} with payment details
	 * @param trigger
	 * 		the trigger to invoke
	 * @return instance of {@link CartToOrderCronJobModel}, corresponding to a scheduled job
	 */
	CartToOrderCronJobModel createOrderFromCartCronJob(final CartModel cart, final AddressModel deliveryAddress,
			final AddressModel paymentAddress, final PaymentInfoModel paymentInfo, List<TriggerModel> trigger);





	/**
	 * Creates the scheduled order cron job.
	 *
	 * @param order
	 * 		the order
	 * @param trigger
	 * 		the trigger
	 * @return instance of {@link OrderScheduleCronJobModel}, corresponding to a scheduled job
	 */
	OrderScheduleCronJobModel createScheduledOrderCronJob(OrderModel order, List<TriggerModel> trigger);

}
