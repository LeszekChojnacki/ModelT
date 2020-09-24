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
package de.hybris.platform.payment.methods;

import de.hybris.platform.payment.commands.request.AuthorizationRequest;
import de.hybris.platform.payment.commands.request.CaptureRequest;
import de.hybris.platform.payment.commands.request.CreateSubscriptionRequest;
import de.hybris.platform.payment.commands.request.DeleteSubscriptionRequest;
import de.hybris.platform.payment.commands.request.EnrollmentCheckRequest;
import de.hybris.platform.payment.commands.request.FollowOnRefundRequest;
import de.hybris.platform.payment.commands.request.PartialCaptureRequest;
import de.hybris.platform.payment.commands.request.StandaloneRefundRequest;
import de.hybris.platform.payment.commands.request.SubscriptionAuthorizationRequest;
import de.hybris.platform.payment.commands.request.SubscriptionDataRequest;
import de.hybris.platform.payment.commands.request.UpdateSubscriptionRequest;
import de.hybris.platform.payment.commands.request.VoidRequest;
import de.hybris.platform.payment.commands.result.AuthorizationResult;
import de.hybris.platform.payment.commands.result.CaptureResult;
import de.hybris.platform.payment.commands.result.EnrollmentCheckResult;
import de.hybris.platform.payment.commands.result.RefundResult;
import de.hybris.platform.payment.commands.result.SubscriptionDataResult;
import de.hybris.platform.payment.commands.result.SubscriptionResult;
import de.hybris.platform.payment.commands.result.VoidResult;


/**
 * Card payment service
 */
public interface CardPaymentService extends PaymentMethod
{
	/**
	 * authorize card payment
	 *
	 * @param request
	 *           instance of {@link AuthorizationRequest}
	 * @return authorization result
	 */
	AuthorizationResult authorize(AuthorizationRequest request);

	/**
	 * authorize card payment
	 *
	 * @param request
	 *           instance of {@link SubscriptionAuthorizationRequest}
	 * @return authorization result
	 */
	AuthorizationResult authorize(SubscriptionAuthorizationRequest request);

	/**
	 * capture card payment
	 *
	 * @param request
	 *           instance of {@link CaptureRequest}
	 * @return Capture result.
	 */
	CaptureResult capture(CaptureRequest request);

	/**
	 * partial capture card payment
	 *
	 * @param request
	 *           instance of {@link PartialCaptureRequest}
	 * @return Capture result.
	 */
	CaptureResult partialCapture(PartialCaptureRequest request);


	/**
	 * 3D secure enrollment check
	 *
	 * @param request
	 *           instance of {@link EnrollmentCheckRequest}
	 * @return enrollment check result
	 */
	EnrollmentCheckResult enrollmentCheck(EnrollmentCheckRequest request);

	/**
	 * Void a credit or capture.
	 *
	 * @param request
	 *           instance of {@link VoidRequest}
	 * @return void credit or capture result
	 */
	VoidResult voidCreditOrCapture(VoidRequest request);

	/**
	 * Refunds money to customer not based on previous transaction.
	 *
	 * @param request
	 *           instance of {@link StandaloneRefundRequest}
	 * @return refund standalone
	 */
	RefundResult refundStandalone(StandaloneRefundRequest request);

	/**
	 * Refunds money to customer based on previous transaction.
	 *
	 * @param request
	 *           instance of {@link FollowOnRefundRequest}
	 * @return refund follow on result
	 */
	RefundResult refundFollowOn(FollowOnRefundRequest request);

	/**
	 * Creates an account at the payment provider
	 *
	 * @param request
	 *           instance of {@link CreateSubscriptionRequest}
	 * @return subscription result object (instance of {@link SubscriptionResult})
	 */
	SubscriptionResult createSubscription(CreateSubscriptionRequest request);

	/**
	 * Updated an account at the payment provider
	 *
	 * @param request
	 *           instance of {@link UpdateSubscriptionRequest}
	 * @return subscription result object (instance of {@link SubscriptionResult})
	 */
	SubscriptionResult updateSubscription(UpdateSubscriptionRequest request);

	/**
	 * Gets data from the account at the payment provider
	 *
	 * @param request
	 *           instance of {@link SubscriptionDataRequest}
	 * @return subscription data result object (instance of {@link SubscriptionDataResult})
	 */
	SubscriptionDataResult getSubscriptionData(SubscriptionDataRequest request);

	/**
	 * Deletes the account at the payment provider
	 *
	 * @param request
	 *           instance of {@link DeleteSubscriptionRequest}
	 * @return subscription result object (instance of {@link SubscriptionResult})
	 */
	SubscriptionResult deleteSubscription(DeleteSubscriptionRequest request);
}
