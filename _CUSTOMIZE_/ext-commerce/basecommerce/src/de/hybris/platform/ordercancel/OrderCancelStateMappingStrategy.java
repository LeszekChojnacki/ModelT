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
package de.hybris.platform.ordercancel;

import de.hybris.platform.basecommerce.enums.OrderCancelState;
import de.hybris.platform.core.model.order.OrderModel;



/**
 * Determines OrderCancelState for a given Order. This strategy was introduced together with OrderCancelState
 * enumeration to provide Order-independent states for processing cancel requests.
 */
public interface OrderCancelStateMappingStrategy
{
	OrderCancelState getOrderCancelState(OrderModel order);
}
