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

import static java.nio.charset.Charset.forName;

import de.hybris.platform.couponservices.couponcodegeneration.CouponCodeGenerationException;
import de.hybris.platform.couponservices.couponcodegeneration.CouponCodesGenerator;
import de.hybris.platform.couponservices.model.MultiCodeCouponModel;

import java.io.IOException;
import java.io.InputStream;
import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Custom implementation of the {@link InputStream} iteratively reading the data for generated coupons
 */
public class CouponCodesInputStream extends InputStream
{
	private static final String STRING_SEPARATOR = "\n";
	private static final Logger LOG = LoggerFactory.getLogger(CouponCodesInputStream.class);
	private final CouponCodesGenerator couponCodesGenerator;
	private final MultiCodeCouponModel coupon;
	private final int batchSize;
	private int totalSize;
	private int currentCouponPos;
	private int pos;
	private int count;
	private byte[] buf;

	public CouponCodesInputStream(final MultiCodeCouponModel coupon, final CouponCodesGenerator couponCodesGenerator,
			final int batchSize, final int totalSize)
	{
		this.coupon = coupon;
		this.couponCodesGenerator = couponCodesGenerator;
		this.batchSize = batchSize;
		this.totalSize = totalSize;
	}

	@Override
	public synchronized int read() throws IOException
	{
		if (pos == count)
		{
			readNextChunkOfCodes();
		}
		return (pos < count) ? (buf[pos++] & 0xff) : -1;
	}

	@Override
	public int available() throws IOException
	{
		return totalSize - currentCouponPos + count - pos;
	}

	protected void readNextChunkOfCodes()
	{
		final int nextCodesChunkSize = currentCouponPos + batchSize < totalSize ? batchSize : totalSize - currentCouponPos;
		final StringJoiner stringJoiner = new StringJoiner(STRING_SEPARATOR);
		try
		{
			for (int i = 0; i < nextCodesChunkSize; i++)
			{
				stringJoiner.add(couponCodesGenerator.generateNextCouponCode(coupon));
				currentCouponPos++;
			}
		}
		catch (final CouponCodeGenerationException ex)
		{
			if (ex.getErrorCode() == CouponCodeGenerationException.ERROR_MAXIMUM_COUPON_CODES_GENERATED)
			{
				totalSize = currentCouponPos;
				LOG.warn(ex.getMessage());
			}
			else
			{
				throw ex;
			}
		}

		String chunkString = stringJoiner.toString();
		count = stringJoiner.length();
		pos = 0;
		if (currentCouponPos < totalSize)
		{
			chunkString += STRING_SEPARATOR;
			count++;
		}
		buf = chunkString.getBytes(forName("UTF-8"));
	}

	public int getGeneratedCouponsCount()
	{
		return currentCouponPos;
	}
}
