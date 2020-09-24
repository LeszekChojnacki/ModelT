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
package de.hybris.order.calculation.domain;

import de.hybris.order.calculation.domain.AbstractCharge.ChargeType;
import de.hybris.order.calculation.exception.CurrenciesAreNotEqualException;
import de.hybris.order.calculation.exception.MissingCalculationDataException;
import de.hybris.order.calculation.money.Currency;
import de.hybris.order.calculation.money.Money;
import de.hybris.order.calculation.money.Money.MoneyExtractor;
import de.hybris.order.calculation.money.Percentage;
import de.hybris.order.calculation.strategies.CalculationStrategies;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


/**
 * Central Object that holds all calculation related data and strategies required for calculating the order/cart. The
 * net/gross mode (see {@link #isNet()}) and the current currency ({@link #getCurrency()}) has to be provided during
 * creation. After this point {@link LineItem} or other objects can be created with a different {@link Currency} but
 * when adding it tho the current {@link Order} a {@link CurrenciesAreNotEqualException} is thrown.
 * <p/>
 * The getTotal...() methods returns the overall calculated Money for the different line item posts or taxes. With
 * get...Values() a map is returned which shows the concrete single amount for each charge/discount/tax.
 */

//REVIEW: what happens first? subtotal-discounts+additionalcosts or subtotal+additionalcosts-discounts
//Also: when the discounts are calculated is the starting point always the subtotal and this is also when calculating the additionalcosts?
//OR for calculating the additionalcost the subtotal is already the subtotal-discount ?

public class Order
{
	private static final MoneyExtractor<LineItem> LINE_ITEM_TOTAL_EXTRACTOR = new MoneyExtractor<LineItem>()
	{
		@Override
		public Money extractMoney(final LineItem lineItem)
		{
			return lineItem.getTotal(null);
		}
	};
	private final boolean netMode;
	private final Currency currency;
	private final CalculationStrategies calculationStrategies;

	private final List<LineItem> lineItems;
	private final List<Tax> taxes;
	private final List<OrderDiscount> discounts;
	private final List<OrderCharge> charges;

	/**
	 * Creates a new gross order calculation object based on a specific currency.
	 *
	 * @param currency
	 *           the currency the whole calculation will be in
	 * @param calculationStrategies
	 *           some strategies
	 */
	public Order(final Currency currency, final CalculationStrategies calculationStrategies)
	{
		this(currency, false, calculationStrategies);
	}

	/**
	 * Creates a new order calculation object based on a specific currency and bet/gross mode.
	 *
	 * @param currency
	 *           the currency the whole calculation will be in
	 * @param isNet
	 *           true for net, false for gross mode
	 * @param calculationStrategies
	 *           some strategies
	 */
	public Order(final Currency currency, final boolean isNet, final CalculationStrategies calculationStrategies)
	{
		this.currency = currency;
		this.netMode = isNet;
		this.calculationStrategies = calculationStrategies;
		this.lineItems = new ArrayList<>();
		this.taxes = new ArrayList<>();
		this.discounts = new ArrayList<>();
		this.charges = new ArrayList<>();
	}

	/**
	 * Calculates the order total. Depending on the net/gross status of this order that total is either net or gross.
	 *
	 * Please know that {@link #getTotalIncludingTaxes()} will always return the gross total for both modes.
	 *
	 * @see #getSubTotal()
	 * @see #getTotalIncludingTaxes()
	 * @see #getTotalCharge()
	 * @see #getTotalDiscount()
	 */
	public Money getTotal()
	{
		return getSubTotal().subtract(getTotalDiscount()).add(getTotalCharge());
	}

	/**
	 * Calculates the sum of all line item totals ({@link LineItem#getTotal(Order)}).
	 *
	 * @see #getTotal()
	 */
	public Money getSubTotal()
	{
		if (hasLineItems())
		{
			return Money.sum(getLineItems(), LINE_ITEM_TOTAL_EXTRACTOR);
		}
		else
		{
			return Money.zero(currency);
		}
	}

	/**
	 * Calculates the total tax for a specific {@link Tax} object.
	 */
	public Money getTotalTaxFor(final Tax tax)
	{
		if (!taxes.contains(tax)) // NOSONAR
		{
			throw new IllegalArgumentException("Tax " + tax + " doesnt belong to order " + this + " - cannot calculate total."); // NOSONAR
		}
		final double taxCorrectionFactor = getAutomaticTaxCorrectionFactor();
		return calculateTaxTotal(tax, taxCorrectionFactor);
	}

