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

import de.hybris.platform.jalo.c2l.Currency;
import de.hybris.platform.jalo.order.AbstractOrderEntry;
import de.hybris.platform.jalo.order.Cart;
import de.hybris.platform.jalo.order.delivery.DeliveryMode;
import de.hybris.platform.jalo.type.JaloAbstractTypeException;
import de.hybris.platform.jalo.type.JaloGenericCreationException;
import de.hybris.platform.jalo.user.Address;
import de.hybris.platform.jalo.user.User;

import java.util.List;


/**
 * Will be used by {@link DefaultMultiAddressDeliveryCostsStrategy} for creating the internally used {@link Cart}
 * instance {@link DefaultMultiAddressDeliveryCostsStrategy#createTempCart}
 */
public interface CartFactory
{
	Cart createCartInstance(final List<AbstractOrderEntry> entries, final Address deliveryAddress, final DeliveryMode mode, //NOSONAR
			final User user, final Currency curr, boolean net) throws JaloGenericCreationException, JaloAbstractTypeException;
}
