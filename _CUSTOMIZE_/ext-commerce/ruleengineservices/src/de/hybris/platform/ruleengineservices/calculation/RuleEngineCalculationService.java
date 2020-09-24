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
package de.hybris.platform.ruleengineservices.calculation;

import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.ruleengineservices.rao.AbstractOrderRAO;
import de.hybris.platform.ruleengineservices.rao.CartRAO;
import de.hybris.platform.ruleengineservices.rao.DeliveryModeRAO;
import de.hybris.platform.ruleengineservices.rao.DiscountRAO;
import de.hybris.platform.ruleengineservices.rao.FreeProductRAO;
import de.hybris.platform.ruleengineservices.rao.OrderEntryRAO;
import de.hybris.platform.ruleengineservices.rao.ProductRAO;
import de.hybris.platform.ruleengineservices.rao.ShipmentRAO;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * RuleEngineCalculationService provides methods related to order and cart calculation to be used during rule
 * evaluation.
 */
public interface RuleEngineCalculationService
{

	/**
	 * performs a (re)calculation on the given abstractOrderRao and updates these properties:
	 * <ul>
	 * <li>{@link AbstractOrderRAO#getTotal()}</li>
	 * <li>{@link AbstractOrderRAO#getSubTotal()}</li>
	 * <li>{@link AbstractOrderRAO#getDeliveryCost()}</li>
	 * <li>{@link AbstractOrderRAO#getPaymentCost()}</li>
	 * <li></li>
	 * </ul>
	 *
	 * @param abstractOrderRao
	 *           the abstractOrderRao to calculate
	 */
	void calculateTotals(AbstractOrderRAO abstractOrderRao);

	/**
	 * Creates a new order level discount based on the given values, adds it to the cartRao and updates the cartRao
	 * totals. If the {@code absolute} flag is set to true, the discount is absolute, otherwise its a percentage based
	 * discount.
	 *
	 * @param cartRao
	 *           the cartRao to add the discount to
	 * @param absolute
	 *           whether the discount is absolute or percentage-based
	 * @param amount
	 *           the amount of the discount
	 * @return the created DiscountRAO
	 */
	DiscountRAO addOrderLevelDiscount(CartRAO cartRao, boolean absolute, BigDecimal amount);

	/**
	 * Creates a new order entry level discount based on the given values, adds it to the corresponding orderRao and
	 * updates the orderRao totals. If the {@code absolute} flag is set to true, the discount is absolute, otherwise its
	 * a percentage based discount.
	 *
	 * @param orderEntryRao
	 *           the orderEntryRao to add the discount to
	 * @param absolute
	 *           whether the discount is absolute or percentage-based
	 * @param amount
	 *           the amount of the discount
	 * @return the created DiscountRAO
	 */

	DiscountRAO addOrderEntryLevelDiscount(OrderEntryRAO orderEntryRao, boolean absolute, BigDecimal amount);

	/**
	 * Creates a new order entry level discount for stackable rule action based on the given values, adds it to the
	 * corresponding orderRao and updates the orderRao totals. If the {@code absolute} flag is set to true, the discount
	 * is absolute, otherwise its a percentage based discount.
	 *
	 * @deprecated since 6.7
	 *
	 * @param orderEntryRao
	 *           the orderEntryRao to add the discount to
	 * @param absolute
	 *           whether the discount is absolute or percentage-based
	 * @param amount
	 *           the amount of the discount
	 * @return the created DiscountRAO
	 */

	@Deprecated
	DiscountRAO addOrderEntryLevelDiscountStackable(OrderEntryRAO orderEntryRao, boolean absolute, BigDecimal amount);

	/**
	 * Changes the current delivery mode to the given values, adds it to the cartRao and updates the carRao totals.
	 *
	 * @param cartRAO
	 *           the cartRao to change the delivery mode for
	 * @param deliveryModeRAO
	 *           the new delivery mode
	 * @return the ShipmentRAO
	 */
	ShipmentRAO changeDeliveryMode(CartRAO cartRAO, DeliveryModeRAO deliveryModeRAO);

	/**
	 * Adds free product items to the cart.
	 *
	 * @param cartRao
	 *           cart where to add the product
	 * @param product
	 *           a product to add
	 * @param quantity
	 *           quantity of product items to add
	 * @return FreeProductRAO the free product to be given
	 */
	FreeProductRAO addFreeProductsToCart(final CartRAO cartRao, final ProductModel product, final int quantity);

	/**
	 * Creates a new order entry level discount such that the order entry's price will be equal to the given
	 * {@code fixedPrice} multiplied by quantity.
	 *
	 * @param orderEntryRao
	 *           the orderEntryRao to add the discount to
	 * @param fixedPrice
	 *           the target unit price
	 * @return the created DiscountRAO
	 */
	DiscountRAO addFixedPriceEntryDiscount(OrderEntryRAO orderEntryRao, BigDecimal fixedPrice);

