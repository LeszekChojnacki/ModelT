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
package de.hybris.platform.externaltax.impl;

import de.hybris.platform.core.CoreAlgorithms;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.externaltax.ApplyExternalTaxesStrategy;
import de.hybris.platform.externaltax.ExternalTaxDocument;
import de.hybris.platform.util.TaxValue;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Applies a given {@link ExternalTaxDocument} to a <b>net</b> order following these rules:
 * <ul>
 * <li>the order must be of type 'net'</li>
 * <li>all external tax entry numbers must be matching one of the order entries' numbers</li>
 * <li>all external tax values must be absolute numbers matching the order currency</li>
 * <li>for each order entry : store tax values from {@link ExternalTaxDocument#getTaxesForOrderEntry(int)} as own tax
 * values</li>
 * <li>for each order entry : if {@link ExternalTaxDocument#getTaxesForOrderEntry(int)} returns no value or empty value
 * the own tax values are cleared</li>
 * <li>shipping cost taxes from {@link ExternalTaxDocument#getShippingCostTaxes()} are stored as order total taxes ( see
 * {@link AbstractOrderModel#getTotalTaxValues()}</li>
 * <li>the sum of shipping and entry taxes is stored as order total tax ( see {@link AbstractOrderModel#getTotalTax() )}
 * </li>
 * </ul>
 *
 */
public class DefaultApplyExternalTaxesStrategy implements ApplyExternalTaxesStrategy
{

	@Override
	public void applyExternalTaxes(final AbstractOrderModel order, final ExternalTaxDocument externalTaxes)
	{
		if (!Boolean.TRUE.equals(order.getNet()))
		{
			throw new IllegalStateException("Order " + order.getCode() + " must be of type NET to apply external taxes to it.");
		}

		final BigDecimal entryTaxSum = applyEntryTaxes(order, externalTaxes);
		final BigDecimal shippingTaxSum = applyShippingCostTaxes(order, externalTaxes);

		setTotalTax(order, entryTaxSum.add(shippingTaxSum));
	}

	protected BigDecimal applyEntryTaxes(final AbstractOrderModel order, final ExternalTaxDocument taxDoc)
	{
		BigDecimal totalTax = BigDecimal.ZERO;

		final Set<Integer> consumedEntryNumbers = new HashSet<Integer>(taxDoc.getAllTaxes().keySet());

		for (final AbstractOrderEntryModel entry : order.getEntries())
		{
			final Integer entryNumber = entry.getEntryNumber();
			if (entryNumber == null)
			{
				throw new IllegalStateException("Order entry " + order.getCode() + "." + entry
						+ " does not have a entry number. Cannot apply external tax to it.");
			}

			final List<TaxValue> taxesForOrderEntry = taxDoc.getTaxesForOrderEntry(entryNumber.intValue());
			if (taxesForOrderEntry != null)
			{
				for (final TaxValue taxForOrderEntry : taxesForOrderEntry)
				{
					assertValidTaxValue(order, taxForOrderEntry);
					totalTax = totalTax.add(BigDecimal.valueOf(taxForOrderEntry.getAppliedValue()));
				}
			}
			entry.setTaxValues(taxesForOrderEntry);
			consumedEntryNumbers.remove(entryNumber);
		}

		if (!consumedEntryNumbers.isEmpty())
		{
			throw new IllegalArgumentException("External tax document " + taxDoc
					+ " seems to contain taxes for more lines items than available within " + order.getCode());
		}

		return totalTax;
	}

	protected BigDecimal applyShippingCostTaxes(final AbstractOrderModel order, final ExternalTaxDocument taxDoc)
	{
		BigDecimal totalTax = BigDecimal.ZERO;

		final List<TaxValue> shippingTaxes = taxDoc.getShippingCostTaxes();
		if (shippingTaxes != null)
		{
			for (final TaxValue taxForOrderEntry : shippingTaxes)
			{
				assertValidTaxValue(order, taxForOrderEntry);
				totalTax = totalTax.add(BigDecimal.valueOf(taxForOrderEntry.getAppliedValue()));
			}
		}
		order.setTotalTaxValues(shippingTaxes);

		return totalTax;
	}


	protected void setTotalTax(final AbstractOrderModel order, final BigDecimal totalTaxSum)
	{
		final Integer digits = order.getCurrency().getDigits();
		if (digits == null)
		{
			throw new IllegalStateException("Order " + order.getCode()
					+ " has got a currency without decimal digits defined. Cannot apply external taxes.");
		}
		// unfortunately we've got to round the tax sum due to TaxValue being double based and we're *for sure*
		// getting rounding errors without
		order.setTotalTax(Double.valueOf(CoreAlgorithms.round(totalTaxSum.doubleValue(), digits.intValue())));
	}

	protected void assertValidTaxValue(final AbstractOrderModel order, final TaxValue value)
	{
		if (!value.isAbsolute())
		{
			throw new IllegalArgumentException("External tax " + value + " is not absolute. Cannot apply it to order "
					+ order.getCode());
		}
		if (!order.getCurrency().getIsocode().equalsIgnoreCase(value.getCurrencyIsoCode()))
		{
			throw new IllegalArgumentException("External tax " + value + " currency " + value.getCurrencyIsoCode()
					+ " does not match order currency " + order.getCurrency().getIsocode() + ". Cannot apply.");
		}
	}
}
