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
package de.hybris.platform.couponservices.services.impl;

import static com.google.common.collect.Lists.newArrayList;
import static de.hybris.platform.couponservices.constants.CouponServicesConstants.COUPON_CODE_GENERATION_ALGORITHM_DEFAULT_VALUE;
import static de.hybris.platform.couponservices.constants.CouponServicesConstants.COUPON_CODE_GENERATION_ALGORITHM_PROPERTY;
import static de.hybris.platform.couponservices.constants.CouponServicesConstants.COUPON_CODE_GENERATION_ALLOW_MULTIBYTE_CHARACTERS_PROPERTY;
import static de.hybris.platform.couponservices.constants.CouponServicesConstants.COUPON_CODE_GENERATION_ALPHABET_ALLOW_VARIABLE_LENGTH_PROPERTY;
import static de.hybris.platform.couponservices.constants.CouponServicesConstants.COUPON_CODE_GENERATION_ALPHABET_LENGTH_DEFAULT_VALUE;
import static de.hybris.platform.couponservices.constants.CouponServicesConstants.COUPON_CODE_GENERATION_ALPHABET_LENGTH_PROPERTY;
import static de.hybris.platform.couponservices.constants.CouponServicesConstants.COUPON_CODE_GENERATION_GLOBAL_CHARACTERSET_DEFAULT_VALUE;
import static de.hybris.platform.couponservices.constants.CouponServicesConstants.COUPON_CODE_GENERATION_GLOBAL_CHARACTERSET_PROPERTY;
import static de.hybris.platform.couponservices.constants.CouponServicesConstants.COUPON_CODE_GENERATION_KEY_SIZE_DEFAULT_VALUE;
import static de.hybris.platform.couponservices.constants.CouponServicesConstants.COUPON_CODE_GENERATION_KEY_SIZE_PROPERTY;
import static de.hybris.platform.couponservices.constants.CouponServicesConstants.COUPON_CODE_GENERATION_MEDIA_FOLDER_QUALIFIER;
import static de.hybris.platform.couponservices.constants.CouponServicesConstants.COUPON_CODE_GENERATION_MEDIA_FOLDER_QUALIFIER_DEFAULT_VALUE;
import static de.hybris.platform.couponservices.constants.CouponServicesConstants.COUPON_CODE_GENERATION_PICK_ALPHABET_USING_SECURERANDOM_PROPERTY;
import static de.hybris.platform.couponservices.constants.CouponServicesConstants.COUPON_CODE_GENERATION_SIGNATURE_ALGORITHM_ALLOW_NON_AES_PROPERTY;
import static de.hybris.platform.couponservices.couponcodegeneration.CouponCodeGenerationException.DEFAULT_ERROR_CODE;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import de.hybris.platform.catalog.model.CatalogUnawareMediaModel;
import de.hybris.platform.core.model.media.MediaFolderModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.couponservices.couponcodegeneration.CouponCodeCipherTextGenerationStrategy;
import de.hybris.platform.couponservices.couponcodegeneration.CouponCodeClearTextGenerationStrategy;
import de.hybris.platform.couponservices.couponcodegeneration.CouponCodeGenerationException;
import de.hybris.platform.couponservices.couponcodegeneration.CouponCodesGenerator;
import de.hybris.platform.couponservices.couponcodegeneration.impl.CouponCodesInputStream;
import de.hybris.platform.couponservices.model.CodeGenerationConfigurationModel;
import de.hybris.platform.couponservices.model.MultiCodeCouponModel;
import de.hybris.platform.couponservices.services.CouponCodeGenerationService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.exceptions.SystemException;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.util.Base64;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.SecretKey;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;


/**
 * The default coupon code generation service.
 */
