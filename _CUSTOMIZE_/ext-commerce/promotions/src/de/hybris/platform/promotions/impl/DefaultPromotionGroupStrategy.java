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

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.promotions.PromotionGroupStrategy;
import de.hybris.platform.promotions.PromotionsService;
import de.hybris.platform.promotions.model.PromotionGroupModel;

import org.springframework.beans.factory.annotation.Required;


public class DefaultPromotionGroupStrategy implements PromotionGroupStrategy
{
	private PromotionsService promotionsService;

	@Override
	public PromotionGroupModel getDefaultPromotionGroup()
	{
		return promotionsService.getDefaultPromotionGroup();
	}

	@Override
	public PromotionGroupModel getDefaultPromotionGroup(final AbstractOrderModel order)
	{
		return promotionsService.getDefaultPromotionGroup();
	}

	protected PromotionsService getPromotionsService()
	{
		return promotionsService;
	}

	@Required
	public void setPromotionsService(final PromotionsService promotionsService)
	{
		this.promotionsService = promotionsService;
	}
}
