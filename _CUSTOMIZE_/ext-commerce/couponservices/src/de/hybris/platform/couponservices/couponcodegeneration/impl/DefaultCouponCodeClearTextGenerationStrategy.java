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

import static java.util.Objects.requireNonNull;

import de.hybris.platform.couponservices.couponcodegeneration.CouponCodeClearTextGenerationStrategy;
import de.hybris.platform.couponservices.couponcodegeneration.CouponCodeGenerationException;
import de.hybris.platform.couponservices.model.MultiCodeCouponModel;
import de.hybris.platform.servicelayer.exceptions.SystemException;

import java.util.List;

import org.springframework.beans.factory.InitializingBean;

import com.google.common.primitives.Ints;


public class DefaultCouponCodeClearTextGenerationStrategy extends AbstractCouponCodeGenerationStrategy
		implements CouponCodeClearTextGenerationStrategy, InitializingBean
{
	private int[] bitshifts;
	private int[] offsets;

	@Override
	public void afterPropertiesSet()
	{
		// load default values for offsets
		if (offsets == null)
		{
			offsets = new int[]
			{ 3, 5, 7, 11, 13, 17, 19, 23 };
		}
		// load default values for bitshifts
		if (bitshifts == null)
		{
			bitshifts = new int[]
			{ 0, 8, 16, 24, 32, 40, 48, 56 };
		}
	}

	@Override
	public String generateClearText(final MultiCodeCouponModel coupon, final int length)
	{
		requireNonNull(coupon);
		checkLength(length);
		checkMaximumCouponCodesGenerated(coupon, length);
		final long seed = coupon.getCouponCodeNumber().longValue();

		// we generate two characters per seed byte
		final StringBuilder clearText = new StringBuilder();
		int index = 0;
		while (clearText.length() < length)
		{
			// the 'long' seed gets bitshifted by 1byte/8bits for each two characters.
			// As only the lowest byte is taken into account by the createTwoCharactersFromByte
			// method, only that byte gets turned into a 'unsigned byte' by applying the 'bitwise AND' with 255/0xFF
			final int theByte = (int) ((seed >> getBitshifts()[index]) & 255);
			clearText.append(createTwoCharactersFromByte(theByte, getOffsets()[index], coupon.getAlphabet()));
			index++;
		}

		// increment number at the end
		coupon.setCouponCodeNumber(Long.valueOf(seed + 1L));

		return clearText.toString();
	}


	@Override
	public long getCouponCodeNumberForClearText(final MultiCodeCouponModel coupon, final String clearText)
	{
		// we store one int for each two characters
		final int[] ints = new int[clearText.length() / 2];
		int counter = 0;
		int partIndex = 0;
		while (partIndex < clearText.length() - 1)
		{
			final String twoChars = clearText.substring(partIndex, partIndex + 2);
			final int offset = getOffsets()[counter];
			ints[counter] = createIntFromTwoCharactersString(twoChars, offset, coupon.getAlphabet());
			partIndex += 2;
			counter++;
		}
		// now we concatenate the lowest byte of each int into the originating seed.
		// this requires using the same bitshifts that were used during clear text generation
		long seedNumber = 0L;
		for (int i = 0; i < ints.length; i++)
		{
			final long part = ints[i]; // cast to long in order to correctly handle the bit shifting
			final int bitshiftBy = getBitshifts()[i];
			seedNumber = seedNumber | (part << bitshiftBy);
		}
		return seedNumber;
	}

	/**
	 * Checks if the given coupon's couponCodeNumber has exceeded the maximum number of coupon codes that can be
	 * generated for the given length.
	 *
	 * <pre>
	 * The maximum number of coupon codes are:
	 * length 2		2^8  = 256 coupons
	 * length 4		2^16 = 65.536 coupons
	 * length 6		2^24 = 16.777.216 coupons
	 * length 7		2^32 = 4.294.967.296 coupons
	 * </pre>
	 */
	protected void checkMaximumCouponCodesGenerated(final MultiCodeCouponModel coupon, final int length)
	{
		final long couponCodeNumber = coupon.getCouponCodeNumber().longValue();
		long limit = 0L;
		switch (length)
		{
			case 2:
				limit = 256L;
				break;

			case 4:
				limit = 65536L;
				break;

			case 6:
				limit = 16777216L;
				break;

			case 8:
				limit = 4294967296L;
				break;

			default:

		}
		if (couponCodeNumber >= limit)
		{
			throw new CouponCodeGenerationException("The maximum of " + limit + " coupon codes have been generated for this coupon.",
					CouponCodeGenerationException.ERROR_MAXIMUM_COUPON_CODES_GENERATED);
		}
	}

	protected int[] getBitshifts()
	{
		return bitshifts;
	}

	/**
	 * defines how many bits to shift for each two characters that are created from the seed (the
	 * {@code MultiCodeCouponModel.couponCodeNumber}) (default values are 0, 8, 16, 24, 32, 40, 48, 56)
	 */
	public void setBitshifts(final List<Integer> bitshiftsList)
	{
		if (bitshiftsList != null)
		{
			bitshifts = Ints.toArray(bitshiftsList);
		}
	}

	protected int[] getOffsets()
	{
		return offsets;
	}

	/**
	 * defines the offset which is added when picking a character from the alphabet. (The default values are 3, 5, 7, 11,
	 * 13, 17, 19, 23, meaning that for the first two characters (i.e. one byte) the offset is 3.
	 */
	public void setOffsets(final List<Integer> offsetsList)
	{
		if (offsetsList != null)
		{
			offsets = Ints.toArray(offsetsList);
		}
	}

	/**
	 * checks that the given length is supported by this strategy (only values 2,4,6,8 are valid)
	 */
	protected void checkLength(final int length)
	{
		if (!(length == 2 || length == 4 || length == 6 || length == 8))
		{
			throw new SystemException(
					"coupon code generation is only supported for 2,4,6 or 8 characters of clear text, not " + length);
		}
	}
}
