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


import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.jalo.c2l.Currency;
import de.hybris.platform.jalo.order.AbstractOrderEntry;
import de.hybris.platform.jalo.order.Cart;
import de.hybris.platform.jalo.order.delivery.DeliveryMode;
import de.hybris.platform.jalo.type.ComposedType;
import de.hybris.platform.jalo.type.JaloAbstractTypeException;
import de.hybris.platform.jalo.type.JaloGenericCreationException;
import de.hybris.platform.jalo.user.Address;
import de.hybris.platform.jalo.user.User;
import de.hybris.platform.servicelayer.internal.jalo.order.InMemoryCart;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Will be used by {@link DefaultMultiAddressDeliveryCostsStrategy} for creating the internally used {@link Cart}
 * instance {@link DefaultMultiAddressDeliveryCostsStrategy#createTempCart}
 */
public class DefaultCartFactory implements CartFactory
{
	@SuppressWarnings("deprecation")
	@Override
	public Cart createCartInstance(final List<AbstractOrderEntry> entries, final Address deliveryAddress, final DeliveryMode mode,
			final User user, final Currency curr, final boolean net) throws JaloGenericCreationException, JaloAbstractTypeException
	{
		final ComposedType composedType = de.hybris.platform.jalo.type.TypeManager.getInstance() //NOSONAR
				.getComposedType(MultiAddressInMemoryCart.class);//NOSONAR

		final Map<String, Object> values = new HashMap<>();
		values.put(Cart.CODE, "TempCart");
		values.put(Cart.USER, user);
		values.put(Cart.CURRENCY, curr);
		values.put(AbstractOrderModel.DELIVERYADDRESS, deliveryAddress);
		values.put(AbstractOrderModel.DELIVERYMODE, mode);
		values.put(Cart.NET, Boolean.valueOf(net));

		final InMemoryCart cart = (InMemoryCart) composedType.newInstance(values);

		for (final AbstractOrderEntry entry : entries)
		{
			cart.addNewEntry(entry.getProduct(), entry.getQuantity().longValue(), entry.getUnit()); //NOSONAR
		}
		return cart;
	}

}
