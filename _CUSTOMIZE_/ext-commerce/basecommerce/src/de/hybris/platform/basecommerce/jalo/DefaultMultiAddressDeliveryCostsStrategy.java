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

import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.c2l.Currency;
import de.hybris.platform.jalo.order.AbstractOrder;
import de.hybris.platform.jalo.order.AbstractOrderEntry;
import de.hybris.platform.jalo.order.delivery.DeliveryCostsStrategy;
import de.hybris.platform.jalo.order.delivery.DeliveryMode;
import de.hybris.platform.jalo.order.delivery.JaloDeliveryModeException;
import de.hybris.platform.jalo.type.JaloAbstractTypeException;
import de.hybris.platform.jalo.type.JaloGenericCreationException;
import de.hybris.platform.jalo.user.Address;
import de.hybris.platform.jalo.user.User;
import de.hybris.platform.util.PriceValue;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Calculates the delivery costs of the specified {@link AbstractOrder} by grouping its {@link AbstractOrderEntry} based
 * on their assigned {@link DeliveryMode} and 'DeliveryAddress'. For each of these "groups" a new order
 * {@link DefaultMultiAddressDeliveryCostsStrategy#createTempCart} and the total of their delivery costs will be
 * returned by {@link DefaultMultiAddressDeliveryCostsStrategy#getCost}.
 */
public class DefaultMultiAddressDeliveryCostsStrategy implements DeliveryCostsStrategy
{
	private static final Logger LOG = Logger.getLogger(DefaultMultiAddressDeliveryCostsStrategy.class.getName());

	private CartFactory cartFactory;

	public CartFactory getCartFactory()
	{
		if (cartFactory == null)
		{
			cartFactory = new DefaultCartFactory();
		}
		return cartFactory;
	}

	public void setCartFactory(final CartFactory cartFactory)
	{
		this.cartFactory = cartFactory;
	}

	/**
	 * Called during {@link AbstractOrder#calculate()}, {@link AbstractOrder#calculate(java.util.Date)},
	 * {@link AbstractOrder#recalculate()} and {@link AbstractOrder#recalculate(java.util.Date)} to fetch and set the
	 * delivery cost for the assigned order.
	 *
	 * @return the delivery costs of this abstract order
	 */
	@Override
	public PriceValue findDeliveryCosts(final SessionContext ctx, final AbstractOrder order)
	{
		try
		{
			return getCost(ctx, order);
		}
		catch (final JaloDeliveryModeException e)
		{
			LOG.error("Delivery mode error for mode!", e);
		}
		return null;
	}

	/**
	 * Calculates the delivery costs of the specified {@link AbstractOrder} by grouping its {@link AbstractOrderEntry}
	 * based on their assigned {@link DeliveryMode} and 'DeliveryAddress'. For each of these "groups" a new order
	 * {@link DefaultMultiAddressDeliveryCostsStrategy#createTempCart} will be created and its delivery cost calculated.
	 *
	 * @param ctx
	 *           the session context
	 *
	 * @param order
	 *           the original order (containing {@link AbstractOrderEntry}, which can have their own 'DeliveryAddress'
	 * @return the sum of all delivery costs
	 */
	protected PriceValue getCost(final SessionContext ctx, final AbstractOrder order) throws JaloDeliveryModeException // NOSONAR
	{
		double price = 0.0;

		// group the order entries by building the structure: DeliveryMode -> ( Address -> AbstractOrderEntry ))
		final Map<DeliveryMode, Map<Address, List<AbstractOrderEntry>>> addressesForDeliveryMode = new HashMap<>();

		final List<AbstractOrderEntry> entries = order.getEntries();

		// grouping
		for (final AbstractOrderEntry entry : entries)
		{
			// order entry has its own address?
			Address address = BasecommerceManager.getInstance().getDeliveryAddress(entry);
			if (address == null)
			{
				address = order.getDeliveryAddress();
			}

			// order entry has its own delivery mode?
			DeliveryMode deliveryMode = BasecommerceManager.getInstance().getDeliveryMode(entry);
			if (deliveryMode == null)
			{
				deliveryMode = order.getDeliveryMode();
			}

			// are their stored addresses for the specified delivery mode?
			Map<Address, List<AbstractOrderEntry>> orderEntriesForAddress = addressesForDeliveryMode.get(deliveryMode);

			if (orderEntriesForAddress == null)
			{
				orderEntriesForAddress = new HashMap<>();
				final List orderEntries = new ArrayList<AbstractOrderEntry>();
				orderEntries.add(entry);
				orderEntriesForAddress.put(address, orderEntries);
			}
			else
			{
				List orderEntries = orderEntriesForAddress.get(address);
				if (orderEntries == null)
				{
					orderEntries = new ArrayList<AbstractOrderEntry>();
				}
				orderEntries.add(entry);
				orderEntriesForAddress.put(address, orderEntries);
			}
			addressesForDeliveryMode.put(deliveryMode, orderEntriesForAddress);
		}

		// delivery cost calculation
		final Iterator deliveryModeIterator = addressesForDeliveryMode.entrySet().iterator();
		while (deliveryModeIterator.hasNext())
		{
			final Map.Entry deliveryModeEntry = (Map.Entry) deliveryModeIterator.next();

			final DeliveryMode mode = (DeliveryMode) deliveryModeEntry.getKey();
			final Map<Address, List<AbstractOrderEntry>> entriesForAddress = (Map<Address, List<AbstractOrderEntry>>) deliveryModeEntry
					.getValue();

			final Iterator deliveryAddressIterator = entriesForAddress.entrySet().iterator();
			while (deliveryAddressIterator.hasNext())
			{
				final Map.Entry deliveryAddressEntry = (Map.Entry) deliveryAddressIterator.next();
				final List<AbstractOrderEntry> orderEntries = (List<AbstractOrderEntry>) deliveryAddressEntry.getValue();
				try
				{
					final AbstractOrder temp = createTempCart(orderEntries, (Address) deliveryAddressEntry.getKey(), mode,
							order.getUser(), order.getCurrency(), order.isNetAsPrimitive());
					temp.calculate(); // NOSONAR
					price += temp.getDeliveryCosts();

				}
				catch (final Exception e1)
				{
					LOG.warn("Delivery cost calculation failed for address: " + ((Address) deliveryAddressEntry.getKey()).toString(),
							e1);
				}
			}
		}
		return new PriceValue(order.getCurrency().getIsocode(), price, order.isNet().booleanValue());
	}

	/**
	 * Creates a temporary order, which will be used for calculating the delivery costs of the assigned
	 * {@link AbstractOrderEntry}. The configured {@link CartFactory} will be used for the internal cart/order creation
	 *
	 * @param entries
	 *           the order entries, which will be added to the returned order
	 * @param deliveryAddress
	 *           the delivery address of the order
	 * @param mode
	 *           the DeliveryMode of the order
	 * @param user
	 *           the order user
	 * @param curr
	 *           the currency
	 * @param net
	 *           the pricing of the order
	 *
	 * @return the 'new' order
	 */
	protected AbstractOrder createTempCart(final List<AbstractOrderEntry> entries, final Address deliveryAddress,
			final DeliveryMode mode, final User user, final Currency curr, final boolean net)
			throws JaloGenericCreationException, JaloAbstractTypeException
	{
		return getCartFactory().createCartInstance(entries, deliveryAddress, mode, user, curr, net);
	}

}
