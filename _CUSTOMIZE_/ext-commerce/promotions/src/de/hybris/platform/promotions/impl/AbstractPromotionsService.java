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
package de.hybris.platform.promotions.impl;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.order.AbstractOrder;
import de.hybris.platform.jalo.order.Cart;
import de.hybris.platform.jalo.order.Order;
import de.hybris.platform.jalo.product.Product;
import de.hybris.platform.promotions.jalo.AbstractPromotion;
import de.hybris.platform.promotions.jalo.OrderPromotion;
import de.hybris.platform.promotions.jalo.ProductPromotion;
import de.hybris.platform.promotions.jalo.PromotionGroup;
import de.hybris.platform.promotions.jalo.PromotionResult;
import de.hybris.platform.promotions.model.AbstractPromotionModel;
import de.hybris.platform.promotions.model.OrderPromotionModel;
import de.hybris.platform.promotions.model.ProductPromotionModel;
import de.hybris.platform.promotions.model.PromotionGroupModel;
import de.hybris.platform.promotions.model.PromotionResultModel;
import de.hybris.platform.servicelayer.internal.service.AbstractBusinessService;

import java.util.List;



public class AbstractPromotionsService extends AbstractBusinessService //NOSONAR
{

	protected OrderPromotion getPromotion(final OrderPromotionModel promotion)
	{
		return getModelService().getSource(promotion);
	}

	protected ProductPromotion getPromotion(final ProductPromotionModel promotion)
	{
		return getModelService().getSource(promotion);
	}

	protected PromotionGroup getPromotionGroup(final PromotionGroupModel group)
	{
		return getModelService().getSource(group);
	}

	protected Order getOrder(final OrderModel order)
	{
		return getModelService().getSource(order);
	}

	protected AbstractOrder getOrder(final AbstractOrderModel order)
	{
		return getModelService().getSource(order);
	}

	protected Cart getCart(final CartModel cart)
	{
		return getModelService().getSource(cart);
	}

	protected Product getProduct(final ProductModel product) // NOSONAR
	{
		return getModelService().getSource(product);
	}

	protected SessionContext getSessionContext()
	{
		return JaloSession.getCurrentSession().getSessionContext();
	}

	protected AbstractPromotion getPromotion(final AbstractPromotionModel promotion)
	{
		return getModelService().getSource(promotion);
	}

	protected PromotionResult getResult(final PromotionResultModel result)
	{
		return getModelService().getSource(result);
	}

	/**
	 * Method intended to be called over known set of ItemModel which can get stale while calling jalo logic over their
	 * jalo counterparts.
	 */
	protected void refreshModifiedModelsAfter(final List<ItemModel> models)
	{
		for (final ItemModel singleModel : models)
		{
			getModelService().refresh(singleModel);
		}
	}
}
