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
package de.hybris.platform.payment;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.payment.commands.request.StandaloneRefundRequest;
import de.hybris.platform.payment.dto.BillingInfo;
import de.hybris.platform.payment.dto.CardInfo;
import de.hybris.platform.payment.dto.NewSubscription;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;

import java.math.BigDecimal;
import java.util.Currency;


/**
 * service can serve payments based on orders
 */
public interface PaymentService
{
	/**
	 * authorize payment
	 *
	 * @param order
	 * 		the order
	 * @param card
	 * 		the card
	 * @return Payment Transaction Entry
	 * @deprecated Since 4.2 use any of the other authorize methods
	 */
	@Deprecated
	PaymentTransactionEntryModel authorize(final OrderModel order, final CardInfo card); //NOSONAR

	/**
	 * authorize payment
	 *
	 * @param merchantTransactionCode
	 * 		the transaction code
	 * @param amount
	 * 		the amount
	 * @param currency
	 * 		the currency
	 * @param deliveryAddress
	 * 		the delivery address (paymentAddress is used if null)
	 * @param subscriptionID
	 * 		the subscriptionID
	 * @return Payment Transaction Entry
	 */
	PaymentTransactionEntryModel authorize(final String merchantTransactionCode, final BigDecimal amount, final Currency currency,
			final AddressModel deliveryAddress, final String subscriptionID);

	/**
	 * authorize payment
	 *
	 * @param merchantTransactionCode
	 * 		the transaction code
	 * @param amount
	 * 		the amount
	 * @param currency
	 * 		the currency
	 * @param deliveryAddress
	 * 		the delivery address (paymentAddress is used if null)
	 * @param subscriptionID
	 * 		the subscriptionID
	 * @param cv2
	 * 		card verification value
	 * @param paymentProvider
	 * 		code of payment provider
	 * @return Payment Transaction Entry
	 */
	PaymentTransactionEntryModel authorize(final String merchantTransactionCode, final BigDecimal amount, final Currency currency,
			final AddressModel deliveryAddress, final String subscriptionID, String cv2, String paymentProvider);

	/**
	 * authorize payment
	 *
	 * @param merchantTransactionCode
	 * 		the transaction code
	 * @param amount
	 * 		the amount
	 * @param currency
	 * 		the currency
	 * @param deliveryAddress
	 * 		the delivery address (paymentAddress is used if null)
	 * @param paymentAddress
	 * 		the payment address
	 * @param card
	 * 		the card
	 * @return Payment Transaction Entry
	 */
	PaymentTransactionEntryModel authorize(final String merchantTransactionCode, final BigDecimal amount, final Currency currency,
			final AddressModel deliveryAddress, final AddressModel paymentAddress, final CardInfo card);

	/**
	 * authorize payment
	 *
	 * @param transaction
	 * 		the payment transaction
	 * @param amount
	 * 		the amount
	 * @param currency
	 * 		the currency
	 * @param deliveryAddress
	 * 		the delivery address (paymentAddress is used if null)
	 * @param subscriptionID
	 * 		the subscriptionID
	 * @param paymentProvider
	 * 		code of payment provider
	 * @return Payment Transaction Entry
	 */
	PaymentTransactionEntryModel authorize(final PaymentTransactionModel transaction, final BigDecimal amount,
			final Currency currency, final AddressModel deliveryAddress, final String subscriptionID, String paymentProvider);

	/**
	 * authorize payment
	 *
	 * @param transaction
	 * 		the payment transaction
	 * @param amount
	 * 		the amount
	 * @param currency
	 * 		the currency
	 * @param deliveryAddress
	 * 		the delivery address (paymentAddress is used if null)
	 * @param subscriptionID
	 * 		the subscriptionID
	 * @return Payment Transaction Entry
	 */
	PaymentTransactionEntryModel authorize(final PaymentTransactionModel transaction, final BigDecimal amount,
			final Currency currency, final AddressModel deliveryAddress, final String subscriptionID);


	/**
	 * authorize payment
	 *
	 * @param transaction
	 * 		the payment transaction
	 * @param amount
	 * 		the amount
	 * @param currency
	 * 		the currency
	 * @param deliveryAddress
	 * 		the delivery address (paymentAddress is used if null)
	 * @param paymentAddress
	 * 		the payment address
	 * @param card
	 * 		the card
	 * @return Payment Transaction Entry
	 */
	PaymentTransactionEntryModel authorize(final PaymentTransactionModel transaction, final BigDecimal amount,
			final Currency currency, final AddressModel deliveryAddress, final AddressModel paymentAddress, final CardInfo card);

