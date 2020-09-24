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
import de.hybris.platform.order.CalculationService;
import de.hybris.platform.order.InvalidCartException;
import de.hybris.platform.order.OrderService;
import de.hybris.platform.order.exceptions.CalculationException;
import de.hybris.platform.orderscheduling.OrderUtility;
import de.hybris.platform.servicelayer.exceptions.SystemException;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.Date;
import java.util.UUID;

import javax.annotation.Resource;


/**
 * The Class DefaultOrderUtilityImpl. This is a preliminary release of a new functionality. It is incomplete and subject
 * to change in future versions. Use at your own risk.
 */
public class DefaultOrderUtilityImpl implements OrderUtility
{
	@Resource
	private ModelService modelService;
	@Resource
	private OrderService orderService;
	@Resource
	private CalculationService calculationService;

	@Override
	public OrderModel createOrderFromCart(final CartModel cart, final AddressModel deliveryAddress,
			final AddressModel paymentAddress, final PaymentInfoModel paymentInfo) throws InvalidCartException
	{
		if (cart.getEntries().isEmpty())
		{
			return null;
		}
		final OrderModel order = getOrderService().placeOrder(cart, deliveryAddress, paymentAddress, paymentInfo); // NOSONAR
		order.setDate(new Date());
		order.setCode(UUID.randomUUID().toString());
		getModelService().save(order);
		runScheduledOrder(order);
		return order;
	}

	@Override
	public OrderModel createOrderFromOrderTemplate(final OrderModel template)
	{
		final OrderModel order = getModelService().clone(template);
		final UUID idOne = UUID.randomUUID();
		order.setVersionID(idOne.toString());
		getModelService().save(order);
		runScheduledOrder(order);

		return order;
	}

	@Override
	public OrderModel runScheduledOrder(final OrderModel order)
	{
		try
		{
			getCalculationService().calculate(order);
		}
		catch (final CalculationException e)
		{
			throw new SystemException("Could not calculate order [" + order.getCode() + "] due to : " + e.getMessage(), e);
		}

		runOrder(order);

		return order;
	}


	@Override
	public void runOrder(final OrderModel order)
	{
		// not implemented
	}

	protected CalculationService getCalculationService()
	{
		return calculationService;
	}

	public void setCalculationService(final CalculationService calculationService)
	{
		this.calculationService = calculationService;
	}

	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	public ModelService getModelService()
	{
		return modelService;
	}

	public void setOrderService(final OrderService orderService)
	{
		this.orderService = orderService;
	}

	public OrderService getOrderService()
	{
		return orderService;
	}
}
