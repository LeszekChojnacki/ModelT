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

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;
import static java.lang.Integer.valueOf;
import static java.util.Objects.requireNonNull;

import de.hybris.platform.couponservices.couponcodegeneration.CouponCodeCipherTextGenerationStrategy;
import de.hybris.platform.couponservices.couponcodegeneration.CouponCodeClearTextGenerationStrategy;
import de.hybris.platform.couponservices.couponcodegeneration.CouponCodesGenerator;
import de.hybris.platform.couponservices.model.CodeGenerationConfigurationModel;
import de.hybris.platform.couponservices.model.MultiCodeCouponModel;
import de.hybris.platform.servicelayer.exceptions.SystemException;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link CouponCodesGenerator} interface
 */
public class DefaultCouponCodesGenerator implements CouponCodesGenerator, InitializingBean
{

	private Map<Integer, Integer> codeLengthMapping;
	private CouponCodeClearTextGenerationStrategy clearTextStrategy;
	private CouponCodeCipherTextGenerationStrategy cipherTextStrategy;

	@Override
	public String generateNextCouponCode(final MultiCodeCouponModel coupon)
	{
		validateParameterNotNullStandardMessage("coupon", coupon);
		validateParameterNotNullStandardMessage("coupon.codeGenerationConfiguration", coupon.getCodeGenerationConfiguration());

		final CodeGenerationConfigurationModel config = coupon.getCodeGenerationConfiguration();

		final Integer codeLength = Integer.valueOf(config.getCouponPartCount() * config.getCouponPartLength());

		if (!getCodeLengthMapping().containsKey(codeLength))
		{
			throw new SystemException("Cannot create coupon code's of length:" + codeLength
					+ ". This implementation only supports coupon code lengths of:" + printCodeLengths());
		}

		final int clearTextLength = getCodeLengthMapping().get(codeLength).intValue();
		final int cipherTextLength = codeLength.intValue() - clearTextLength;

		final StringBuilder couponCode = new StringBuilder();

		// we add prefix and separator
		couponCode.append(coupon.getCouponId());
		couponCode.append(config.getCodeSeparator());

		final String clearText = getClearTextStrategy().generateClearText(coupon, clearTextLength);
		final String cipherText = getCipherTextStrategy().generateCipherText(coupon, clearText, cipherTextLength);

		final String codeWithSeparators = insertCodeSeparators(clearText + cipherText, coupon);
		couponCode.append(codeWithSeparators);
		return couponCode.toString();
	}

	@Override
	public void afterPropertiesSet()
	{
		// set default values for code length mapping:
		if (codeLengthMapping == null)
		{
			codeLengthMapping = new HashMap<>();
			codeLengthMapping.put(valueOf(4), valueOf(2));
			codeLengthMapping.put(valueOf(8), valueOf(4));
			codeLengthMapping.put(valueOf(12), valueOf(6));
			codeLengthMapping.put(valueOf(16), valueOf(8));
			codeLengthMapping.put(valueOf(20), valueOf(8));
			codeLengthMapping.put(valueOf(24), valueOf(8));
			codeLengthMapping.put(valueOf(28), valueOf(8));
			codeLengthMapping.put(valueOf(32), valueOf(8));
			codeLengthMapping.put(valueOf(36), valueOf(8));
			codeLengthMapping.put(valueOf(40), valueOf(8));
		}
	}

	protected String insertCodeSeparators(final String generatedCode, final MultiCodeCouponModel coupon)
	{
		requireNonNull(generatedCode);
		requireNonNull(coupon);
		requireNonNull(coupon.getCodeGenerationConfiguration());
		final CodeGenerationConfigurationModel config = coupon.getCodeGenerationConfiguration();
		final int requiredLength = config.getCouponPartCount() * config.getCouponPartLength();
		if (generatedCode.length() != requiredLength)
		{
			throw new IllegalArgumentException(
					"generated code must be " + requiredLength + " characters long (as defined by the coupon's configuration, but was "
							+ generatedCode.length() + "Coupon prefix:" + coupon.getCouponId());
		}
		final StringJoiner result = new StringJoiner(config.getCodeSeparator());
		int idx = 0;
		for (int part = 0; part < config.getCouponPartCount(); part++)
		{
			final StringBuilder codePart = new StringBuilder();
			for (int length = 0; length < config.getCouponPartLength(); length++)
			{
				codePart.append(generatedCode.charAt(idx));
				idx++;
			}
			result.add(codePart.toString());
		}
		return result.toString();
	}

	protected String printCodeLengths()
	{
		final StringJoiner joiner = new StringJoiner(",");
		codeLengthMapping.keySet().forEach(length -> joiner.add(length.toString()));
		return joiner.toString();
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

	public void setCodeLengthMapping(final Map<Integer, Integer> codeLengthMapping)
	{
		this.codeLengthMapping = codeLengthMapping;
	}

	@Override
	public Map<Integer, Integer> getCodeLengthMapping()
	{
		return codeLengthMapping;
	}
}