	/**
	 * capture payment
	 *
	 * @param transaction
	 * 		payment transaction
	 * @return payment transaction entry
	 */
	PaymentTransactionEntryModel capture(PaymentTransactionModel transaction);

	/**
	 * Cancel payment
	 *
	 * @param transaction
	 * 		payment transaction
	 * @return payment transaction entry
	 */
	PaymentTransactionEntryModel cancel(final PaymentTransactionEntryModel transaction);

	/**
	 * Refund transaction
	 *
	 * @param transaction
	 * 		payment transaction
	 * @param amount
	 * 		amount to refund
	 * @return payment transaction entry
	 */
	PaymentTransactionEntryModel refundFollowOn(final PaymentTransactionModel transaction, BigDecimal amount);


	/**
	 * Refund value to a card (no transaction is obligatory)
	 *
	 * @param request
	 * 		instance of {@link StandaloneRefundRequest} for a refund details
	 * @return payment transaction entry
	 * @deprecated Since 4.2 use {@link #refundStandalone(String, BigDecimal, Currency, AddressModel, CardInfo)}
	 */
	@Deprecated
	PaymentTransactionEntryModel refundStandalone(StandaloneRefundRequest request); // NOSONAR


	/**
	 * Refund value to a card (no transaction is obligatory)
	 *
	 * @param merchantTransactionCode
	 * 		any code to locate transaction
	 * @param amount
	 * 		amount to refund
	 * @param currency
	 * 		currency, used for refunding
	 * @param paymentAddress
	 * 		address to refund
	 * @param card
	 * 		card to refund
	 * @return payment transaction entry
	 */
	PaymentTransactionEntryModel refundStandalone(final String merchantTransactionCode, final BigDecimal amount,
			final Currency currency, final AddressModel paymentAddress, final CardInfo card);

	/**
	 * Refund value to a card (no transaction is obligatory)
	 *
	 * @param merchantTransactionCode
	 * 		any code to locate transaction
	 * @param amount
	 * 		amount to refund
	 * @param currency
	 * 		currency, used for refunding
	 * @param paymentAddress
	 * 		address to refund
	 * @param card
	 * 		card to refund
	 * @param providerName
	 * 		name of the payment provider
	 * @param subscriptionId
	 * 		subscription identifier
	 * @return payment transaction entry
	 */
	default PaymentTransactionEntryModel refundStandalone(final String merchantTransactionCode, final BigDecimal amount,
			final Currency currency, final AddressModel paymentAddress, final CardInfo card, final String providerName,
			final String subscriptionId)
	{
		return refundStandalone(merchantTransactionCode, amount, currency, paymentAddress, card);
	}

	/**
	 * Take a partial capture of authorized transaction
	 *
	 * @param transaction
	 * 		authorized transaction
	 * @param amount
	 * 		value to capture
	 * @return payment transaction entry
	 */
	PaymentTransactionEntryModel partialCapture(final PaymentTransactionModel transaction, BigDecimal amount);

	/**
	 * Get {@link PaymentTransactionModel} by code
	 *
	 * @param code
	 * 		the code of the {@link PaymentTransactionModel} we were looking for
	 * @return the {@link PaymentTransactionModel}
	 */
	PaymentTransactionModel getPaymentTransaction(final String code);

	/**
	 * Get {@link PaymentTransactionEntryModel} by code
	 *
	 * @param code
	 * 		the code of the {@link PaymentTransactionModel} we were looking for
	 * @return the instance of {@link PaymentTransactionEntryModel}
	 */
	PaymentTransactionEntryModel getPaymentTransactionEntry(final String code);

	/**
	 * Attaches PaymentInfo to the assigned PaymentTransactionModel instance.
	 *
	 * @param paymentTransactionModel
	 * 		the payment transaction
	 * @param userModel
	 * 		the user
	 * @param cardInfo
	 * 		the card info
	 * @param amount
	 * 		the amount expressed as {@link BigDecimal}
	 */
	void attachPaymentInfo(final PaymentTransactionModel paymentTransactionModel, final UserModel userModel,
			final CardInfo cardInfo, BigDecimal amount);

