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
package de.hybris.platform.payment.strategy.impl;

import de.hybris.platform.payment.strategy.TransactionCodeGenerator;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * This class is used by {@link de.hybris.platform.payment.impl.DefaultPaymentServiceImpl} to provide the merchant
 * transaction code based on the specified order.
 */
public class DefaultMerchantTransactionCodeGenerator implements TransactionCodeGenerator
{
	public static final String DELIMITER = "-";

	private KeyGenerator keyGenerator;

	@Required
	public void setKeyGenerator(final KeyGenerator keyGenerator)
	{
		this.keyGenerator = keyGenerator;
	}

	/**
	 * this implementation generates a code of the following pattern <BASE>-<NUMBERSERIES_GENERATED_SEQUENZ> NOTE: if
	 * 'base' is <empty> only <NUMBERSERIES_GENERATED_SEQUENZ> will be returned.
	 *
	 * @param base
	 *           the leading part of the generated code
	 * @return the merchant transaction code
	 */
	@Override
	public String generateCode(final String base)
	{
		if (StringUtils.isBlank(base))
		{
			return (String) keyGenerator.generate();
		}
		else
		{
			final StringBuilder builder = new StringBuilder(base);
			builder.append(DELIMITER);
			builder.append((String) keyGenerator.generate());
			return builder.toString();
		}
	}
}