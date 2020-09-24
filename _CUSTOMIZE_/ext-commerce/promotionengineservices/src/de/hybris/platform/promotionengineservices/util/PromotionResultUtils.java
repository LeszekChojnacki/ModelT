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
package de.hybris.platform.promotionengineservices.util;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.promotions.model.PromotionResultModel;

import org.springframework.beans.factory.annotation.Required;


/**
 * The class provides some utility methods related to PromotionResult functionality.
 */
public class PromotionResultUtils
{
	private CartService cartService;

	public AbstractOrderModel getOrder(final PromotionResultModel promotionResult)
	{
		if (getCartService().hasSessionCart()
				&& getCartService().getSessionCart().getCode().equals(promotionResult.getOrderCode()))
		{
			return getCartService().getSessionCart();
		}
		else if (promotionResult.getOrder() != null)
		{
			return promotionResult.getOrder();
		}
		return null;
	}

	protected CartService getCartService()
	{
		return cartService;
	}

	@Required
	public void setCartService(final CartService cartService)
	{
		this.cartService = cartService;
	}
}
