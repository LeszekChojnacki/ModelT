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
package de.hybris.platform.basecommerce.jalo;

import de.hybris.platform.jalo.order.AbstractOrder;
import de.hybris.platform.jalo.order.AbstractOrderEntry;
import de.hybris.platform.jalo.order.Cart;
import de.hybris.platform.jalo.order.delivery.DeliveryCostsStrategy;
import de.hybris.platform.jalo.order.delivery.DeliveryMode;


/**
 * Calculates the delivery costs of the specified {@link AbstractOrder} by grouping its {@link AbstractOrderEntry} based
 * on their assigned {@link DeliveryMode} and 'DeliveryAddress'. For each of these "groups" a new order
 * {@link DefaultMultiAddressDeliveryCostsStrategy#createTempCart} and the total of their delivery costs will be
 * returned by {@link DefaultMultiAddressDeliveryCostsStrategy#getCost}.
 */
public interface MultiAddressDeliveryCostsStrategy extends DeliveryCostsStrategy
{
	Cart getCartFactory();

	void setCartFactory(CartFactory factory);
}
