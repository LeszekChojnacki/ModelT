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
package de.hybris.platform.ruleengineservices.calculation.impl;

import static com.google.common.collect.Lists.newArrayList;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;
import static java.math.BigDecimal.valueOf;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import de.hybris.order.calculation.domain.AbstractCharge.ChargeType;
import de.hybris.order.calculation.domain.AbstractDiscount;
import de.hybris.order.calculation.domain.LineItem;
import de.hybris.order.calculation.domain.LineItemDiscount;
import de.hybris.order.calculation.domain.Order;
import de.hybris.order.calculation.domain.OrderCharge;
import de.hybris.order.calculation.domain.OrderDiscount;
import de.hybris.order.calculation.money.AbstractAmount;
import de.hybris.order.calculation.money.Currency;
import de.hybris.order.calculation.money.Money;
import de.hybris.order.calculation.money.Percentage;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.ruleengineservices.calculation.MinimumAmountValidationStrategy;
import de.hybris.platform.ruleengineservices.calculation.NumberedLineItem;
import de.hybris.platform.ruleengineservices.calculation.RuleEngineCalculationService;
import de.hybris.platform.ruleengineservices.rao.AbstractOrderRAO;
import de.hybris.platform.ruleengineservices.rao.AbstractRuleActionRAO;
import de.hybris.platform.ruleengineservices.rao.CartRAO;
import de.hybris.platform.ruleengineservices.rao.DeliveryModeRAO;
import de.hybris.platform.ruleengineservices.rao.DiscountRAO;
import de.hybris.platform.ruleengineservices.rao.FreeProductRAO;
import de.hybris.platform.ruleengineservices.rao.OrderEntryConsumedRAO;
import de.hybris.platform.ruleengineservices.rao.OrderEntryRAO;
import de.hybris.platform.ruleengineservices.rao.ProductRAO;
import de.hybris.platform.ruleengineservices.rao.ShipmentRAO;
import de.hybris.platform.ruleengineservices.util.CurrencyUtils;
import de.hybris.platform.ruleengineservices.util.OrderUtils;
import de.hybris.platform.ruleengineservices.util.RaoUtils;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;


/**
 * The DefaultRuleEngineCalculationService uses the order calculation facilities to calculate the order and cart.
 */
public class DefaultRuleEngineCalculationService implements RuleEngineCalculationService
{
	private static final int DECLARATIVE_UNROUNDED_PRECISION = 10;

	private Converter<ProductModel, ProductRAO> productConverter;
	private Converter<AbstractOrderRAO, Order> abstractOrderRaoToOrderConverter;
	private MinimumAmountValidationStrategy minimumAmountValidationStrategy;
	private OrderUtils orderUtils;
	private CurrencyUtils currencyUtils;
	private RaoUtils raoUtils;
	private boolean consumptionEnabled;

	/**
	 * creates a DiscountRAO for the given CartRAO based on the input. Adds the discount to the cart RAO and recalculates
	 * the totals of the given CartRAO. The absolute parameter determines if the discount is an absolute one or a
	 * percentage based one. If absolute is set to false, the amount is read as an percent value, e.g. 10 for 10% (and
	 * not 0.1 for 10%)
	 * @param cartRao
	 *           the cartRAO
	 * @param absolute
	 *           whether the discount is absolute or a percentage discount
	 * @param amount
	 *           the amount
	 * @return the created and linked DiscountRAO
	 */
	@Override
	public DiscountRAO addOrderLevelDiscount(final CartRAO cartRao, final boolean absolute, final BigDecimal amount)
	{
		validateParameterNotNull(cartRao, "cart rao must not be null"); // NOSONAR
		validateParameterNotNull(amount, "amount must not be null"); // NOSONAR

		final Order cart = getAbstractOrderRaoToOrderConverter().convert(cartRao);
		final OrderDiscount discount = createOrderDiscount(cart, absolute, amount);

		final DiscountRAO discountRAO = createDiscountRAO(discount);
		getRaoUtils().addAction(cartRao, discountRAO);

		// recalculate totals
		recalculateTotals(cartRao, cart);
		return discountRAO;

	}

