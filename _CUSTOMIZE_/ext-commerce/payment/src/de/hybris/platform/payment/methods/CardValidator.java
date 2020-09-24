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


import de.hybris.platform.core.enums.CreditCardType;
import de.hybris.platform.payment.commands.result.CardValidationResult;
import de.hybris.platform.payment.dto.CardInfo;

import java.util.List;


/**
 * Provides various credit card validation routines. The Card Validation process also discovers useful information about
 * the card number, such as the card scheme, issuing country and bank.
 */
public interface CardValidator
{
	/**
	 * Performs a luhn check on the given credit card number, in order to determine if it is a valid number.
	 */
	boolean luhnCheck(String number);

	/**
	 * Performs the complete suite of card validation, including the luhn check. The Card Validation process also
	 * discovers useful information about the card number, such as the card scheme, issuing country and bank. This is all
	 * returned in the card validation result.
	 */
	CardValidationResult checkCard(CardInfo cardInfo);

	/**
	 * Returns the card schemes supported by the system.
	 */
	List<CreditCardType> getSupportedCardSchemes();

	/**
	 * Determines if the provided card scheme is supported by the system.
	 */
	boolean isCardSchemeSupported(CreditCardType cardScheme);
}