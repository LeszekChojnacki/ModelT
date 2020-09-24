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
package de.hybris.platform.payment.methods.impl;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNumeric;
import static org.apache.commons.lang.StringUtils.split;
import static org.apache.commons.lang.StringUtils.strip;

import de.hybris.platform.core.enums.CreditCardType;
import de.hybris.platform.payment.commands.result.CardValidationError;
import de.hybris.platform.payment.commands.result.CardValidationResult;
import de.hybris.platform.payment.dto.CardInfo;
import de.hybris.platform.payment.methods.CardValidator;
import de.hybris.platform.util.Config;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;


public class CardValidatorImpl implements CardValidator
{
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CardValidatorImpl.class.getName());

	private List<CreditCardType> supportedCardSchemes;
	private List<CreditCardType> exemptFromCv2 = new ArrayList<>();

	/**
	 * Lifted from Wikipedia. Checks whether a string of digits is a valid credit card number according to the Luhn
	 * algorithm.
	 *
	 * 1. Starting with the second to last digit and moving left, double the value of all the alternating digits. For any
	 * digits that thus become 10 or more, add their digits together. For example, 1111 becomes 2121, while 8763 becomes
	 * 7733 (from (1+6)7(1+2)3).
	 *
	 * 2. Add all these digits together. For example, 1111 becomes 2121, then 2+1+2+1 is 6; while 8763 becomes 7733, then
	 * 7+7+3+3 is 20.
	 *
	 * 3. If the total ends in 0 (put another way, if the total modulus 10 is 0), then the number is valid according to
	 * the Luhn formula, else it is not valid. So, 1111 is not valid (as shown above, it comes out to 6), while 8763 is
	 * valid (as shown above, it comes out to 20).
	 *
	 * @param number
	 *           the credit card number to validate. If no credit card number is provided, false will be returned.
	 * @return true if the number is valid, false otherwise.
	 */
	@Override
	public boolean luhnCheck(final String number)
	{
		if (number == null)
		{
			return false;
		}
		final int[][] sumTable =
		{
				{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 },
				{ 0, 2, 4, 6, 8, 1, 3, 5, 7, 9 } };
		int sum = 0;
		int flip = 0;

		for (int i = number.length() - 1; i >= 0; i--)
		{
			sum += sumTable[flip++ & 0x1][Character.digit(number.charAt(i), 10)];
		}

		return sum % 10 == 0;
	}

	/**
	 * Return whether the card number is an international Maestro card number. Taken from information supplied by
	 * DataCash: http://www.barclaycardbusiness.co.uk/docs/binranges.pdf
	 */
	public boolean isInternationalMaestro(final String cardNumber)
	{
		if (isBlank(cardNumber) || cardNumber.length() < 6)
		{
			return false;
		}

		// take the first 6 chars and convert to an int
		final int val = NumberUtils.toInt(cardNumber.substring(0, 6));

		return (inRange(val, 500000, 50999) || inRange(val, 560000, 589999) || inRange(val, 600000, 699999)) && // international maestro ranged //NOSONAR
				!inRange(val, 675900, 675999) && !inRange(val, 676700, 676799); // still want uk maestro and solo ranges
	}

	/**
	 * Return whether a number is within a given range (inclusive)
	 *
	 * @param num
	 *           The number to test
	 * @param lower
	 *           The low end of the range
	 * @param upper
	 *           The upper end of the range
	 * @return True if number is within lower and upper range (inclusive)
	 */
	protected boolean inRange(final int num, final int lower, final int upper)
	{
		return (num >= lower) && (num <= upper);
	}

	/**
	 * Set all the card schemes that are supported by the site. This may be a reduced list from all known card schemes.
	 */
	public void setSupportedCardSchemes(final List<CreditCardType> supportedCardSchemes)
	{
		this.supportedCardSchemes = supportedCardSchemes;
	}

	@Override
	public List<CreditCardType> getSupportedCardSchemes()
	{
		return this.supportedCardSchemes;
	}

	/**
	 * Sets a list of cards that do not require cv2 numbers. By default all cards require cv2.
	 */
	public void setCv2ExemptCardSchemes(final List<CreditCardType> cv2ExemptCardSchemes)
	{
		this.exemptFromCv2 = cv2ExemptCardSchemes;
	}

	@Override
	public boolean isCardSchemeSupported(final CreditCardType cardScheme)
	{
		return this.supportedCardSchemes.contains(cardScheme);
	}

	/**
	 * Checks that the name consists of two separate names
	 */
	protected void validateName(final CardValidationResult result, final CardInfo cardInfo)
	{
		if (isBlank(cardInfo.getCardHolderFullName())
				|| split(cardInfo.getCardHolderFullName()).length <= 1)
		{
			result.addValidationError(CardValidationError.INVALID_NAME);
		}
	}


	/**
	 * checks to see if the card scheme is supported for the given result with card scheme data. If it is not, an error
	 * is added to the result object.
	 *
	 * @param result
	 */
	@SuppressWarnings("PMD")
	protected void validateCardScheme(final CardValidationResult result, final CardInfo cardInfo)
	{

		if (cardInfo.getCardType() == null)
		{
			result.addValidationError(CardValidationError.MISSING_CARD_SCHEME);
		}
		else if (!isCardSchemeSupported(cardInfo.getCardType())) // check to see if the card scheme is actually supported
		{
			result.addValidationError(CardValidationError.UNSUPPORTED_CARD_SCHEME);
		}

	}

	/**
	 * Implements basic card validation. i.e. has the luhn check passed and is the number a number.
	 */
	protected void validateCardNumber(final CardValidationResult validationResult, final CardInfo cardInfo)
	{
		if (isNumeric(strip(cardInfo.getCardNumber())))
		{
			if (Boolean.parseBoolean(Config.getParameter("validation.luhncheck")))
			{
				if (!isLuhnCheckCompliant(cardInfo))
				{
					validationResult.addValidationError(CardValidationError.LUHN_CHECK_FAILED);
				}
			}
			else
			{
				LOG.debug("Card not luhn checked as luhn check is disabled");
			}
		}
		else
		{
			validationResult.addValidationError(CardValidationError.INVALID_NUMBER);
		}
	}

	protected boolean isLuhnCheckCompliant(final CardInfo cardInfo)
	{
		boolean isLunhCheckCompliant = true;
		if (isInternationalMaestro(cardInfo.getCardNumber()))
		{
			LOG.info("Skipping Luhn check as card is an international maestro card");
		}
		else
		{
			if (!luhnCheck(cardInfo.getCardNumber()))
			{
				isLunhCheckCompliant = false;
			}
		}
		return isLunhCheckCompliant;
	}

	protected void validateDates(final CardValidationResult validationResult, final CardInfo cardInfo)
	{
		if (!isValidDate(cardInfo.getExpirationMonth(), cardInfo.getExpirationYear(), true))
		{
			validationResult.addValidationError(CardValidationError.INVALID_EXPIRY_DATE);
		}


		if (!isValidDate(cardInfo.getIssueMonth(), cardInfo.getIssueYear(), false))
		{
			validationResult.addValidationError(CardValidationError.INVALID_ISSUE_DATE);
		}

		if (hasExpirationDate(cardInfo) && isExpired(cardInfo))
		{
			validationResult.addValidationError(CardValidationError.INVALID_EXPIRY_DATE);
		}
	}

	protected boolean isExpired(final CardInfo cardInfo)
	{
		return cardInfo.getExpirationYear().intValue() < cardInfo.getIssueYear().intValue()
				|| cardInfo.getExpirationYear().intValue() == cardInfo.getIssueYear().intValue()
						&& cardInfo.getExpirationMonth().intValue() <= cardInfo.getIssueMonth().intValue();
	}

	protected boolean hasExpirationDate(final CardInfo cardInfo)
	{
		return cardInfo.getExpirationYear() != null && cardInfo.getIssueYear() != null;
	}

	protected boolean isValidDate(final Integer month, final Integer year, final boolean expiryDate)
	{
		if (month == null || year == null)
		{
			return !expiryDate;
		}
		else
		{
			final int currentYear = Calendar.getInstance().get(Calendar.YEAR);
			final int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1; // plus 1 because Calendar returns 0 for January and 11 for December
			if ((month.intValue() < 1) || (month.intValue() > 12))
			{
				return false;
			}
			if (expiryDate)
			{
				return isBeforeCurrentYear(year, month, currentYear, currentMonth);
			}
			else
			{
				return isAfterCurrentYear(year, month, currentYear, currentMonth);
			}
		}
	}

	protected boolean isBeforeCurrentYear(final Integer year, final Integer month, final int currentYear, final int currentMonth)
	{
		return !(year.intValue() < currentYear || year.intValue() == currentYear && month.intValue() < currentMonth);
	}

	protected boolean isAfterCurrentYear(final Integer year, final Integer month, final int currentYear, final int currentMonth)
	{
		return !(year.intValue() > currentYear || year.intValue() == currentYear && month.intValue() > currentMonth);
	}

	@SuppressWarnings({"PMD","squid:MethodCyclomaticComplexity"})
	protected void validateCv2(final CardValidationResult validationResult, final CardInfo cardInfo,
			final boolean isCv2AvsPolicyDisabled)
	{
		if (isCv2AvsPolicyDisabled)
		{
			// cv2 checking is disabled so do no more
			return;
		}

		// check to see if the card is exempt from cv2 validation
		// if we are unable to determine the scheme we insist on the cv2 number
		// to cover us for security
		if (cardInfo.getCardType() != null && this.exemptFromCv2.contains(cardInfo.getCardType()))
		{
			// card is exempt so give up. if the user has provided an
			// incorrect card type, this will be caught by the validateCardScheme
			// method
			return;
		}

		if (cardInfo.getCv2Number() == null)
		{
			// must have a cv2
			validationResult.addValidationError(CardValidationError.MISSING_CV2);
		}
		else
		{
			validateCv2Format(validationResult, cardInfo);
		}
	}

	protected void validateCv2Format(final CardValidationResult validationResult, final CardInfo cardInfo)
	{
		if (!isNumeric(cardInfo.getCv2Number()))
		{
			// cv2 must be numeric
			validationResult.addValidationError(CardValidationError.INVALID_CV2_FORMAT);
		}
		else if (cardInfo.getCardType() != null)
		{
			// hard-wired these rules, maybe someday configurable but doesnt really need to be
			if (cardInfo.getCardType().equals(CreditCardType.AMEX))
			{
				// amex cards have 4 digit cv2 numbers
				if (cardInfo.getCv2Number().length() != 4)
				{
					validationResult.addValidationError(CardValidationError.INVALID_CV2_LENGTH);
				}
			}
			else if (cardInfo.getCv2Number().length() != 3)
			{
				// all other cards have 3 digit cv2
				validationResult.addValidationError(CardValidationError.INVALID_CV2_LENGTH);
			}
		}
		else if ((cardInfo.getCv2Number().length() != 3) && (cardInfo.getCv2Number().length() != 4))
		{
			// we dont know the card scheme, but we can at least check that the cv2 number
			// is 3 or 4 digits
			validationResult.addValidationError(CardValidationError.INVALID_CV2_LENGTH);
		}
	}


	@Override
	public CardValidationResult checkCard(final CardInfo cardInfo)
	{
		// the most basic card validation
		final CardValidationResult cardValidationResult = new CardValidationResult();
		validateCardNumber(cardValidationResult, cardInfo);

		// name on card
		validateName(cardValidationResult, cardInfo);

		if (!cardValidationResult.isSuccess())
		{
			// if basic validation fails terminate
			LOG.info("basic card validation failed" + cardValidationResult);
			return cardValidationResult;
		}

		// validate the provided dates
		validateDates(cardValidationResult, cardInfo);


		// now we know the card scheme add any card scheme related errors
		validateCardScheme(cardValidationResult, cardInfo);


		// check the cv2 number is added
		validateCv2(cardValidationResult, cardInfo, false);

		if (LOG.isInfoEnabled())
		{
			LOG.info("result of card validation:" + cardValidationResult);
		}
		return cardValidationResult;

	}
}
