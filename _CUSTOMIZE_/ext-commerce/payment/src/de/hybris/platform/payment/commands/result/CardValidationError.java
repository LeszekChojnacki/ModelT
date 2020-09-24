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
package de.hybris.platform.payment.commands.result;

/**
 * Noteworthy errors captured at card validation time. Card Validation is the process of verifying that the correct card
 * information has been provided. All these errors should be recoverable by the user, so therefore each error should be
 * mapped to a helpful error message.
 */
public enum CardValidationError
{
	INVALID_NUMBER, // the number was an invalid format or length
	LUHN_CHECK_FAILED, // the number failed the modulus 10 luhn check
	INVALID_ISSUE_NUMBER, // the issue number was invalid
	INVALID_ISSUE_DATE, // the issue date was invalid
	INVALID_EXPIRY_DATE, // the expiry date was invalid
	INVALID_CURRENCY, // the card does not support the given currency
	MISSING_CV2, // the card required a cv2 number which was missing
	INVALID_CV2_LENGTH, // the cv2 length was wrong
	INVALID_CV2_FORMAT, // the cv2 number has an incorrect format
	UNSUPPORTED_CARD_SCHEME, // the card scheme is not supported
	INCORRECT_CARD_SCHEME, // the card scheme provided does not match what was confirmed from the card bin
	MISSING_CARD_SCHEME, // the card scheme was not provided, but is required
	UNKNOWN_CARD_SCHEME, // Unable to determine the card scheme
	INVALID_NAME
	// Invalid name on card.
}
