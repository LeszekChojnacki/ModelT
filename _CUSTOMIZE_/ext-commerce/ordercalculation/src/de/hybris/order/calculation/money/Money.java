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
package de.hybris.order.calculation.money;


import static java.util.Arrays.asList;

import de.hybris.order.calculation.exception.AmountException;
import de.hybris.order.calculation.exception.CalculationException;
import de.hybris.order.calculation.exception.CurrenciesAreNotEqualException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.collections.CollectionUtils;


/**
 * Represents a monetary amount by combining a {@link BigDecimal} and a {@link Currency}. Please know that the scale of
 * the amount is always equal to the amount of currency digits.Therefore the following rules apply:
 * <table border=1>
 * <tr>
 * <th colspan=2>used values</th>
 * <th>result</th>
 * </tr>
 * <tr>
 * <th>BigDecimal</th>
 * <th>Currency.getDigits()</th>
 * <th>Money.getAmount() as BigDecimal</th>
 * </tr>
 * <tr>
 * <td>new BigDecimal(1.000)</td>
 * <td>0</td>
 * <td>1</td>
 * </tr>
 * <tr>
 * <td>new BigDecimal(1.000)</td>
 * <td>3</td>
 * <td>1.000</td>
 * </tr>
 * <tr>
 * <td>BigDecimal.ZERO</td>
 * <td>2</td>
 * <td>0.00</td>
 * </tr>
 * <tr>
 * <td>BigDecimal.valueOf(-1.34)</td>
 * <td>3</td>
 * <td>-1.340</td>
 * </tr>
 * <tr>
 * <td>BigDecimal.valueOf(1.234)</td>
 * <td>1</td>
 * <td>1.2 since we simply cut off the extra digits</td>
 * </tr>
 * </table>
 * <p/>
 * <p/>
 * This class provides common operations like {@link #add(Money)}, {@link #subtract(Money)} and {@link #split(List)}.
 * <p/>
 * A Money in the smallest amount (example 0.01 EUR) <b>can not</b> be split in smaller amounts. Splitting this amount
 * in two will return {0.01 EUR, 0.00 EUR}.
 * <p/>
 * Last but not least there are a number of tool methods to ease working with this class: {@link #zero(Currency)},
 * {@link #valueOf(Currency, String...)}, {@link #sum(Collection)}, {@link #sortAscending(List)} and
 * {@link #getPercentages(Money...)} to name a few.
 */
public class Money extends AbstractAmount
{
	private final Currency currency;
	private final BigDecimal amount;
	private static final ConcurrentMap<String, Money> commonMoneyCacheMap = new ConcurrentHashMap<String, Money>();

	private static final Comparator<Map.Entry<?, Money>> MAP_ASC_COMP = new Comparator<Map.Entry<?, Money>>()
	{
		@Override
		public int compare(final Entry<?, Money> entry1, final Entry<?, Money> entry2)
		{
			return ASC_COMP.compare(entry1.getValue(), entry2.getValue());
		}
	};
	
	/**
	 * Creates a Money object. A new BigDecimal object is used internally, based on {@link Currency#getDigits()}.
	 * <p/>
	 * <code>amount</code> and <code>this.getAmount()</code> are maybe <b>not</b> equal (because of the different scale
	 * of the BigDecimal) <b>BUT</b> <code>new Money(BigDecimal.ONE, curr)</code> and
	 * <code>new Money(new BigDecimal("1.000"), curr)</code> are equal with each other!
	 *
	 * @param amount
	 * 			 the value or amount of the money
	 * @param currency
	 * 			 the amount of the money in the hybris {@link Currency} pojo
	 * @throws ArithmeticException
	 * 			 if the amount must be rounded because the {@link Currency#getDigits()} does not provide enough digits.
	 */
	public Money(final BigDecimal amount, final Currency currency)
	{
		if (currency == null)
		{
			throw new IllegalArgumentException("Currency cannot be null");                    // NOSONAR
		}
		this.amount = amount.setScale(currency.getDigits(), BigDecimal.ROUND_DOWN);
		this.currency = currency;
	}

	/**
	 * Creates zero money of the given Currency.
	 *
	 * @param currency
	 */
	public Money(final Currency currency)
	{
		if (currency == null)
		{
			throw new IllegalArgumentException("Currency cannot be null");
		}
		this.amount = BigDecimal.ZERO;
		this.currency = currency;
	}

	/**
	 * Creates a new money from a given amount of smallest pieces and a currency.
	 */
	public Money(final long amountInSmallestPieces, final Currency currency)
	{
		if (currency == null)
		{
			throw new IllegalArgumentException("Currency cannot be null");
		}
		this.amount = new BigDecimal(BigInteger.valueOf(amountInSmallestPieces), currency.getDigits());
		this.currency = currency;
	}

