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
import de.hybris.platform.order.InvalidCartException;


/**
 * This is a preliminary release of a new functionality. It is incomplete and subject to change in future versions. Use
 * at your own risk.
 */
public interface OrderUtility
{

	/**
	 * Run order.
	 * 
	 * @param order
	 *           the order
	 */
	void runOrder(OrderModel order);


	/**
	 * Creates the order from order template.
	 * 
	 * @param template
	 *           the template
	 * 
	 * @return OrderModel
	 */
	OrderModel createOrderFromOrderTemplate(OrderModel template);


	/**
	 * Creates the order from cart.
	 * 
	 * @param cart
	 *           the cart
	 * @return OrderModel
	 */
	OrderModel createOrderFromCart(final CartModel cart, final AddressModel deliveryAddress, final AddressModel paymentAddress,
			final PaymentInfoModel paymentInfo) throws InvalidCartException;

	/**
	 * Run scheduled order.
	 * 
	 * @param order
	 *           the order
	 * @return OrderModel
	 */
	OrderModel runScheduledOrder(OrderModel order);
}