	/**
	 * Calculates total taxes for all assigned {@link Tax} object.
	 *
	 * @return a map ( {@link Tax} -&gt; {@link Money} )
	 */
	public Map<Tax, Money> getTotalTaxes()
	{
		final Map<Tax, Money> result = new LinkedHashMap<>(taxes.size());
		final double taxCorrectionFactor = getAutomaticTaxCorrectionFactor();
		for (final Tax tax : getTaxes())
		{
			result.put(tax, calculateTaxTotal(tax, taxCorrectionFactor));
		}
		return result;
	}

	//	/*
	//	 * Adjustment factor to be used for automatically adjusting all taxes by the
	//	 * factory derived from order total changing due to global discounts and additional
	//	 * costs (EXCLUDING additional costs that have a fixed tax rate!).
	//	 *
	//	 * Formula:
	//	 *
	//	 * factor = ( total - SUM( additional cost without tax ) ) / subtotal
	//	 *
	//	 * Example 1:
	//	 * 	subtotal = 50e
	//	 * 	shipping = 25e ( no tax assigned )
	//  *		total = 75e
	//	 * 	f = ( 75e - 0e ) / 50e = 1.5  -> 150% up
	//  *
	//	 * Example 2:
	//	 * 	subtotal = 50e
	//	 * 	shipping = 25e ( 19% taxed )
	//  *		total = 75e
	//	 * 	f = ( 75e - 25e ) / 50e = 1  -> all taxes stay same ( shipping cost is already in 19% tax values! )
	//	 */
	protected double getAutomaticTaxCorrectionFactor()
	{
		if (BigDecimal.ZERO.compareTo(getSubTotal().getAmount()) == 0)
		{
			return 1;
		}
		final BigDecimal totalWoFixedTaxedCharges = getTotal().subtract(getFixedTaxedAdditionalCharges(getTotalCharges()))
				.getAmount();
		return totalWoFixedTaxedCharges.doubleValue() / getSubTotal().getAmount().doubleValue();
	}

	protected Money getFixedTaxedAdditionalCharges(final Map<OrderCharge, Money> aocValues)
	{
		Money sum = new Money(getCurrency());
		for (final Map.Entry<OrderCharge, Money> e : aocValues.entrySet())
		{
			if (hasAssignedTaxes(e.getKey()))
			{
				sum = sum.add(e.getValue());
			}
		}
		return sum;
	}

	/**
	 * Looks up potentially assigned {@link Tax} object for a given {@link Taxable} object.
	 *
	 * @return the assigned taxes or an empty collection
	 */
	public Collection<Tax> getTaxesFor(final Taxable object)
	{
		Collection<Tax> ret = null;
		for (final Tax t : getTaxes())
		{
			if (t.getTargets().contains(object))
			{
				if (ret == null)
				{
					ret = new LinkedHashSet<>();
				}
				ret.add(t);
			}
		}
		return ret == null ? Collections.emptySet() : ret;
	}