	/**
	 * Creates a new money from String and currency.
	 */
	public Money(final String amount, final Currency currency)
	{
		this(new BigDecimal(amount), currency);
	}


	/**
	 * Creates a list of money objects for a specific currency and any number of String values.
	 */
	public static List<Money> valueOf(final Currency curr, final String... amounts)
	{
		final Money[] ret = new Money[amounts.length];
		int index = 0;
		for (final String s : amounts)
		{
			ret[index++] = new Money(s, curr);
		}
		return asList(ret);
	}

	/**
	 * Creates a list of money objects for a specific currency and any number of long values.
	 */
	public static List<Money> valueOf(final Currency curr, final long... amounts)
	{
		final Money[] ret = new Money[amounts.length];
		int index = 0;
		for (final long l : amounts)
		{
			ret[index++] = new Money(l, curr);
		}
		return asList(ret);
	}

	/**
	 * Creates a list of money objects for a specific currency and any number of BigDecimal values.
	 */
	public static List<Money> valueOf(final Currency curr, final BigDecimal... amounts)
	{
		final Money[] ret = new Money[amounts.length];
		int index = 0;
		for (final BigDecimal a : amounts)
		{
			ret[index++] = new Money(a, curr);
		}
		return asList(ret);
	}

	/**
	 * Shortcut to obtain commonly used zero money instances.
	 * Since they are cached this methods should be preferred to {@link #Money(Currency)}.
	 */
	public static Money zero(final Currency curr)
	{
		final String cacheKey = "Z" + curr.getIsoCode().toLowerCase() + curr.getDigits();
		Money ret = commonMoneyCacheMap.get(cacheKey);
		if (ret == null)
		{
			ret = new Money(curr);
			final Money previous = commonMoneyCacheMap.putIfAbsent(cacheKey, ret);
			if (previous != null)
			{
				ret = previous;
			}
		}
		return ret;
	}

	// ---------------------------  getters / setters ---------------------------

	/**
	 * Returns the currency of this money.
	 */
	public Currency getCurrency()
	{
		return currency;
	}

	/**
	 * The monetary amount. The amount scale is equal to the currency digits!
	 */
	public BigDecimal getAmount()
	{
		return amount;
	}

	// --------------------------- operations ---------------------------

	/**
	 * Checks if the current Currency is equal to given one.
	 *
	 * @throws CurrenciesAreNotEqualException
	 * 			 if the currencies are not equal.
	 */
	public void assertCurreniesAreEqual(final Currency curr)
	{
		if (curr == null)
		{
			throw new IllegalArgumentException("Currency cannot be null");
		}
		if (!getCurrency().equals(curr))
		{
			throw new CurrenciesAreNotEqualException("The Currencies are not the same. " + getCurrency() + " <-> " + curr);
		}
	}

	/**
	 * Checks if the current Currency is equal to given one.
	 *
	 * @throws CurrenciesAreNotEqualException
	 * 			 if the currencies are not equal.
	 */
	public void assertCurreniesAreEqual(final Money other)
	{
		if (other == null)
		{
			throw new IllegalArgumentException("Money cannot be null");
		}
		if (!getCurrency().equals(other.getCurrency()))
		{
			throw new CurrenciesAreNotEqualException(
						 "The Currencies are not the same. " + getCurrency() + " <-> " + other.getCurrency());
		}
	}

	/**
	 * Add to the current {@link Money} amount the given {@link Money} amount. The result is a new Money object with the
	 * sum of both amounts and in the same currency.
	 *
	 * @param money
	 * 			 this will be added to the current money amount
	 * @return a new object with the sum
	 * @throws CurrenciesAreNotEqualException
	 * 			 if {@link Money#getCurrency()} is different in the given objects
	 */
	public Money add(final Money money)
	{
		assertCurreniesAreEqual(money);
		return new Money(this.amount.add(money.getAmount()), this.currency);
	}

	// REVIEW: May be we provide add( BigDecimal | long | String ) : Money as well ? Since this Money is defining a currency
	// we would make simple calculation easier...

	/**
	 * Subtracts from current {@link Money} amount the given {@link Money} amount. The result is a new Money object with
	 * the difference of both amounts and in the same currency.
	 *
	 * @param money
	 * 			 this will be subtracted to the current money amount
	 * @return a new object with the difference
	 * @throws CurrenciesAreNotEqualException
	 * 			 if {@link Money#getCurrency()} is different in the given objects
	 */
	public Money subtract(final Money money)
	{
		assertCurreniesAreEqual(money);
		return new Money(getAmount().subtract(money.getAmount()), currency);
	}