public class DefaultCouponCodeGenerationService implements CouponCodeGenerationService, InitializingBean
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultCouponCodeGenerationService.class);

	protected static final String UNEXPECTED_ERROR_MSG = "multiCodeCouponGenerator.unexpectedError";

	private ConfigurationService configurationService;

	private CouponCodeClearTextGenerationStrategy clearTextStrategy;

	private CouponCodeCipherTextGenerationStrategy cipherTextStrategy;

	private ModelService modelService;

	private MediaService mediaService;

	private String codeSeparatorPattern;

	private Pattern codeSeparatorRegex;

	private CouponCodesGenerator couponCodesGenerator;

	private Integer batchSize;

	private KeyGenerator keyGenerator;

	@Override
	public void afterPropertiesSet()
	{
		batchSize = getConfigurationService().getConfiguration().getInteger("couponservices.code.generation.batch.size",
				Integer.valueOf(1000));
		codeSeparatorRegex = Pattern.compile(getCodeSeparatorPattern());
	}

	@Override
	public String generateCouponSignature()
	{
		final String algorithm = getAndValidateAlgorithm();
		final int keysize = getConfigurationService().getConfiguration().getInt(COUPON_CODE_GENERATION_KEY_SIZE_PROPERTY,
				COUPON_CODE_GENERATION_KEY_SIZE_DEFAULT_VALUE);
		try
		{
			// generate the signature using the defined algorithm and key size
			final javax.crypto.KeyGenerator kgen = javax.crypto.KeyGenerator.getInstance(algorithm);
			kgen.init(keysize);
			final SecretKey skey = kgen.generateKey();
			return Base64.encodeBytes(skey.getEncoded(), Base64.DONT_BREAK_LINES);
		}
		catch (final NoSuchAlgorithmException | InvalidParameterException e)
		{
			LOG.error("Cannot generate coupon signature", e);
			throw new CouponCodeGenerationException(
					"Cannot create coupon signature due to " + e.getClass().getSimpleName() + " exception. Message:" + e.getMessage(),
					CouponCodeGenerationException.DEFAULT_ERROR_CODE);
		}
	}

	@Override
	public String generateCouponAlphabet()
	{
		final String globalCharacterSet = getAndValidateGlobalCharacterSet();

		final int alphabetLength = getAndValidateAlphabetLength(globalCharacterSet);

		// (securely and) randomly select an alphabet for this coupon out of the global character set
		// this ensures that each new coupon has different codes (even if all other parameters are the same)
		final StringBuilder alphabet = new StringBuilder();
		// using SecureRandom can lead to extremely bad performance on VMs due to missing hardware support for cryptographically safe randomness.
		// as the selection of the alphabet is not a high-security task, we default to regular Random.
		final boolean useSecureRandom = getConfigurationService().getConfiguration()
				.getBoolean(COUPON_CODE_GENERATION_PICK_ALPHABET_USING_SECURERANDOM_PROPERTY, false);
		final Random random = useSecureRandom ? new SecureRandom() : new Random();

		while (alphabet.length() < alphabetLength)
		{
			final int pos = random.nextInt(globalCharacterSet.length());
			final char nextChar = globalCharacterSet.charAt(pos);
			if (alphabet.toString().indexOf(nextChar) == -1)
			{
				alphabet.append(Character.toString(nextChar));
			}
		}
		return alphabet.toString();
	}

	/**
	 * @param coupon
	 * 		multi code coupon
	 * @return generated coupon code
	 * @throws CouponCodeGenerationException
	 */
	@Override
	public String generateCouponCode(final MultiCodeCouponModel coupon)
	{
		return getCouponCodesGenerator().generateNextCouponCode(coupon);
	}

	@Override
	public Optional<MediaModel> generateCouponCodes(final MultiCodeCouponModel multiCodeCoupon, final int quantity)
	{
		return generateMediaForMultiCodeCoupon(multiCodeCoupon, quantity);
	}

	protected Optional<MediaModel> generateMediaForMultiCodeCoupon(final MultiCodeCouponModel multiCodeCoupon, final int quantity)
	{
		validateParameterNotNullStandardMessage("MultiCode Coupon Model", multiCodeCoupon);

		final Optional<MediaModel> couponCodesMedia = ofNullable(createMedia(multiCodeCoupon, quantity));
		final Collection<MediaModel> couponCodes = multiCodeCoupon.getGeneratedCodes() != null
				? new ArrayList<>(multiCodeCoupon.getGeneratedCodes()) : newArrayList();
		couponCodesMedia.ifPresent(couponCodes::add);
		multiCodeCoupon.setGeneratedCodes(couponCodes);
		getModelService().save(multiCodeCoupon);
		return couponCodesMedia;
	}

	protected MediaModel createMedia(final MultiCodeCouponModel coupon, final int quantity)
	{
		final String actionCode = coupon.getCouponId();
		final SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
		final String date = sdfDate.format(new Date());
		final String sequentialCode = (String) getKeyGenerator().generate();
		final MediaFolderModel mediaFolder = getMediaFolderForCouponCodes();
		CatalogUnawareMediaModel media = null;
		InputStream is = null;

		try
		{
			final CouponCodesInputStream couponCodeInputStream = new CouponCodesInputStream(coupon, getCouponCodesGenerator(),
					getBatchSize().intValue(), quantity);
			is = IOUtils.toBufferedInputStream(couponCodeInputStream);

			if (couponCodeInputStream.getGeneratedCouponsCount() > 0)
			{
				media = new CatalogUnawareMediaModel();
				final String mediaCode = String.format("%d %s %s %s",
						Integer.valueOf(couponCodeInputStream.getGeneratedCouponsCount()), actionCode, date, sequentialCode);
				media.setCode(mediaCode);
				media.setFolder(mediaFolder);
				getModelService().save(media);
				getMediaService().setStreamForMedia(media, is, mediaCode + ".csv", "text/csv");
			}
		}
		catch (final IOException ex)
		{
			throw new IllegalStateException(UNEXPECTED_ERROR_MSG, ex);
		}
		finally
		{
			IOUtils.closeQuietly(is);
		}
		return media;
	}


	protected MediaFolderModel getMediaFolderForCouponCodes()
	{
		final String mediaFolderQualifier = getConfigurationService().getConfiguration().getString(
				COUPON_CODE_GENERATION_MEDIA_FOLDER_QUALIFIER, COUPON_CODE_GENERATION_MEDIA_FOLDER_QUALIFIER_DEFAULT_VALUE);

		return getMediaService().getFolder(mediaFolderQualifier);
	}

	@Override
	public String extractCouponPrefix(final String couponCode)
	{
		requireNonNull(couponCode);
		final Matcher matcher = codeSeparatorRegex.matcher(couponCode);
		String prefix = null;
		if (matcher.find())
		{
			prefix = couponCode.substring(0, matcher.start());
		}
		return prefix;
	}

	@Override
	public boolean verifyCouponCode(final MultiCodeCouponModel coupon, final String couponCode)
	{
		requireNonNull(coupon);
		requireNonNull(couponCode);
		requireNonNull(coupon.getCodeGenerationConfiguration());

		boolean verificationResult = false;
		if (validateCouponFormat(coupon, couponCode))
		{
			verificationResult = verifyCipherText(coupon, couponCode);
		}
		else
		{
			LOG.debug("coupon code verification failed as given coupon code doesn't match expected format.");
		}
		return verificationResult;
	}

	/**
	 * validates the given coupon code with regards to its format (i.e. correct prefix, separators, coupon parts).
	 */
	protected boolean validateCouponFormat(final MultiCodeCouponModel coupon, final String couponCode)
	{
		try
		{
			// recreate coupon code input based on given code and coupons configuration
			final String separator = coupon.getCodeGenerationConfiguration().getCodeSeparator();
			final String prefix = coupon.getCouponId();
			final int partLength = coupon.getCodeGenerationConfiguration().getCouponPartLength();
			final int partCount = coupon.getCodeGenerationConfiguration().getCouponPartCount();
			final int separatorLength = separator.length();
			final StringBuilder generatedCode = new StringBuilder(coupon.getCouponId());
			final int offset = prefix.length() + separatorLength;
			for (int i = 0; i < partCount; i++)
			{
				final int start = offset + (i * (partLength + separatorLength));
				generatedCode.append(separator);
				generatedCode.append(couponCode.substring(start, start + partLength));
			}
			return generatedCode.toString().equals(couponCode);
		}
		catch (final StringIndexOutOfBoundsException e)
		{
			LOG.error("verification failed, given coupon code is not of required length.", e);
			return false;
		}
	}


	/**
	 * @return true if generated ciphertext matches given ciphertext , false otherwise
	 */
	protected boolean verifyCipherText(final MultiCodeCouponModel coupon, final String couponCode)
	{
		// remove prefix, split up couponCode into cleartext and ciphertext
		final Pair<String, String> pair = extractClearTextAndCipherText(coupon, couponCode);
		boolean verificationResult = false;
		if (nonNull(pair))
		{
			// check if generating ciphertext matches given ciphertext
			final String calculatedCipherText = getCipherTextStrategy().generateCipherText(coupon, pair.getLeft(),
					pair.getRight().length());
			if (pair.getRight().equals(calculatedCipherText))
			{
				try
				{
					verificationResult = verifyUsedCouponCodeNumber(coupon, pair);
				}
				catch (final SystemException e)
				{
					LOG.error("SystemException occured: {}", e);
				}
			}
			else
			{
				LOG.debug("generated ciphertext {} doesn't match given ciphertext: {}", calculatedCipherText, pair.getRight());
			}
		}
		return verificationResult;
	}


	/**
	 * @return true if used coupon code number is greater than 0 and less than CouponCodeNumber on multi code
	 * coupon,false otherwise
	 */
	protected boolean verifyUsedCouponCodeNumber(final MultiCodeCouponModel coupon, final Pair<String, String> pair)
	{
		final long usedCouponCodeNumber = getClearTextStrategy().getCouponCodeNumberForClearText(coupon, pair.getLeft());
		return (0 <= usedCouponCodeNumber) && (usedCouponCodeNumber <= coupon.getCouponCodeNumber().longValue());
	}

	@Override
	public boolean isValidCodeSeparator(final String codeSeparator)
	{
		boolean isValid = false;
		if (isNotEmpty(codeSeparator))
		{
			final Matcher matcher = codeSeparatorRegex.matcher(codeSeparator);
			isValid = codeSeparator.length() == 1 && matcher.find();
		}
		return isValid;
	}

	/**
	 * returns a string pair containing the clear text and the cipher text part of the given coupon code.
	 *
	 * @return a string pair or null if the parts cannot be determined
	 */
	protected Pair<String, String> extractClearTextAndCipherText(final MultiCodeCouponModel coupon, final String couponCode)
	{
		final int prefixAndSeparatorLength = coupon.getCouponId().length()
				+ coupon.getCodeGenerationConfiguration().getCodeSeparator().length();
		if (couponCode.length() <= prefixAndSeparatorLength)
		{
			return null;
		}
		final String codeWithOutPrefix = couponCode.substring(prefixAndSeparatorLength);

		String codeWithoutSeparators = null;
		try
		{
			codeWithoutSeparators = removeCodeSeparators(codeWithOutPrefix, coupon);
		}
		catch (final IllegalArgumentException ex)
		{
			LOG.debug("error during coupon code separator removal", ex);
			return null;
		}
		final Integer clearTextLength = getCouponCodesGenerator().getCodeLengthMapping()
				.get(Integer.valueOf(codeWithoutSeparators.length()));
		if (clearTextLength == null)
		{
			LOG.debug("cannot extract cleartext because given code has no supported length mapping:{}",
					Integer.valueOf(codeWithoutSeparators.length()));
			return null;
		}
		return Pair.of(codeWithoutSeparators.substring(0, clearTextLength.intValue()),
				codeWithoutSeparators.substring(clearTextLength.intValue()));
	}

	/**
	 * removes the code separator from the given {@code codeWithoutPrefix}. Note: Expects the given code to be without
	 * its prefix.
	 *
	 * @throws IllegalArgumentException
	 * 		if the given code doesn't match with the required length of the given coupon's configuration
	 */
	protected String removeCodeSeparators(final String codeWithOutPrefix, final MultiCodeCouponModel coupon)
	{
		requireNonNull(codeWithOutPrefix);
		requireNonNull(coupon);
		requireNonNull(coupon.getCodeGenerationConfiguration());
		final CodeGenerationConfigurationModel config = coupon.getCodeGenerationConfiguration();
		final String codeWithoutSeparators = codeWithOutPrefix.replace(config.getCodeSeparator(), "");
		final int requiredLength = config.getCouponPartCount() * config.getCouponPartLength();
		if (codeWithoutSeparators.length() != requiredLength)
		{
			throw new IllegalArgumentException("given code (without separators) must be " + requiredLength
					+ " characters long (as defined by the coupon's configuration, but was " + codeWithoutSeparators.length()
					+ " given code:" + coupon.getCouponId() + config.getCodeSeparator() + codeWithOutPrefix);
		}
		return codeWithoutSeparators;
	}

	/**
	 * returns the amount of cipher-text characters to be created for the given coupon.
	 */
	protected int getCipherTextLength(final MultiCodeCouponModel coupon)
	{
		return getLengthFor(coupon, false);
	}

	/**
	 * returns the amount of clear-text characters to be created for the given coupon.
	 */
	protected int getClearTextLength(final MultiCodeCouponModel coupon)
	{
		return getLengthFor(coupon, true);
	}

	protected int getLengthFor(final MultiCodeCouponModel coupon, final boolean clearText)
	{
		requireNonNull(coupon);
		requireNonNull(coupon.getCodeGenerationConfiguration());
		final int generatedCodeLength = coupon.getCodeGenerationConfiguration().getCouponPartCount()
				* coupon.getCodeGenerationConfiguration().getCouponPartLength();
		final Integer length = getCouponCodesGenerator().getCodeLengthMapping().get(Integer.valueOf(generatedCodeLength));
		if (isNull(length))
		{
			throw new CouponCodeGenerationException("no code length mapping defined for coupon code length: " + generatedCodeLength,
					DEFAULT_ERROR_CODE);
		}
		return clearText ? length.intValue() : generatedCodeLength - length.intValue();
	}

	/**
	 * validates the coupon alphabet length and returns it
	 */
	protected int getAndValidateAlphabetLength(final String globalCharacterSet)
	{
		final int alphabetLength = getConfigurationService().getConfiguration()
				.getInt(COUPON_CODE_GENERATION_ALPHABET_LENGTH_PROPERTY, COUPON_CODE_GENERATION_ALPHABET_LENGTH_DEFAULT_VALUE);

		if (!getConfigurationService().getConfiguration().getBoolean(COUPON_CODE_GENERATION_ALPHABET_ALLOW_VARIABLE_LENGTH_PROPERTY,
				false) && alphabetLength != COUPON_CODE_GENERATION_ALPHABET_LENGTH_DEFAULT_VALUE)
		{

			throw new CouponCodeGenerationException(
					"The default coupon alphabet length is 16, other values are not supported. If you "
							+ "(think that you) know what you're doing you can disable this check by setting the system " + "property '"
							+ COUPON_CODE_GENERATION_ALPHABET_ALLOW_VARIABLE_LENGTH_PROPERTY + "' to true.", // NOSONAR
					DEFAULT_ERROR_CODE); // NOSONAR

		}
		// ensure that the alphabet we pick is not longer than the global character set we pick from
		return Math.min(alphabetLength, globalCharacterSet.length());
	}

	/**
	 * validates that the global character set doesn't contain the defined code separators and that all characters are
	 * single-byte characters.
	 *
	 * @return the validated global character set
	 * @throws CouponCodeGenerationException
	 * 		if the validation fails
	 */
	protected String getAndValidateGlobalCharacterSet()
	{
		final String globalCharacterSet = getConfigurationService().getConfiguration().getString(
				COUPON_CODE_GENERATION_GLOBAL_CHARACTERSET_PROPERTY, COUPON_CODE_GENERATION_GLOBAL_CHARACTERSET_DEFAULT_VALUE);

		final Matcher matcher = codeSeparatorRegex.matcher(globalCharacterSet);
		if (matcher.find())
		{
			throw new CouponCodeGenerationException("The globally defined coupon code character set (defined via system property '"
					+ COUPON_CODE_GENERATION_GLOBAL_CHARACTERSET_PROPERTY
					+ "' must not contain characters that are also defined as code separators! Found separator:" + matcher.group()
					+ " in character set:" + globalCharacterSet, DEFAULT_ERROR_CODE);
		}

		// by default we check that each character of the global character set has a 1-byte only representation.
		if (!getConfigurationService().getConfiguration().getBoolean(COUPON_CODE_GENERATION_ALLOW_MULTIBYTE_CHARACTERS_PROPERTY,
				false))
		{

			// ensure that all characters of the global character set are 1-byte only
			final int charSetLength = globalCharacterSet.length();
			final int charByteArrayLength = globalCharacterSet.getBytes(StandardCharsets.UTF_8).length;
			if (charSetLength != charByteArrayLength)
			{
				throw new CouponCodeGenerationException(
						"By default the globally defined coupon code character set (defined via system property '"
								+ COUPON_CODE_GENERATION_GLOBAL_CHARACTERSET_PROPERTY
								+ " must not contain characters that require more than one byte (e.g. Ä,好, etc). If you "
								+ "(think that you) know what you're doing you can disable this check by setting the system "
								+ "property '" + COUPON_CODE_GENERATION_ALLOW_MULTIBYTE_CHARACTERS_PROPERTY + "' to true.", // NOSONAR
						DEFAULT_ERROR_CODE);
			}
		}

		return globalCharacterSet;
	}

	/**
	 * validates the configured algorithm used for the cipher part of the coupon code
	 */
	protected String getAndValidateAlgorithm()
	{
		final String algorithm = getConfigurationService().getConfiguration().getString(COUPON_CODE_GENERATION_ALGORITHM_PROPERTY,
				COUPON_CODE_GENERATION_ALGORITHM_DEFAULT_VALUE);

		// by default we don't allow non-AES algorithms for the signature creation as this has not been tested.
		// (and the algorithm relies on AES' 16 byte block ciphers)
		if (!getConfigurationService().getConfiguration()
				.getBoolean(COUPON_CODE_GENERATION_SIGNATURE_ALGORITHM_ALLOW_NON_AES_PROPERTY, false)
				&& !COUPON_CODE_GENERATION_ALGORITHM_DEFAULT_VALUE.equals(algorithm))
		{

			throw new CouponCodeGenerationException("You configured a non-AES algorithm (" + algorithm
					+ ") which is not supported! If you (think that you) know what you're doing you can disable "
					+ "this check by setting the system property '" + COUPON_CODE_GENERATION_SIGNATURE_ALGORITHM_ALLOW_NON_AES_PROPERTY
					+ "' to true.", DEFAULT_ERROR_CODE); // NOSONAR

		}
		return algorithm;
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

	protected CouponCodeClearTextGenerationStrategy getClearTextStrategy()
	{
		return clearTextStrategy;
	}

	@Required
	public void setClearTextStrategy(final CouponCodeClearTextGenerationStrategy clearTextStrategy)
	{
		this.clearTextStrategy = clearTextStrategy;
	}

	protected CouponCodeCipherTextGenerationStrategy getCipherTextStrategy()
	{
		return cipherTextStrategy;
	}

	@Required
	public void setCipherTextStrategy(final CouponCodeCipherTextGenerationStrategy cipherTextStrategy)
	{
		this.cipherTextStrategy = cipherTextStrategy;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	protected MediaService getMediaService()
	{
		return mediaService;
	}

	@Required
	public void setMediaService(final MediaService mediaService)
	{
		this.mediaService = mediaService;
	}

	protected String getCodeSeparatorPattern()
	{
		return codeSeparatorPattern;
	}

	@Required
	public void setCodeSeparatorPattern(final String codeSeparatorPattern)
	{
		this.codeSeparatorPattern = codeSeparatorPattern;
	}

	protected CouponCodesGenerator getCouponCodesGenerator()
	{
		return couponCodesGenerator;
	}

	@Required
	public void setCouponCodesGenerator(final CouponCodesGenerator couponCodesGenerator)
	{
		this.couponCodesGenerator = couponCodesGenerator;
	}

	protected Integer getBatchSize()
	{
		return batchSize;
	}

	protected KeyGenerator getKeyGenerator()
	{
		return keyGenerator;
	}

	@Required
	public void setKeyGenerator(final KeyGenerator keyGenerator)
	{
		this.keyGenerator = keyGenerator;
	}
}
