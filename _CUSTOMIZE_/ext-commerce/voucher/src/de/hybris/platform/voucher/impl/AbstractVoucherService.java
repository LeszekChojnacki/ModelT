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
package de.hybris.platform.voucher.impl;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.jalo.order.AbstractOrder;
import de.hybris.platform.jalo.order.Cart;
import de.hybris.platform.jalo.order.Order;
import de.hybris.platform.jalo.product.Product;
import de.hybris.platform.jalo.user.User;
import de.hybris.platform.servicelayer.internal.service.AbstractBusinessService;
import de.hybris.platform.voucher.jalo.Restriction;
import de.hybris.platform.voucher.jalo.Voucher;
import de.hybris.platform.voucher.model.RestrictionModel;
import de.hybris.platform.voucher.model.VoucherModel;


public abstract class AbstractVoucherService extends AbstractBusinessService //NOSONAR
{

	protected Cart getCart(final CartModel cart)
	{
		return getModelService().getSource(cart);
	}

	protected Order getOrder(final OrderModel order)
	{
		return getModelService().getSource(order);
	}

	protected AbstractOrder getAbstractOrder(final AbstractOrderModel order)
	{
		return getModelService().getSource(order);
	}

	protected Voucher getVoucher(final VoucherModel voucher)
	{
		return getModelService().getSource(voucher);
	}

	protected Restriction getRestriction(final RestrictionModel restriction)
	{
		return getModelService().getSource(restriction);
	}

	protected Product getProduct(final ProductModel product)  //NOSONAR
	{
		return getModelService().getSource(product);
	}

	protected User getUser(final UserModel user)
	{
		return getModelService().getSource(user);
	}

}
