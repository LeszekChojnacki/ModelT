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
package de.hybris.platform.ruleengineservices.converters;

import de.hybris.order.calculation.domain.AbstractCharge.ChargeType;
import de.hybris.order.calculation.domain.LineItem;
import de.hybris.order.calculation.domain.LineItemDiscount;
import de.hybris.order.calculation.domain.Order;
import de.hybris.order.calculation.domain.OrderCharge;
import de.hybris.order.calculation.domain.OrderDiscount;
import de.hybris.order.calculation.money.AbstractAmount;
import de.hybris.order.calculation.money.Currency;
import de.hybris.order.calculation.money.Money;
import de.hybris.order.calculation.money.Percentage;
import de.hybris.order.calculation.strategies.CalculationStrategies;
import de.hybris.platform.ruleengineservices.calculation.NumberedLineItem;
import de.hybris.platform.ruleengineservices.rao.AbstractOrderRAO;
import de.hybris.platform.ruleengineservices.rao.CartRAO;
import de.hybris.platform.ruleengineservices.rao.DiscountRAO;
import de.hybris.platform.ruleengineservices.rao.OrderEntryRAO;
import de.hybris.platform.ruleengineservices.rao.ShipmentRAO;
import de.hybris.platform.ruleengineservices.util.OrderUtils;
import de.hybris.platform.ruleengineservices.util.RaoUtils;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Converts {@link AbstractOrderRAO} to {@link Order}.
 */
public class AbstractOrderRaoToOrderConverter implements Converter<AbstractOrderRAO, Order>
{
	private CalculationStrategies calculationStrategies;
	private Converter<AbstractOrderRAO, Currency> abstractOrderRaoToCurrencyConverter;
	private Converter<OrderEntryRAO, NumberedLineItem> orderEntryRaoToNumberedLineItemConverter;
	private OrderUtils orderUtils;
	private RaoUtils raoUtils;

	@Override
	public Order convert(final AbstractOrderRAO cartRao)
	{
		final Currency currency = getAbstractOrderRaoToCurrencyConverter().convert(cartRao);
		final Order cart = new Order(currency, getCalculationStrategies());

		final OrderCharge shippingCharge = convertToShippingOrderCharge(cartRao);
		if (shippingCharge != null)
		{
			cart.addCharge(shippingCharge);
		}
		final OrderCharge paymentCharge = convertToPaymentOrderCharge(cartRao);
		if (paymentCharge != null)
		{
			cart.addCharge(paymentCharge);
		}

		if (CollectionUtils.isNotEmpty(cartRao.getActions()))
		{
			final List<OrderDiscount> orderDiscounts = new ArrayList<>();
			getRaoUtils().getDiscounts(cartRao)
					.forEach(action -> orderDiscounts.add(convertToOrderDiscount(action, cartRao)));
			cart.addDiscounts(orderDiscounts);
		}

		cart.addLineItems(convertEntriesToLineItems(cartRao));

		return cart;
	}

	protected List<LineItem> convertEntriesToLineItems(final AbstractOrderRAO cartRao)
	{
		final List<LineItem> lineItems = new ArrayList<>();

		if (CollectionUtils.isEmpty(cartRao.getEntries()))
		{
			return lineItems;
		}

		for (final OrderEntryRAO entryRao : cartRao.getEntries())
		{
			final NumberedLineItem lineItem = getOrderEntryRaoToNumberedLineItemConverter().convert(entryRao);
			lineItems.add(lineItem);
			if (CollectionUtils.isNotEmpty(entryRao.getActions()))
			{
				final List<LineItemDiscount> lineItemDiscounts = new ArrayList<>();
				entryRao.getActions().stream().filter(action -> action instanceof DiscountRAO)
						.filter(a -> isDiscountNotOrderLevel(cartRao, (DiscountRAO) a))
						.forEach(action -> lineItemDiscounts.add(convertToLineItemDiscount((DiscountRAO) action, cartRao)));
				if (CollectionUtils.isNotEmpty(lineItemDiscounts))
				{
					lineItem.addDiscounts(lineItemDiscounts);
				}
			}
		}
		return lineItems;
	}

	protected boolean isDiscountNotOrderLevel(final AbstractOrderRAO orderRAO, final DiscountRAO discount)
	{
		if (CollectionUtils.isNotEmpty(orderRAO.getActions()))
		{
			return orderRAO.getActions().stream().filter(a -> a instanceof DiscountRAO).noneMatch(discount::equals);
		}
		return true;
	}

	protected OrderDiscount convertToOrderDiscount(final DiscountRAO discountRao, final AbstractOrderRAO cartRao)
	{
		AbstractAmount amount;
		if (StringUtils.isEmpty(discountRao.getCurrencyIsoCode()))
		{
			amount = new Percentage(discountRao.getValue());
		}
		else
		{
			amount = new Money(discountRao.getValue(), getAbstractOrderRaoToCurrencyConverter().convert(cartRao));
		}
		return new OrderDiscount(amount);
	}