	/**
	 * Creates a subscription at the payment provider side and stores sensitive data there. Future payment authorization
	 * and refundStandalone calls will not have to provide the sensitive information, subscriptionID would be sufficient.
	 * Call this method after a successful authorize txn and provide THE SAME billing and card info. Please note: the
	 * card and billing info is not saved in the returning PaymentTransactionEntryModel from authorize method call ON
	 * PURPOSE.
	 *
	 * @param transaction
	 * 		a previous recent successful authorize payment transaction
	 * @param paymentAddress
	 * 		the same billing address as for the authorize txn
	 * @param card
	 * 		the same card as for the authorize txn
	 * @return instance of {@link NewSubscription}
	 */
	NewSubscription createSubscription(final PaymentTransactionModel transaction, final AddressModel paymentAddress,
			final CardInfo card);

	/**
	 * Creates a subscription at the payment provider side and stores sensitive data there. Future payment authorization
	 * and refundStandalone calls will not have to provide the sensitive information, subscriptionID would be sufficient.
	 * This method does not need an authorized payment transaction, the authorization is expected to be done implicitly
	 * by the payment provider.
	 *
	 * @param merchantTransactionCode
	 * 		the transactionCode
	 * @param paymentProvider
	 * 		the paymentProvider who will hold the subscription. You might also have just one payment provider in
	 * 		your implementation.
	 * @param currency
	 * 		the default customer's currency
	 * @param paymentAddress
	 * 		the same billing address as for the authorize txn
	 * @param card
	 * 		the same card as for the authorize txn
	 * @return new subscription object (instance of {@link NewSubscription})
	 */
	NewSubscription createSubscription(final String merchantTransactionCode, final String paymentProvider, final Currency currency,
			final AddressModel paymentAddress, final CardInfo card);

	/**
	 * Updates the data of the subscription at the payment provider.
	 *
	 * @param merchantTransactionCode
	 * 		the transactionCode
	 * @param subscriptionID
	 * 		the subscription ID
	 * @param paymentProvider
	 * 		the paymentProvider who holds the subscription. You get this value from the
	 * 		{@link PaymentTransactionModel} of the createSubscription command. You might also have just one payment
	 * 		provider in your implementation.
	 * @param paymentAddress
	 * 		the new billing address, could be null
	 * @param card
	 * 		the updated card, could be null
	 * @return instance of {@link PaymentTransactionEntryModel}
	 */
	PaymentTransactionEntryModel updateSubscription(final String merchantTransactionCode, final String subscriptionID,
			final String paymentProvider, final AddressModel paymentAddress, final CardInfo card);

	/**
	 * Gets the stored card info or payment address
	 *
	 * @param merchantTransactionCode
	 * 		the transactionCode
	 * @param subscriptionID
	 * 		the subscription ID
	 * @param paymentProvider
	 * 		the paymentProvider who holds the subscription. You get this value from the
	 * 		{@link PaymentTransactionModel} of the createSubscription command. You might also have just one payment
	 * 		provider in your implementation.
	 * @param billingInfo
	 * 		the billing address is returned in this DTO
	 * @param card
	 * 		the card information is returned in this DTO
	 * @return instance of {@link PaymentTransactionEntryModel}
	 */
	PaymentTransactionEntryModel getSubscriptionData(final String merchantTransactionCode, final String subscriptionID,
			final String paymentProvider, final BillingInfo billingInfo, final CardInfo card);

	/**
	 * Deletes the subscription at the payment provider. Warning: Deleting a customer profile is permanent. You cannot
	 * recover a deleted customer profile.
	 *
	 * @param merchantTransactionCode
	 * 		the transactionCode
	 * @param subscriptionID
	 * 		the subscription ID
	 * @param paymentProvider
	 * 		the paymentProvider who holds the subscription. You get this value from the
	 * 		{@link PaymentTransactionModel} of the createSubscription command. You might also have just one payment
	 * 		provider in your implementation.
	 * @return instance of {@link PaymentTransactionEntryModel}
	 */
	PaymentTransactionEntryModel deleteSubscription(final String merchantTransactionCode, final String subscriptionID,
			final String paymentProvider);

	/**
	 * A new code for a {@link PaymentTransactionEntryModel} based on the {@link PaymentTransactionModel} associated to
	 * it and the number of entries.
	 *
	 * @param transaction
	 * 		An associated Payment transaction
	 * @param paymentTransactionType
	 * 		A type of entry.
	 * @return A new unique code withing a transaction context.
	 */
	String getNewPaymentTransactionEntryCode(PaymentTransactionModel transaction, PaymentTransactionType paymentTransactionType);
}