	// REVIEW: May be we provide subtract( BigDecimal | long | String ) : Money as well ? Since this Money is defining a currency
	// we would make simple calculation easier...


	/**
	 * @return <b>currency.hashCode + amount.hashCode</b>
	 */
	@Override
	public int hashCode()
	{
		return currency.hashCode() + amount.hashCode() + super.hashCode();
	}

	/**
	 * Returns true if the given parameter is also an instanceof money <b>and</b> the current currency is equal to the
	 * currency of the given money <b>and</b> the current amount is comparedTo == 0 to the given money amount. (this is
	 * not BigDecimal.equals(BigDecimal), the check is BigDecimal.compareTo(BigDecimal))
	 */
	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}

		if (obj == null)
		{
			return false;
		}

		if (!(obj.getClass().equals(Money.class)))
		{
			return false;
		}

		return ((Money) obj).getCurrency().equals(currency) && ((Money) obj).getAmount().compareTo(amount) == 0;
	}

	/**
	 * Returns the amount of the value formatted with the {@link Currency#getDigits()} and the currency isocode. Example:
	 * "1.20 EUR"
	 *
	 * @return {0.xx} {currency.toString}; <br/>
	 * xx.lenght == currency.digits
	 */
	@Override
	public String toString()
	{
		return amount.toPlainString() + " " + currency.toString();
	}

	/**
	 * Same as {@link #split(List)} but because of the variable parameter list the sum must not be exactly 100%. If less
	 * the missing {@link Percentage} value will be added. This means, the size of the resulting list is equal to the
	 * given percentage list if the sum of them is exactly 100% <b>or</b> the size of the resulting list is one more if
	 * the given percentage list is less than 100%. Anyway a {@link CalculationException} is thrown if the sum is greater
	 * than 100%.
	 *
	 * @param percentages
	 * 			 the variable list of percentages
	 * @return a {@link List} of {@link Money} objects which are summed up the same amount of this current Money object.
	 */
	public List<Money> split(final Percentage... percentages)
	{
		//fist part: go through the percent params, create a list<percent> and if less 100 add the remaining val
		BigDecimal hundertPercent = BigDecimal.valueOf(100);
		final List<Percentage> perc = new ArrayList<Percentage>();
		if (percentages == null || percentages.length == 0)
		{
			perc.add(new Percentage(hundertPercent));
		}
		else
		{
			for (final Percentage val : percentages)
			{
				perc.add(val);
				hundertPercent = hundertPercent.subtract(val.getRate());
			}

			if (hundertPercent.compareTo(BigDecimal.ZERO) > 0)
			{
				//only add the remaining percent if it is positive value
				//if nothin left, nothing to add
				//and if negative the other method throw the exception
				perc.add(new Percentage(hundertPercent));
			}
		}
		//second part: call the other split method
		return split(perc);
	}

	/**
	 * Splits the current Money into a list of new Money objects, based on the given list of {@link Percentage} objects.
	 * The sum of the Percentages must be equal to 100% or a {@link CalculationException} is thrown. The size of the
	 * returned money list is equal to the size of the given percentage list. The sum of the returned money list is equal
	 * to the given amount of money. A money can only be split into the smallest possible amount of this money which
	 * depends on the currency. If a small amount of money can not be split anymore this amount is added to the last
	 * entry in the returned list.
	 * <p/>
	 * Example: splitting 0.01 EURO into half results in [0.00EUR , 0.01EUR]
	 *
	 * @param percentages
	 * 			 the list of {@link Percentage} in which the current money should be split into.
	 * @return a {@link List} of {@link Money} objects which are summed up the same amount of this current Money object.
	 * @throws AmountException
	 * 			 if the sum of the percentages is not 100%
	 * @throws IllegalArgumentException
	 * 			 if the percent list is null or empty
	 */
	public List<Money> split(final List<Percentage> percentages)
	{
		checkPercentages(percentages);

		final long total = amount.unscaledValue().longValue();
		long remainder = total;
		final long[] results = new long[percentages.size()];

		for (int index = 0; index < percentages.size(); index++)
		{
			results[index] = (long) (total * percentages.get(index).getRate().doubleValue() / 100);
			remainder = remainder - results[index];
		}
		for (int index = 0; index < remainder; index++)
		{
			results[index]++;
		}

		final List<Money> result = new ArrayList<Money>();
		for (int index = 0; index < percentages.size(); index++)
		{
			result.add(new Money(results[index], currency));
		}
		return result;
	}

	/*
	 * checks the given list of percentages if the sum is exactly 100%. Throws Exception otherwise.
	 */
	protected BigDecimal checkPercentages(final List<Percentage> percentages)
	{
		if (percentages == null || percentages.isEmpty())
		{
			throw new IllegalArgumentException("Parameter 'percentages' is null or empty!");
		}
		BigDecimal sum = BigDecimal.ZERO;

		for (final Percentage percentage : percentages)
		{
			sum = sum.add(percentage.getRate());
		}

		final int compareRes = sum.compareTo(BigDecimal.valueOf(100));
		if (compareRes != 0)
		{
			throw new AmountException("Sum of all given Percentages is " + (compareRes > 0 ? "greater" : "less") + " than 100%");
		}
		return sum;
	}

	public static <T extends Object> List<T> sortByMoneyAscending(final Map<T, Money> moneyMap)
	{
		return sortByMoney(moneyMap, MAP_ASC_COMP);
	}

	protected static <T extends Object> List<T> sortByMoney(final Map<T, Money> moneyMap,
				 final Comparator<Map.Entry<?, Money>> comp)
	{
		final List<Map.Entry<T, Money>> entryList = new ArrayList<Map.Entry<T, Money>>(moneyMap.entrySet());
		Collections.sort(entryList, comp);

		final T[] ret = (T[]) new Object[entryList.size()];
		int index = 0;
		for (final Map.Entry<T, Money> e : entryList)
		{
			ret[index++] = e.getKey();
		}
		return asList(ret);
	}
	

	public static <T extends Object> List<T> sortByMoneyDescending(final Map<T, Money> moneyMap)
	{
		return sortByMoney(moneyMap, MAP_DESC_COMP);
	}

	private static final Comparator<Map.Entry<?, Money>> MAP_DESC_COMP = new Comparator<Map.Entry<?, Money>>()     // NOSONAR
	{
		@Override
		public int compare(final Entry<?, Money> entry1, final Entry<?, Money> entry2)
		{
			return DESC_COMP.compare(entry1.getValue(), entry2.getValue());
		}
	};

	public static void sortAscending(final List<Money> elements)
	{
		Collections.sort(elements, ASC_COMP);
	}

	private static final Comparator<Money> ASC_COMP = new Comparator<Money>()                               // NOSONAR
	{
		@Override
		public int compare(final Money money1, final Money money2)
		{
			if (money1 == null || money2 == null)
			{
				throw new IllegalArgumentException("cannot sort NULL money elements");
			}
			else if (!money1.getCurrency().equals(money2.getCurrency()))
			{
				throw new CurrenciesAreNotEqualException("cannot sort mixed-currency money elements");
			}
			else
			{
				return money1.getAmount().compareTo(money2.getAmount());
			}
		}
	};

	public static void sortDescending(final List<Money> elements)
	{
		Collections.sort(elements, DESC_COMP);
	}

	private static final Comparator<Money> DESC_COMP = new Comparator<Money>()                                // NOSONAR
	{
		@Override
		public int compare(final Money money1, final Money money2)
		{
			if (money1 == null || money2 == null)
			{
				throw new IllegalArgumentException("cannot sort NULL money elements");
			}
			else if (!money1.getCurrency().equals(money2.getCurrency()))
			{
				throw new CurrenciesAreNotEqualException("cannot sort mixed-currency money elements");
			}
			else
			{
				return money2.getAmount().compareTo(money1.getAmount());
			}
		}
	};

	public interface MoneyExtractor<T extends Object>
	{
		Money extractMoney(T object);
	}

	public static final <T extends Object> Money sum(final Collection<T> elements, final MoneyExtractor<T> extractor)
	{
		if (CollectionUtils.isEmpty(elements))
		{
			throw new AmountException("Cannot sum nothing");                                          // NOSONAR
		}
		return sumUnscaled(elements, extractor);
	}

	public static final Money sum(final Money... money)
	{
		if (money == null || money.length == 0)
		{
			throw new AmountException("Cannot sum nothing");
		}
		return sum(asList(money));
	}

	public static final Money sum(final Collection<Money> elements)
	{
		return new Money(sumUnscaled(elements), elements.iterator().next().getCurrency());
	}

	protected static final <T extends Object> Money sumUnscaled(final Collection<T> elements, final MoneyExtractor<T> extractor)
	{
		if (elements == null || elements.isEmpty())
		{
			throw new AmountException("Cannot sum nothing");
		}
		long res = 0;
		Currency curr = null;
		for (final T t : elements)
		{
			final Money money = extractor.extractMoney(t);
			if (curr == null)
			{
				curr = money.getCurrency();
			}
			else if (!curr.equals(money.getCurrency()))
			{
				throw new CurrenciesAreNotEqualException("Cannot sum up Money with different currencies");
			}
			res += money.getAmount().unscaledValue().longValue();
		}
		return new Money(res, curr);
	}


	protected static final long sumUnscaled(final Collection<Money> elements)
	{
		if (elements == null || elements.isEmpty())
		{
			throw new AmountException("Cannot sum nothing");
		}
		long res = 0;
		Currency curr = null;
		for (final Money x : elements)
		{
			if (curr == null)
			{
				curr = x.getCurrency();
			}
			else if (!curr.equals(x.getCurrency()))
			{
				throw new CurrenciesAreNotEqualException("Cannot sum up Money with different currencies");
			}
			res += x.getAmount().unscaledValue().longValue();
		}
		return res;
	}

	/**
	 * Calculates a list of {@link Percentage} objects reflecting the distribution of money within the given money list.
	 * Please note that the scale of these percentages is zero.
	 *
	 * @see #getPercentages(int, Money...)
	 */
	public static List<Percentage> getPercentages(final Money... moneys)
	{
		return getPercentages(0, moneys);
	}

	/**
	 * Calculates a list of {@link Percentage} objects reflecting the distribution of money within the given money list.
	 * The specified scale is defining the precision of these percentages.
	 *
	 * @see #getPercentages(Money...)
	 */
	public static List<Percentage> getPercentages(final int scale, final Money... moneys)
	{
		return getPercentages(asList(moneys), scale);
	}

	/**
	 * Calculates a list of {@link Percentage} objects reflecting the distribution of money within the given money list.
	 * The specified scale is defining the precision of these percentages.
	 *
	 * @see #getPercentages(Money...)
	 */
	public static List<Percentage> getPercentages(final List<Money> moneys, final int scale)
	{
		// factor to multiple single money with
		// f = 100 * 10 ^ scale
		// example:
		// scale=2 -> f = 100 * 10 ^ 2 = 100 * 100 = 10.000
		final long percentageFactor = BigDecimal.TEN.pow(2 + scale).longValue();

		// result percentages (unscaled)
		final long[] ratios = new long[moneys.size()];

		// get sum of money (unscaled)
		final long sumUnscaled = sumUnscaled(moneys);
		// remainder to distribute after long division
		long remainderPercentage = percentageFactor;

		// get ration for each money by integer division and countown of remainder
		int index = 0;
		for (final Money m : moneys)
		{
			final long moneyUnscaled = m.getAmount().unscaledValue().longValue();
			final long moneyPercentageUnscaled = (moneyUnscaled * percentageFactor) / sumUnscaled;
			ratios[index++] = moneyPercentageUnscaled;
			remainderPercentage -= moneyPercentageUnscaled;
		}
		if (remainderPercentage > 0)
		{
			final int[] biggestMoneyOrderedPositionList = getSortedPositionTable(moneys);
			// distribute remainder evenly starting with biggest money to avoid
			// some small money getting to big
			for (int i = 0; remainderPercentage > 0 && i < biggestMoneyOrderedPositionList.length; i++, remainderPercentage--)
			{
				ratios[biggestMoneyOrderedPositionList[i]] += 1;
			}
		}
		final Percentage[] ret = new Percentage[ratios.length];
		int idx = 0;
		for (final long ratio : ratios)
		{
			ret[idx++] = new Percentage(new BigDecimal(BigInteger.valueOf(ratio), scale));
		}
		return asList(ret);
	}

	/*
	 * For [ money0,...,moneyN ] this method returns a [ biggestMoneyOriginalPos, ..., smallestMoneyOriginalPos ]
	 *
	 * Example: [ 1.99, 10, 0.55 ] -> [ 1, 0, 2 ] ; [ 10, 9, 8, ... , 0 ] -> [ 0, 1, ..., 9 ] ; [ 0, 1, 2,..., 10 ] -> [
	 * 9, 8, ..., 0 ]
	 */
	protected static int[] getSortedPositionTable(final List<Money> moneyList)
	{
		final Map<Integer, Money> posMap = new HashMap<Integer, Money>(moneyList.size());
		int index = 0;
		for (final Money m : moneyList)
		{
			posMap.put(Integer.valueOf(index++), m);
		}
		final int[] ret = new int[moneyList.size()];
		index = 0;
		for (final Integer originalPos : Money.sortByMoneyDescending(posMap))
		{
			ret[index++] = originalPos.intValue();
		}
		return ret;
	}
}
