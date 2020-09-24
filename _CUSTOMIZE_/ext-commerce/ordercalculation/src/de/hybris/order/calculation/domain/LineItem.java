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

import de.hybris.order.calculation.exception.CurrenciesAreNotEqualException;
import de.hybris.order.calculation.exception.MissingCalculationDataException;
import de.hybris.order.calculation.money.AbstractAmount;
import de.hybris.order.calculation.money.Currency;
import de.hybris.order.calculation.money.Money;
import de.hybris.order.calculation.money.Percentage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Represents one entry (or element) for an {@link Order}. Central objects that holds all line items related data
 * required for amount calculation. A {@link LineItem} is {@link Taxable} which means {@link #getTotal(Order)} is used
 * for tax calculation. This line item holds the basePrice and the numberOfUnits. Additionally the giveAwayCount can be
 * set (10 items are bought, 5 of them are free) and this line item holds a ordered {@link List} of
 * {@link LineItemDiscount}s and {@link LineItemCharge}s. The line item must use the same {@link Currency} as the linked
 * {@link Order} or a {@link CurrenciesAreNotEqualException} is thrown.
 */
public class LineItem implements Taxable
{
	private int giveAwayCount;
	private int numberOfUnits;
	private final List<LineItemDiscount> discounts;
	private final List<LineItemCharge> charges;
	private Order order;
	private final Money basePrice;

	private static final String CALCULATE_ITEM_ERROR = "Could not calculate discount for LineItem ";

	/**
	 * Creates a new line item with the given base price and one unit.
	 */
	public LineItem(final Money basePrice)
	{
		if (basePrice == null)
		{
			throw new IllegalArgumentException("The basePrice for the LineItem is null!");
		}
		this.basePrice = basePrice;
		this.giveAwayCount = 0;
		this.numberOfUnits = 1;
		this.discounts = new ArrayList<>();
		this.charges = new ArrayList<>();
	}

	/**
	 * Creates a new line item with the given base price and number of units.
	 */
	public LineItem(final Money basePrice, final int numberOfUnits)
	{
		this(basePrice);
		setNumberOfUnits(numberOfUnits);
	}

	// ----------------------------- calculation area --------------------------------------------------

	/**
	 * Calculates to line item total. This includes sub total, discounts and charges.
	 *
	 * @see #getSubTotal()
	 * @see #getTotalDiscount()
	 * @see #getTotalCharge()
	 */
	@Override
	public Money getTotal(final Order context)
	{
		return getSubTotal().subtract(getTotalDiscount()).add(getTotalCharge());
	}

	/**
	 * Calculates the line item sub total. This is just the bas price multiplied by the number of applicable units (see
	 * {@link #setGiveAwayUnits(int)} for how some units may be excluded).
	 *
	 * @see #getTotal(Order)
	 */
	public Money getSubTotal()
	{
		// REVIEW: -> Strategy ?
		return getOrder().getCalculationStrategies().getRoundingStrategy().multiply(getBasePrice(),
				BigDecimal.valueOf(this.getNumberOfUnitsForCalculation()));
	}

	/**
	 * Calculates the total of all line item discounts.
	 *
	 * @see #getTotalDiscounts()
	 */
	public Money getTotalDiscount()
	{
		final Map<LineItemDiscount, Money> discountValues = getTotalDiscounts();
		return discountValues.isEmpty() ? Money.zero(getOrder().getCurrency()) : Money.sum(discountValues.values());
	}

	/**
	 * Calculates the total of all line item charges.
	 *
	 * @see #getTotalCharges()
	 */
	public Money getTotalCharge()
	{
		final Map<LineItemCharge, Money> chargeValues = getTotalCharges();
		return chargeValues.isEmpty() ? Money.zero(getOrder().getCurrency()) : Money.sum(chargeValues.values());
	}


	/**
	 * Calculates the totals of all line item discounts separately.
	 *
	 * @return a map ( {@link LineItemDiscount} -&gt; {@link Money} )
	 *
	 * @see #getTotalDiscount()
	 */
	public Map<LineItemDiscount, Money> getTotalDiscounts()
	{
		if (discounts.isEmpty())
		{
			return Collections.emptyMap();
		}

		//REVIEW: Surely this would belong into a strategy later on if we stick to the strategies idea.
		final Map<LineItemDiscount, Money> resultmap = new LinkedHashMap<>(discounts.size());
		Money currentValue = getSubTotal();
		Money calculatedDiscount;

		for (final LineItemDiscount lid : discounts)
		{
			calculatedDiscount = calculateDiscount(currentValue, lid);
			currentValue = currentValue.subtract(calculatedDiscount);
			resultmap.put(lid, calculatedDiscount);
		}
		return resultmap;
	}


	/**
	 * Calculates totals of all line item charges separately.
	 *
	 * @return a map ( {@link LineItemCharge} -&gt; {@link Money} )
	 *
	 * @see #getTotalCharge()
	 */
	public Map<LineItemCharge, Money> getTotalCharges()
	{
		if (charges.isEmpty())
		{
			return Collections.emptyMap();
		}

		//REVIEW: Surely this would belong into a strategy later on if we stick to the strategies idea.
		final Map<LineItemCharge, Money> resultmap = new LinkedHashMap<>(charges.size());
		Money currentValue = getSubTotal();
		Money calculatedAddCharge;

		for (final LineItemCharge lic : charges)
		{
			calculatedAddCharge = calculateCharge(currentValue, lic);
			currentValue = currentValue.add(calculatedAddCharge);
			resultmap.put(lic, calculatedAddCharge);
		}
		return resultmap;
	}



	// ----------------------------- non public calculation area --------------------------------------------------

	/*
	 * calculates the discount amount based on the given Money value before and the given discount. Returns the result in
	 * Money.
	 */
	protected Money calculateDiscount(final Money currentValue, final LineItemDiscount discount)
	{
		final Money zeroMoney = new Money(getOrder().getCurrency());
		if (zeroMoney.equals(currentValue))
		{
			return zeroMoney;
		}

		if (discount.isPerUnit())
		{
			//discount is per unit, so: how many units have this discount?
			final int numberDiscountUnit = Math.min(discount.getApplicableUnits(), getNumberOfUnits());

			if (discount.getAmount() instanceof Money)
			{
				//return the amount multiply by the affected number of units
				return getOrder().getCalculationStrategies().getRoundingStrategy().multiply((Money) discount.getAmount(),
						BigDecimal.valueOf(numberDiscountUnit));
			}
			if (discount.getAmount() instanceof Percentage)
			{
				//same as above but for percent
				final Percentage percent = (Percentage) discount.getAmount();
				final BigDecimal basePriceAmount = getBasePrice().getAmount();

				return getOrder().getCalculationStrategies().getRoundingStrategy().roundToMoney(
						basePriceAmount.multiply(percent.getRate()).movePointLeft(2).multiply(BigDecimal.valueOf(numberDiscountUnit)),
						getOrder().getCurrency());
			}
			throw new MissingCalculationDataException(CALCULATE_ITEM_ERROR + this);
		}

		//the discount amount is for the whole line item
		if (discount.getAmount() instanceof Money)
		{
			//discount is in money, the amount is just returned, no calculation needed
			return (Money) discount.getAmount();
		}
		if (discount.getAmount() instanceof Percentage)
		{
			//the discount has a percentage value, the absolute amount is calculated hete
			return getOrder().getCalculationStrategies().getRoundingStrategy().getPercentValue(currentValue,
					(Percentage) discount.getAmount());
		}

		throw new MissingCalculationDataException(CALCULATE_ITEM_ERROR + this);
	}

	/*
	 * calculates the charge amount for the given money value before and the given LineItemCharge. The returned value is
	 * in Money.
	 */
	protected Money calculateCharge(final Money currentValue, final LineItemCharge charge)
	{
		final Money zeroMoney = Money.zero(getBasePrice().getCurrency());
		if (charge.isDisabled())
		{
			//the charge is ignored, therefor zero money is returned
			return zeroMoney;
		}

		if (charge.isPerUnit())
		{
			//the charge only applies for some units of the lineitem
			final int applyForThisItemCount = Math.min(charge.getApplicableUnits(), getNumberOfUnits());

			if (charge.getAmount() instanceof Money)
			{
				return getOrder().getCalculationStrategies().getRoundingStrategy().multiply((Money) charge.getAmount(),
						BigDecimal.valueOf(applyForThisItemCount));
			}
			if (charge.getAmount() instanceof Percentage)
			{
				//unitcount * baseprice * %
				final Percentage percent = (Percentage) charge.getAmount();
				return getOrder().getCalculationStrategies().getRoundingStrategy().roundToMoney(getBasePrice().getAmount()
						.multiply(percent.getRate()).movePointLeft(2).multiply(BigDecimal.valueOf(applyForThisItemCount)),
						getOrder().getCurrency());
			}
			throw new MissingCalculationDataException(CALCULATE_ITEM_ERROR + this);
		}

		//the charge apply on the whole lineitem
		if (charge.getAmount() instanceof Money)
		{
			//absolute amount for the whole line item -> just return it
			return (Money) charge.getAmount();
		}
		if (charge.getAmount() instanceof Percentage)
		{
			//the subtotal * percentrate is the charge here
			return getOrder().getCalculationStrategies().getRoundingStrategy().getPercentValue(currentValue,
					(Percentage) charge.getAmount());
		}

		throw new MissingCalculationDataException(CALCULATE_ITEM_ERROR + this);
	}

	// ----------------------------- getters / setters -----------------------------------------------------

	/**
	 * Returns the regular number of units within that line item.
	 *
	 * Please note that some units may be excluded from calculation via {@link #setGiveAwayUnits(int)}.
	 *
	 * @see #setNumberOfUnits(int)
	 * @see #setGiveAwayUnits(int)
	 * @see #getGiveAwayUnits()
	 */
	public int getNumberOfUnits()
	{
		return numberOfUnits;
	}

	/**
	 * Changes the number of regular units within that line item.
	 *
	 * Please note that some units may be excluded from calculation via {@link #setGiveAwayUnits(int)}.
	 *
	 * @see #getNumberOfUnits()
	 * @see #setGiveAwayUnits(int)
	 * @see #getGiveAwayUnits()
	 */
	public final void setNumberOfUnits(final int numberOfUnits)
	{
		//method is final because method is also called in the constructor
		if (numberOfUnits < 0)
		{
			throw new IllegalArgumentException("The numberOfUnits cannot be negative!");
		}
		this.numberOfUnits = numberOfUnits;
	}

	/**
	 * Sets the {@link Order} to for this line item belongs to. Is needed for the calculation because all line item must
	 * have the same currency ({@link Order#getCurrency()} is used to check it).
	 */
	public void setOrder(final Order order)
	{
		this.order = order;
	}

	/**
	 * @return the order this line item belongs to. Can be null but no calculation is then possible!
	 * @throws MissingCalculationDataException
	 *            if order is null and this method or any calculation method (getTotal...) is called.
	 */
	public Order getOrder()
	{
		if (order == null)
		{
			throw new MissingCalculationDataException("Order for LineItem [" + this + "] was not set.");
		}
		return order;
	}

	/**
	 * Returns the base price of this line item.
	 */
	public Money getBasePrice()
	{
		return basePrice;
	}

	/**
	 * <code>{numberOfUnits}x {basePrice} {currency}(free:{giveAwayCount}) discounts:[...] charges:[...]</code>
	 */
	@Override
	public String toString()
	{
		return getNumberOfUnits() + "x " + getBasePrice()//
				+ (getGiveAwayUnits() > 0 ? "(free:" + getGiveAwayUnits() + ")" : "") //
				+ (discounts.isEmpty() ? "" : " discounts:" + discounts) //
				+ (charges.isEmpty() ? "" : " charges:" + charges);
	}

	// ----- discount stuff

	/**
	 * Returns all discounts assigned to this line item.
	 */
	public List<LineItemDiscount> getDiscounts()
	{
		return Collections.unmodifiableList(discounts);
	}

	/**
	 * Adds multiple discounts to this line item.
	 *
	 * @throws CurrenciesAreNotEqualException
	 *            in case at least one discount is absolute and uses a different currency than the enclosing order
	 */
	public void addDiscounts(final LineItemDiscount... discounts)
	{
		addDiscounts(Arrays.asList(discounts));
	}

	/**
	 * Adds multiple discounts to this line item.
	 *
	 * @throws CurrenciesAreNotEqualException
	 *            in case at least one discount is absolute and uses a different currency than the enclosing order
	 */
	public void addDiscounts(final List<LineItemDiscount> discounts)
	{
		for (final LineItemDiscount prodD : discounts)
		{
			if (!this.discounts.contains(prodD))
			{
				assertCurrency(prodD.getAmount());
				this.discounts.add(prodD);
			}
		}
	}

	/**
	 * Adds a single discount to this line item at a specific position.
	 *
	 * @param index
	 *           the position to add the discount at
	 *
	 * @throws CurrenciesAreNotEqualException
	 *            in case at it's a absolute discount and uses a different currency than the enclosing order
	 */
	public void addDiscount(final int index, final LineItemDiscount discount)
	{
		if (!this.discounts.contains(discount))
		{
			assertCurrency(discount.getAmount());
			this.discounts.add(index, discount);
		}
	}

	protected void assertCurrency(final AbstractAmount amount)
	{
		if (amount instanceof Money)
		{
			((Money) amount).assertCurreniesAreEqual(getBasePrice().getCurrency());
		}
	}

	/**
	 * Adds a single discount to this line item.
	 *
	 * @throws CurrenciesAreNotEqualException
	 *            in case at it's a absolute discount and uses a different currency than the enclosing order
	 */
	public void addDiscount(final LineItemDiscount discount)
	{
		addDiscount(discounts.size(), discount);
	}

	/**
	 * Removes all attached discounts of the current line item.
	 */
	public void clearDiscounts()
	{
		this.discounts.clear();
	}

	/**
	 * Removes a single discount from this line item.
	 */
	public void removeDiscount(final LineItemDiscount discount)
	{
		if (!this.discounts.remove(discount))
		{
			throw new IllegalArgumentException("Discount " + discount + " doesnt belong to line item " + this + " - cannot remove.");
		}
	}

	// ------------ charge stuff

	/**
	 * Returns all charges assigned to this line item.
	 */
	public List<LineItemCharge> getCharges()
	{
		return Collections.unmodifiableList(charges);
	}

	/**
	 * Adds multiple charges to this line item.
	 *
	 * @throws CurrenciesAreNotEqualException
	 *            in case there is at least one absolute charge which uses a different currency than the enclosing order
	 */
	public void addCharges(final LineItemCharge... lineItemCharges)
	{
		addCharges(Arrays.asList(lineItemCharges));
	}

	/**
	 * Adds multiple charges to this line item.
	 *
	 * @throws CurrenciesAreNotEqualException
	 *            in case there is at least one absolute charge which uses a different currency than the enclosing order
	 */
	public void addCharges(final List<LineItemCharge> lineItemCharges)
	{
		for (final LineItemCharge apc : lineItemCharges)
		{
			if (!this.charges.contains(apc))
			{
				assertCurrency(apc.getAmount());
				charges.add(apc);
			}
		}
	}

	/**
	 * Adds a single charge to this line item.
	 *
	 * @throws CurrenciesAreNotEqualException
	 *            in case at it's a absolute charge and uses a different currency than the enclosing order
	 */
	public void addCharge(final LineItemCharge lineItemCharge)
	{
		addCharge(this.charges.size(), lineItemCharge);
	}

	/**
	 * Adds a single charge to this line item at a specific position.
	 *
	 * @param index
	 *           the position to add the charge at
	 *
	 * @throws CurrenciesAreNotEqualException
	 *            in case at it's a absolute charge and uses a different currency than the enclosing order
	 */
	public void addCharge(final int index, final LineItemCharge lineItemCharge)
	{
		if (!this.charges.contains(lineItemCharge))
		{
			assertCurrency(lineItemCharge.getAmount());
			this.charges.add(index, lineItemCharge);
		}
	}

	/**
	 * Removes all attached charges of the current line item.
	 */
	public void clearCharges()
	{
		this.charges.clear();
	}

	/**
	 * Removes a charge from this line item.
	 */
	public void removeCharge(final LineItemCharge charge)
	{
		if (!this.charges.remove(charge))
		{
			throw new IllegalArgumentException("Charge " + charge + " doesnt belong to line item " + this + " - cannot remove.");
		}
	}

	/**
	 * Returns the number of units that are applicable for calculation.
	 *
	 * Basically this is {@link #getNumberOfUnits()} - {@link #getGiveAwayUnits()}.
	 */
	public int getNumberOfUnitsForCalculation()
	{
		return Math.max(0, getNumberOfUnits() - getGiveAwayUnits());
	}

	/**
	 * Tells the number of units to be 'for free' meaning that they're excluded from calculation completely. In
	 * consequence for calculation this line item appears to hold &lt;numberOfUnits&gt; - &lt;giveAwayCount&gt; units.
	 *
	 * In case the number of give-away units is greater than the actual available number of units the line item appear to
	 * have zero units.
	 *
	 * @see #setNumberOfUnits(int)
	 * @see #getNumberOfUnits()
	 * @see #setGiveAwayUnits(int)
	 */
	public int getGiveAwayUnits()
	{
		return giveAwayCount;
	}

	/**
	 * Changes the number of units to be 'for free' meaning that they're excluded from calculation completely. In
	 * consequence for calculation this line item appears to hold &lt;numberOfUnits&gt; - &lt;giveAwayCount&gt; units.
	 *
	 * In case the number of give-away units is greater than the actual available number of units the line item appear to
	 * have zero units.
	 *
	 * @see #setNumberOfUnits(int)
	 * @see #getNumberOfUnits()
	 * @see #getGiveAwayUnits()
	 */
	public final void setGiveAwayUnits(final int giveAwayCount)
	{
		if (giveAwayCount < 0)
		{
			throw new IllegalArgumentException("The give away count cannot be negative");
		}
		this.giveAwayCount = giveAwayCount;
	}

	/**
	 * Returns all taxes which are targeting this line item.
	 *
	 * @see Order#getTaxesFor(Taxable)
	 */
	public Collection<Tax> getTaxes()
	{
		return getOrder().getTaxesFor(this);
	}
}
