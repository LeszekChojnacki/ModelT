/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 *
 */
package de.hybris.platform.warehousing.consignment.strategies.impl;

import de.hybris.platform.basecommerce.enums.ConsignmentStatus;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.ordersplitting.model.ConsignmentEntryModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.util.TaxValue;
import de.hybris.platform.warehousing.consignment.strategies.ConsignmentAmountCalculationStrategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;
import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.valueOf;


/**
 * Default implementation for {@link ConsignmentAmountCalculationStrategy}
 */
public class DefaultConsignmentAmountCalculationStrategy implements ConsignmentAmountCalculationStrategy
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultConsignmentAmountCalculationStrategy.class);

	@Override
	public BigDecimal calculateCaptureAmount(final ConsignmentModel consignment)
	{
		validateParameterNotNullStandardMessage("consignment", consignment);

		final BigDecimal result;
		final BigDecimal orderAmountWithoutShipping = calculateOrderAmountWithoutDeliveryCostAndDeliveryTax(consignment);
		final BigDecimal alreadyCapturedAmount = calculateAlreadyCapturedAmount(consignment);

		if (alreadyCapturedAmount.equals(orderAmountWithoutShipping))
		{
			result = ZERO;
		}
		else if (alreadyCapturedAmount.compareTo(orderAmountWithoutShipping) < 0)
		{
			result = calculateAmountToCapture(consignment, alreadyCapturedAmount, orderAmountWithoutShipping);
		}
		else
		{
			throw new IllegalStateException(
					"Consignment: " + consignment.getCode() + " is trying to capture an amount greater than the order total");
		}
		LOGGER.debug("Calculated {} to be captured for consignment {}", result, consignment.getCode());
		return result;
	}

	@Override
	public BigDecimal calculateAlreadyCapturedAmount(final ConsignmentModel consignment)
	{
		return consignment.getOrder().getConsignments().stream()
				.filter(consignmentModel -> !CollectionUtils.isEmpty(consignmentModel.getPaymentTransactionEntries()))
				.map(this::calculateAmountCaptured).reduce(BigDecimal::add).orElse(ZERO);
	}

	@Override
	public BigDecimal calculateTotalOrderAmount(final ConsignmentModel consignment)
	{
		return valueOf(consignment.getOrder().getTotalPrice()).add(valueOf(consignment.getOrder().getTotalTax()));
	}

	@Override
	public BigDecimal calculateDiscountAmount(final ConsignmentModel consignment)
	{
		final BigDecimal result;

		if (consignment.getPaymentTransactionEntries() != null && !consignment.getPaymentTransactionEntries().isEmpty())
		{
			result = BigDecimal.ZERO;
		}
		else if (isOnlyOrLastConsignment(consignment))
		{
			result = valueOf(consignment.getOrder().getTotalDiscounts()).subtract(consignment.getOrder().getConsignments().stream()
					.filter(consignmentModel -> !consignment.getCode().equals(consignmentModel.getCode()))
					.map(this::calculateDiscountAmountForConsignment).reduce(BigDecimal::add).orElse(BigDecimal.ZERO));
		}
		else
		{
			result = calculateDiscountAmountForConsignment(consignment);
		}

		return result.setScale(consignment.getOrder().getCurrency().getDigits(), RoundingMode.HALF_UP);
	}

	@Override
	public BigDecimal calculateConsignmentEntryAmount(final ConsignmentEntryModel consignmentEntry, final boolean includeTaxes)
	{
		final BigDecimal consignmentTotalWithoutTax = consignmentEntry.getOrderEntry().getQuantity().equals(0L) ?
				ZERO :
				valueOf(consignmentEntry.getQuantity()).multiply(valueOf(consignmentEntry.getOrderEntry().getTotalPrice()))
						.divide(valueOf(consignmentEntry.getOrderEntry().getQuantity()),
								consignmentEntry.getOrderEntry().getOrder().getCurrency().getDigits(), RoundingMode.HALF_UP);

		BigDecimal result = consignmentTotalWithoutTax;

		if (includeTaxes && !ZERO.equals(consignmentTotalWithoutTax))
		{
			result = consignmentTotalWithoutTax.add(calculateConsignmentEntryTaxAmount(consignmentEntry));
		}

		return result;
	}

	/**
	 * Calculates discount amount for {@link ConsignmentModel} by taking a proportional part of
	 * {@link de.hybris.platform.core.model.order.OrderModel#TOTALDISCOUNTS}.
	 * Returns the discount value without taking into account if it's the last consignment
	 *
	 * @param consignment
	 * 		{@link ConsignmentModel} to calculate discount
	 * @return {@link BigDecimal} the discount amount
	 */
	protected BigDecimal calculateDiscountAmountForConsignment(final ConsignmentModel consignment)
	{
		final BigDecimal consignmentAmountWithoutTax = calculateConsignmentAmount(consignment, false);
		return consignmentAmountWithoutTax.multiply(valueOf(consignment.getOrder().getTotalDiscounts()))
				.divide(valueOf(consignment.getOrder().getTotalPrice()).subtract(valueOf(consignment.getOrder().getDeliveryCost())),
						consignment.getOrder().getCurrency().getDigits(), RoundingMode.HALF_UP);
	}

	/**
	 * Decides if this {@link ConsignmentModel} is the only or last one to be captured
	 *
	 * @param consignment
	 * 		given {@link ConsignmentModel}
	 * @return true if the given {@link ConsignmentModel} is the only one or the last one to be captured
	 */
	protected boolean isOnlyOrLastConsignment(final ConsignmentModel consignment)
	{
		return consignment.getOrder().getConsignments().size() == 1 || consignment.getOrder().getConsignments().stream().filter(
				consignmentModel -> !consignmentModel.getCode().equalsIgnoreCase(consignment.getCode())
						&& !ConsignmentStatus.CANCELLED.equals(consignmentModel.getStatus()))
				.noneMatch(consignmentModel -> CollectionUtils.isEmpty(consignmentModel.getPaymentTransactionEntries()));
	}

	/**
	 * Calculates the amount to capture. Takes into account the amount which has been already captured and the order total
	 * so that the result never exceeds the order total.
	 *
	 * @param consignment
	 * 		given {@link ConsignmentModel}
	 * @param alreadyCapturedAmount
	 * 		the amount which has been already captured
	 * @param totalOrderAmount
	 * 		the order total
	 * @return {@link BigDecimal} amount to be captured on this consignment
	 */
	protected BigDecimal calculateAmountToCapture(final ConsignmentModel consignment, final BigDecimal alreadyCapturedAmount,
			final BigDecimal totalOrderAmount)
	{
		final BigDecimal result;
		if (isOnlyOrLastConsignment(consignment))
		{
			result = totalOrderAmount.subtract(alreadyCapturedAmount);
		}
		else
		{
			final BigDecimal consignmentAmountWithTax = calculateConsignmentAmount(consignment, true)
					.subtract(calculateDiscountAmountForConsignment(consignment));
			if (consignmentAmountWithTax.add(alreadyCapturedAmount).compareTo(totalOrderAmount) > 0)
			{
				result = totalOrderAmount.subtract(alreadyCapturedAmount);
			}
			else
			{
				result = consignmentAmountWithTax;
			}
		}
		return result;
	}

	/**
	 * Calculates the amount of the {@link ConsignmentModel} based on the appropriate {@link OrderEntryModel#TOTALPRICE}
	 *
	 * @param consignment
	 * 		given {@link ConsignmentModel}
	 * @param includeTaxes
	 * 		if true returns the amount with taxes otherwise - without taxes
	 * @return {@link BigDecimal} the cost of the consignment
	 */
	protected BigDecimal calculateConsignmentAmount(final ConsignmentModel consignment, final boolean includeTaxes)
	{
		return consignment.getConsignmentEntries().stream()
				.map(consignmentEntry -> calculateConsignmentEntryAmount(consignmentEntry, includeTaxes)).reduce(BigDecimal::add)
				.orElse(ZERO);
	}

	/**
	 * Calculates Tax amount for {@link ConsignmentEntryModel}
	 *
	 * @param consignmentEntry
	 * 		given {@link ConsignmentEntryModel}
	 * @return {@link BigDecimal} amount of the consignment entry
	 */
	protected BigDecimal calculateConsignmentEntryTaxAmount(final ConsignmentEntryModel consignmentEntry)
	{
		final BigDecimal result;
		if (consignmentEntry.getQuantity() == 0)
		{
			result = BigDecimal.ZERO;
		}
		else
		{
			final BigDecimal taxAmountPerEntry = valueOf(consignmentEntry.getOrderEntry().getTaxValues().stream().findFirst()
					.orElseThrow(() -> new IllegalStateException(
							"No Tax value found for product: " + consignmentEntry.getOrderEntry().getProduct().getCode())).getValue());
			final BigDecimal orderEntryQuantity = valueOf(consignmentEntry.getOrderEntry().getQuantity())
					.add(valueOf((((OrderEntryModel) consignmentEntry.getOrderEntry()).getQuantityUnallocated())));

			result = taxAmountPerEntry
					.divide(orderEntryQuantity, consignmentEntry.getConsignment().getOrder().getCurrency().getDigits(),
							RoundingMode.HALF_UP).multiply(valueOf(consignmentEntry.getQuantity()));
		}

		return result;
	}

	/**
	 * Calculates amount already captured for the given {@link ConsignmentModel}
	 *
	 * @param consignment
	 * 		the given {@link ConsignmentModel}
	 * @return {@link BigDecimal} amount captured for the consignment
	 */
	protected BigDecimal calculateAmountCaptured(final ConsignmentModel consignment)
	{
		Optional<PaymentTransactionEntryModel> paymentTransactionEntry = consignment.getPaymentTransactionEntries().stream()
				.findFirst();
		return paymentTransactionEntry.isPresent() ? paymentTransactionEntry.get().getAmount() : null;
	}

	/**
	 * Calculates order amount without delivery cost and its corresponding tax
	 *
	 * @param consignment
	 * @return {@link BigDecimal} order amount for the consignment
	 */
	protected BigDecimal calculateOrderAmountWithoutDeliveryCostAndDeliveryTax(final ConsignmentModel consignment)
	{
		return calculateTotalOrderAmount(consignment).subtract(valueOf(consignment.getOrder().getDeliveryCost())).subtract(valueOf(
				consignment.getOrder().getTotalTaxValues().stream().findFirst().orElseGet(() -> new TaxValue("", 0, true, ""))
						.getValue()));
	}

}
