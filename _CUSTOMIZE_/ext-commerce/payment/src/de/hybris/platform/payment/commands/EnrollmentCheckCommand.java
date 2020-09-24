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
package de.hybris.platform.payment.commands;

import de.hybris.platform.payment.commands.request.EnrollmentCheckRequest;
import de.hybris.platform.payment.commands.result.EnrollmentCheckResult;
import de.hybris.platform.payment.dto.TransactionStatus;
import de.hybris.platform.payment.dto.TransactionStatusDetails;


/**
 * Command for handling 3-D secure (also called Payer Authentication) enrollment check. Enrollment check verifies
 * whether the customer is enrolled in one of the 3-D secure payer authentication programs. Currently only Visa,
 * Mastercard (Maestro) and JCB provides Payer Authentication services. <b>WARNING!</b> The command's reply
 * {@link EnrollmentCheckResult} returns {@link TransactionStatus#REJECTED} status with
 * {@link TransactionStatusDetails#THREE_D_SECURE_AUTHENTICATION_REQUIRED} when card is enrolled (and thus 3-D secure
 * authentication is required). This command returns {@link TransactionStatus#ACCEPTED} status with
 * {@link TransactionStatusDetails#THREE_D_SECURE_NOT_SUPPORTED} when card is NOT enrolled (and thus 3-D secure
 * authentication is not required).
 */
public interface EnrollmentCheckCommand extends Command<EnrollmentCheckRequest, EnrollmentCheckResult>
{
	//empty
}