	protected LineItemDiscount convertToLineItemDiscount(final DiscountRAO discountRao, final AbstractOrderRAO cartRao)
	{
		AbstractAmount amount;
		if (StringUtils.isEmpty(discountRao.getCurrencyIsoCode()))
		{
			amount = new Percentage(discountRao.getValue());
		}
		else
		{
			amount = new Money(discountRao.getValue(), getAbstractOrderRaoToCurrencyConverter().convert(cartRao));
		}
		final boolean perUnit = discountRao.isPerUnit() || getRaoUtils().isAbsolute(discountRao);

		if (discountRao.getAppliedToQuantity() > 0)
		{
			return new LineItemDiscount(amount, perUnit, (int) discountRao.getAppliedToQuantity());
		}

		return new LineItemDiscount(amount, perUnit);
	}

	/**
	 * creates the OrderCharge of type SHIPPING based on the given cart. If the cart contains a {@link ShipmentRAO} it
	 * will be taken for the shipping charge calculation, otherwise the {@link CartRAO#getDeliveryCost()} is used.
	 *
	 * @param cartRao
	 *           the cart to get the shipping order charge from
	 * @return an OrderCharge of type SHIPPING or null if no shipping cost is available
	 */
	protected OrderCharge convertToShippingOrderCharge(final AbstractOrderRAO cartRao)
	{
		// if shipment discount has been applied, take it to calculate order charge
		final Optional<ShipmentRAO> shipment = getRaoUtils().getShipment(cartRao);
		if (shipment.isPresent() && shipment.get().getMode() != null)
		{
			return getOrderUtils().createShippingCharge(getAbstractOrderRaoToCurrencyConverter().convert(cartRao), true,
					(shipment.get()).getMode().getCost());
		}
		// if no discount, take current delivery cost
		else if (cartRao.getDeliveryCost() != null)
		{
			return new OrderCharge(new Money(cartRao.getDeliveryCost(), getAbstractOrderRaoToCurrencyConverter().convert(cartRao)),
					ChargeType.SHIPPING);
		}
		else
		{
			return null;
		}
	}

	/**
	 * creates the OrderCharge of type PAYMENT based on the given cart.
	 *
	 * @param cartRao
	 *           the cart to get the payment order charge from
	 * @return an OrderCharge of type PAYMENT or null if no payment cost is available
	 */
	protected OrderCharge convertToPaymentOrderCharge(final AbstractOrderRAO cartRao)
	{
		if (cartRao.getPaymentCost() != null)
		{
			return new OrderCharge(new Money(cartRao.getPaymentCost(), getAbstractOrderRaoToCurrencyConverter().convert(cartRao)),
					ChargeType.PAYMENT);
		}
		else
		{
			return null;
		}
	}

	@Override
	public Order convert(final AbstractOrderRAO paramSOURCE, final Order paramTARGET)
	{
		throw new UnsupportedOperationException();
	}

	protected Converter<AbstractOrderRAO, Currency> getAbstractOrderRaoToCurrencyConverter()
	{
		return abstractOrderRaoToCurrencyConverter;
	}

	@Required
	public void setAbstractOrderRaoToCurrencyConverter(
			final Converter<AbstractOrderRAO, Currency> abstractOrderRaoToCurrencyConverter)
	{
		this.abstractOrderRaoToCurrencyConverter = abstractOrderRaoToCurrencyConverter;
	}

	protected Converter<OrderEntryRAO, NumberedLineItem> getOrderEntryRaoToNumberedLineItemConverter()
	{
		return orderEntryRaoToNumberedLineItemConverter;
	}

	@Required
	public void setOrderEntryRaoToNumberedLineItemConverter(
			final Converter<OrderEntryRAO, NumberedLineItem> orderEntryRaoToNumberedLineItemConverter)
	{
		this.orderEntryRaoToNumberedLineItemConverter = orderEntryRaoToNumberedLineItemConverter;
	}

	protected CalculationStrategies getCalculationStrategies()
	{
		return calculationStrategies;
	}

	@Required
	public void setCalculationStrategies(final CalculationStrategies calculationStrategies)
	{
		this.calculationStrategies = calculationStrategies;
	}

	protected OrderUtils getOrderUtils()
	{
		return orderUtils;
	}

	@Required
	public void setOrderUtils(final OrderUtils orderUtils)
	{
		this.orderUtils = orderUtils;
	}

	protected RaoUtils getRaoUtils()
	{
		return raoUtils;
	}

	@Required
	public void setRaoUtils(final RaoUtils raoUtils)
	{
		this.raoUtils = raoUtils;
	}

}
