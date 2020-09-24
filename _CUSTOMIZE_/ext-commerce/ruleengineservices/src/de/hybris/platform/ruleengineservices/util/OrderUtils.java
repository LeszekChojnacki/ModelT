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
package de.hybris.platform.ruleengineservices.util;

import com.google.common.collect.Lists;
import de.hybris.order.calculation.domain.AbstractCharge.ChargeType;
import de.hybris.order.calculation.domain.OrderCharge;
import de.hybris.order.calculation.money.AbstractAmount;
import de.hybris.order.calculation.money.Currency;
import de.hybris.order.calculation.money.Money;
import de.hybris.order.calculation.money.Percentage;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Required;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;


/**
 * The class provides some utility methods related to Order functionality.
 */
public class OrderUtils
{
	private ModelService modelService;

	/**
	 * Creates an {@code OrderCharge} of {@link ChargeType#SHIPPING} for the given values.
	 * @param currency
	 *           the currency to use
	 * @param absolute
	 *           whether the shipping charge is percentage-based or absolute.
	 * @param value
	 *           the value of the charge
	 * @return the newly created OrderCharge
	 */
	public OrderCharge createShippingCharge(final Currency currency, final boolean absolute, final BigDecimal value)
	{
		AbstractAmount amount;
		if (absolute)
		{
			amount = new Money(value, currency);
		}
		else
		{
			amount = new Percentage(value);
		}
		return new OrderCharge(amount, ChargeType.SHIPPING);
	}

	/**
	 * Updates multiple order entry quantities at once. Entries that receive a quantity &lt; 1 will be removed as well as
	 * entries that receive NULL as quantity value. Refreshes a given cart instance after that.
	 * Entries with entry numbers that do not occur in the parameter map are not touched.
	 * @param order
	 *           the order to update order entry quantities at
	 * @param quantities
	 *           the entry specific quantities as map of { entry number -> quantity }
	 */
	public void updateOrderQuantities(final OrderModel order, final Map<Integer, Long> quantities)
	{
		checkArgument(order != null, "cart cannot be null");
		if (MapUtils.isNotEmpty(quantities))
		{
			final Collection<OrderEntryModel> toRemove = Lists.newArrayList();
			final Collection<OrderEntryModel> toSave = Lists.newArrayList();
			for (final Map.Entry<OrderEntryModel, Long> e : getEntryQuantityMap(order, quantities).entrySet())
			{
				final OrderEntryModel cartEntry = e.getKey();
				final Long quantity = e.getValue();
				if (quantity == null || quantity.longValue() < 1)
				{
					toRemove.add(cartEntry);
				}
				else
				{
					cartEntry.setQuantity(quantity);
					toSave.add(cartEntry);
				}
			}
			getModelService().removeAll(toRemove);
			getModelService().saveAll(toSave);
			getModelService().refresh(order);
		}
	}

	protected Map<OrderEntryModel, Long> getEntryQuantityMap(final OrderModel order, final Map<Integer, Long> quantities)
	{
		final List<OrderEntryModel> entries = (List) order.getEntries();

		return quantities.entrySet().stream().collect(Collectors.toMap(e -> getEntry(entries, e.getKey()), e -> e.getValue()));
	}

	protected OrderEntryModel getEntry(final List<OrderEntryModel> entries, final Integer entryNumber)
	{
		return entries
				.stream()
				.filter(e -> entryNumber.equals(e.getEntryNumber()))
				.findFirst()
				.orElseThrow(
						() -> new IllegalArgumentException("no cart entry found with entry number " + entryNumber + " (got " + entries
								+ ")"));
	}


	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}
}