	/**
	 * @return true if there is at least one {@link Tax} objects targeting the specified {@link Taxable} object
	 */
	public boolean hasAssignedTaxes(final Taxable object)
	{
		for (final Tax t : getTaxes())
		{
			if (t.getTargets().contains(object))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the overall sum of taxes within this order.
	 *
	 * To get the exact money for each tax please use {@link #getTotalTaxes()}.
	 *
	 * @see #getTotalTaxes()
	 */
	public Money getTotalTax()
	{
		final Map<Tax, Money> taxValues = getTotalTaxes();
		return taxValues.isEmpty() ? Money.zero(getCurrency()) : Money.sum(taxValues.values());
	}

	// REVIEW: This is not entirely correct since there may be additional charges (and discounts)
	// with direct assignment of Taxes -> these must be treated as normal charges and added to the
	// sum money for their tax.
	// Logically all additional charges and discounts that do have direct tax assignments must NOT be
	// contributing to the 'auto-tax' correction factor.
	protected Money calculateTaxTotal(final Tax tax, final double autoTaxCorrectionFactor)
	{
		if (tax.getAmount() instanceof Money)
		{
			return (Money) tax.getAmount();
		}
		else if (tax.getAmount() instanceof Percentage)
		{
			Money taxedChargeSum = Money.zero(getCurrency());
			for (final Taxable taxcharge : tax.getTargets())
			{
				taxedChargeSum = taxedChargeSum.add(taxcharge.getTotal(this));
			}
			final BigDecimal taxRate = ((Percentage) tax.getAmount()).getRate();
			final BigDecimal costRate = isNet() ? new BigDecimal(100) : taxRate.add(new BigDecimal(100));

			final double overallFactor = (taxRate.doubleValue() * autoTaxCorrectionFactor) / costRate.doubleValue();

			return getCalculationStrategies().getTaxRondingStrategy().multiply(taxedChargeSum, BigDecimal.valueOf(overallFactor));
		}
		else
		{
			throw new IllegalStateException();
		}
	}

	/**
	 * Calculates the total of all charges which apply to the order at a whole.
	 *
	 * Please note that line item charges are not included here since they are already part of the line item total!
	 *
	 * @see #getTotalCharges()
	 * @see #getTotalChargeOfType(ChargeType)
	 */
	public Money getTotalCharge()
	{
		final Map<OrderCharge, Money> map = getTotalCharges();
		return map.isEmpty() ? new Money(getCurrency()) : Money.sum(map.values());
	}

	/**
	 * Calculates the total of all charges of one specific type which apply to the order at a whole.
	 *
	 * Please note that line item charges are not included here since they are already part of the line item total!
	 *
	 * @param chargeType
	 *           the type of charge to select
	 *
	 * @see #getTotalCharge()
	 * @see #getTotalCharges()
	 */
	public Money getTotalChargeOfType(final ChargeType chargeType)
	{
		final Money zero = Money.zero(getCurrency());

		return hasCharges() ? Money.sum(getTotalCharges().entrySet(), new MoneyExtractor<Map.Entry<OrderCharge, Money>>()
		{
			@Override
			public Money extractMoney(final Entry<OrderCharge, Money> moneyEntry)
			{
				return chargeType.equals(moneyEntry.getKey().getChargeType()) ? moneyEntry.getValue() : zero;
			}
		}) : zero;
	}

	/**
	 * Calculates the total of all discounts assigned to this order.
	 *
	 * Please note that this does not include line item discounts since they are already included in line item totals!
	 *
	 * @see #getTotalDiscounts()
	 */
	public Money getTotalDiscount()
	{
		final Map<OrderDiscount, Money> orderDiscountValues = getTotalDiscounts();
		return orderDiscountValues.isEmpty() ? new Money(getCurrency()) : Money.sum(orderDiscountValues.values());
	}

	/**
	 * Calculates totals of all charges assigned to this order separately.
	 *
	 * @return a map ( {@link OrderCharge} -&gt; {@link Money} )
	 */
	public Map<OrderCharge, Money> getTotalCharges()
	{
		if (charges.isEmpty())
		{
			return Collections.emptyMap();
		}

		//REVIEW: this is clearly calculation and belongs into the appropriate strategy
		final Map<OrderCharge, Money> resultmap = new LinkedHashMap<>(charges.size());
		Money currentValue = getSubTotal();
		Money calculatedAddCharge;

		for (final OrderCharge orderCharge : charges)
		{
			calculatedAddCharge = calculateOrderCharge(currentValue, orderCharge);
			currentValue = currentValue.add(calculatedAddCharge);
			resultmap.put(orderCharge, calculatedAddCharge);
		}
		return resultmap;
	}

	/**
	 * Calculates totals of all discounts assigned to this order separately.
	 *
	 * @return a map ( {@link OrderDiscount} -&gt {@link Money} )
	 */
	public Map<OrderDiscount, Money> getTotalDiscounts()
	{
		if (discounts.isEmpty())
		{
			return Collections.emptyMap();
		}

		//REVIEW: this is clearly calculation and belongs into the appropriate strategy
		final Map<OrderDiscount, Money> resultmap = new LinkedHashMap<>(discounts.size());
		Money currentValue = getSubTotal();
		Money calculatedDiscount;

		for (final OrderDiscount orderDisc : discounts)
		{
			calculatedDiscount = calculateOrderDiscount(currentValue, orderDisc);
			currentValue = currentValue.subtract(calculatedDiscount);
			resultmap.put(orderDisc, calculatedDiscount);
		}
		return resultmap;
	}

	protected Money calculateOrderCharge(final Money currentValue, final OrderCharge addCharge)
	{
		if (addCharge.isDisabled())
		{
			return new Money(getCurrency());
		}
		else
		{
			if (addCharge.getAmount() instanceof Money)
			{
				//absolute value
				return (Money) addCharge.getAmount();
			}
			else if (addCharge.getAmount() instanceof Percentage)
			{
				final Percentage percent = (Percentage) addCharge.getAmount();
				// REVIEW: wtf ? is this really a good way to do /100 ???
				return getCalculationStrategies().getRoundingStrategy().multiply(currentValue, percent.getRate().movePointLeft(2));
			}
		}
		throw new MissingCalculationDataException("Could not calculate order charge for Order");
	}

	/**
	 * Calculates order discount
	 * @param currentValue
	 * @param discount
	 * @throws CurrenciesAreNotEqualException
	 * @throws MissingCalculationDataException
	 * @return money
	 */
	protected Money calculateOrderDiscount(final Money currentValue, final OrderDiscount discount)
	{
		if (discount.getAmount() instanceof Money)
		{
			//absolute value
			return (Money) discount.getAmount();
		}
		else if (discount.getAmount() instanceof Percentage)
		{
			final Percentage percent = (Percentage) discount.getAmount();
			return getCalculationStrategies().getRoundingStrategy().multiply(currentValue, percent.getRate().movePointLeft(2));
		}
		throw new MissingCalculationDataException("Could not calculate order discount for Order");
	}

	/**
	 * Calculates the order total always including taxes. This makes most sense in case of this order being in net mode
	 * when {@link #getTotal()} will show order total without taxes only.
	 */
	public Money getTotalIncludingTaxes()
	{
		return isNet() ? getTotal().add(getTotalTax()) : getTotal();
	}


	// --------------- getters / setters ----------------------------

	// stuff around line items

	/**
	 * Returns all line items attached to this order.
	 */
	public List<LineItem> getLineItems()
	{
		return Collections.unmodifiableList(this.lineItems);
	}

	/**
	 * Attaches multiple line items to this order.
	 *
	 * Please note that each line item will receive a back reference to this order.
	 *
	 * @throws CurrenciesAreNotEqualException
	 *            in case at least one line item has got a base price of different currency than the order currency
	 */
	public void addLineItems(final LineItem... lineItems)
	{
		addLineItems(Arrays.asList(lineItems));
	}

	/**
	 * Attaches multiple line items to this order.
	 *
	 * Please note that each line item will receive a back reference to this order.
	 *
	 * @throws CurrenciesAreNotEqualException
	 *            in case at least one line item has got a base price of different currency than the order currency
	 */
	public void addLineItems(final List<LineItem> lineItems)
	{
		for (final LineItem lineItem : lineItems)
		{
			if (!this.lineItems.contains(lineItem))
			{
				lineItem.getBasePrice().assertCurreniesAreEqual(getCurrency());
				lineItem.setOrder(this);
				this.lineItems.add(lineItem);
			}
		}
	}

	/**
	 * Attaches a line items to this order.
	 *
	 * Please note that the line item will receive a back reference to this order.
	 *
	 * @throws CurrenciesAreNotEqualException
	 *            in case the line item has got a base price of different currency than the order currency
	 */
	public void addLineItem(final LineItem lineitem)
	{
		addLineItem(lineItems.size(), lineitem);
	}

	/**
	 * Attaches a line items to this order at the specified position.
	 *
	 * Please note that the line item will receive a back reference to this order.
	 *
	 *
	 * @param index
	 *           insert the given line item into this position.
	 * @param lineitem
	 *           the line item to be added
	 * @throws CurrenciesAreNotEqualException
	 *            in case the line item has got a base price of different currency than the order currency
	 */
	public void addLineItem(final int index, final LineItem lineitem)
	{
		lineitem.getBasePrice().assertCurreniesAreEqual(getCurrency());
		lineitem.setOrder(this);
		lineItems.add(index, lineitem);
	}

	/**
	 * Removes all attached line items.
	 *
	 * Please note that these line items also lose their back reference to this order.
	 */
	public void clearLineItems()
	{
		for (final LineItem lineitem : lineItems)
		{
			lineitem.setOrder(null);
		}
		lineItems.clear();
	}

	/**
	 * Removes a single line item.
	 *
	 * Please note that the line items also lose its back reference to this order.
	 */
	public void removeLineItem(final LineItem lineitem)
	{
		if (lineItems.remove(lineitem)) // NOSONAR
		{
			lineitem.setOrder(null);
		}
		else
		{
			throw new IllegalArgumentException(
					"Line item " + lineitem + " does not belong to order " + this + " - cannot remove it.");
		}
	}

	/**
	 * Returns all taxes assigned to this order.
	 */
	public Collection<Tax> getTaxes()
	{
		return Collections.unmodifiableList(taxes);
	}


	/**
	 * Assigns multiple taxes to this order.
	 *
	 * Please note that each tax receives a back reference to this order.
	 *
	 * @throws CurrenciesAreNotEqualException
	 *            in case there is at least one absolute tax having a different currency than the one of this order
	 */
	public void addTaxes(final Tax... taxes)
	{
		addTaxes(Arrays.asList(taxes));
	}

	/**
	 * Assigns multiple taxes to this order.
	 *
	 * Please note that each tax receives a back reference to this order.
	 *
	 * @throws CurrenciesAreNotEqualException
	 *            in case there is at least one absolute tax having a different currency than the one of this order
	 */
	public void addTaxes(final Collection<Tax> taxes)
	{
		for (final Tax tax : taxes)
		{
			if (!this.taxes.contains(tax))
			{
				if (tax.getAmount() instanceof Money)
				{
					((Money) tax.getAmount()).assertCurreniesAreEqual(getCurrency());
				}
				this.taxes.add(tax);
			}
		}
	}

	/**
	 * Assigns a tax to this order.
	 *
	 * Please note that the tax receives a back reference to this order.
	 *
	 * @throws CurrenciesAreNotEqualException
	 *            in case it's a absolute tax having a different currency than the one of this order
	 */
	public void addTax(final Tax tax)
	{
		if (!taxes.contains(tax)) // NOSONAR
		{
			if (tax.getAmount() instanceof Money)
			{
				((Money) tax.getAmount()).assertCurreniesAreEqual(getCurrency());
			}
			taxes.add(tax);
		}
	}

	/**
	 * Removes all attached taxes from this order.
	 *
	 * Please note that all back references to this order are cleared.
	 */
	public void clearTaxes()
	{
		taxes.clear();
	}

	/**
	 * Removes the given tax from this order.
	 */
	public void removeTax(final Tax tax)
	{
		if (!taxes.remove(tax)) // NOSONAR
		{
			throw new IllegalArgumentException("Tax " + tax + " doesnt belong to order " + this + " - cannot remove.");  // NOSONAR
		}
	}

	/**
	 * Returns all discounts attached to this order.
	 */
	public List<OrderDiscount> getDiscounts()
	{
		return Collections.unmodifiableList(discounts);
	}

	/**
	 * Assigns multiple discounts to this order.
	 *
	 * Please note that the discounts will receive a back reference to this order.
	 *
	 * @throws CurrenciesAreNotEqualException
	 *            in case there is at least one absolute discount having a different currency than the one of this order
	 */
	public void addDiscounts(final OrderDiscount... discounts)
	{
		addDiscounts(Arrays.asList(discounts));
	}

	/**
	 * Assigns multiple discounts to this order.
	 *
	 * Please note that the discounts will receive a back reference to this order.
	 *
	 * @throws CurrenciesAreNotEqualException
	 *            in case there is at least one absolute discount having a different currency than the one of this order
	 */
	public void addDiscounts(final List<OrderDiscount> discounts)
	{
		for (final OrderDiscount d : discounts)
		{
			if (!this.discounts.contains(d))
			{
				if (d.getAmount() instanceof Money)
				{
					((Money) d.getAmount()).assertCurreniesAreEqual(getCurrency());
				}
				this.discounts.add(d);
			}
		}
	}

	/**
	 * Assigns a discount to this order.
	 *
	 * Please note that the discount will receive a back reference to this order.
	 *
	 * @throws CurrenciesAreNotEqualException
	 *            in case it's a absolute discount having a different currency than the one of this order
	 */
	public void addDiscount(final OrderDiscount discount)
	{
		addDiscount(discounts.size(), discount);
	}

	/**
	 * Assigns a discount to this order at a speific position.
	 *
	 * Please note that the discount will receive a back reference to this order.
	 *
	 * @throws CurrenciesAreNotEqualException
	 *            in case it's a absolute discount having a different currency than the one of this order
	 */
	public void addDiscount(final int index, final OrderDiscount discount)
	{
		if (!discounts.contains(discount)) // NOSONAR
		{
			if (discount.getAmount() instanceof Money)
			{
				((Money) discount.getAmount()).assertCurreniesAreEqual(getCurrency());
			}
			this.discounts.add(index, discount);
		}
	}

	/**
	 * Removes all assigned discounts from this order.
	 *
	 * Please note that all back reference to this order are also cleared.
	 */
	public void clearDiscounts()
	{
		this.discounts.clear();
	}

	/**
	 * Removes a discount from this order.
	 *
	 * Please note that the back reference to this order isalso cleared.
	 */
	public void removeDiscount(final OrderDiscount orderDiscount)
	{
		if (!discounts.remove(orderDiscount)) // NOSONAR
		{
			throw new IllegalArgumentException("Discount " + orderDiscount + " doesnt belong to order " + this + " - cannot remove");  // NOSONAR
		}
	}

	/**
	 * Returns all charges assigned to this order.
	 */
	public List<OrderCharge> getCharges()
	{
		return Collections.unmodifiableList(charges);
	}


	/**
	 * Assigns multiple charges to this order.
	 *
	 * Please note that each one will receive a back reference to this order.
	 *
	 * @throws CurrenciesAreNotEqualException
	 *            in case at least one absolute charge is using a different currency than the one used for this order
	 */
	public void addCharges(final OrderCharge... charges)
	{
		addCharges(Arrays.asList(charges));
	}

	/**
	 * Assigns multiple charges to this order.
	 *
	 * Please note that each one will receive a back reference to this order.
	 *
	 * @throws CurrenciesAreNotEqualException
	 *            in case at least one absolute charge is using a different currency than the one used for this order
	 */
	public void addCharges(final List<OrderCharge> charges)
	{
		for (final OrderCharge aoc : charges)
		{
			if (!this.charges.contains(aoc))
			{
				if (aoc.getAmount() instanceof Money)
				{
					((Money) aoc.getAmount()).assertCurreniesAreEqual(getCurrency());
				}
				this.charges.add(aoc);
			}
		}
	}

	/**
	 * Assigns a single charge to this order.
	 *
	 * Please note that it will receive a back reference to this order.
	 *
	 * @throws CurrenciesAreNotEqualException
	 *            in case it's a absolute charge using a different currency than the one used for this order
	 */
	public void addCharge(final OrderCharge aoc)
	{
		addCharge(charges.size(), aoc);
	}

	/**
	 * Assigns a single charge to this order at a specific position.
	 *
	 * Please note that it will receive a back reference to this order.
	 *
	 * @param index
	 *           the position to add the charge at
	 *
	 * @throws CurrenciesAreNotEqualException
	 *            in case it's a absolute charge using a different currency than the one used for this order
	 */
	public void addCharge(final int index, final OrderCharge aoc)
	{
		if (!charges.contains(aoc)) // NOSONAR
		{
			if (aoc.getAmount() instanceof Money)
			{
				((Money) aoc.getAmount()).assertCurreniesAreEqual(getCurrency());
			}
			charges.add(index, aoc);
		}
	}

	/**
	 * Removes all assigned charges from this order.
	 *
	 * Please note that all back references are cleared as well.
	 */
	public void clearCharges()
	{
		charges.clear();
	}

	/**
	 * Removes a charge from this order.
	 *
	 * Please note that the back reference is cleared as well.
	 */
	public void removeCharge(final OrderCharge aoc)
	{
		if (!charges.remove(aoc)) // NOSONAR
		{
			throw new IllegalArgumentException("Charge " + aoc + " doesnt belong to order " + this + " - cannot remove.");  // NOSONAR
		}
	}

	/**
	 * Tells whether or not this order has got line items.
	 */
	public boolean hasLineItems()
	{
		return !lineItems.isEmpty();
	}

	/**
	 * Tells whether this order has got assigned charges or not.
	 */
	public boolean hasCharges()
	{
		return !charges.isEmpty();
	}

	/**
	 * Tells whether this order has got assigned discounts or not.
	 */
	public boolean hasDisounts()
	{
		return !discounts.isEmpty();
	}

	/**
	 * Tells whether this order has got assigned taxes or not.
	 */
	public boolean hasTaxes()
	{
		return !taxes.isEmpty();
	}

	/**
	 * @return true if this order is in net mode, returns false if the order is in gross mode.
	 */
	public boolean isNet()
	{
		return netMode;
	}

	/**
	 * @return false if this order is in net mode, returns true if the order is in gross mode.
	 */
	public boolean isGross()
	{
		return !netMode;
	}

	/**
	 * Delegate to all necessary strategies.
	 */
	public CalculationStrategies getCalculationStrategies()
	{
		if (calculationStrategies == null)
		{
			throw new MissingCalculationDataException("No calculation strategies were set!");
		}
		return calculationStrategies;
	}

	/**
	 * Returns the currency which this order is being calculated for.
	 */
	public Currency getCurrency()
	{
		return currency;
	}
}
