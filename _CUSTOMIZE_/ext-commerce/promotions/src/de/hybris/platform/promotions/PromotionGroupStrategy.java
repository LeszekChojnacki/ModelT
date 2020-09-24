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
package de.hybris.platform.promotions;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.promotions.model.PromotionGroupModel;


public interface PromotionGroupStrategy
{
	/**
	 * Method returns default promotions group
	 *
	 * @return default promotion group
	 */
	PromotionGroupModel getDefaultPromotionGroup();

	/**
	 * Method returns default promotions group for given order, because it can depend on store where order was created.
	 *
	 * @param order
	 *           order for which default promotions group will be returned
	 * @return default promotions group
	 */
	PromotionGroupModel getDefaultPromotionGroup(final AbstractOrderModel order);
}