	@Override
	public BigDecimal getCurrentPrice(final Set<OrderEntryRAO> orderEntryForDiscounts,
			final Map<Integer, Integer> discountedOrderEntryMap)
	{
		final Order cart = getAbstractOrderRaoToOrderConverter().convert(orderEntryForDiscounts.iterator().next().getOrder());
		return orderEntryForDiscounts.stream().map(orderEntry ->
		{
			final NumberedLineItem li = this.findLineItem(cart, orderEntry);
			li.setGiveAwayUnits(orderEntry.getQuantity() - discountedOrderEntryMap.get(orderEntry.getEntryNumber()).intValue());
			return li;
		}).map(lineItem -> lineItem.getSubTotal().getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	/**
	 * creates a DiscountRAO for the given OrderEntryRAO based on the input. Adds the discount to the order entry RAO and
	 * recalculates the totals of the corresponding CartRAO. The absolute parameter determines if the discount is an
	 * absolute one or a percentage based one. If absolute is set to false, the amount is read as an percent value, e.g.
	 * 10 for 10% (and not 0.1 for 10%)
	 * @param orderEntryRao
	 *           the orderEntryRao
	 * @param absolute
	 *           whether the discount is absolute or a percentage discount
	 * @param amount
	 *           the amount
	 * @return the created and linked DiscountRAO
	 */
	@Override
	public DiscountRAO addOrderEntryLevelDiscount(final OrderEntryRAO orderEntryRao, final boolean absolute,
			final BigDecimal amount)
	{
		validateParameterNotNull(orderEntryRao, "order entry rao must not be null");
		validateParameterNotNull(orderEntryRao.getOrder(), "corresponding entry cart rao must not be null");
		validateParameterNotNull(amount, "amount must not be null");
		return addOrderEntryLevelDiscount(orderEntryRao, absolute, amount, getConsumedQuantityForOrderEntry(orderEntryRao));

	}

	/**
	 * @deprecated since 6.7
	 */
	@Deprecated
	@Override
	public DiscountRAO addOrderEntryLevelDiscountStackable(final OrderEntryRAO orderEntryRao, final boolean absolute,
			final BigDecimal amount)
	{
		validateParameterNotNull(orderEntryRao, "order entry rao must not be null");
		validateParameterNotNull(orderEntryRao.getOrder(), "corresponding entry cart rao must not be null");
		validateParameterNotNull(amount, "amount must not be null");
		return addOrderEntryLevelDiscount(orderEntryRao, absolute, amount, 0);
	}

	protected DiscountRAO addOrderEntryLevelDiscount(final OrderEntryRAO orderEntryRao, final boolean absolute,
			final BigDecimal amount, final int consumedQty)
	{
		Preconditions.checkArgument(consumedQty >= 0, "consumed quantity can't be negative");
		final Order cart = getAbstractOrderRaoToOrderConverter().convert(orderEntryRao.getOrder());
		final NumberedLineItem lineItem = findLineItem(cart, orderEntryRao);

		// convert percentage discount in absolute discount if needed
		final int qty = orderEntryRao.getQuantity() - consumedQty;
		final BigDecimal adjustedAmount = absolute ? amount.multiply(valueOf(qty)) : convertPercentageDiscountToAbsoluteDiscount(
				amount, qty, lineItem);

		final DiscountRAO discountRAO = createAbsoluteDiscountRAO(lineItem, adjustedAmount, qty, true);
		getRaoUtils().addAction(orderEntryRao, discountRAO);

		// recalculate totals
		final AbstractOrderRAO cartRao = orderEntryRao.getOrder();
		recalculateTotals(cartRao, cart);

		return discountRAO;
	}

	@Override
	public int getConsumedQuantityForOrderEntry(final OrderEntryRAO orderEntryRao)
	{
		final Set<OrderEntryRAO> entries = orderEntryRao.getOrder().getEntries();
		if (isNotEmpty(entries))
		{
			final Set<AbstractRuleActionRAO> allActions = entries.stream().filter(e -> isNotEmpty(e.getActions()))
					.flatMap(e -> e.getActions().stream()).collect(Collectors.toSet());
			return getConsumedQuantityForOrderEntry(orderEntryRao, allActions);
		}
		else
		{
			final Set<AbstractRuleActionRAO> actions = orderEntryRao.getActions();
			if (isNotEmpty(actions))
			{
				return getConsumedQuantityForOrderEntry(orderEntryRao, actions);
			}
		}
		return 0;
	}

	protected int getConsumedQuantityForAllDiscounts(final Set<AbstractRuleActionRAO> actions)
	{
		int consumedQty = 0;

		if (isConsumptionEnabled())
		{
			for (final AbstractRuleActionRAO action : actions)
			{
				if (action instanceof DiscountRAO)
				{
					final DiscountRAO discountRAO = (DiscountRAO) action;
					final Set<OrderEntryConsumedRAO> consumedEntries = discountRAO.getConsumedEntries();
					if (isNotEmpty(consumedEntries))
					{
						consumedQty += consumedEntries.stream().mapToInt(OrderEntryConsumedRAO::getQuantity).reduce(0, (q1, q2) -> q1 + q2);
					}
				}
			}
		}

		return consumedQty;
	}

	protected int getConsumedQuantityForOrderEntry(final OrderEntryRAO orderEntryRao, final Set<AbstractRuleActionRAO> actions)
	{
		int consumedQty = 0;

		if (isConsumptionEnabled())
		{
			for (final AbstractRuleActionRAO action : actions)
			{
				if (action instanceof DiscountRAO)
				{
					final DiscountRAO discountRAO = (DiscountRAO) action;
					final Set<OrderEntryConsumedRAO> consumedEntries = discountRAO.getConsumedEntries();
					if (isNotEmpty(consumedEntries))
					{
						consumedQty += consumedEntries.stream().filter(e -> e.getOrderEntry().equals(orderEntryRao)).mapToInt(OrderEntryConsumedRAO::getQuantity).reduce(0, (q1, q2) -> q1 + q2);
					}
				}
			}
		}

		return consumedQty;
	}

	/**
	 * @deprecated since 6.7
	 */
	@Override
	@Deprecated
	public <T extends AbstractOrderRAO> int getOrderTotalAvailableQuantity(final T orderRAO)
	{
		final Set<OrderEntryRAO> orderEntries = orderRAO.getEntries();
		int availableQuantity = 0;
		if (CollectionUtils.isNotEmpty(orderEntries))
		{
			availableQuantity += orderEntries.stream().mapToInt(e -> e.getQuantity() - getConsumedQuantityForOrderEntry(e))
					.reduce(0, (q1, q2) -> q1 + q2);
			final Set<AbstractRuleActionRAO> actions = orderRAO.getActions();
			if (isNotEmpty(actions))
			{
				final Set<AbstractRuleActionRAO> orderEntryLevelActions = orderEntries.stream()
						.filter(e -> CollectionUtils.isNotEmpty(e.getActions())).flatMap(e -> e.getActions().stream())
						.collect(Collectors.toSet());
				final Set<AbstractRuleActionRAO> notAccountedActions = actions.stream()
						.filter(a -> !orderEntryLevelActions.contains(a)).collect(Collectors.toSet());
				if (CollectionUtils.isNotEmpty(notAccountedActions))
				{
					availableQuantity -= getConsumedQuantityForAllDiscounts(notAccountedActions);
				}
			}
		}
		return availableQuantity;
	}

	@Override
	public int getProductAvailableQuantityInOrderEntry(final OrderEntryRAO orderEntryRAO)
	{
		return orderEntryRAO.getQuantity() - getConsumedQuantityForOrderEntry(orderEntryRAO);
	}

	@Override
	public DiscountRAO addFixedPriceEntryDiscount(final OrderEntryRAO orderEntryRao, final BigDecimal fixedPrice)
	{
		validateParameterNotNull(orderEntryRao, "cart rao must not be null");
		validateParameterNotNull(fixedPrice, "fixedPrice must not be null");
		validateParameterNotNull(orderEntryRao.getOrder(), "Order must not be null");
		validateParameterNotNull(orderEntryRao.getPrice(), "Product price is null");

		if (orderEntryRao.getPrice().compareTo(fixedPrice) > 0)
		{
			final BigDecimal price = orderEntryRao.getPrice();
			final BigDecimal discountAmount = price.subtract(fixedPrice);
			final BigDecimal scaledDiscountAmount = getCurrencyUtils()
					.applyRounding(discountAmount, orderEntryRao.getCurrencyIsoCode());

			return addOrderEntryLevelDiscount(orderEntryRao, true, scaledDiscountAmount);
		}
		return null;
	}

	/**
	 * @deprecated since 6.7
	 */
	@Deprecated
	@Override
	public DiscountRAO addFixedPriceEntryDiscountStackable(final OrderEntryRAO orderEntryRao, final BigDecimal fixedPrice)
	{
		validateParameterNotNull(orderEntryRao, "cart rao must not be null");
		validateParameterNotNull(fixedPrice, "fixedPrice must not be null");
		validateParameterNotNull(orderEntryRao.getOrder(), "Order must not be null");
		validateParameterNotNull(orderEntryRao.getPrice(), "Product price is null");

		if (orderEntryRao.getPrice().compareTo(fixedPrice) > 0)
		{
			final BigDecimal price = orderEntryRao.getPrice();
			final BigDecimal discountAmount = price.subtract(fixedPrice);
			final BigDecimal scaledDiscountAmount = getCurrencyUtils()
					.applyRounding(discountAmount, orderEntryRao.getCurrencyIsoCode());

			return addOrderEntryLevelDiscountStackable(orderEntryRao, true, scaledDiscountAmount);
		}
		return null;
	}

	@Override
	public FreeProductRAO addFreeProductsToCart(final CartRAO cartRao, final ProductModel product, final int quantity)
	{
		// find existing free product order entry or create new one

		final Optional<OrderEntryRAO> oeOptional = cartRao.getEntries().stream()
				.filter(e -> e.isGiveAway() && e.getProduct().getCode().equals(product.getCode()))
				.findFirst();

		final OrderEntryRAO orderEntryRao = oeOptional.orElseGet(OrderEntryRAO::new);

		orderEntryRao.setGiveAway(true);
		orderEntryRao.setBasePrice(BigDecimal.ZERO);
		orderEntryRao.setPrice(BigDecimal.ZERO);
		orderEntryRao.setCurrencyIsoCode(cartRao.getCurrencyIsoCode());
		orderEntryRao.setQuantity(orderEntryRao.getQuantity() + quantity);
		orderEntryRao.setProduct(getProductConverter().convert(product));
		orderEntryRao.setOrder(cartRao);

		if (cartRao.getEntries() == null)
		{
			cartRao.setEntries(new LinkedHashSet<>());
		}

		if (!oeOptional.isPresent())
		{
			cartRao.getEntries().add(orderEntryRao);
		}

		ensureOrderEntryRAOEntryNumbers(cartRao);

		final FreeProductRAO result = new FreeProductRAO();
		result.setQuantityAdded(quantity);

		getRaoUtils().addAction(cartRao, result);
		result.setAddedOrderEntry(orderEntryRao);

		return result;
	}

	/**
	 * for the given {@code AbstractOrderRAO} this method ensures that each of the {@link AbstractOrderRAO#getEntries()} has a
	 * entry number set. Note: This method only sets {@code OrderEntryRAO#setEntryNumber(Integer)} if it is not yet
	 * set, it does not check for duplicates or any other inconsistencies.
	 * @param abstractOrderRao
	 *           the AbstractOrderRAO to check
	 */
	protected void ensureOrderEntryRAOEntryNumbers(final AbstractOrderRAO abstractOrderRao)
	{
		if (abstractOrderRao != null && abstractOrderRao.getEntries() != null)
		{
			// get maximum and all nulled entries
			final List<OrderEntryRAO> nullEntries = newArrayList();
			final Set<OrderEntryRAO> abstractOrderRaoEntries = abstractOrderRao.getEntries();
			abstractOrderRaoEntries.stream().filter(e -> isNull(e.getEntryNumber())).forEach(nullEntries::add);
			int max = abstractOrderRaoEntries.stream().filter(e -> nonNull(e.getEntryNumber()))
					.mapToInt(OrderEntryRAO::getEntryNumber).max().orElse(-1);
			if (isNotEmpty(nullEntries))
			{
				for (final OrderEntryRAO orderEntryRAO : nullEntries)
				{
					max = max != -1 ? max + 1 : 1;
					orderEntryRAO.setEntryNumber(max);
				}
			}
		}
	}

	@Override
	public ShipmentRAO changeDeliveryMode(final CartRAO cartRao, final DeliveryModeRAO mode)
	{
		validateParameterNotNull(cartRao, "cart rao must not be null"); // NOSONAR
		validateParameterNotNull(mode, "mode must not be null");
		validateParameterNotNull(mode.getCost(), "mode cost must not be null");
		validateParameterNotNull(mode.getCode(), "mode code must not be null");
		final Order cart = getAbstractOrderRaoToOrderConverter().convert(cartRao);

		// remove all shipping charges first
		removeShippingCharges(cart);
		if (BigDecimal.ZERO.compareTo(mode.getCost()) < 0)
		{
			final OrderCharge shipping = createShippingCharge(cart, true, mode.getCost());
			cart.addCharge(shipping);
		}
		recalculateTotals(cartRao, cart);

		final ShipmentRAO shipmentRAO = createShipmentRAO(mode);
		getRaoUtils().addAction(cartRao, shipmentRAO);
		return shipmentRAO;
	}

	@Override
	public void calculateTotals(final AbstractOrderRAO cartRao)
	{
		final Order cart = getAbstractOrderRaoToOrderConverter().convert(cartRao);
		recalculateTotals(cartRao, cart);
	}


	/**
	 * returns the corresponding NumberedLineItem for the given {@code entryRao}. The lookup is based on both having the
	 * same {@code entryNumber}.
	 * @param cart
	 *           the cart to look up the line item from
	 * @param entryRao
	 *           the entry rao
	 * @return the corresponding NumberedLineItem
	 * @throws IllegalArgumentException
	 *            if no corresponding NumberedLineItem can be found
	 */
	protected NumberedLineItem findLineItem(final Order cart, final OrderEntryRAO entryRao)
	{
		validateParameterNotNull(cart, "cart must not be null");
		validateParameterNotNull(entryRao, "entry rao must not be null");
		validateParameterNotNull(entryRao.getEntryNumber(), "entry rao must have an entry number!");
		for (final LineItem item : cart.getLineItems())
		{
			if (item instanceof NumberedLineItem && entryRao.getEntryNumber().equals(((NumberedLineItem) item).getEntryNumber()))
			{
				return (NumberedLineItem) item;
			}
		}
		throw new IllegalArgumentException("can't find corresponding LineItem for the given orderEntryRao:" + entryRao);
	}

	/**
	 * returns the corresponding OrderEntryRAO for the given {@code lineItem}. The lookup is based on both having the
	 * same {@code entryNumber}.
	 * @param order
	 *           the order to look up the order entry rao from
	 * @param lineItem
	 *           the line item
	 * @return the corresponding OrderEntryRAP or null if none is found
	 */
	protected OrderEntryRAO findOrderEntryRAO(final AbstractOrderRAO order, final NumberedLineItem lineItem)
	{
		validateParameterNotNull(order, "order must not be null");
		validateParameterNotNull(lineItem, "lineItem must not be null");
		if (order.getEntries() != null)
		{
			for (final OrderEntryRAO rao : order.getEntries())
			{
				if (rao.getEntryNumber() != null && rao.getEntryNumber().equals(lineItem.getEntryNumber()))
				{
					return rao;
				}
			}
		}
		return null;
	}

	/**
	 * Uses the given {@code cart} to recalculate the totals of the given {@code cartRao}.
	 * @param cartRao
	 *           the cartRao to update
	 * @param cart
	 *           the cart to use for calculation
	 */
	protected void recalculateTotals(final AbstractOrderRAO cartRao, final Order cart)
	{
		// calculate total and set it
		cartRao.setSubTotal(cart.getSubTotal().getAmount());
		cartRao.setTotal(cart.getTotal().subtract(cart.getTotalCharge()).getAmount());
		cartRao.setTotalIncludingCharges(cart.getTotal().getAmount());
		cartRao.setDeliveryCost(cart.getTotalChargeOfType(ChargeType.SHIPPING).getAmount());
		cartRao.setPaymentCost(cart.getTotalChargeOfType(ChargeType.PAYMENT).getAmount());
		if (!isEmpty(cartRao.getEntries()))
		{
			for (final OrderEntryRAO entryRao : cartRao.getEntries())
			{
				final NumberedLineItem lineItem = findLineItem(cart, entryRao);
				entryRao.setTotalPrice(lineItem.getTotal(cart).getAmount());
			}
		}
	}

	/**
	 * Creates an OrderDiscount based on the given values and adds it to the given {@code cart}.
	 * @param cart
	 *           the cart to add the order discount to
	 * @param absolute
	 *           whether the discount is absolute or percentage based
	 * @param amount
	 *           the amount
	 * @return the created OrderDiscount
	 */
	protected OrderDiscount createOrderDiscount(final Order cart, final boolean absolute, final BigDecimal amount)
	{
		final Currency currency = cart.getCurrency();
		final BigDecimal adjustedDiscountAmount = absolute ? amount : convertPercentageDiscountToAbsoluteDiscount(amount, cart);
		final AbstractAmount discountAmount = new Money(adjustedDiscountAmount, currency);

		final OrderDiscount discount = new OrderDiscount(discountAmount);
		if (getMinimumAmountValidationStrategy().isOrderLowerLimitValid(cart, discount))
		{
			cart.addDiscount(discount);
			return discount;
		}
		else
		{
			final AbstractAmount zeroDiscountAmount = new Money(BigDecimal.ZERO, currency);
			final OrderDiscount zeroDiscount = new OrderDiscount(zeroDiscountAmount);
			cart.addDiscount(zeroDiscount);
			return zeroDiscount;
		}
	}

	/**
	 * Creates a new DiscountRAO based on the given AbstractDiscount.
	 * @param discount
	 *           the discount
	 * @return a new DiscountRAO
	 */
	protected DiscountRAO createDiscountRAO(final AbstractDiscount discount)
	{
		validateParameterNotNull(discount, "OrderDiscount must not be null.");
		final DiscountRAO discountRAO = new DiscountRAO();

		if (discount.getAmount() instanceof Money)
		{
			final Money money = (Money) discount.getAmount();
			discountRAO.setValue(money.getAmount());
			discountRAO.setCurrencyIsoCode(money.getCurrency().getIsoCode());
		}
		else
		{
			throw new IllegalArgumentException("OrderDiscount must have Money or Percentage amount set.");
		}
		if (discount instanceof LineItemDiscount)
		{
			final LineItemDiscount lineItemDiscount = (LineItemDiscount) discount;
			discountRAO.setAppliedToQuantity(lineItemDiscount.getApplicableUnits());
			discountRAO.setPerUnit(true);
		}
		return discountRAO;
	}


	/**
	 * Creates an LineItemDiscount based on the given values and adds it to the given {@code lineItem}. Sets perUnit
	 * value for partial line discounts
	 * @param lineItem
	 *           the line item to add the order discount to
	 * @param absolute
	 *           whether the discount is absolute or percentage based
	 * @param amount
	 *           the amount
	 * @param perUnit
	 *           true if the discount does not necessarily apply to whole line item
	 * @return the created OrderDiscount
	 */
	protected LineItemDiscount createLineItemDiscount(final LineItem lineItem, final boolean absolute, final BigDecimal amount,
			final boolean perUnit)
	{
		final Currency currency = lineItem.getBasePrice().getCurrency();
		final AbstractAmount discountAmount = absolute ? new Money(amount, currency) : new Percentage(amount);
		final LineItemDiscount discount = new LineItemDiscount(discountAmount, perUnit);

		return validateLineItemDiscount(lineItem, absolute, currency, discount);
	}

	protected LineItemDiscount createLineItemDiscount(final LineItem lineItem, final boolean absolute, final BigDecimal amount,
			final boolean perUnit, final int applicableUnits)
	{
		final Currency currency = lineItem.getBasePrice().getCurrency();
		BigDecimal adjustedAmount = amount;
		if (absolute)
		{
			if (applicableUnits >= 1)
			{
				adjustedAmount = amount.divide(valueOf(applicableUnits), DECLARATIVE_UNROUNDED_PRECISION, BigDecimal.ROUND_DOWN);
			}
			else
			{
				adjustedAmount = BigDecimal.ZERO;
			}
		}
		final AbstractAmount discountAmount = absolute ? new Money(adjustedAmount, currency) : new Percentage(adjustedAmount);
		final LineItemDiscount discount = new LineItemDiscount(discountAmount, perUnit, applicableUnits);

		return validateLineItemDiscount(lineItem, absolute, currency, discount);
	}

	protected LineItemDiscount validateLineItemDiscount(final LineItem lineItem, final boolean absolute, final Currency currency,
			final LineItemDiscount discount)
	{
		if (getMinimumAmountValidationStrategy().isLineItemLowerLimitValid(lineItem, discount))
		{
			lineItem.addDiscount(discount);
			return discount;
		}
		else
		{
			final AbstractAmount zeroDiscountAmount = absolute ? new Money(BigDecimal.ZERO, currency) : Percentage.ZERO;
			final LineItemDiscount zeroDiscount = new LineItemDiscount(zeroDiscountAmount);
			lineItem.addDiscount(zeroDiscount);
			return zeroDiscount;
		}
	}

	/**
	 * Creates an LineItemDiscount based on the given values and adds it to the given {@code lineItem}.
	 * @param lineItem
	 *           the line item to add the order discount to
	 * @param absolute
	 *           whether the discount is absolute or percentage based
	 * @param amount
	 *           the amount
	 * @return the created OrderDiscount
	 */
	protected LineItemDiscount createLineItemDiscount(final LineItem lineItem, final boolean absolute, final BigDecimal amount)
	{
		return createLineItemDiscount(lineItem, absolute, amount, absolute);
	}

	/**
	 * Returns all Shipping charges from the Order.
	 * @param cart
	 *           instance of {@link Order}
	 */
	protected void removeShippingCharges(final Order cart)
	{
		validateParameterNotNull(cart, "cart must not be null.");
		for (final OrderCharge charge : cart.getCharges())
		{
			if (ChargeType.SHIPPING.equals(charge.getChargeType()))
			{
				cart.removeCharge(charge);
			}
		}
	}

	/**
	 * creates an {@code OrderCharge} of {@link ChargeType#SHIPPING} for the given values and adds it to the given cart.
	 * @param cart
	 *           the given cart to apply the shipping charge to
	 * @param absolute
	 *           whether the shipping charge is percentage-based or absolute.
	 * @param value
	 *           the value of the charge
	 * @return the newly created OrderCharge
	 */
	protected OrderCharge createShippingCharge(final Order cart, final boolean absolute, final BigDecimal value)
	{
		final OrderCharge shippingCharge = getOrderUtils().createShippingCharge(cart.getCurrency(), absolute, value);
		cart.addCharge(shippingCharge);
		return shippingCharge;
	}

	/**
	 * Creates a new ShipmentRAO based on the given Delivery Mode.
	 */
	protected ShipmentRAO createShipmentRAO(final DeliveryModeRAO mode)
	{
		validateParameterNotNull(mode, "mode must not be null.");
		final ShipmentRAO shipmentRao = new ShipmentRAO();
		shipmentRao.setMode(mode);
		return shipmentRao;
	}

	protected Converter<ProductModel, ProductRAO> getProductConverter()
	{
		return productConverter;
	}

	@Required
	public void setProductConverter(final Converter<ProductModel, ProductRAO> productConverter)
	{
		this.productConverter = productConverter;
	}

	protected Converter<AbstractOrderRAO, Order> getAbstractOrderRaoToOrderConverter()
	{
		return abstractOrderRaoToOrderConverter;
	}

	@Required
	public void setAbstractOrderRaoToOrderConverter(final Converter<AbstractOrderRAO, Order> abstractOrderRaoToOrderConverter)
	{
		this.abstractOrderRaoToOrderConverter = abstractOrderRaoToOrderConverter;
	}

	@Override
	public BigDecimal calculateSubTotals(final CartRAO cartRao, final Collection<ProductRAO> excludedProducts)
	{
		validateParameterNotNull(cartRao, "Cart must not be null.");

		if (isEmpty(excludedProducts))
		{
			return cartRao.getSubTotal();
		}

		final CartRAO cloneCart = new CartRAO();
		cloneCart.setEntries(new HashSet<>());

		final List<String> productCodes = Lists.newArrayList();
		excludedProducts.forEach(p -> productCodes.add(p.getCode()));
		cartRao.getEntries().stream()
				.filter(entry -> entry.getProduct() != null && !productCodes.contains(entry.getProduct().getCode()))
				.forEach(eRao -> cloneCart.getEntries().add(eRao));

		cloneCart.setPaymentCost(cartRao.getPaymentCost());
		cloneCart.setDeliveryCost(cartRao.getDeliveryCost());
		cloneCart.setDiscountValues(cartRao.getDiscountValues());
		cloneCart.setCurrencyIsoCode(cartRao.getCurrencyIsoCode());
		cloneCart.setActions(cartRao.getActions());
		cloneCart.setOriginalTotal(cartRao.getOriginalTotal());

		calculateTotals(cloneCart);

		return cloneCart.getSubTotal();
	}

	@Override
	public BigDecimal getAdjustedUnitPrice(final int quantity, final OrderEntryRAO orderEntryRao)
	{
		final Order cart = getAbstractOrderRaoToOrderConverter().convert(orderEntryRao.getOrder());
		final NumberedLineItem lineItem = findLineItem(cart, orderEntryRao);
		return lineItem.getTotalDiscount().getAmount().divide(valueOf(quantity), RoundingMode.HALF_UP);
	}


	@Override
	public List<DiscountRAO> addFixedPriceEntriesDiscount(final CartRAO cartRao,
			final Map<Integer, Integer> selectedOrderEntryMap, final Set<OrderEntryRAO> selectedOrderEntryRaos,
			final BigDecimal fixedPrice)
	{
		validateParameterNotNull(cartRao, "cartRao must not be null");
		validateParameterNotNull(cartRao.getEntries(), "cartRao.entries must not be null");
		validateParameterNotNull(selectedOrderEntryMap, "selectedOrderEntryMap must not be null");
		validateParameterNotNull(selectedOrderEntryRaos, "selectedOrderEntryRaos must not be null");
		for (final OrderEntryRAO orderEntryRAO : selectedOrderEntryRaos)
		{
			validateParameterNotNull(orderEntryRAO, "orderEntryRao must not be null");
			if (!cartRao.getEntries().contains(orderEntryRAO))
			{
				throw new IllegalArgumentException("orderEntryRao from given set of selectedOrderEntryRaos:"
						+ orderEntryRAO.toString() + " must be part of the given cartRAO.entries!");
			}
		}
		final Order cart = getAbstractOrderRaoToOrderConverter().convert(cartRao);
		final List<DiscountRAO> result = Lists.newArrayList();
		for (final OrderEntryRAO orderEntryRao : selectedOrderEntryRaos)
		{
			final BigDecimal unitPrice = orderEntryRao.getPrice();

			final Integer quantityToDiscount = selectedOrderEntryMap.get(orderEntryRao.getEntryNumber());
			//	total discount is (quantity * baseprice) - (quantity * targetprice)
			final BigDecimal totalEntryDiscountAmount = unitPrice.multiply(BigDecimal.valueOf(quantityToDiscount))
					.subtract(fixedPrice.multiply(BigDecimal.valueOf(quantityToDiscount)));
			final BigDecimal roundedTotalEntryDiscountAmount = getCurrencyUtils().applyRounding(totalEntryDiscountAmount,
					orderEntryRao.getCurrencyIsoCode());

			final NumberedLineItem lineItem = findLineItem(cart, orderEntryRao);
			final DiscountRAO discountRAO = createAbsoluteDiscountRAO(lineItem, roundedTotalEntryDiscountAmount,
					quantityToDiscount.intValue(), true);
			getRaoUtils().addAction(orderEntryRao, discountRAO);

			calculateTotals(cartRao);
			result.add(discountRAO);
		}
		return result;
	}

	protected DiscountRAO createAbsoluteDiscountRAO(final LineItem lineItem, final BigDecimal amount, final int applicableUnits)
	{
		return createAbsoluteDiscountRAO(lineItem, amount, applicableUnits, false);
	}

	protected DiscountRAO createAbsoluteDiscountRAO(final LineItem lineItem, final BigDecimal amount, final int applicableUnits,
			final boolean perUnit)
	{
		final int appliedToQuantity = perUnit ? applicableUnits : lineItem.getNumberOfUnits();
		final Currency currency = lineItem.getBasePrice().getCurrency();
		final BigDecimal adjustedAmount = amount.divide(valueOf(appliedToQuantity), DECLARATIVE_UNROUNDED_PRECISION,
				RoundingMode.DOWN);
		final AbstractAmount discountAmount = new Money(adjustedAmount, currency);
		LineItemDiscount discount = new LineItemDiscount(discountAmount, true, appliedToQuantity);
		discount = validateLineItemDiscount(lineItem, true, currency, discount);

		final DiscountRAO discountRAO = new DiscountRAO();
		discountRAO.setPerUnit(perUnit);
		discountRAO.setAppliedToQuantity(appliedToQuantity);
		final Money money = (Money) discount.getAmount();
		discountRAO.setValue(money.getAmount().compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : adjustedAmount);
		discountRAO.setCurrencyIsoCode(money.getCurrency().getIsoCode());
		return discountRAO;
	}

	@Override
	public List<DiscountRAO> addOrderEntryLevelDiscount(final Map<Integer, Integer> selectedOrderEntryMap,
			final Set<OrderEntryRAO> selectedOrderEntryRaos, final boolean absolute, final BigDecimal amount)
	{
		validateParameterNotNull(selectedOrderEntryMap, "selectedOrderEntryMap must not be null");
		validateParameterNotNull(selectedOrderEntryRaos, "selectedOrderEntryRaos must not be null");

		validateParameterNotNull(amount, "amount must not be null");

		final List<DiscountRAO> result = Lists.newArrayList();
		for (final OrderEntryRAO orderEntryRao : selectedOrderEntryRaos)
		{
			final int qty = selectedOrderEntryMap.get(orderEntryRao.getEntryNumber()).intValue();
			final Order cart = getAbstractOrderRaoToOrderConverter().convert(orderEntryRao.getOrder());
			final NumberedLineItem lineItem = findLineItem(cart, orderEntryRao);

			// convert percentage discount in absolute discount if needed, otherwise just multiply by number of target items (perUnit is true)
			final BigDecimal adjustedAmount = absolute ? amount.multiply(valueOf(qty))
					: convertPercentageDiscountToAbsoluteDiscount(amount, qty, lineItem);

			final DiscountRAO discountRAO = createAbsoluteDiscountRAO(lineItem, adjustedAmount, qty, true);
			getRaoUtils().addAction(orderEntryRao, discountRAO);
			result.add(discountRAO);

			final CartRAO cartRao = (CartRAO) orderEntryRao.getOrder();
			recalculateTotals(cartRao, cart);
		}
		return result;
	}

	protected BigDecimal convertPercentageDiscountToAbsoluteDiscount(final BigDecimal percentageAmount,
			final int quantityToConsume, final NumberedLineItem orderLineItem)
	{
		final List<LineItemDiscount> lineItemDiscounts = orderLineItem.getDiscounts();
		final int numItemsDiscounted = lineItemDiscounts.stream().mapToInt(LineItemDiscount::getApplicableUnits).sum();
		final int availableItems = orderLineItem.getNumberOfUnits() - numItemsDiscounted;
		BigDecimal valueToDiscount;
		if (quantityToConsume <= availableItems)
		{
			valueToDiscount = orderLineItem.getBasePrice().getAmount().multiply(valueOf(quantityToConsume));
		}
		else
		// quantityToConsume > availableItems
		{
			final BigDecimal availableItemsValueToDiscount = orderLineItem.getBasePrice().getAmount()
					.multiply(valueOf(availableItems));
			final BigDecimal residualItemsValueToDiscount = orderLineItem.getBasePrice().getAmount()
					.multiply(valueOf(numItemsDiscounted)).subtract(orderLineItem.getTotalDiscount().getAmount())
					.multiply(valueOf((double) (quantityToConsume - (long) availableItems) / numItemsDiscounted));
			valueToDiscount = availableItemsValueToDiscount.add(residualItemsValueToDiscount);
		}
		final BigDecimal fraction = percentageAmount.divide(valueOf(100.0), DECLARATIVE_UNROUNDED_PRECISION, RoundingMode.DOWN);
		return valueToDiscount.multiply(fraction);
	}

	protected BigDecimal convertPercentageDiscountToAbsoluteDiscount(final BigDecimal percentageAmount,
			final Order cart)
	{
		final BigDecimal valueToDiscount = cart.getSubTotal().getAmount();
		return valueToDiscount.multiply(percentageAmount).divide(valueOf(100.0), RoundingMode.HALF_UP);
	}

	protected MinimumAmountValidationStrategy getMinimumAmountValidationStrategy()
	{
		return minimumAmountValidationStrategy;
	}

	@Required
	public void setMinimumAmountValidationStrategy(final MinimumAmountValidationStrategy minimumAmountValidationStrategy)
	{
		this.minimumAmountValidationStrategy = minimumAmountValidationStrategy;
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

	protected CurrencyUtils getCurrencyUtils()
	{
		return currencyUtils;
	}

	@Required
	public void setCurrencyUtils(final CurrencyUtils currencyUtils)
	{
		this.currencyUtils = currencyUtils;
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

	protected boolean isConsumptionEnabled()
	{
		return consumptionEnabled;
	}

	@Required
	public void setConsumptionEnabled(final boolean consumptionEnabled)
	{
		this.consumptionEnabled = consumptionEnabled;
	}
}
