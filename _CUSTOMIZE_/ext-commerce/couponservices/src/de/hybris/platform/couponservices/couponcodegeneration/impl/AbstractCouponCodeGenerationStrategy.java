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
package de.hybris.platform.couponservices.couponcodegeneration.impl;

import de.hybris.platform.servicelayer.exceptions.SystemException;

import org.apache.log4j.Logger;


/**
 * Abstract base class for the default coupon code generation strategies
 */
public abstract class AbstractCouponCodeGenerationStrategy
{

	private static final Logger LOG = Logger.getLogger(AbstractCouponCodeGenerationStrategy.class);

	/**
	 * creates a two-character string from the given int value
	 */
	protected String createTwoCharactersFromByte(final int value, final int offset, final String alphabet)
	{
		return pickCharacter(value >> 4, offset, alphabet).concat(pickCharacter(value & 15, offset + 4, alphabet));
	}

	/**
	 * returns a one-character string from the given alphabet. The value and offset are used to lookup the character from
	 * the alphabet.
	 */
	protected String pickCharacter(final int value, final int offset, final String alphabet)
	{
		final int alphabetIndex = (value + offset) % alphabet.length();
		return alphabet.substring(alphabetIndex, alphabetIndex + 1);
	}

	/**
	 * this is the inverse function to {@link #createTwoCharactersFromByte(int, int, String)}: If given a two-character
	 * input {@code value} and the offset used to create the two character input it will derive the originating int
	 * (byte) used to create this string.
	 */
	protected int createIntFromTwoCharactersString(final String value, final int offset, final String alphabet)
	{
		return (pickInt(value.charAt(0), offset, alphabet) << 4) + pickInt(value.charAt(1), offset + 4, alphabet);
	}

	/**
	 * This is the inverse function to {@link #pickCharacter(int, int, String)}: For the the given char {@code value} it
	 * will derive the originating int and return it.
	 */
	protected int pickInt(final char value, final int offset, final String alphabet)
	{
		int position = alphabet.indexOf(value);
		if (position == -1)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Invalid character given: '" + value + "' is not in current coupon alphabet:" + alphabet);
			}
			throw new SystemException("Invalid character given: " + value + "'' is not in current coupon alphabet.");
		}
		else
		{
			position -= offset;
			while (position < 0)
			{
				position += alphabet.length();
			}
			return position % alphabet.length();
		}
	}
}