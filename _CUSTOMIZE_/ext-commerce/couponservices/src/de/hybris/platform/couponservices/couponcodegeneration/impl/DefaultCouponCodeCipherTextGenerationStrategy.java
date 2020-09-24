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

import static de.hybris.platform.couponservices.constants.CouponServicesConstants.COUPON_CODE_GENERATION_ALGORITHM_DEFAULT_VALUE;
import static de.hybris.platform.couponservices.constants.CouponServicesConstants.COUPON_CODE_GENERATION_ALGORITHM_PROPERTY;
import static java.util.Objects.requireNonNull;

import de.hybris.platform.couponservices.couponcodegeneration.CouponCodeCipherTextGenerationStrategy;
import de.hybris.platform.couponservices.model.MultiCodeCouponModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.exceptions.SystemException;
import de.hybris.platform.util.Base64;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;


public class DefaultCouponCodeCipherTextGenerationStrategy extends AbstractCouponCodeGenerationStrategy
		implements CouponCodeCipherTextGenerationStrategy, InitializingBean
{
	private Map<Integer, Integer> lengthToIntMapping;
	private ConfigurationService configurationService;

	@Override
	public void afterPropertiesSet()
	{
		// set default values if not configured
		if (getLengthToIntMapping() == null)
		{
			final Map<Integer, Integer> lengthMapping = new HashMap<>();
			lengthMapping.put(Integer.valueOf(2), Integer.valueOf(1)); // 2  characters -> 1 byte   -> 1 int
			lengthMapping.put(Integer.valueOf(4), Integer.valueOf(1)); // 4  characters -> 2 bytes  -> 1 int
			lengthMapping.put(Integer.valueOf(6), Integer.valueOf(1)); // 6  characters -> 3 bytes  -> 1 int
			lengthMapping.put(Integer.valueOf(8), Integer.valueOf(2)); // 8  characters -> 4 bytes  -> 2 ints
			lengthMapping.put(Integer.valueOf(12), Integer.valueOf(2)); // 12 characters -> 6 bytes  -> 2 ints
			lengthMapping.put(Integer.valueOf(16), Integer.valueOf(3)); // 16 characters -> 8 bytes  -> 3 ints
			lengthMapping.put(Integer.valueOf(20), Integer.valueOf(4)); // 20 characters -> 10 bytes -> 4 ints
			lengthMapping.put(Integer.valueOf(24), Integer.valueOf(4)); // 24 characters -> 12 bytes -> 4 ints
			lengthMapping.put(Integer.valueOf(28), Integer.valueOf(5)); // 28 characters -> 14 bytes -> 5 ints
			lengthMapping.put(Integer.valueOf(32), Integer.valueOf(6)); // 32 characters -> 16 bytes -> 6 ints
			setLengthToIntMapping(lengthMapping);
		}
	}

	@Override
	public String generateCipherText(final MultiCodeCouponModel coupon, final String clearText, final int length)
	{
		requireNonNull(coupon);
		requireNonNull(clearText);
		checkLength(length);
		checkLength(clearText);

		final byte[] encryptedBytes = encrypt(coupon, clearText);

		// transform the 16 byte encrypted block into an int[] based on the lengthToIntMappings
		final int[] cipherTextInput = transform(encryptedBytes, getLengthToIntMapping().get(Integer.valueOf(length)).intValue());

		// construct the cipher text
		final String cipherText = constructCipherText(coupon, cipherTextInput);

		// return only the required length
		return cipherText.substring(0, length);

	}

	/**
	 * encrypts the given cleartext using the signature defined on the coupon.
	 */
	protected byte[] encrypt(final MultiCodeCouponModel coupon, final String clearText)
	{
		try
		{
			final byte[] encryptedBytes = getCipher(coupon).doFinal(clearText.getBytes());
			if (encryptedBytes.length != 16)
			{
				throw new SystemException(
						"encrypted cipher data must be 16 bytes, but was " + encryptedBytes.length + ". Given cleartext:" + clearText);
			}
			return encryptedBytes;
		}
		catch (final BadPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException e)
		{
			throw new SystemException("error during cipher text generation for coupon:" + coupon.getCouponId(), e);
		}
	}

	/**
	 * constructs the ciphertext based on the given input byte array.
	 */
	protected final String constructCipherText(final MultiCodeCouponModel coupon, final int[] cipherTextInput)
	{
		final StringBuilder cipherText = new StringBuilder();
		// from each int we take the lower 3 bytes to create 2 characters each
		for (int i = 0; i < cipherTextInput.length; i++)
		{
			cipherText.append(createTwoCharactersFromByte((cipherTextInput[i] >> 16) & 255, 0, coupon.getAlphabet()));
			cipherText.append(createTwoCharactersFromByte((cipherTextInput[i] >> 8) & 255, 0, coupon.getAlphabet()));
			cipherText.append(createTwoCharactersFromByte(cipherTextInput[i] & 255, 0, coupon.getAlphabet()));
		}
		return cipherText.toString();
	}

	/**
	 * transforms the given input of 16-byte array into an array of ints of specified length. Each of the resulting ints
	 * is composed of three (unsigned) bytes (using the lower 24 bits of the int).
	 */
	protected int[] transform(final byte[] encryptedData, final int length)
	{
		final int[] result = new int[length];
		// for each int we only take three bytes
		for (int i = 0; i < result.length; i++)
		{
			// for each three bytes we create one int:
			// (we do %16 to avoid ArrayIndexOutOfBoundsExceptions)
			// each byte is first converted to an unsigned byte (using the good old 'bitwise and' with 255/0xFF)
			// and gets automatially promoted to int in Java
			// then the resulting int is a 'concatenation' of the input ints being bitshifted
			// to form an int with the lower 24 bits containing the three unsigned bytes
			// (pseudocode) example:
			// byte1 = 00110011;                  // NOSONAR
			// byte2 =         00001111;          // NOSONAR
			// byte3 =                 01010101;  // NOSONAR
			// result= 001100110000111101010101;  // NOSONAR
			final int offset = i * 3;
			final int byte1 = encryptedData[(offset + 0) % 16] & 255;
			final int byte2 = encryptedData[(offset + 1) % 16] & 255;
			final int byte3 = encryptedData[(offset + 2) % 16] & 255;
			result[i] = byte1 << 16 | byte2 << 8 | byte3;
		}
		return result;
	}

	protected Cipher getCipher(final MultiCodeCouponModel coupon) throws NoSuchAlgorithmException
	{
		Cipher cipher = null;
		try
		{
			final String algorithm = getConfigurationService().getConfiguration()
					.getString(COUPON_CODE_GENERATION_ALGORITHM_PROPERTY, COUPON_CODE_GENERATION_ALGORITHM_DEFAULT_VALUE);
			final SecretKeySpec skeySpec = new SecretKeySpec(decodeSignature(coupon), algorithm);
			cipher = Cipher.getInstance(algorithm);
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		}
		catch (final NoSuchPaddingException | InvalidKeyException e)
		{
			throw new SystemException("error getting cipher for coupon:" + coupon.getCouponId(), e);
		}
		return cipher;
	}

	/**
	 * decodes the coupon.signature via {@link Base64#decode(String)}.
	 */
	protected byte[] decodeSignature(final MultiCodeCouponModel coupon)
	{
		return Base64.decode(coupon.getSignature());
	}

	/**
	 * checks that the given length is supported (2,4,6,8,12,16,20,24,28,32).
	 */
	protected void checkLength(final int length)
	{
		final Integer[] validChipherTextLength =
		{ Integer.valueOf(2), Integer.valueOf(4), Integer.valueOf(6), Integer.valueOf(8), Integer.valueOf(12), Integer.valueOf(16),
				Integer.valueOf(20), Integer.valueOf(24), Integer.valueOf(28), Integer.valueOf(32) };

		final List<Integer> validChipherTextLengthList = Arrays.asList(validChipherTextLength);

		if (!validChipherTextLengthList.contains(Integer.valueOf(length)))
		{
			throw new SystemException(
					"coupon code generation is only supported for 2,4,6,8,12,16,20,24,28 or 32 characters of cipher text, not "
							+ length);
		}
	}

	/**
	 * checks that the given clearText is of allowed length (such that {@code clearText.getBytes().length <= 16)})
	 */
	protected void checkLength(final String clearText)
	{
		if (clearText.getBytes().length > 16)
		{
			throw new SystemException("coupon code cipher text generation is only supported for input cleartext  >= 16 bytes, not "
					+ clearText.getBytes().length);
		}
	}

	protected ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	@Required
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}

	protected Map<Integer, Integer> getLengthToIntMapping()
	{
		return lengthToIntMapping;
	}

	/**
	 * This mapping defines how many input {@code int} values are needed for a given {@code length} of cipher-text. For
	 * each 2 characters of ciphertext one input int is needed. Only the lower three bytes of an int are used. Therefore,
	 * the default mapping is:
	 *
	 * <pre>
	 * 4  characters -> 2 bytes  -> 1 int
	 * 6  characters -> 3 bytes  -> 1 int
	 * 8  characters -> 4 bytes  -> 2 ints
	 * 12 characters -> 6 bytes  -> 2 ints
	 * 16 characters -> 8 bytes  -> 3 ints
	 * 20 characters -> 10 bytes -> 4 ints
	 * 28 characters -> 14 bytes -> 5 ints
	 * 32 characters -> 16 bytes -> 6 ints
	 * </pre>
	 */
	public void setLengthToIntMapping(final Map<Integer, Integer> lengthToIntMapping)
	{
		this.lengthToIntMapping = lengthToIntMapping;
	}
}