	/**
	 * Creates a new order entry level discount for stackable rule action such that the order entry's price will be equal
	 * to the given {@code fixedPrice} multiplied by quantity.
	 *
	 * @deprecated since 6.7
	 *
	 * @param orderEntryRao
	 *           the orderEntryRao to add the discount to
	 * @param fixedPrice
	 *           the target unit price
	 * @return the created DiscountRAO
	 */
	@Deprecated
	DiscountRAO addFixedPriceEntryDiscountStackable(OrderEntryRAO orderEntryRao, BigDecimal fixedPrice);

	/**
	 * Runs cart calculation for cart not including the excluded products. CartRao argument is not modified by this
	 * process.
	 *
	 * @param cartRao
	 *           Cart to be calculated
	 * @param excludedProducts
	 *           List of products to be excluded from the cart calculation
	 * @return BigDecimal result of the cartSubTotals (without charges like payment or delivery) minus the excluded
	 *         products
	 */
	BigDecimal calculateSubTotals(CartRAO cartRao, Collection<ProductRAO> excludedProducts);

	/**
	 * Calculates current price of the set of Order Entries using only specified quantity of units for every Order Entry.
	 *
	 * @param orderEntryForDiscounts
	 *           set with Order Entries which price need to be calculated
	 * @param discountedOrderEntryMap
	 *           Map having orderEntry.entryNumber as keys and Unit Quantity To Be Consumed as values
	 * @return BigDecimal current price
	 */
	BigDecimal getCurrentPrice(Set<OrderEntryRAO> orderEntryForDiscounts, Map<Integer, Integer> discountedOrderEntryMap);

	/**
	 * Gets the total discount for line item and recalculates the discounted unit price.
	 *
	 * @param quantity
	 *           quantity of line item
	 * @param orderEntryRao
	 *           orderEntryRao to get discounted unit price for
	 * @return adjusted unit price
	 */
	BigDecimal getAdjustedUnitPrice(int quantity, OrderEntryRAO orderEntryRao);

	/**
	 * Creates order entry level discounts for the cart {@code cartRao}, with the {@code fixedPrice} given as parameter
	 * and for the products from entries in {@code selectedOrderEntryRaos} and related quantities given in
	 * {@code selectedOrderEntryMap}.
	 *
	 * @param cartRao
	 *           Cart RAO used for totals calculations
	 * @param selectedOrderEntryMap
	 *           Map having orderEntry.entryNumber as keys and unit quantity to be consumed as values
	 * @param selectedOrderEntryRaos
	 *           Order entries to be discounted
	 * @param fixedPrice
	 *           Target price for discounted product
	 * @return List of discounts that are generated
	 */
	List<DiscountRAO> addFixedPriceEntriesDiscount(final CartRAO cartRao, Map<Integer, Integer> selectedOrderEntryMap,
			Set<OrderEntryRAO> selectedOrderEntryRaos, BigDecimal fixedPrice);

	/**
	 * Creates order entry level discounts for the cart {@code cartRao}, adds it to the cartRao and updates the cartRao
	 * totals. Discounts are applied to entries contained within the selectedOrderEntryMap {@code selectedOrderEntryRaos}
	 * based on strategy.
	 *
	 * @param selectedOrderEntryMap
	 *           Map having orderEntry.entryNumber as keys and unit quantity to be consumed as values
	 * @param selectedOrderEntryRaos
	 *           Order entries to be discounted
	 * @param absolute
	 *           boolean value true is discount is absolute currency amount
	 * @param amount
	 *           value of discount
	 * @return List of discounts created as a result of this action
	 */
	List<DiscountRAO> addOrderEntryLevelDiscount(Map<Integer, Integer> selectedOrderEntryMap,
			Set<OrderEntryRAO> selectedOrderEntryRaos, boolean absolute, BigDecimal amount);

	/**
	 * Return consumed quantity for a given order entry
	 *
	 * @param orderEntryRao
	 *           Cart order entry
	 * @return consumed quantity
	 */
	int getConsumedQuantityForOrderEntry(OrderEntryRAO orderEntryRao);

	/**
	 * Given the order, return the total available quantity of items
	 *
	 * @deprecated since 6.7
	 *
	 * @param orderRAO
	 *           instance of {@link AbstractOrderRAO}
	 * @return total number of items available for consumption
	 */
	@Deprecated
	<T extends AbstractOrderRAO> int getOrderTotalAvailableQuantity(T orderRAO);

	/**
	 * give the order entry and the product, return the number of available product items to consume
	 *
	 * @param orderEntryRAO
	 *           instance of {@link OrderEntryRAO}
	 * @return total number of product items to consume
	 */
	int getProductAvailableQuantityInOrderEntry(OrderEntryRAO orderEntryRAO);
}
